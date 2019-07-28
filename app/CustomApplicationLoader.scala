import controllers._
import play.api.ApplicationLoader.Context
import play.api.mvc.EssentialFilter
import router.Routes
import play.api.{ApplicationLoader, BuiltInComponentsFromContext}
import play.filters.HttpFiltersComponents
import play.filters.cors.CORSComponents

class CustomApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {
    new MyComponents(
      context = context
    ).application
  }
}

class MyComponents(context: Context) extends
  BuiltInComponentsFromContext(context) with
  CORSComponents with
  HttpFiltersComponents with
  AssetsComponents {

  lazy val processingProgressController = new ProcessingProgressController(cc = controllerComponents)
  lazy val imageProcessingController = new ImageProcessingController(cc = controllerComponents)

  lazy val router = new Routes(
    httpErrorHandler,
    imageProcessingController,
    processingProgressController,
    assets
  )

  override def httpFilters: Seq[EssentialFilter] = corsFilter +: super.httpFilters
}