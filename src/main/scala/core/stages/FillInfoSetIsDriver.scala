package core.stages

import com.bot4s.telegram.methods.EditMessageReplyMarkup
import com.bot4s.telegram.models.{ChatId, InlineKeyboardMarkup, Message}
import core.Main.request
import core.{Button, ButtonPressed, Stage}
import date_base.StageType
import date_base.StageType.StageType
import date_base.dao.UserDao

import scala.concurrent.{ExecutionContext, Future}

object FillInfoSetIsDriver extends Stage {

  override val stageType: StageType = StageType.FillInfoSetIsDriver

  private val driverButton = Button(
    "Я водитель"
  )

  private val passengerButton = Button(
    "Я пассажир"
  )

  private def setIsDriver(chatId: Long, isDriver: Boolean, messageId: Option[Int])(implicit ec: ExecutionContext) =
    for {
      _ <- UserDao.update(chatId, _.copy(isDriver = isDriver))
      _ <- UserDao.setNextStage(chatId)
      _ <- request(
        EditMessageReplyMarkup(
          Some(ChatId(chatId)),
          messageId = messageId,
          replyMarkup = None
        ))
      _ <- sendLastMessage(chatId)
    } yield ()

  override def buttonPressedProcess(pressed: ButtonPressed)(implicit ec: ExecutionContext): Future[Unit] = {
    pressed.button match {
      case Button(driverButton.tag) =>
        setIsDriver(pressed.user.chatID, isDriver = true, pressed.messageId).recover { a: Throwable =>
          println(a)
          a.printStackTrace()
          a
        }
      case Button(passengerButton.tag) =>
        setIsDriver(pressed.user.chatID, isDriver = false, pressed.messageId).recover { a: Throwable =>
          println(a)
          a.printStackTrace()
          a
        }
    }
  }

  override def sendLastMessage(chatId: Long): Future[Message] = {
    Stage.messageWithoutButton(
      chatId,
      "Функционал дальше в разработке :)"
    )
  }

  def chooseStatusButtons(): InlineKeyboardMarkup = {
    InlineKeyboardMarkup.singleRow(Seq(
      driverButton.getInlineKeyboardButton,
      passengerButton.getInlineKeyboardButton,
    ))
  }
}
