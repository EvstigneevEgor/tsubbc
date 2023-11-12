package date_base

import slick.jdbc.SQLiteProfile.api._

case class User(chatID: Long, userName: String, fullName: String, communicate: Option[String], isDriver: Boolean, isAuthorized: Boolean)

class UserTable(tag: Tag) extends Table[User](tag, "user") {
  def id = column[Long]("id", O.PrimaryKey)

  def userName = column[String]("name")

  def fullName = column[String]("full_name")

  def communicate = column[Option[String]]("communicate")

  def isDriver = column[Boolean]("is_driver")

  def isAuthorized = column[Boolean]("is_authorized")

  def * = (id, userName, fullName, communicate, isDriver, isAuthorized) <>
    (User.tupled, User.unapply)

}