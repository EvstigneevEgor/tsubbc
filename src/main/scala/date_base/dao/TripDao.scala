package date_base.dao

import core.Main.executionContext
import date_base.BaseConfig.{db, trip}
import date_base.Trip
import slick.dbio.Effect
import slick.jdbc.SQLiteProfile.api._
import slick.sql.FixedSqlAction

import scala.concurrent.Future

object TripDao {
  def insert(t: Trip): Future[Long] = db.run(insertAndGetIdAction(t))

  def insertAndGetIdAction: Trip => FixedSqlAction[Long, NoStream, Effect.Write] =
    (trip returning trip.map(_.id)) += (_: Trip)

  def delete(id: Long): Future[Int] = db.run(trip.filter(_.id === id).delete)

  def get(id: Long): Future[Option[Trip]] = db.run(trip.filter(_.id === id).result.headOption)


  def update(id: Long, f: Trip => Trip): Future[Unit] = db.run {
    for {
      bdVs <- trip.filter(_.id === id).result.headOption
      _ <- bdVs.map(a => trip.filter(_.id === id).update(f(a))).getOrElse(DBIO.successful(0))
    } yield ()
  }

  def getTripByInitiator(initiatorId: Long): Future[Seq[Trip]] = db.run(trip.filter(_.initiatorID === initiatorId).sortBy(_.dateCreate).result)
}
