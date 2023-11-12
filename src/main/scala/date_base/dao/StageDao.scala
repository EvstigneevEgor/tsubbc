package date_base.dao

import date_base.BaseConfig.{db, userStageTable}
import date_base.{Stage, UserStage}
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

object StageDao extends Dao[UserStage] {

  override def insert(t: UserStage): Future[Int] = db.run(userStageTable += t)

  def delete(id: Long): Future[Int] = db.run(userStageTable.filter(_.id === id).delete)

  def setNextStage(id: Long)(implicit ec: ExecutionContext): Future[Unit] = db.run {
    for {
      existingUser <- userStageTable.filter(_.id === id).result.headOption
      _ <- existingUser match {
        case Some(user) =>
          Stage.getNextStage(user.stage) match {
            case Some(value) =>
              val updatedUser = user.copy(stage = value)
              userStageTable.filter(_.id === id).update(updatedUser)
            case None => DBIO.successful(0) // Handle the case where the next stage is not available
          }
        case None => DBIO.successful(0) // Handle the case where the user with the given ID does not exist
      }
    } yield ()
  }

  def update(us: UserStage)(implicit ec: ExecutionContext): Future[Unit] = db.run {
    for {
      bdVs <- userStageTable.filter(_.id === us.id).result.headOption
      _ <- bdVs.map(a => userStageTable.filter(_.id === us.id).update(a.copy(stage = us.stage))).getOrElse(DBIO.successful(0))
    } yield ()
  }

  override def get(id: Long): Future[Option[UserStage]] = db.run(userStageTable.filter(_.id === id).result.headOption)

  def getOrCreate(id: Long)(implicit ec: ExecutionContext): Future[UserStage] = db.run(
    for {
      existStage <- userStageTable.filter(_.id === id).result.headOption
      a <- existStage match {
        case Some(value) =>
          DBIO.successful(value)
        case None =>
          val newStage = UserStage(id, Stage.NotAuthorized)
          (userStageTable += newStage).map(_ => newStage)
      }
    } yield (a)
  )

}
