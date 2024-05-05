package core

import cats.implicits.toFunctorOps
import com.bot4s.telegram.methods.{SendContact, SendMessage}
import com.bot4s.telegram.models.Message
import core.Main.request
import date_base.dao.UserDao

import scala.collection.immutable.SortedSet
import scala.concurrent.{ExecutionContext, Future}

// команды для дебага
object Commands extends Enumeration {
  type Commands = Value
  val DropMe: Value = Value("/dropMe")
  val ShowMe: Value = Value("/showMe")
  val AllContacts: Value = Value("/allContacts")
  val allCommands: SortedSet[String] = Commands.values.map(_.toString)

  def getActionByCommand(commands: Commands)(implicit ec: ExecutionContext): Message => Future[Unit] = {
    commands match {
      case ShowMe => implicit msg: Message =>
        for { // Получение информации о пользователе и его текущем этапе, если они есть
          user <- UserDao.get
          userStage <- UserDao.get
          _ <- request(SendMessage(msg.source, s"Все что о вас известно:\n$user\n$userStage")).void
        } yield ()

      case DropMe => implicit msg: Message =>
        for {
          user <- UserDao.get
          _ <- user.fold(Future.successful(0))(a =>
            UserDao.delete(a.chatID).flatMap(_ => UserDao.delete(a.chatID))
          )
          _ <- request(SendMessage(msg.source, "Вы успешно удалены из системы")).void
        } yield ()

      case AllContacts => implicit msg: Message =>
        for { // Получение всех пользователей и отправка контактов тех, у кого есть информация о контакте
          users <- UserDao.getAll
          filteredUser = users.filter(_.communicate.isDefined)
          _ <- Future.traverse(filteredUser)(user => request(SendContact(msg.source, user.communicate.get, user.fullName)).void) // Отправка контактов каждого пользователя, у которого есть информация о контакте
        } yield ()
    }
  }
}

