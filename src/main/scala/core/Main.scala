package core

import com.bot4s.telegram.api._
import com.bot4s.telegram.api.declarative.{Callbacks, Commands}
import com.bot4s.telegram.clients.FutureSttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.models._
import core.Commands.allCommands
import date_base.StageType
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

  val config = ConfigSource.resources("token.conf").load[AppConfig] match { // для запуска положите в ресурсы токен
    case Right(appConfig) => appConfig
    case Left(failures) =>
      sys.error(s"Failed to load configuration: $failures")
  }
  override val client: RequestHandler[Future] = new FutureSttpClient(config.token)
  private val naherIdiExeption = new RuntimeException("Что-то пошло не так. По техническим причинам услуга временно недоступна")

  Commands.values.foreach(cmd => onCommand(cmd.toString)(Commands.getActionByCommand(cmd))) //функция, которая принимает каждый элемент коллекции Commands как cmd и выполняет операцию

  override def receiveMessage(msg: Message): Future[Unit] = { // Переопределение метода receiveMessage, который принимает сообщение типа Message и возвращает Future[Unit]
    // Проверка наличия текста в сообщении и контактных данных
    (msg.text.filterNot(allCommands.contains), msg.contact) match { // Фильтрация текста сообщения и проверка на наличие контактной информации в сообщении
      case (_, Some(contact)) =>
        for {
          user <- getOrCreateUser(msg) // Получение или создание пользователя на основе источника сообщения
          activity = ContactReceive(contact, user = user, messageId = Some(msg.messageId))
          _ <- Stage.getStageByType(user.stage).process(activity)
        } yield ()
      case (Some(sText), _) =>
        for {
          user <- getOrCreateUser(msg) // Получение или создание пользователя на основе источника сообщения
          activity = MessageReceive(sText, user = user, messageId = Some(msg.messageId))
          _ <- Stage.getStageByType(user.stage).process(activity)
        } yield ()
      case _ => Future.unit // Случай, если текста и контактной информации в сообщении нет
    }
  }


  onCallbackQuery {
    implicit cbq: CallbackQuery =>
      for {
        user <- cbq.message match {
          case Some(message) => getOrCreateUser(message)
          case None => Future.failed(naherIdiExeption)
        }
        activity <- cbq.data match {
          case Some(tag) => Future.successful(Some(ButtonPressed(Button(tag), user = user, messageId = cbq.message.map(_.messageId))))
          case None => Stage.getStageByType(user.stage).sendFirstMessage(user).map(_ => None)
        }
        _ <- activity match {
          case Some(btn) =>
            Stage.getStageByType(user.stage).process(btn)
          case None => Future.unit
        }
      } yield ()
  }

  private def getOrCreateUser(implicit msg: Message) = {
    UserDao.get(msg.source)
      .flatMap {
        case Some(value) => Future.successful(value)
        case None =>
          UserDao.insert(createNewUser(msg))
            .flatMap(_ =>
              UserDao.get(msg.source)
            )
            .flatMap {
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
      stage = StageType.NotAuthorized,
      previousStage = StageType.NotAuthorized,
      carInfo = None,
      editingTripId = None
    )
  }

  Await.result(run(), Duration.Inf)

}