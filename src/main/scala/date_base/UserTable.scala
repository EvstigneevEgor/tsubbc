package date_base

import date_base.StageType.StageType
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.SQLiteProfile.api._

object StageType extends Enumeration {
  type StageType = Value
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

  //*todo сделать метод который будет выдавать человекочитаемое название этапа*//
  def getReadableName(stageType: StageType): String = ???

  def getNextStage(stage: StageType): Option[StageType] =
    stage match {
      case NotAuthorized => Some(FillInfoSetCommunicate)
      case Main => None
      case FillInfoSetCommunicate => Some(FillInfoSetIsDriver)
      case FillInfoSetIsDriver => Some(Main)
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

  val columnMapper = MappedColumnType.base[StageType, String](
    e => e.toString, // Преобразование объекта типа Stage в строку
    s => StageType.withName(s) // Преобразование строки из базы данных в объект типа Stage
  )
}

case class User(
                 chatID: Long,
                 userName: String,
                 fullName: String,
                 communicate: Option[String],
                 isDriver: Boolean,
                 carInfo: Option[String],
                 isAuthorized: Boolean,
                 stage: StageType,
                 previousStage: StageType,
                 editingTripId: Option[Long]
               )

class UserTable(tag: Tag) extends Table[User](tag, "user") {
  implicit val boolColumnType: JdbcType[StageType] with BaseTypedType[StageType] = StageType.columnMapper

  def id = column[Long]("id", O.PrimaryKey)

  def userName = column[String]("name")

  def fullName = column[String]("full_name")

  def communicate = column[Option[String]]("communicate")

  def isDriver = column[Boolean]("is_driver")

  def carInfo = column[Option[String]]("car_info")

  def isAuthorized = column[Boolean]("is_authorized")

  def stage = column[StageType]("stage")

  def previousStage = column[StageType]("previous_stage")

  def editingTripId = column[Option[Long]]("editing_trip_Id")


  def * = (id, userName, fullName, communicate, isDriver, carInfo, isAuthorized, stage, previousStage, editingTripId) <>
    (User.tupled, User.unapply)

}