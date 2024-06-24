package prices.routes

import cats.Monad
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import munit.FunSuite
import org.http4s.circe.jsonOf
import org.http4s.headers.Authorization
import org.http4s.{AuthScheme, Credentials, EntityDecoder, Method, Request, Status, Uri}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import prices.data.{InstanceKind, InstancePrice}
import prices.services.InstanceKindService
import prices.services.SmartCloudInstancePricesService.SmartCloudInstancePricesService

class PricesRoutesTest extends FunSuite {

  implicit val instancePriceDecoder: EntityDecoder[IO, InstancePrice] = jsonOf[IO, InstancePrice]

  test("Test PricesRoutes return correct InstancePrice success") {
    val price = InstancePrice("sc2-micro", 0.0867, "2024-06-24T21:22:59.291Z")
    val instancePricesService = mock[SmartCloudInstancePricesService[IO]]
    when(instancePricesService.get(InstanceKind("sc2-micro")))
      .thenReturn(Monad[IO].pure(Right(price)))

    val pricesRoutes = PricesRoutes(instancePricesService)
    val (status, message) = (for {
      response <-pricesRoutes.routes.orNotFound.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"${pricesRoutes.prefix}?kind=sc2-micro"))
              .withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, "Any Bearer")))
      )
      message <- response.as[InstancePrice]
    } yield (response.status, message)).unsafeRunSync()

    assertEquals(status, Status.Ok)
    assertEquals(message, price)
  }

  test("Test PricesRoutes return Not found if kind missing") {
    val instancePricesService = mock[SmartCloudInstancePricesService[IO]]

    val pricesRoutes = PricesRoutes(instancePricesService)
    val io = pricesRoutes.routes.orNotFound.run(
      Request(method = Method.GET, uri = Uri.unsafeFromString(s"${pricesRoutes.prefix}"))
        .withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, "Any Bearer")))
    ).unsafeRunSync()

    assertEquals(io.status, Status.NotFound)
  }

  test("Test PricesRoutes return InternalServerError if any exceptions") {
    val instancePricesService = mock[SmartCloudInstancePricesService[IO]]
    when(instancePricesService.get(InstanceKind("sc2-micro")))
      .thenReturn(
        Monad[IO].pure(Left(
          InstanceKindService.Exception.APICallFailure("InternalServerError")
        ))
      )

    val pricesRoutes = PricesRoutes(instancePricesService)
    val io = pricesRoutes.routes.orNotFound.run(
      Request(method = Method.GET, uri = Uri.unsafeFromString(s"${pricesRoutes.prefix}?kind=sc2-micro"))
        .withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, "Any Bearer")))
    ).unsafeRunSync()

    assertEquals(io.status, Status.InternalServerError)
  }

}
