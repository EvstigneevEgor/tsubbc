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

  /** описание машины */
  val FillInfoSetCar: Value = Value("fill_info_set_car")
  /** Пассажир должен указать дату и время поездки */
  val FindTripSetTime: Value = Value("find_trip_set_time")
  /** Пользователь может узнать инф о поездке */
  val CheckTrip: Value = Value("check_trip")

  /** Пассажир должен указать откуда он хочет уехать */
  val FindTripSetFirst: Value = Value("find_trip_set_first")
  /** Пассажир должен указать куда он направляется */
  val FindTripSetSecond: Value = Value("find_trip_set_second")
  /** Пассажир может оставить комментарий */
  val FindTripSetComment: Value = Value("find_trip_set_comment")

  /** Водитель должен указать дату и время поездки */
  val CreateTripSetTime: Value = Value("create_trip_set_time")
  /** Водитель должен указать начальную точку */
  val CreateTripSetFirst: Value = Value("create_trip_set_first")
  /** Водитель должен указать конечную точку */
  val CreateTripSetSecond: Value = Value("create_trip_set_second")
  /** Водитель может оставить комментарий */
  val CreateTripSetComment: Value = Value("create_trip_set_comment")


  def getNextStage(stage: Stage): Option[Stage] =
    stage match {
      case NotAuthorized => Some(FillInfoSetCommunicate)
      case FillInfoSetCommunicate => Some(FillInfoSetIsDriver)
      case FillInfoSetIsDriver => Some(Main)
      case Main => None
      case FillInfoSetCar => Some(Main)
      case FindTripSetTime => Some(FindTripSetFirst)
      case FindTripSetFirst => Some(FindTripSetSecond)
      case FindTripSetSecond => Some(FindTripSetComment)
      case FindTripSetComment => Some(Main)
      case CreateTripSetTime => Some(CreateTripSetFirst)
      case CreateTripSetFirst => Some(CreateTripSetSecond)
      case CreateTripSetSecond => Some(CreateTripSetComment)
      case CreateTripSetComment => Some(Main)
    }

  val columnMapper = MappedColumnType.base[Stage, String](
    e => e.toString, // Преобразование объекта типа Stage в строку
    s => Stage.withName(s) // Преобразование строки из базы данных в объект типа Stage
  )
}


case class UserStage(id: Long, stage: Stage) {
  def setNewStage(): UserStage = { // Метод для установки нового этапа пользователя
    Stage.getNextStage(stage).fold(this)(newStage => copy(stage = newStage)) // Получение следующего этапа из объекта Stage
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