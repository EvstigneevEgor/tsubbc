package date_base.dao

import date_base.BaseConfig.{db, userStageTable}
import date_base.UserStage
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

object StageDao extends Dao[UserStage] {

  override def insert(t: UserStage): Future[Int] = db.run(userStageTable += t)

  def delete(id: Long): Future[Int] = db.run(userStageTable.filter(_.id === id).delete)

  def setNextStage(id: Long)(implicit ec: ExecutionContext): Future[Unit] = db.run {
    for {
      bdVs <- userStageTable.filter(_.id === id).result.headOption
      _ <- bdVs.map(a => userStageTable.update(a.setNewStage())).getOrElse(DBIO.successful())
    } yield ()
  }

  def update(us: UserStage)(implicit ec: ExecutionContext): Future[Unit] = db.run {
    for {
      bdVs <- userStageTable.filter(_.id === us.id).result.headOption
      _ <- bdVs.map(a => userStageTable.update(a.copy(stage = us.stage))).getOrElse(DBIO.successful(0))
    } yield ()
  }

  override def get(id: Long): Future[Option[UserStage]] = db.run(userStageTable.filter(_.id === id).result.headOption)

}
