package date_base

import date_base.TripType.TripType
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.SQLiteProfile.api._
import java.time.LocalDateTime

case class Trip(id: Int, dateTime: LocalDateTime, comment: String, initiatorID:Int, tripType: TripType )

object TripType extends Enumeration {
  type TripType = Value
  val ByDriver: Value = Value("by_driver")
  val ByPassenger: Value = Value("by_passenger")

  val columnMapper = MappedColumnType.base[TripType, String](
      e => e.toString, // Преобразование объекта типа TripType в строку
      s => TripType.withName(s) // Преобразование строки из базы данных в объект типа TripType
  )
}

class TripTable(tag: Tag) extends Table[Trip](tag, "trip")  {

  implicit val boolColumnType: JdbcType[TripType] with BaseTypedType[TripType] = TripType.columnMapper

  def id = column[Int]("id", O.PrimaryKey)

  def dateTime= column[LocalDateTime]("dateTime")

  def comment = column[String]("comment")

  def initiatorID = column[Int]("initiatorID")

  def tripType = column[TripType]("type")

  def * = (id, dateTime, comment, initiatorID, tripType) <>
    (Trip.tupled, Trip.unapply)
}
