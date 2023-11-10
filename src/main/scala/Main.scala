import com.bot4s.telegram.api._
import com.bot4s.telegram.api.declarative._
import com.bot4s.telegram.clients.FutureSttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.models._
import date_base.dao.{StageDao, UserDao}
import sttp.client3.SttpBackend
import sttp.client3.okhttp.OkHttpFutureBackend

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

// Это пример бота

object Test extends TelegramBot with App with Polling with Commands[Future] {
  implicit val backend: SttpBackend[Future, Any] = OkHttpFutureBackend() //хз что это такое
  override val client: RequestHandler[Future] = new FutureSttpClient("6625745173:AAHfi3Bc-SdP3OXSROrE6PdMa_BgKrVJt6w")
  val naherIdiExeption = new RuntimeException("Что-то пошло не так. По техническим причинам услуга временно недоступна")

  implicit class RichMessage(x: Message) {
    def getOurChatId = x.chat.chatId.toEither.left.map(_.toString).merge

    def getNameOrNameCalling = x.chat.firstName.getOrElse("Мудила")
  }

  onCommand("/start") { implicit msg =>
    val chatId: String = msg.getOurChatId
    for {
      user <- getOrCreate(chatId)
      optStage <- StageDao.get(user.id.get)
    } yield ()

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

  //  def executeStage(user: date_base.User, userStage: Future[UserStage]) = {
  //    userStage.flatMap {
  //      _.stage match {
  //        case date_base.Stage.NotAuthorized => ???
  //        case date_base.Stage.FillInfoSetCommunicate => ???
  //        case date_base.Stage.FillInfoSetIsDriver => ???
  //      }
  //    }
  //  }

  private val eventualUnit: Future[Unit] = run()
  Await.result(eventualUnit.recover(a => println(a)), Duration.Inf) // жди результат бесконечно
}