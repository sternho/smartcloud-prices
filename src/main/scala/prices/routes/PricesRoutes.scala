package prices.routes

import cats.effect._
import cats.implicits._
import org.http4s.{EntityEncoder, HttpRoutes}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import prices.data.InstanceKind
import prices.routes.protocol._
import prices.services.InstanceKindService.Exception.APICallFailure
import prices.services.InstancePricesService

final case class PricesRoutes[F[_] : Sync](instancePricesService: InstancePricesService[F]) extends Http4sDsl[F] {

  val prefix = "/prices"

  implicit val instancePricesResponseEncoder: EntityEncoder[F, InstancePriceResponse] = jsonEncoderOf[F, InstancePriceResponse]

  protected val get: HttpRoutes[F] = HttpRoutes.of {
    case request @GET -> Root =>
      request.params.get("kind") match {
        case Some(kind) =>
          instancePricesService.get(InstanceKind(kind))
            .flatMap {
              case Left(APICallFailure(message)) => InternalServerError(message)
              case Right(price)                  => Ok(InstancePriceResponse(price))
            }
        case _ =>
          NotFound("Request Parameter [kind] is mandatory")
      }
  }

  def routes: HttpRoutes[F] =
    Router(
      prefix -> get
    )

}
