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

  val columnMapper = MappedColumnType.base[Stage, String](
    e => e.toString,  // Преобразование объекта типа Stage в строку
    s => Stage.withName(s)  // Преобразование строки из базы данных в объект типа Stage
  )
}


case class UserStage(id: Long, stage: Stage) {
  def setNewStage(): UserStage = {  // Метод для установки нового этапа пользователя
    Stage.getNextStage(stage).fold(this)(newStage => copy(stage = newStage))  // Получение следующего этапа из объекта Stage
  }
}

// Определение таблицы в базе данных с помощью Slick:
class UserStageTable(tag: Tag) extends Table[UserStage](tag, "user_stage") {
  implicit val boolColumnType: JdbcType[Stage] with BaseTypedType[Stage] = Stage.columnMapper // Неявная колонка типа Stage для маппинга типа данных Stage в базу данных
  def id = column[Long]("id", O.PrimaryKey)

  def stage = column[Stage]("stage")

  def * = (id, stage) <> // Объявление *, как сопоставлять столбцы таблицы с моделью UserStage
    (UserStage.tupled, UserStage.unapply)

}