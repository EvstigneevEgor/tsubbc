package date_base

import date_base.Stage.Stage
import slick.ast.BaseTypedType
import slick.jdbc.H2Profile.MappedColumnType
import slick.jdbc.JdbcType
import slick.jdbc.SQLiteProfile.api._

object Stage extends Enumeration {
  type Stage = Value
  /** Юзер только зашел, и еще не авторизован -> FillInfoSetCommunicate */
  val NotAuthorized: Value = Value("not_authorized")
  /** Юзер должен указать средства коммуникации  -> FillInfoSetIsDriver */
  val FillInfoSetCommunicate: Value = Value("fill_info_set_communicate")
  /** Юзер должен выбрать роль */
  val FillInfoSetIsDriver: Value = Value("fill_info_set_is_driver")
  /** главная страница -> Вариативно??? */
  val Main: Value = Value("main")

  def getNextStage(stage: Stage): Option[Stage] =
    stage match {
      case NotAuthorized => Some(FillInfoSetCommunicate)
      case FillInfoSetCommunicate => Some(FillInfoSetIsDriver)
      case FillInfoSetIsDriver => Some(Main)
      case Main => None
    }

  // A ColumnType that maps it to NUMBER values 1 and 0
  val columnMapper = MappedColumnType.base[Stage, String](
    e => e.toString,
    s => Stage.withName(s)
  )
}


case class UserStage(id: Long, stage: Stage) {
  def setNewStage(): UserStage = {
    Stage.getNextStage(stage).fold(this)(newStage => copy(stage = newStage))
  }
}

class UserStageTable(tag: Tag) extends Table[UserStage](tag, "user_stage") {
  def id = column[Long]("id", O.PrimaryKey)

  implicit val boolColumnType: JdbcType[Stage] with BaseTypedType[Stage] = Stage.columnMapper

  def stage = column[Stage]("stage")

  def * = (id, stage) <>
    (UserStage.tupled, UserStage.unapply)

}