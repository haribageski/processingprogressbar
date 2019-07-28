package controllers

import java.util.UUID

import play.api.mvc.{AbstractController, ControllerComponents}
import services.FakeImageProcessor

import scala.concurrent.Future

class ImageProcessingController(cc: ControllerComponents) extends
  AbstractController(cc) with
  play.api.i18n.I18nSupport {
  // I18nSupport implicitly convert an implicit request to an implicit Messages, which is needed for Forms in HTML

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def homePage() = Action { implicit request =>
    Ok(views.html.homePage())
  }

  def processImage(sessionId: String) = Action(parse.multipartFormData).async { implicit request =>
    val sessionUUID = UUID.fromString(sessionId)
    request.body
      .file("picture")
      .map { picture =>
        FakeImageProcessor.processImage(
          sessionUUID, picture.ref
        ).map(_ => Ok)
      }.getOrElse(Future.successful {
      println("Uploaded photo did not arrive to the backend")
      InternalServerError.as(HTML)
    })
  }

}
