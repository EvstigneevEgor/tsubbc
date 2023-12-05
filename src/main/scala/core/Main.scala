package core

import com.bot4s.telegram.api._
import com.bot4s.telegram.api.declarative.{Callbacks, Commands}
import com.bot4s.telegram.clients.FutureSttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models._
import core.AnkIsDriverStage.chooseStatusButtons
import core.Commands.allCommands
import date_base.Stage
import date_base.dao.UserDao
import pureconfig._
import pureconfig.generic.auto._
import sttp.client3.SttpBackend
import sttp.client3.okhttp.OkHttpFutureBackend

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Main extends TelegramBot with App with Polling with Commands[Future] with Callbacks[Future] {
  implicit val backend: SttpBackend[Future, Any] = OkHttpFutureBackend() //хз что это такое

  case class AppConfig(token: String)

  val config = ConfigSource.resources("token.conf").load[AppConfig] match {
    case Right(appConfig) => appConfig
    case Left(failures) =>
      sys.error(s"Failed to load configuration: $failures")
  }
  override val client: RequestHandler[Future] = new FutureSttpClient(config.token)
  private val naherIdiExeption = new RuntimeException("Что-то пошло не так. По техническим причинам услуга временно недоступна")

  Commands.values.foreach(cmd => onCommand(cmd.toString)(Commands.getActionByCommand(cmd))) //функция, которая принимает каждый элемент коллекции Commands как cmd и выполняет операцию

  override def receiveMessage(msg: Message): Future[Unit] = { // Переопределение метода receiveMessage, который принимает сообщение типа Message и возвращает Future[Unit]
    implicit val m: Message = msg // Неявное значение типа Message для использования внутри метода
    // Проверка наличия текста в сообщении и контактных данных
    (msg.text.filterNot(allCommands.contains), msg.contact) match { // Фильтрация текста сообщения и проверка на наличие контактной информации в сообщении
      case (text, contact) if text.isDefined || contact.isDefined => // Случай, если есть текст или контактная информация в сообщении, то
        for {
          user <- getOrCreateUser // Получение или создание пользователя на основе источника сообщения
          _ <- executeStage(user).recover(a => println(s"eeeeerr ${a}")) // Выполнение этапа и обработка возможных ошибок, если они возникнут
        } yield ()
      case _ => Future.unit // Случай, если текста и контактной информации в сообщении нет
    }

  }

  private def getOrCreateUser(implicit msg: Message) = {
    UserDao.get(msg.source)
      .flatMap {
        case Some(value) => Future.successful(value)
        case None =>
          UserDao.insert(createNewUser(msg)).flatMap(_ => UserDao.get).flatMap {
            case Some(value) => Future.successful(value)
            case None => reply("Внутренняя ошибка приложения").flatMap(_ => {
              Future.failed(naherIdiExeption)
            })
          }
      }
  }

  private def createNewUser(msg: Message) = {
    date_base.User(
      userName = msg.chat.username.getOrElse("guest"),
      chatID = msg.source,
      isDriver = false,
      isAuthorized = false,
      fullName = msg.getNameOrNameCalling,
      communicate = None,
      stage = Stage.NotAuthorized
    )
  }

  private def executeStage(user: date_base.User)(implicit msg: Message) = {
    val sendMessageWithoutButton = messageWithoutButton(msg.source, _)
    val sendMessageWithButton = messagesWithButtons(msg.source, _, _)
    user.stage match { // Проверка текущего этапа в сообщении
      case Stage.NotAuthorized => // Случай, если текущий этап "NotAuthorized"
        for {
          a <- UserDao.setNextStage(user.chatID) // Установка следующего этапа в базе данных
          _ <- sendMessageWithoutButton(s"Привет, ${msg.getNameOrNameCalling}! Добро пожаловать в чат-бот Бла Бла Кар ТГУ. Давай заполним анкету")
          _ <-
            request(SendMessage(msg.source, "Как другие пользователи могут с тобой связаться?", replyMarkup = Some(ReplyKeyboardMarkup.singleButton(KeyboardButton.requestContact("Дать линку"))))) // Запрос на отправку контактной информации пользователем
        } yield ()

      case Stage.FillInfoSetCommunicate => // Случай, если текущий этап "FillInfoSetCommunicate"
        msg.contact match { // Проверяем наличие контактной информации в сообщении
          case Some(value) => for { // Случай, если контактная информация присутствует
            _ <- UserDao.update(user.chatID, _.copy(communicate = Some(value.phoneNumber))) // Обновляем информацию пользователя с полученным номером телефона
            _ <- UserDao.setNextStage(user.chatID) // Устанавливаем следующий этап в базе данных
            _ <- sendMessageWithButton("Отлично!", ReplyKeyboardRemove())
            _ <- sendMessageWithButton("Теперь расскажи, ты водитель или только пассажир?", chooseStatusButtons())
          } yield ()
          case None => Future.unit
        }

      case Stage.FillInfoSetIsDriver => Future.unit

      case Stage.Main => Future.unit
    }
  }

  //  onCallbackWithTag(IS_DRIVER_TAG)(buttonsIsDriverAction("Анкета сохранена. Ты можешь отредактировать её в любое время"))
  Await.result(run(), Duration.Inf)


}