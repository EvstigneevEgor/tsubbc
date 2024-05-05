package date_base

import date_base.TripPointType.TripPointType
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.SQLiteProfile.api._


case class TripPoint(id: Option[Long], tripID: Long, description: String, longitude: Option[Double], latitude: Option[Double], tripPointType: TripPointType)

object TripPointType extends Enumeration {
  type TripPointType = Value
  val starting: Value = Value("starting")
  val ending: Value = Value("ending")
  val intermediate: Value = Value("intermediate")

  implicit val columnMapper: JdbcType[TripPointType] with BaseTypedType[TripPointType] = MappedColumnType.base[TripPointType, String](
    e => e.toString, // Преобразование объекта типа TripPointType в строку
    s => TripPointType.withName(s) // Преобразование строки из базы данных в объект типа TripPointType
  )
}

class TripPointTable(tag: Tag) extends Table[TripPoint](tag, "trip_point") {

  implicit val boolColumnType: JdbcType[TripPointType] with BaseTypedType[TripPointType] = TripPointType.columnMapper

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def tripID = column[Long]("tripID")

  def description = column[String]("description")

  def longitude = column[Option[Double]]("longitude")

  def latitude = column[Option[Double]]("latitude")

  def tripPointType = column[TripPointType]("tripPointType")

  def * = (id.?, tripID, description, longitude, latitude, tripPointType) <>
    (TripPoint.tupled, TripPoint.unapply)
}