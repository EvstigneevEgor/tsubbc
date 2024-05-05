package date_base

import date_base.TripType.TripType
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.SQLiteProfile.api._

import java.time.LocalDateTime

case class Trip(id: Option[Long] = None, dateTime: LocalDateTime, comment: Option[String], initiatorID: Long, tripType: TripType, createDate: Option[LocalDateTime] = None)

object TripType extends Enumeration {
  type TripType = Value
  val ByDriver: Value = Value("by_driver")
  val ByPassenger: Value = Value("by_passenger")

  implicit val columnMapper: JdbcType[TripType] with BaseTypedType[TripType] = MappedColumnType.base[TripType, String](
    e => e.toString, // Преобразование объекта типа TripType в строку
    s => TripType.withName(s) // Преобразование строки из базы данных в объект типа TripType
  )
}

class TripTable(tag: Tag) extends Table[Trip](tag, "trip") {

  implicit val boolColumnType: JdbcType[TripType] with BaseTypedType[TripType] = TripType.columnMapper

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def dateTime = column[LocalDateTime]("dateTime")

  def dateCreate = column[LocalDateTime]("dateTime", O.AutoInc)

  def comment = column[Option[String]]("comment")

  def initiatorID = column[Long]("initiatorID")

  def tripType = column[TripType]("type")

  def * = (id.?, dateTime, comment, initiatorID, tripType, dateCreate.?) <>
    (Trip.tupled, Trip.unapply)
}
