package core.stages

import com.bot4s.telegram.models.Message
import core.{MessageReceive, Stage}
import date_base.StageType.StageType
import date_base.dao.UserDao
import date_base.{StageType, User}

import scala.concurrent.{ExecutionContext, Future}

object FillInfoSetCar extends Stage {
  override val stageType: StageType = StageType.FillInfoSetCar

  override def messageReceiveProcess(receive: MessageReceive)(implicit ec: ExecutionContext): Future[Unit] = {
    for {
      _ <- UserDao.update(receive.user.chatID, _.copy(carInfo = Some(receive.text))) // Обновляем информацию пользователя с полученным номером телефона
      nextStage = StageType.getNextStage(receive.user.stage).getOrElse(StageType.Main)
      _ <- UserDao.setStage(receive.user.chatID, nextStage) // Устанавливаем следующий этап в базе данных
      _ <- Stage.messageWithoutButton(receive.user.chatID, s"Зарегестрировал тебя как водителя (${receive.text})")
      _ <- Stage.getStageByType(nextStage).sendFirstMessage(receive.user)
    } yield ()
  }

  override def sendFirstMessage(user: User): Future[Message] =
    Stage.messageWithoutButton(
      user.chatID,
      "Опиши свою машину, чтобы попутчики тебя быстрее находили. Стоит указать марку, цвет и номер")
}
