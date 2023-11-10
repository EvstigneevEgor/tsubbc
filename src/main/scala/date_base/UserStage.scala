package date_base

import date_base.Stage.Stage
import slick.jdbc.H2Profile.MappedColumnType
import slick.jdbc.SQLiteProfile.api._

object Stage extends Enumeration {
  type Stage = Value
  /** Юзер только зашел, и еще не авторизован -> FillInfoSetCommunicate */
  val NotAuthorized = Value("not_authorized")
  /** Юзер должен указать средства коммуникации  -> FillInfoSetIsDriver */
  val FillInfoSetCommunicate = Value("fill_info_set_communicate")
  /** Юзер должен выбрать роль */
  val FillInfoSetIsDriver = Value("fill_info_set_is_driver")

  // A ColumnType that maps it to NUMBER values 1 and 0
  val columnMapper = MappedColumnType.base[Stage, String](
    e => e.toString,
    s => Stage.withName(s)
  )
}


case class UserStage(id: Long, stage: Stage)

class UserStageTable(tag: Tag) extends Table[UserStage](tag, "user_stage") {
  def id = column[Long]("id", O.PrimaryKey)

  implicit val boolColumnType = Stage.columnMapper

  def stage = column[Stage]("stage")

  def * = (id, stage) <>
    (UserStage.tupled, UserStage.unapply)

}