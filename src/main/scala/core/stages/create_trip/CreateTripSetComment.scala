package core.stages.create_trip

import com.bot4s.telegram.models.Message
import core.Stage
import date_base.StageType.StageType
import date_base.{StageType, User}

import scala.concurrent.Future

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

  override def sendFirstMessage(user: User): Future[Message] = ???
}
