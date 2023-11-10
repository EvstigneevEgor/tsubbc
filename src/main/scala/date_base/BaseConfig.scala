package date_base

import slick.jdbc.SQLiteProfile.api._


object BaseConfig {

  val user = TableQuery[UserTable]
  val userStageTable = TableQuery[UserStageTable]
  val db = Database.forConfig("sqlite.db")
  // Manually populate database
  val setup = DBIO.seq(
    // Create the tables, including primary and foreign keys
    (user.schema).createIfNotExists,
    (userStageTable.schema).createIfNotExists
  )
  db.run(setup)
}