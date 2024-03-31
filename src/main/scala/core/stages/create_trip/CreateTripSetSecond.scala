package core.stages.create_trip

import com.bot4s.telegram.models.Message
import core.Stage
import date_base.StageType.StageType
import date_base.{StageType, User}

import scala.concurrent.Future

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

  override def sendFirstMessage(user: User): Future[Message] = ???
}
