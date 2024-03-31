package date_base.dao


import date_base.BaseConfig.{db, transaction}
import date_base.Transaction
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.Future

object TransactionDao extends Dao[Transaction] {
  override def insert(t: Transaction): Future[Int] = db.run(transaction += t)

  override def get(id: Long): Future[Option[Transaction]] = db.run(transaction.filter(_.id === id).result.headOption)

  override def delete(id: Long): Future[Int] = db.run(transaction.filter(_.id === id).delete)

}
