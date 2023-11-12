import AnkIsDriverStage.{IS_DRIVER_TAG, buttonsIsDriverAction, chouseStatusButtons}
import Commands.allCommands
import Package._
import com.bot4s.telegram.api._
import com.bot4s.telegram.api.declarative.{Callbacks, Commands}
import com.bot4s.telegram.clients.FutureSttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models._
import date_base.Stage
import date_base.Stage.Stage
import date_base.dao.{StageDao, UserDao}
import sttp.client3.SttpBackend
import sttp.client3.okhttp.OkHttpFutureBackend

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Main extends TelegramBot with App with Polling with Commands[Future] with Callbacks[Future] {
  implicit val backend: SttpBackend[Future, Any] = OkHttpFutureBackend() //хз что это такое
  override val client: RequestHandler[Future] = new FutureSttpClient("6625745173:AAHfi3Bc-SdP3OXSROrE6PdMa_BgKrVJt6w")
  private val naherIdiExeption = new RuntimeException("Что-то пошло не так. По техническим причинам услуга временно недоступна")

  Commands.values.foreach(cmd => onCommand(cmd.toString)(Commands.getActionByCommand(cmd)))

  override def receiveMessage(msg: Message): Future[Unit] = {
    implicit val m: Message = msg
    (msg.text.filterNot(allCommands.contains), msg.contact) match {
      case (text, contact) if text.isDefined || contact.isDefined =>
        for {
          user <- getOrCreate(msg.source)
          stage <- StageDao.getOrCreate(user.chatID)
          messageContext = MessageContext(user, stage.stage)
          _ <- executeStage(messageContext).recover(a => println(s"eeeeerr ${a}"))
        } yield ()
      case _ => Future.unit
    }

  }

  private def getOrCreate(chatId: Long)(implicit msg: Message) = {
    UserDao.get(chatId).flatMap {
      case Some(value) => Future.successful(value)
      case None =>
        val user = date_base.User(
          userName = msg.chat.username.getOrElse("guest"),
          chatID = msg.source,
          isDriver = false,
          isAuthorized = false,
          fullName = msg.getNameOrNameCalling,
          communicate = None
        )
        UserDao.insert(user).flatMap(_ => UserDao.get(user.chatID)).flatMap {
          case Some(value) => Future.successful(value)
          case None => reply("Внутренняя ошибка приложения").flatMap(_ => {
            Future.failed(naherIdiExeption)
          })
        }
    }
  }

  private def executeStage(messageContext: MessageContext)(implicit msg: Message) = {
    val sendMessageWithoutButton = messageWithoutButton(msg.source, _)
    val sendMessageWithButton = messagesWithButtons(msg.source, _, _)
    messageContext.stage match {
      case Stage.NotAuthorized =>
        for {
          a <- StageDao.setNextStage(messageContext.getId)
          _ <- sendMessageWithoutButton(s"Привет, ${msg.getNameOrNameCalling}! Добро пожаловать в чат-бот Бла Бла Кар ТГУ. Давай заполним анкету")
          _ <-
            request(SendMessage(msg.source, "Как другие пользователи могут с тобой связаться?", replyMarkup = Some(ReplyKeyboardMarkup.singleButton(KeyboardButton.requestContact("Дать линку")))))
        } yield ()

      case Stage.FillInfoSetCommunicate =>
        msg.contact match {
          case Some(value) => for {
            _ <- UserDao.update(messageContext.getId, _.copy(communicate = Some(value.phoneNumber)))
            _ <- StageDao.setNextStage(messageContext.getId)
            _ <- sendMessageWithButton("Отлично!", ReplyKeyboardRemove())
            _ <- sendMessageWithButton("Теперь расскажи, ты водитель или только пассажир?", chouseStatusButtons())
          } yield ()
          case None => Future.unit
        }

      case Stage.FillInfoSetIsDriver => Future.unit

      case Stage.Main => Future.unit
    }
  }

  onCallbackWithTag(IS_DRIVER_TAG)(buttonsIsDriverAction("Анкета сохранена. Ты можешь отредактировать её в любое время"))
  Await.result(run(), Duration.Inf)


  def messagesWithButtons(id: Long, message: String, buttons: ReplyMarkup) =
    request(SendMessage(id, message, replyMarkup = Some(buttons)))

  def messageWithoutButton(id: Long, message: String) =
    request(SendMessage(id, message))
}

case class MessageContext(user: date_base.User, stage: Stage) {
  def getId = user.chatID
}