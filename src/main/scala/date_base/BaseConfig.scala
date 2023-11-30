package date_base

import slick.jdbc.SQLiteProfile.api._


object BaseConfig {

  val user = TableQuery[UserTable]
  val trip = TableQuery[TripTable]
  val trip_point = TableQuery[TripPointTable]
  val transaction = TableQuery[TransactionTable]
  val db = Database.forConfig("sqlite.db")
  // Manually populate database
  val setup = DBIO.seq(
    (user.schema).createIfNotExists, // Create the table, including primary and foreign keys
    (trip.schema).createIfNotExists, // Create the table, including primary and foreign keys
    (trip_point.schema).createIfNotExists, // Create the table, including primary and foreign keys
    (transaction.schema).createIfNotExists, // Create the table, including primary and foreign keys
  )
  db.run(setup)


}