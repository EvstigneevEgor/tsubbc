package core.stages.create_trip

import com.bot4s.telegram.models.Message
import core.{MessageReceive, Stage}
import date_base.StageType.StageType
import date_base.dao.{TripDao, TripPointDao, UserDao}
import date_base.{StageType, TripPoint, TripPointType, User}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Должен выводится текст:
 * Теперь адрес куда ты направляешься
 *
 * По факту закодить нужно метод [[sendFirstMessage]]. Можно подсмотреть в [[core.stages.anket]]
 *
 * Ответ пользователь будет давать текстом. Соответсвенно, нужно переопределить метод [[messageReceiveProcess]] можно подсмотреть в [[core.stages.anket.FillInfoSetCar.messageReceiveProcess тут]].
 * Ответ ПОКА НЕнужно пытаться парсить, дальше создавать запись в [[date_base.TripPointTable]]
 *
 * После чего нужно переводить юзера на следующий этап [[StageType.CreateTripSetComment]]  и выводить его [[sendFirstMessage]]
 *
 * @author Egor
 */
object CreateTripSetSecond extends Stage {

  override val stageType: StageType = StageType.CreateTripSetSecond

  override def messageReceiveProcess(receive: MessageReceive)(implicit ec: ExecutionContext): Future[Unit] = {
    for {
      user <- UserDao.get(receive.user.chatID)
      editingTrip <- user.flatMap(_.editingTripId).map(TripDao.get).getOrElse(Future.successful(None))
      tripPoints <- editingTrip.fold(Future.successful(Seq[TripPoint]()))(trip => TripPointDao.getByTrip(trip.id.get))
      isAlreadyDefined = tripPoints.exists {
        _.tripPointType == TripPointType.ending
      }
      _ <- if (isAlreadyDefined)
        Future.failed(new RuntimeException(s"У пользователя${receive.user.chatID} уже проставлна начальная точки поездки $tripPoints"))
      else editingTrip.fold(Future.successful()) { trip =>
        val point = TripPoint(None, trip.id.get, receive.text, None, None, TripPointType.ending)
        for {
          _ <- TripPointDao.insert(point)
          nextStage = StageType.getNextStage(receive.user.stage).getOrElse(StageType.Main)
          _ <- UserDao.setStage(receive.user.chatID, nextStage) // Устанавливаем следующий этап в базе данных
          _ <- Stage.getStageByType(nextStage).sendFirstMessage(receive.user)
        } yield ()
      }
    } yield ()
  }

  override def sendFirstMessage(user: User): Future[Message] = Stage.messageWithoutButton(
    user.chatID,
    "Теперь адрес куда ты направляешься")

}
