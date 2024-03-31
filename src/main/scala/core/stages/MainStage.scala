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

object MainStage extends Stage {

  override val stageType: StageType = StageType.Main

  private val editAnketButton = Button("Редактировать анкету")
  private val searchTripButton = Button("Найти поездку")
  private val createTripButton = Button("Найти попутчика")
  private val tripInfoButton = Button("Информация о текущей поездке")
  private val listTripButton = Button("Список активных поездок")


  def mainButtons(user: User): InlineKeyboardMarkup = {
    InlineKeyboardMarkup.singleColumn(
      Seq(
        editAnketButton.getInlineKeyboardButton,
        searchTripButton.getInlineKeyboardButton) ++
        Option(createTripButton.getInlineKeyboardButton).filter(_ => user.isDriver).iterator.toSeq ++
        Seq(tripInfoButton.getInlineKeyboardButton, // Добавить условие активной/ых поездок
          listTripButton.getInlineKeyboardButton)
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
