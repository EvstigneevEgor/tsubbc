package core.stages

import cats.implicits.toFunctorOps
import com.bot4s.telegram.models.{KeyboardButton, Message, ReplyKeyboardMarkup}
import core.{Activity, MessageReceive, Stage}
import date_base.StageType
import date_base.StageType.StageType
import date_base.dao.UserDao

import scala.concurrent.{ExecutionContext, Future}

object NotAuthorized extends Stage {
  override val stageType: StageType = StageType.NotAuthorized

  override def wrongActivity(activity: Activity)(implicit ec: ExecutionContext): Future[Unit] =
    Stage.messageWithoutButton(activity.user.chatID, "Неожиданное повидение пользователя").void

  override def messageReceiveProcess(receive: MessageReceive)(implicit ec: ExecutionContext): Future[Unit] = {
    for {
      _ <- UserDao.setNextStage(receive.user.chatID) // Установка следующего этапа в базе данных
      _ <- Stage.messageWithoutButton(receive.user.chatID, s"Привет, ${receive.user.fullName}! Добро пожаловать в чат-бот Бла Бла Кар ТГУ. Давай заполним анкету")
      _ <- sendLastMessage(receive.user.chatID) // Запрос на отправку контактной информации пользователем
    } yield ()
  }

  override def sendLastMessage(chatId: Long): Future[Message] = {
    Stage.messagesWithButtons(
      chatId,
      "Как другие пользователи могут с тобой связаться?",
      ReplyKeyboardMarkup.singleButton(KeyboardButton.requestContact("Дать линку"))
    )
  }


}
