package core.stages.create_trip

import com.bot4s.telegram.models.Message
import core.Stage
import date_base.StageType.StageType
import date_base.{StageType, User}

import scala.concurrent.Future

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

  override def sendFirstMessage(user: User): Future[Message] = ???
}
