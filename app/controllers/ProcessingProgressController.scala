package controllers

import java.util.UUID

import akka.stream.scaladsl.Source
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.mvc.{AbstractController, ControllerComponents}
import storage.ProcessingStatusStorage
import models.ProcessingStatus

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class ProcessingProgressController(cc: ControllerComponents) extends AbstractController(cc) {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def msg(message: String): String = {
    s"$message\n\n"
  }

  def getCurrentStatus(sessionId: String): Future[String] = ProcessingStatusStorage.find(UUID.fromString(sessionId))
    .map {
      case Some(status) =>
        msg(status.entryName)
      case None =>
        msg(ProcessingStatus.Status.NotStarted.entryName)
    }

  def getProgressStatuses(sessionId: String) = Action {
    val tickSource = Source.tick(0 millis, 1 second, "")
    val source = tickSource.mapAsync(1)(_ => getCurrentStatus(sessionId))
    Ok.chunked(source via EventSource.flow)
      .as(ContentTypes.EVENT_STREAM)
      .withHeaders("Cache-Control" -> "no-cache")
      .withHeaders("Connection" -> "keep-alive")
  }

  def resetProcessingStatus(sessionId: String) = Action.async {
    ProcessingStatusStorage.updateOrStore(UUID.fromString(sessionId), ProcessingStatus.Status.NotStarted)
      .map(_ => Ok)
  }
}