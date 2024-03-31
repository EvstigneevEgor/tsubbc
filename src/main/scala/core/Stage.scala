package core

import cats.implicits.toFunctorOps
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.{Message, ReplyMarkup}
import core.Main.request
import core.Stage.getStageByType
import date_base.StageType
import date_base.StageType.StageType

import scala.concurrent.{ExecutionContext, Future}

trait Stage {
  val stageType: StageType

  def buttonPressedProcess(pressed: ButtonPressed)(implicit ec: ExecutionContext): Future[Unit] = wrongActivity(pressed)

  def messageReceiveProcess(receive: MessageReceive)(implicit ec: ExecutionContext): Future[Unit] = wrongActivity(receive)

  def contactReceiveProcess(contact: ContactReceive)(implicit ec: ExecutionContext): Future[Unit] = wrongActivity(contact)

  def wrongActivity(activity: Activity)(implicit ec: ExecutionContext): Future[Unit] =
    getStageByType(activity.user.previousStage).sendLastMessage(activity.user.chatID).void

  def sendLastMessage(chatId: Long): Future[Message]

  def process(activity: Activity)(implicit ec: ExecutionContext): Future[Unit] = {
    activity match {
      case pressed: ButtonPressed => buttonPressedProcess(pressed)
      case receive: MessageReceive => messageReceiveProcess(receive)
      case contact: ContactReceive => contactReceiveProcess(contact)
    }
  }
}

object Stage {

  def messagesWithButtons(id: Long, message: String, buttons: ReplyMarkup): Future[Message] =
    request(SendMessage(id, message, replyMarkup = Some(buttons)))

  def messageWithoutButton(id: Long, message: String): Future[Message] =
    request(SendMessage(id, message))

  // shit refactor me please
  def getStageByType(st: StageType): Stage = {
    st match {
      case StageType.NotAuthorized => stages.NotAuthorized
      case StageType.FillInfoSetCommunicate => stages.FillInfoSetCommunicate
      case StageType.FillInfoSetIsDriver => stages.FillInfoSetIsDriver
      case StageType.Main => ???
      case StageType.FillInfoSetCar => ???
      case StageType.FindTripSetTime => ???
      case StageType.CheckTrip => ???
      case StageType.FindTripSetFirst => ???
      case StageType.FindTripSetSecond => ???
      case StageType.FindTripSetComment => ???
      case StageType.CreateTripSetTime => ???
      case StageType.CreateTripSetFirst => ???
      case StageType.CreateTripSetSecond => ???
      case StageType.CreateTripSetComment => ???
    }
  }
}
