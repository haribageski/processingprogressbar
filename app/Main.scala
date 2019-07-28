import java.io.File

import play.api.{Configuration, Environment, Play}
import play.core.server.{AkkaHttpServer, ServerConfig}

object Main {

  @SuppressWarnings(Array("UnusedMethodParameter"))
  def main(args: Array[String]): Unit = {
    val _ = startServer()
  }

  def startServer(): AkkaHttpServer = {
    try {
      val environment = new Environment(new File("."), this.getClass.getClassLoader, play.api.Mode.Prod)
      val configuration = Configuration.load(environment)
      val context = play.api.ApplicationLoader.Context.create(environment)
      val application = (new CustomApplicationLoader).load(context)

      Play.start(application)

      val serverConfig = ServerConfig(
        port = configuration.getOptional[Int]("play.server.http.port")
      )
      AkkaHttpServer.fromApplication(application, serverConfig)
    } catch {
      case t: Throwable =>
        println(s"Exception in Main.startServer method: $t")
        throw t
    }

  }

}
