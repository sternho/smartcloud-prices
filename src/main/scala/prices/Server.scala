package prices

import cats.effect._
import cats.implicits.toSemigroupKOps
import com.comcast.ip4s._
import fs2.Stream
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import prices.config.Config
import prices.http.{HttpClient, SmartCloudHttpClient}
import prices.routes.{InstanceKindRoutes, PricesRoutes}
import prices.services.{SmartCloudInstanceKindService, SmartCloudInstancePricesService}

object Server {

  def serve(config: Config): Stream[IO, ExitCode] = {

    val httpClient: HttpClient[IO] = new HttpClient[IO]()
    val priceHttpClient = SmartCloudHttpClient.make(config.smartcloud, httpClient)

    val instanceKindService = SmartCloudInstanceKindService.make[IO](priceHttpClient)
    val instancePricesService = SmartCloudInstancePricesService.make[IO](priceHttpClient)

    val httpApp = (
      InstanceKindRoutes[IO](instanceKindService).routes
        <+> PricesRoutes[IO](instancePricesService).routes
    ).orNotFound

    Stream
      .eval(
        EmberServerBuilder
          .default[IO]
          .withHost(Host.fromString(config.app.host).get)
          .withPort(Port.fromInt(config.app.port).get)
          .withHttpApp(Logger.httpApp(true, true)(httpApp))
          .build
          .useForever
      )
  }


}
