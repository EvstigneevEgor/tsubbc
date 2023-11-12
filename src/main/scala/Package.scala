import com.bot4s.telegram.models.Message

import scala.concurrent.{ExecutionContext, Future}

object Package {

  implicit class RichFuture[T](x: Future[T])(implicit ec: ExecutionContext) {
    def flatTap[R](anotherFuture: Future[R]): Future[T] = x.flatMap(a => anotherFuture.map(_ => a))
  }

  implicit class RichMessage(x: Message) {
    def getOurChatId: String = x.from.get.id.toString

    def getNameOrNameCalling: String = x.from.map(_.firstName).getOrElse("Мудила")
  }

}
