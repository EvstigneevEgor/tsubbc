import cats.implicits.toFunctorOps
import cats.instances.future._
import com.bot4s.telegram.Implicits._
import com.bot4s.telegram.api._
import com.bot4s.telegram.api.declarative.{Callbacks, Commands}
import com.bot4s.telegram.clients.FutureSttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.methods.{EditMessageReplyMarkup, SendMessage}
import com.bot4s.telegram.models._
import date_base.Stage.Stage
import date_base.dao.{StageDao, UserDao}
import date_base.{Stage, UserStage}
import sttp.client3.SttpBackend
import sttp.client3.okhttp.OkHttpFutureBackend

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
// Это пример бота

object Main extends TelegramBot with App with Polling with Commands[Future] with Callbacks[Future] {
  implicit val backend: SttpBackend[Future, Any] = OkHttpFutureBackend() //хз что это такое
  override val client: RequestHandler[Future] = new FutureSttpClient("6625745173:AAHfi3Bc-SdP3OXSROrE6PdMa_BgKrVJt6w")
  private val naherIdiExeption = new RuntimeException("Что-то пошло не так. По техническим причинам услуга временно недоступна")
  private val allComands = Seq("/dropMe", "/showMe", "/counter")

  implicit class RichFuture[T](x: Future[T]) {
    def flatTap[R](anotherFuture: Future[R]): Future[T] = x.flatMap(a => anotherFuture.map(_ => a))
  }

  implicit class RichMessage(x: Message) {
    def getOurChatId: String = x.from.get.id.toString

    def getNameOrNameCalling: String = x.from.map(_.firstName).getOrElse("Мудила")
  }

  onCommand("/dropMe") { implicit msg =>
    for {
      user <- UserDao.getByChatId(msg.getOurChatId)
      _ <- user.fold(Future.successful(0))(
        _.id.fold(Future.successful(0))(id => UserDao.delete(id).flatMap(_ => StageDao.delete(id)))
      )
      _ <- request(SendMessage(msg.source, "Вы успешно удалены из системы")).void
    } yield ()
  }

  onCommand("/showMe") { implicit msg =>
    for {
      user <- UserDao.getByChatId(msg.getOurChatId)
      userStage <- user.map(user => StageDao.get(user.id.get)).getOrElse(Future.successful(None))
      _ <- request(SendMessage(msg.source, s"Все что о вас известно:\n$user\n$userStage")).void
    } yield ()
  }


  override def receiveMessage(msg: Message): Future[Unit] = // сообщение : ответ
  {
    msg.text.filterNot(allComands.contains) match {
      case Some(_) => val chatId: String = msg.getOurChatId
        implicit val m: Message = msg
        for {
          user <- getOrCreate(chatId)
          stage <- StageDao.get(user.id.get).flatMap {
            case Some(value) => Future.successful(value)
            case None =>
              val newStage = UserStage(user.id.get, Stage.NotAuthorized)
              StageDao.insert(newStage).map(_ => newStage)
          }
          _ <- executeStage(user.id.get, stage.stage)
        } yield ()
      case None => Future.unit
    }

  }

  private def getOrCreate(chatId: String)(implicit msg: Message) = {
    UserDao.getByChatId(chatId).flatMap {
      case Some(value) => Future.successful(value)
      case None =>
        val user = date_base.User(
          userName = msg.chat.username.getOrElse("guest"),
          chatID = msg.getOurChatId,
          isDriver = false,
          isAuthorized = false,
          fullName = msg.getNameOrNameCalling,
          communicate = None
        )
        UserDao.insert(user).flatMap(_ => UserDao.getByChatId(user.chatID)).flatMap {
          case Some(value) => Future.successful(value)
          case None => reply("Внутренняя ошибка приложения").flatMap(_ => {
            Future.failed(naherIdiExeption)
          })
        }
    }
  }

  private def executeStage(userId: Long, actualStage: Stage)(implicit msg: Message) = {
    actualStage match {
      case date_base.Stage.NotAuthorized =>
        for {
          _ <- StageDao.setNextStage(userId)
          _ <- request(SendMessage(msg.source, s"Привет, ${msg.getNameOrNameCalling}! Добро пожаловать в чат-бот Бла Бла Кар ТГУ. Давай заполним анкету"))
          _ <- request(SendMessage(msg.source, "Как другие пользователи могут с тобой связаться?\nНапример:\n\"@username\"\n\"вк: https://vk.com/id\"\n\"+7 9123456789\""))
        } yield ()

      case date_base.Stage.FillInfoSetCommunicate =>
        for {
          _ <- UserDao.update(userId, _.copy(communicate = msg.text))
          _ <- StageDao.setNextStage(userId)
          _ <- request(SendMessage(msg.source, "Отлично!"))
          _ <- request(SendMessage(msg.source, "Теперь расскажи, ты водитель или только пассажир?", replyMarkup = chouseStatusButtons()))
        } yield ()

      case date_base.Stage.FillInfoSetIsDriver =>
        //        msg.text match {
        //          case Some(value) if value.toLowerCase.trim == "водитель" => setIsDriver(userId, isDriver = true)
        //          case Some(value) if value.toLowerCase.trim == "пассажир" => setIsDriver(userId, isDriver = false)
        //          case _ => Future.successful(List("Теперь расскажи, ты Водитель или только Пассажир?"))
        //        }
        Future.successful()
      case date_base.Stage.Main => Future.successful()
    }
  }

  private def setIsDriver(chatId: Long, isDriver: Boolean) =
    for {
      user <- UserDao.getByChatId(chatId.toString)
      _ <- UserDao.update(user.flatMap(_.id).get, _.copy(isDriver = isDriver))
      _ <- StageDao.setNextStage(user.flatMap(_.id).get)
    } yield ()


  private val IS_DRIVER_TAG = "IS_DRIVER_TAG"

  def tag = prefixTag(IS_DRIVER_TAG) _


  private def chouseStatusButtons() = {
    InlineKeyboardMarkup.singleRow(Seq(
      InlineKeyboardButton.callbackData(s"Я водитель !!!", tag("Водитель")),
      InlineKeyboardButton.callbackData(s"Я пассажир !!!", tag("Пассажир"))
    ))
  }

  onCallbackWithTag(IS_DRIVER_TAG) { implicit cbq =>
    val ackFuture = ackCallback(cbq.from.firstName + " pressed the button!")
    val maybeEditFuture = for {
      data <- cbq.data
      msg <- cbq.message
      response <- data.trim.toLowerCase match {
        case "водитель" => Some(setIsDriver(cbq.from.id, isDriver = true))
        case "пассажир" => Some(setIsDriver(cbq.from.id, isDriver = false))
        case _ => None
      }
      response <- response.flatMap(_ => request(
        EditMessageReplyMarkup(
          ChatId(msg.source),
          msg.messageId,
          replyMarkup = None
        )
      )).flatTap(request(SendMessage(msg.source, "Анкета сохранена. Ты можешь отредактировать её в любое время")))
    } yield response

    ackFuture.zip(maybeEditFuture.getOrElse(Future.successful(()))).void
  }


  Await.result(run(), Duration.Inf)
}