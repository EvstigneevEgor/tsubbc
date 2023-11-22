package core

import cats.implicits.toFunctorOps
import com.bot4s.telegram.methods.{SendContact, SendMessage}
import com.bot4s.telegram.models.Message
import core.Main.request
import date_base.dao.{StageDao, UserDao}

import scala.concurrent.{ExecutionContext, Future}
// Это пример бота

object Commands extends Enumeration {
  type Commands = Value
  val DropMe: Value = Value("/dropMe")
  val ShowMe: Value = Value("/showMe")
  val AllContacts: Value = Value("/allContacts")
  val allCommands = Commands.values.map(_.toString)

  def getActionByCommand(commands: Commands)(implicit ec: ExecutionContext) = {
    commands match {
      case ShowMe => { implicit msg: Message =>
        for {// Получение информации о пользователе и его текущем этапе, если они есть
          user <- UserDao.get
          userStage <- StageDao.get
          _ <- request(SendMessage(msg.source, s"Все что о вас известно:\n$user\n$userStage")).void
        } yield ()
      }
      case DropMe => { implicit msg: Message =>
        for {
          user <- UserDao.get
          _ <- user.fold(Future.successful(0))(a =>
            UserDao.delete(a.chatID).flatMap(_ => StageDao.delete(a.chatID))
          )
          _ <- request(SendMessage(msg.source, "Вы успешно удалены из системы")).void
        } yield ()
      }
      case AllContacts => { implicit msg: Message =>
        for {  // Получение всех пользователей и отправка контактов тех, у кого есть информация о контакте
          users <- UserDao.getAll()
          filteredUser = users.filter(_.communicate.isDefined)
          _ <- Future.traverse(filteredUser)(user => request(SendContact(msg.source, user.communicate.get, user.fullName)).void)  // Отправка контактов каждого пользователя, у которого есть информация о контакте
        } yield ()
      }
    }
  }
}

