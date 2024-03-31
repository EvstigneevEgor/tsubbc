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
 * 3. Найти попутчика [[createTripButton]]: Переносит на этап [[StageType.CreateTripSetTime]] (todo). Доступен только водителю [[https://github.com/EvstigneevEgor/tsubbc/blob/62b2db0a0b6f7d52cd0d46a9227d7e6aa8bf7e88/src/main/scala/core/stages/MainStage.scala#L42 уже есть фильтр]]
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
  private val tripInfoButton = Button("Информация о текущей поездке")
  //  private val listTripButton = Button("Список активных поездок")


  def mainButtons(user: User): InlineKeyboardMarkup = {
    InlineKeyboardMarkup.singleColumn(
      Seq(
        editAnketButton.getInlineKeyboardButton,
        searchTripButton.getInlineKeyboardButton) ++
        Option(createTripButton.getInlineKeyboardButton).filter(_ => user.isDriver).iterator.toSeq ++
        Seq(tripInfoButton.getInlineKeyboardButton) // @todo Добавить условие активной/ых поездок
      //          ,listTripButton.getInlineKeyboardButton)
    )
  }

  override def buttonPressedProcess(pressed: ButtonPressed)(implicit ec: ExecutionContext): Future[Unit] = {
    pressed.button match {
      case Button(editAnketButton.tag) =>
        for {
          _ <- UserDao.update(pressed.user.chatID, _.copy(stage = StageType.FillInfoSetIsDriver, carInfo = None))
          _ <- request(
            EditMessageReplyMarkup(
              Some(ChatId(pressed.user.chatID)),
              messageId = pressed.messageId,
              replyMarkup = None
            ))
          _ <- Stage.getStageByType(StageType.FillInfoSetIsDriver).sendFirstMessage(pressed.user)
        } yield ()
      case _ =>
        Stage.messageWithoutButton(
          pressed.user.chatID,
          "Функционал дальше в разработке :)"
        ).void
    }
  }

  override def sendFirstMessage(user: User): Future[Message] = {
    Stage.messagesWithButtons(user.chatID, "[Меню]", mainButtons(user))

  }
}
