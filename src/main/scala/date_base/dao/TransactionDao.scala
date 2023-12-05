package date_base.dao


import date_base.BaseConfig.{db, transaction, user}
import date_base.{BaseConfig, Stage, Transaction, User}
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

object TransactionDao extends Dao[Transaction] {
  override def insert(t: Transaction): Future[Int] = db.run(transaction += t)

  override def get(id: Long): Future[Option[Transaction]] = db.run(transaction.filter(_.id === id).result.headOption)

  override def delete(id: Long): Future[Int] = db.run(transaction.filter(_.id === id).delete)

}
