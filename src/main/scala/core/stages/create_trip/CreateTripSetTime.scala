package core.stages.create_trip

import com.bot4s.telegram.models.Message
import core.{MessageReceive, Stage}
import date_base.StageType.StageType
import date_base.dao.{TripDao, UserDao}
import date_base.{StageType, Trip, TripType, User}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}

/**
 * Должен выводится текст:
 * Прежде чем найти попутчика, давай заполним информацию о планируемой поездке.
 * Укажи время и дату поездки в формате ДД.ММ.ГГГГ чч:мм. Например: 28.03.2024 14:00
 *
 * Возможно будет красивше, если будет выводится в 2 сообщениях. По факту закодить нужно в методе [[sendFirstMessage]] можно подсмотреть в [[core.stages.anket]]
 *
 * Ответ пользователь будет давать текстом. Соответсвенно, нужно переопределить метод [[messageReceiveProcess]] можно подсмотреть в [[core.stages.anket.FillInfoSetCar.messageReceiveProcess тут]].
 * Ответ нужно пытаться распарсить как дату формата (ДД.ММ.ГГГГ чч:мм), при успехе создавать запись в [[date_base.TripTable]]
 *
 * после чего нужно переводить юзера на следующий этап [[StageType.CreateTripSetFirst]] и выводить его [[sendFirstMessage]]
 *
 * @author Egor
 */
object CreateTripSetTime extends Stage {

  override val stageType: StageType = StageType.CreateTripSetTime
  val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

  override def messageReceiveProcess(receive: MessageReceive)(implicit ec: ExecutionContext): Future[Unit] = {
    for {
      user <- UserDao.get(receive.user.chatID)
      ldt <- Future(LocalDateTime.parse(receive.text, dateTimeFormatter)).recoverWith { a =>
        Stage.messageWithoutButton(
          user.get.chatID,
          s"не получилось распарсить ваше время попробуйте заполнить время соблюдая шаблон dd-MM-yyyy HH:mm. todo drop (${a.getMessage})").flatMap(_ =>
          sendFirstMessage(user.get)
        )
        Future.failed(a)
      }
      trip = Trip(
        dateTime = ldt,
        comment = None,
        initiatorID = receive.user.chatID,
        tripType = TripType.ByDriver
      )
      tripId <- TripDao.insert(trip) // создаем поездку
      nextStage = StageType.getNextStage(receive.user.stage).getOrElse(StageType.Main)
      _ <- UserDao.setStage(receive.user.chatID, nextStage) // Устанавливаем следующий этап в базе данных
      _ <- UserDao.update(receive.user.chatID, _.copy(editingTripId = Some(tripId))) // Устанавливаем следующий этап в базе данных
      _ <- Stage.getStageByType(nextStage).sendFirstMessage(receive.user)
    } yield ()
  }

  override def sendFirstMessage(user: User): Future[Message] = Stage.messageWithoutButton(
    user.chatID,
    "Прежде чем найти попутчика, давай заполним информацию о планируемой поездке.\nУкажи время и дату поездки в формате ДД.ММ.ГГГГ чч:мм. Например: 28.03.2024 14:00")
}
