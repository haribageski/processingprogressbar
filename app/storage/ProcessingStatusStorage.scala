package storage

import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

import models.ProcessingStatus
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration

object ProcessingStatusStorage {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val db: H2Profile.backend.Database = Database.forConfig("h2mem1")

  class ProcessingStatusTableRow(tag: Tag) extends Table[ProcessingStatusEntity](tag, "PROCESSING_STATUS") {
    def id = column[String]("PROCESS_ID", O.PrimaryKey) // This is the primary key column
    def status = column[String]("STATUS")
    def timestamp = column[Long]("TIMESTAMP")
    override def * = (id, status, timestamp) <> (ProcessingStatusEntity.tupled, ProcessingStatusEntity.unapply)
  }
  val processingStatuses = TableQuery[ProcessingStatusTableRow]

  val setup = DBIO.seq(processingStatuses.schema.createIfNotExists)
  Await.result(db.run(setup), Duration(2, TimeUnit.SECONDS))

  def find(sessionId: UUID): Future[Option[ProcessingStatus.Status]] = {
    val queryAction = Compiled(for{
      processingStatusTableRow <- processingStatuses if processingStatusTableRow.id === sessionId.toString
      } yield processingStatusTableRow.status
        ).result.headOption.map(_.flatMap(ProcessingStatus.Status.withNameOption))
    db.run(queryAction)
  }

  def updateOrStore(sessionId: UUID, newStatus: ProcessingStatus.Status): Future[Int] = {
    val entity = ProcessingStatusEntity(
      sessionId = sessionId.toString,
      status = newStatus.entryName,
      timestamp = Instant.now().toEpochMilli
    )
    val queryAction = processingStatuses.insertOrUpdate(entity)
    db.run(queryAction)
  }
}
