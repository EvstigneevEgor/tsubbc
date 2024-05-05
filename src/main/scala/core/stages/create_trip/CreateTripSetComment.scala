package core.stages.create_trip

import com.bot4s.telegram.models.Message
import core.{MessageReceive, Stage}
import date_base.StageType.StageType
import date_base.dao.{TripDao, UserDao}
import date_base.{StageType, User}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Должен выводится текст:
 * Можешь оставить комментарий к поездке, если считаешь нужным. Например, если потребуется место в багажнике
 *
 * По факту закодить нужно в методе [[sendFirstMessage]]. Можно подсмотреть в [[core.stages.anket]]
 *
 * Ответ пользователь будет давать текстом. Соответсвенно, нужно переопределить метод [[messageReceiveProcess]] можно подсмотреть в [[core.stages.anket.FillInfoSetCar.messageReceiveProcess тут]].
 * Ответом нужно обогатить запись в [[date_base.TripTable]]
 *
 * После чего нужно переводить юзера в меню [[StageType.Main]]. вывести сообщение
 * Такого вида:
 * Запрос на поездку создан! Теперь ты можешь посмотреть список активных поездок.
 * И Main [[sendFirstMessage]]. Подсмотреть можно [[https://github.com/EvstigneevEgor/tsubbc/blob/62b2db0a0b6f7d52cd0d46a9227d7e6aa8bf7e88/src/main/scala/core/stages/anket/FillInfoSetCar.scala#L19 тут]]
 *
 * @author Egor
 */
object CreateTripSetComment extends Stage {

  override val stageType: StageType = StageType.CreateTripSetComment

  override def messageReceiveProcess(receive: MessageReceive)(implicit ec: ExecutionContext): Future[Unit] = {
    for {
      user <- UserDao.get(receive.user.chatID)
      editingTrip <- user.flatMap(_.editingTripId).map(TripDao.get).getOrElse(Future.successful(None))
      _ <- editingTrip.fold(Future.successful()) { trip =>
        for {
          _ <- TripDao.update(trip.id.get, _.copy(comment = Option(receive.text).filter(_.isBlank)))
          nextStage = StageType.getNextStage(receive.user.stage).getOrElse(StageType.Main)
          _ <- UserDao.setStage(receive.user.chatID, nextStage) // Устанавливаем следующий этап в базе данных
          _ <- UserDao.update(receive.user.chatID, _.copy(editingTripId = None))
          _ <- Stage.getStageByType(nextStage).sendFirstMessage(receive.user)
        } yield ()
      }
    } yield ()
  }

  override def sendFirstMessage(user: User): Future[Message] = Stage.messageWithoutButton(
    user.chatID,
    "Можешь оставить комментарий к поездке, если считаешь нужным. Например, если потребуется место в багажнике")
}
