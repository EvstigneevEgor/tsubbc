package date_base.dao

import date_base.BaseConfig.{db, user}
import date_base.User
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.Future

object UserDao extends Dao[User] {

  override def insert(t: User): Future[Int] = db.run(user += t)

  override def get(id: Long): Future[Option[User]] = db.run(user.filter(_.id === id).result.headOption)

  def getByChatId(chatId: String): Future[Option[User]] = db.run(user.filter(_.chatId === chatId).result.headOption)
}
