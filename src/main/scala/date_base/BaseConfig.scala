package date_base

import slick.jdbc.SQLiteProfile.api._


object BaseConfig {

  val user = TableQuery[UserTable]
  val db = Database.forConfig("sqlite.db")
  // Manually populate database
  val setup = DBIO.seq(
    (user.schema).createIfNotExists, // Create the table, including primary and foreign keys
  )
  db.run(setup)
}