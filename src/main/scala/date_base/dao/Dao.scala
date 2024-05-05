package date_base.dao

import scala.concurrent.Future

trait Dao[T] {
  def insert(t: T): Future[Long]

  def delete(id: Long): Future[Int]

  def get(id: Long): Future[Option[T]]
}
