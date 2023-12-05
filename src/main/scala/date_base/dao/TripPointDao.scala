package date_base.dao

import date_base.BaseConfig.{db, transaction, trip_point}
import date_base.TripPoint
import slick.jdbc.SQLiteProfile.api._
import scala.concurrent.{ExecutionContext, Future}


object TripPointDao extends Dao[TripPoint] {

  override def insert(t: TripPoint): Future[Int] =  db.run(trip_point += t)

  override def delete(id: Long): Future[Int] =  db.run(trip_point.filter(_.id === id).delete)

  override def get(id: Long): Future[Option[TripPoint]] = db.run(trip_point.filter(_.id === id).result.headOption)
}


