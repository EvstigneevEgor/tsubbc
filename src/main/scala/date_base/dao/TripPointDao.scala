package date_base.dao

import date_base.BaseConfig.{db, trip_point}
import date_base.TripPoint
import date_base.TripPointType.TripPointType
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.Future


object TripPointDao {

  def insert(t: TripPoint): Future[Int] = db.run(trip_point += t)

  def delete(id: Long): Future[Int] = db.run(trip_point.filter(_.id === id).delete)

  def get(id: Long): Future[Option[TripPoint]] = db.run(trip_point.filter(_.id === id).result.headOption)

  def getByTrip(id: Long): Future[Seq[TripPoint]] = db.run(trip_point.filter(_.tripID === id).result)

  def getByTripAndType(id: Long, tripPointType: TripPointType): Future[Seq[TripPoint]] = db
    .run(
      trip_point
        .filter(point => (point.tripID === id) && (point.tripPointType === tripPointType))
        .result
    )
}


