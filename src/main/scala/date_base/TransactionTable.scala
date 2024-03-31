package date_base

import date_base.Status.Status
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.SQLiteProfile.api._

/**
 * @Yorymotoru Можешь написать что это?) Я забыл)))
 */
case class Transaction(id: Long, user_id: Int, trip_id: Int, status: Status)

object Status extends Enumeration {
  type Status = Value
  val requested: Value = Value("requested")
  val approved: Value = Value("approved")
  val canceled: Value = Value("canceled")

  val columnMapper = MappedColumnType.base[Status, String](
    e => e.toString, // Преобразование объекта типа Status в строку
    s => Status.withName(s) // Преобразование строки из базы данных в объект типа Status
  )
}

class TransactionTable(tag: Tag) extends Table[Transaction](tag, "transaction") {

  implicit val boolColumnType: JdbcType[Status] with BaseTypedType[Status] = Status.columnMapper

  def id = column[Long]("id", O.PrimaryKey)

  def user_id = column[Int]("user_id")

  def trip_id = column[Int]("trip_id")

  def status = column[Status]("status")

  def * = (id, user_id, trip_id, status) <>
    (Transaction.tupled, Transaction.unapply)
}

