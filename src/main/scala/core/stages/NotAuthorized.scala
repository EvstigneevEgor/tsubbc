package core.stages

import cats.implicits.toFunctorOps
import com.bot4s.telegram.models.Message
import core.{Activity, MessageReceive, Stage}
import date_base.StageType.StageType
import date_base.dao.UserDao
import date_base.{StageType, User}

import scala.concurrent.{ExecutionContext, Future}

object NotAuthorized extends Stage {
  override val stageType: StageType = StageType.NotAuthorized

  override def wrongActivity(activity: Activity)(implicit ec: ExecutionContext): Future[Unit] =
    Stage.messageWithoutButton(activity.user.chatID, "Неожиданное повидение пользователя").void

  override def messageReceiveProcess(receive: MessageReceive)(implicit ec: ExecutionContext): Future[Unit] = {
    for {
      _ <- sendFirstMessage(user = receive.user)
      nextStage = StageType.getNextStage(receive.user.stage).getOrElse(StageType.FillInfoSetCommunicate)
      _ <- UserDao.setStage(receive.user.chatID, nextStage) // Установка следующего этапа в базе данных
      _ <- Stage.getStageByType(nextStage).sendFirstMessage(user = receive.user)
    } yield ()
  }

  override def sendFirstMessage(user: User): Future[Message] =
    Stage.messageWithoutButton(user.chatID, s"Привет, ${user.fullName}! Добро пожаловать в чат-бот Бла Бла Кар ТГУ. Давай заполним анкету")

}
