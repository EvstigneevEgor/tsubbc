package date_base.dao


import date_base.BaseConfig.{db, trip, trip_point}
import date_base.Trip
import slick.jdbc.SQLiteProfile.api._
import scala.concurrent.Future

object TripDao extends Dao[Trip]{
  override def insert(t: Trip): Future[Int] =  db.run(trip += t)

  override def delete(id: Long): Future[Int] = db.run(trip.filter(_.id === id).delete)

  override def get(id: Long): Future[Option[Trip]] = db.run(trip.filter(_.id === id).result.headOption)
}
