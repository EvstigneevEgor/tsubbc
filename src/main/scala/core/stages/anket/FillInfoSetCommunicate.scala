package core.stages.anket

import com.bot4s.telegram.models.{KeyboardButton, Message, ReplyKeyboardMarkup, ReplyKeyboardRemove}
import core.{ContactReceive, Stage}
import date_base.StageType.StageType
import date_base.dao.UserDao
import date_base.{StageType, User}

import scala.concurrent.{ExecutionContext, Future}

object FillInfoSetCommunicate extends Stage {
  override val stageType: StageType = StageType.FillInfoSetCommunicate

  override def contactReceiveProcess(contactReceive: ContactReceive)(implicit ec: ExecutionContext): Future[Unit] =
    for { // Случай, если контактная информация присутствует
      _ <- UserDao.update(contactReceive.user.chatID, _.copy(communicate = Some(contactReceive.contact.phoneNumber))) // Обновляем информацию пользователя с полученным номером телефона
      nextStage = StageType.getNextStage(contactReceive.user.stage).getOrElse(StageType.FillInfoSetIsDriver)
      _ <- UserDao.setStage(contactReceive.user.chatID, nextStage) // Устанавливаем следующий этап в базе данных
      _ <- Stage.messagesWithButtons(contactReceive.user.chatID, "Отлично!", ReplyKeyboardRemove())
      _ <- Stage.getStageByType(nextStage).sendFirstMessage(contactReceive.user)
    } yield ()


  override def sendFirstMessage(user: User): Future[Message] =
    Stage.messagesWithButtons(
      user.chatID,
      "Как другие пользователи могут с тобой связаться?",
      ReplyKeyboardMarkup.singleButton(KeyboardButton.requestContact("Дать линку"))
    )
}
