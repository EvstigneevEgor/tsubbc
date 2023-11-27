package date_base.dao

import com.bot4s.telegram.models.Message
import date_base.BaseConfig.{db, user}
import date_base.{BaseConfig, Stage, User}
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

object UserDao extends Dao[User] {

  override def insert(t: User): Future[Int] = db.run(user += t)

  override def get(id: Long): Future[Option[User]] = db.run(user.filter(_.id === id).result.headOption)

  def get(implicit m: Message): Future[Option[User]] = db.run(user.filter(_.id === m.source).result.headOption)

  def getAll(): Future[Seq[User]] = db.run(user.result)

  def delete(id: Long): Future[Int] = db.run(user.filter(_.id === id).delete)

  def update(id: Long, f: User => User)(implicit ec: ExecutionContext) = db.run {
    for {
      bdVs <- user.filter(_.id === id).result.headOption
      _ <- bdVs.map(a => user.filter(_.id === id).update(f(a))).getOrElse(DBIO.successful(0))
    } yield ()
  }

  def setNextStage(id: Long)(implicit ec: ExecutionContext): Future[Unit] = db.run {
    for {
      existingUser <- user.filter(_.id === id).result.headOption
      _ <- existingUser match {
        case Some(user) =>
          Stage.getNextStage(user.stage) match {
            case Some(value) =>
              val updatedUser = user.copy(stage = value)
              BaseConfig.user.filter(_.id === id).update(updatedUser)
            case None => DBIO.successful(0) // Handle the case where the next stage is not available
          }
        case None => DBIO.successful(0) // Handle the case where the user with the given ID does not exist
      }
    } yield ()
  }

}
