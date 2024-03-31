import com.bot4s.telegram.models.Message

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

    def getNameOrNameCalling: String = x.from.map(_.firstName).getOrElse("Мудила")
  }

}
