package date_base.dao

import com.bot4s.telegram.models.Message
import date_base.BaseConfig.{db, user}
import date_base.User
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
}
