import cats.implicits.toFunctorOps
import com.bot4s.telegram.api._
import com.bot4s.telegram.api.declarative._
import com.bot4s.telegram.clients.FutureSttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models._
import date_base.dao.{StageDao, UserDao}
import date_base.{Stage, UserStage}
import sttp.client3.SttpBackend
import sttp.client3.okhttp.OkHttpFutureBackend

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

// Это пример бота

object Test extends TelegramBot with App with Polling with Commands[Future] {
  implicit val backend: SttpBackend[Future, Any] = OkHttpFutureBackend() //хз что это такое
  override val client: RequestHandler[Future] = new FutureSttpClient("6625745173:AAHfi3Bc-SdP3OXSROrE6PdMa_BgKrVJt6w")
  val naherIdiExeption = new RuntimeException("Что-то пошло не так. По техническим причинам услуга временно недоступна")
  val allComands = Seq("/dropMe", "/showMe")

  implicit class RichMessage(x: Message) {
    def getOurChatId = x.from.get.id.toString

    def getNameOrNameCalling = x.from.map(_.firstName).getOrElse("Мудила")
  }

  onCommand("/dropMe") { implicit msg =>
    for {
      user <- UserDao.getByChatId(msg.getOurChatId)
      _ <- user.fold(Future.successful(0))(
        _.id.fold(Future.successful(0))(id => UserDao.delete(id).flatMap(a => StageDao.delete(id)))
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
        implicit val m = msg
        for {
          user <- getOrCreate(chatId)
          stage <- StageDao.get(user.id.get).flatMap {
            case Some(value) => Future.successful(value)
            case None =>
              val newStage = UserStage(user.id.get, Stage.NotAuthorized)
              StageDao.insert(newStage).map(_ => newStage)
          }
          messages <- executeStage(user, stage)
          _ <- messages.foldLeft(Future.unit)((a, it) => a.flatMap(_ => request(SendMessage(msg.source, it)).void))
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

  def executeStage(user: date_base.User, userStage: UserStage)(implicit msg: Message) = {
    userStage.stage match {
      case date_base.Stage.NotAuthorized =>
        StageDao.update(userStage.copy(stage = Stage.FillInfoSetCommunicate)).map(_ => List(s"Привет, ${msg.getNameOrNameCalling}! Добро пожаловать в чат-бот Бла Бла Кар ТГУ. Давай заполним анкету",
          "Как другие пользователи могут с тобой связаться?\nНапример:\n\"@username\"\n\"вк: https://vk.com/id\"\n\"+7 9123456789\""))

      case date_base.Stage.FillInfoSetCommunicate =>
        for {
          _ <- UserDao.update(user.id.get, _.copy(communicate = msg.text))
          _ <- StageDao.update(userStage.copy(stage = Stage.FillInfoSetIsDriver))
        } yield
          List("Отлично!", "Теперь расскажи, ты водитель или только пассажир?")

      case date_base.Stage.FillInfoSetIsDriver =>
        msg.text match {
          case Some(value) if value.toLowerCase.trim == "водитель" => setIsDriver(user, userStage, isDriver = true)
          case Some(value) if value.toLowerCase.trim == "пассажир" => setIsDriver(user, userStage, isDriver = false)
          case _ => Future.successful(List("Теперь расскажи, ты Водитель или только Пассажир?"))
        }

      case date_base.Stage.Main => Future.successful(List("Ты справился, ублюдлок"))
    }
  }

  private def setIsDriver(user: date_base.User, userStage: UserStage, isDriver: Boolean) =
    for {
      _ <- UserDao.update(user.id.get, _.copy(isDriver = isDriver))
      _ <- StageDao.update(userStage.copy(stage = Stage.Main))
    } yield
      Seq("Анкета сохранена. Ты можешь отредактировать её в любое время")


  private val eventualUnit: Future[Unit] = run()
  Await.result(eventualUnit.recover(a => println(a)), Duration.Inf) // жди результат бесконечно
}