package controllers

import akka.NotUsed
import akka.stream.scaladsl.Source
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.mvc.{AbstractController, ControllerComponents}
import services.ProcessingProgressTracker

class ProcessingProgressController(cc: ControllerComponents) extends AbstractController(cc) {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def getProgressStatuses(sessionId: String) = Action {

    val sourceOfSessionStatuses: Source[String, NotUsed] = ProcessingProgressTracker.getSourceOfStatuses(sessionId)
      .getOrElse(ProcessingProgressTracker.createSourceOfStatuses(sessionId))
      .map(sessionStatus => format(sessionStatus.entryName))

    Ok.chunked(sourceOfSessionStatuses via EventSource.flow)
      .as(ContentTypes.EVENT_STREAM)
      .withHeaders("Cache-Control" -> "no-cache")
      .withHeaders("Connection" -> "keep-alive")
  }

  private def format(message: String): String = {
    s"$message\n\n"
  }
}