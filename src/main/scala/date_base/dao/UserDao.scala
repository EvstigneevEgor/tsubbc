package date_base.dao

import date_base.BaseConfig.{db, user}
import date_base.User
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

object UserDao extends Dao[User] {

  override def insert(t: User): Future[Int] = db.run(user += t)

  override def get(id: Long): Future[Option[User]] = db.run(user.filter(_.id === id).result.headOption)

  def delete(id: Long): Future[Int] = db.run(user.filter(_.id === id).delete)

  def update(id: Long, f: User => User)(implicit ec: ExecutionContext) = db.run {
    for {
      bdVs <- user.filter(_.id === id).result.headOption
      _ <- bdVs.map(a => user.update(f(a))).getOrElse(DBIO.successful(0))
    } yield ()
  }

  def getByChatId(chatId: String): Future[Option[User]] = db.run(user.filter(_.chatId === chatId).result.headOption)
}
