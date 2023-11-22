package core

import com.bot4s.telegram.models._
import date_base.dao.{StageDao, UserDao}

import scala.concurrent.ExecutionContext
// Это пример бота

object AnkIsDriverStage {
  implicit val ec: ExecutionContext = Main.executionContext

  private def setIsDriver(chatId: Long, isDriver: Boolean) =
    for {
      _ <- UserDao.update(chatId, _.copy(isDriver = isDriver))
      _ <- StageDao.setNextStage(chatId)
    } yield ()

  private val newMessageParams: NextMessageParams = NextMessageParams("Анкета сохранена. Ты можешь отредактировать её в любое время")
  private val driverButton = Button(
    "Я водитель",
    a => setIsDriver(a.from.id, isDriver = true),
    newMessageParams = Some(newMessageParams)
  )
  private val passengerButton = Button(
    "Я пассажир",
    a => setIsDriver(a.from.id, isDriver = false),
    newMessageParams = Some(newMessageParams)
  )

  def chooseStatusButtons() = {
    InlineKeyboardMarkup.singleRow(Seq(
      driverButton.getInlineKeyboardButton,
      passengerButton.getInlineKeyboardButton,
    ))
  }

}
