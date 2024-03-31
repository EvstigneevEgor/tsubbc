package core.stages.anket

import com.bot4s.telegram.methods.EditMessageReplyMarkup
import com.bot4s.telegram.models.{ChatId, InlineKeyboardMarkup, Message}
import core.Main.request
import core.{Button, ButtonPressed, RichFuture, Stage}
import date_base.StageType.StageType
import date_base.dao.UserDao
import date_base.{StageType, User}

import scala.concurrent.{ExecutionContext, Future}

object FillInfoSetIsDriver extends Stage {

  override val stageType: StageType = StageType.FillInfoSetIsDriver

  private val driverButton = Button(
    "Я водитель"
  )

  private val passengerButton = Button(
    "Я пассажир"
  )

  private def setIsDriver(user: User, isDriver: Boolean, messageId: Option[Int])(implicit ec: ExecutionContext) =
    for {
      _ <- UserDao.update(user.chatID, _.copy(isDriver = isDriver))
      nextStage = if (isDriver) StageType.FillInfoSetCar else StageType.Main
      _ <- UserDao.setStage(user.chatID, nextStage) // Устанавливаем следующий этап в базе данных
      _ <- request(
        EditMessageReplyMarkup(
          Some(ChatId(user.chatID)),
          messageId = messageId,
          replyMarkup = None
        ))
      _ <- Stage.getStageByType(nextStage).sendFirstMessage(user)
    } yield ()

  override def buttonPressedProcess(pressed: ButtonPressed)(implicit ec: ExecutionContext): Future[Unit] = {
    pressed.button match {
      case Button(driverButton.tag) =>
        setIsDriver(pressed.user, isDriver = true, pressed.messageId)
      case Button(passengerButton.tag) =>
        setIsDriver(pressed.user, isDriver = false, pressed.messageId).flatTap {
          Stage.messageWithoutButton(pressed.user.chatID, s"Зарегестрировал тебя как пассажира")
        }
    }
  }

  override def sendFirstMessage(user: User): Future[Message] = {
    Stage.messagesWithButtons(user.chatID, "Теперь расскажи, ты водитель или только пассажир?", chooseStatusButtons())
  }

  def chooseStatusButtons(): InlineKeyboardMarkup = {
    InlineKeyboardMarkup.singleRow(Seq(
      driverButton.getInlineKeyboardButton,
      passengerButton.getInlineKeyboardButton,
    ))
  }
}
