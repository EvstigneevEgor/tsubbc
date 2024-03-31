package core.stages

import com.bot4s.telegram.models.{Message, ReplyKeyboardRemove}
import core.stages.FillInfoSetIsDriver.chooseStatusButtons
import core.{ContactReceive, Stage}
import date_base.StageType
import date_base.StageType.StageType
import date_base.dao.UserDao

import scala.concurrent.{ExecutionContext, Future}

object FillInfoSetCommunicate extends Stage {
  override val stageType: StageType = StageType.FillInfoSetCommunicate

  override def contactReceiveProcess(contactReceive: ContactReceive)(implicit ec: ExecutionContext): Future[Unit] =
    for { // Случай, если контактная информация присутствует
      _ <- UserDao.update(contactReceive.user.chatID, _.copy(communicate = Some(contactReceive.contact.phoneNumber))) // Обновляем информацию пользователя с полученным номером телефона
      _ <- UserDao.setNextStage(contactReceive.user.chatID) // Устанавливаем следующий этап в базе данных
      _ <- Stage.messagesWithButtons(contactReceive.user.chatID, "Отлично!", ReplyKeyboardRemove())
      _ <- sendLastMessage(contactReceive.user.chatID)
    } yield ()

  override def sendLastMessage(chatId: Long): Future[Message] = Stage.messagesWithButtons(chatId, "Теперь расскажи, ты водитель или только пассажир?", chooseStatusButtons())
}
