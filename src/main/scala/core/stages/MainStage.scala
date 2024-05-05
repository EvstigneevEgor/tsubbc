package core.stages

import cats.implicits.toFunctorOps
import com.bot4s.telegram.methods.EditMessageReplyMarkup
import com.bot4s.telegram.models.{ChatId, InlineKeyboardMarkup, Message}
import core.Main.request
import core.{Button, ButtonPressed, Stage}
import date_base.StageType.StageType
import date_base.dao.UserDao
import date_base.{StageType, User}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Основное состояние бота в ожидании новой команды
 * Пункты меню:
 * <p>
 * 1. Редактировать анкету [[editAnketButton]]: Удаляет информацию о машине из анкеты, и переносит пользователя на этап [[StageType.FillInfoSetIsDriver]]
 * <p>
 * 2. Найти поездку [[searchTripButton]]: Переносит на этап [[StageType.FindTripSetTime]] (todo)
 * <p>
 * 3. Найти попутчика [[createTripButton]]: Переносит на этап [[StageType.CreateTripSetTime]]
 * <p>
 * 4. Информация о текущей поездке [[tripInfoButton]]: Переносит на этап [[StageType.CheckTrip]] . Доступен только для юзеров с активной поездкой (todo)
 * <p>
 * 5. Список активных поездок [[listTripButton]]: Пока не проработан. И не ясна разница с [[tripInfoButton]] @Yorymotoru
 * <p>
 *
 * @author Egor
 */
object MainStage extends Stage {

  override val stageType: StageType = StageType.Main

  private val editAnketButton = Button("Редактировать анкету")
  private val searchTripButton = Button("Найти поездку")
  private val createTripButton = Button("Найти попутчика")
  private val tripInfoButton = Button("Информация о ближайшей поездке")
  //  private val listTripButton = Button("Список активных поездок")


  def mainButtons(user: User): InlineKeyboardMarkup = {
    InlineKeyboardMarkup.singleColumn(
      Seq(
        editAnketButton.getInlineKeyboardButton,
        searchTripButton.getInlineKeyboardButton,
        createTripButton.getInlineKeyboardButton) ++
        Seq(tripInfoButton.getInlineKeyboardButton) // @todo Добавить условие активной/ых поездок
      //          ,listTripButton.getInlineKeyboardButton)
    )
  }

  override def buttonPressedProcess(pressed: ButtonPressed)(implicit ec: ExecutionContext): Future[Unit] = {
    pressed.button match {
      case Button(createTripButton.tag) =>
        for {
          _ <- UserDao.setStage(pressed.user.chatID, StageType.CreateTripSetTime)
          _ <- removeButton(pressed)
          _ <- Stage.getStageByType(StageType.CreateTripSetTime).sendFirstMessage(pressed.user)
        } yield ()
      case Button(editAnketButton.tag) =>
        for {
          _ <- UserDao.setStage(pressed.user.chatID, StageType.FillInfoSetIsDriver)
          _ <- UserDao.update(pressed.user.chatID, _.copy(carInfo = None))
          _ <- removeButton(pressed)
          _ <- Stage.getStageByType(StageType.FillInfoSetIsDriver).sendFirstMessage(pressed.user)
        } yield ()
      case _ =>
        Stage.messageWithoutButton(
          pressed.user.chatID,
          "Функционал дальше в разработке :)"
        ).void
    }
  }

  private def removeButton(pressed: ButtonPressed) = {
    request(
      EditMessageReplyMarkup(
        Some(ChatId(pressed.user.chatID)),
        messageId = pressed.messageId,
        replyMarkup = None
      ))
  }

  override def sendFirstMessage(user: User): Future[Message] = {
    Stage.messagesWithButtons(user.chatID, "[Меню]", mainButtons(user))

  }
}
