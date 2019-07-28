package services

import java.io.File
import java.util.UUID
import models.ProcessingStatus.Status

import storage.ProcessingStatusStorage

object FakeImageProcessor {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def processImage(sessionId: UUID, file: File) = for {
    _ <- ProcessingStatusStorage.updateOrStore(sessionId, Status.Preparing)
    _ = Thread.sleep(1000)
    _ <- ProcessingStatusStorage.updateOrStore(sessionId, Status.Parsing)
    _ = Thread.sleep(2000)
    _ <- ProcessingStatusStorage.updateOrStore(sessionId, Status.Optimising)
    _ = Thread.sleep(1000)
    _ <- ProcessingStatusStorage.updateOrStore(sessionId, Status.Done)
  } yield None
}
