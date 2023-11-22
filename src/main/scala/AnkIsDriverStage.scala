import Main.{prefixTag, request}
import Package._
import cats.implicits.toFunctorOps
import cats.instances.future._
import com.bot4s.telegram.Implicits._
import com.bot4s.telegram.methods.{EditMessageReplyMarkup, SendMessage}
import com.bot4s.telegram.models._
import date_base.dao.{StageDao, UserDao}

import scala.concurrent.{ExecutionContext, Future}
// Это пример бота

object AnkIsDriverStage {
  implicit val ec: ExecutionContext = Main.executionContext  // Неявное значение ExecutionContext для выполнения асинхронных операций
  val DRIVER = "водитель"
  val NOT_DRIVER = "пассажир"

  private def setIsDriver(chatId: Long, isDriver: Boolean) =
    for {
      _ <- UserDao.update(chatId, _.copy(isDriver = isDriver)) // Обновление информации о пользователе в базе данных: установка статуса водителя/пассажира
      _ <- StageDao.setNextStage(chatId)
      //Оба эти шага выполняются асинхронно и объединены в блок for, чтобы вернуть Future, который успешно завершится, когда оба действия будут выполнены.
    } yield ()


  val IS_DRIVER_TAG = "IS_DRIVER_TAG"

  def tag = prefixTag(IS_DRIVER_TAG) _


  def chouseStatusButtons() = {

    InlineKeyboardMarkup.singleRow(Seq(
      InlineKeyboardButton.callbackData(s"Я водитель", tag(DRIVER)),
      InlineKeyboardButton.callbackData(s"Я пассажир", tag(NOT_DRIVER))
    ))
  }


  def buttonsIsDriverAction(succsessMessage: String): CallbackQuery => Future[Unit] = {
    implicit cbq: CallbackQuery =>  // На основе полученного CallbackQuery выполняется действие
      val maybeEditFuture = for {
        data <- cbq.data  // Извлечение данных из CallbackQuery
        msg <- cbq.message
        response <- data.trim.toLowerCase match {  // Определение действия в зависимости от полученных данных
          case DRIVER => Some(setIsDriver(cbq.from.id, isDriver = true))
          case NOT_DRIVER => Some(setIsDriver(cbq.from.id, isDriver = false))
          case _ => None
        }
        response <- response.flatMap(_ => request( // Обновление сообщения, скрытие кнопок и отправка сообщения о успешном выполнении действия
          EditMessageReplyMarkup(
            ChatId(msg.source),
            msg.messageId,
            replyMarkup = None
          )
        )).flatTap(request(SendMessage(msg.source, succsessMessage)))
      } yield response

      maybeEditFuture.map(_.as(())).getOrElse(Future.unit) // Преобразование результата в Future[Unit], если есть, в противном случае возвращается Future.unit
  }

}
