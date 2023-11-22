import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.{Message, ReplyMarkup}
import core.Main.request

import scala.concurrent.{ExecutionContext, Future}

package object core {
  implicit class RichFuture[T](x: Future[T])(implicit ec: ExecutionContext) {
    def flatTap[R](anotherFuture: Future[R]): Future[T] = x.flatMap(a => anotherFuture.map(_ => a))

    def flatTap[R](anotherFuture: Option[Future[R]]): Future[T] =
      x.flatMap(a => anotherFuture.fold(Future.successful(a)) {
        _.map(_ => a)
      })
  }

  implicit class RichMessage(x: Message) {
    def getOurChatId: String = x.from.get.id.toString

    def getNameOrNameCalling: String = x.from.map(_.firstName).getOrElse("Мудила")
  }

  def messagesWithButtons(id: Long, message: String, buttons: ReplyMarkup) =
    request(SendMessage(id, message, replyMarkup = Some(buttons)))

  def messageWithoutButton(id: Long, message: String) =
    request(SendMessage(id, message))
}
