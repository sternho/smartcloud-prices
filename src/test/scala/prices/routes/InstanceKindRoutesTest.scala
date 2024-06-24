package prices.routes

import cats.Monad
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import munit.FunSuite
import org.http4s.headers.Authorization
import org.http4s.{AuthScheme, Credentials, Method, Request, Status, Uri}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import prices.data.InstanceKind
import prices.services.InstanceKindService
import prices.services.SmartCloudInstanceKindService.SmartCloudInstanceKindService

class InstanceKindRoutesTest extends FunSuite {

  test("Test PricesRoutes return correct InstancePrice success") {
    val listKind = List(
      InstanceKind("sc2-micro"),
      InstanceKind("sc2-small"),
      InstanceKind("sc2-medium"),
    )
    val instancePricesService = mock[SmartCloudInstanceKindService[IO]]
    when(instancePricesService.getAll())
      .thenReturn(Monad[IO].pure(Right(listKind)))

    val kindRoutes = InstanceKindRoutes(instancePricesService)
    val (status, message) = (for {
      response <- kindRoutes.routes.orNotFound.run(
        Request(method = Method.GET, uri = Uri.unsafeFromString(kindRoutes.prefix))
          .withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, "Any Bearer")))
      )
      message <- response.as[String]
    } yield (response.status, message)).unsafeRunSync()

    assertEquals(status, Status.Ok)
    assertEquals(message, """[{"kind":"sc2-micro"},{"kind":"sc2-small"},{"kind":"sc2-medium"}]""")
  }

  test("Test PricesRoutes return InternalServerError if any exceptions") {
    val instancePricesService = mock[SmartCloudInstanceKindService[IO]]
    when(instancePricesService.getAll())
      .thenReturn(
        Monad[IO].pure(Left(
          InstanceKindService.Exception.APICallFailure("InternalServerError")
        ))
      )

    val kindRoutes = InstanceKindRoutes(instancePricesService)
    val io = kindRoutes.routes.orNotFound.run(
      Request(method = Method.GET, uri = Uri.unsafeFromString(kindRoutes.prefix))
        .withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, "Any Bearer")))
    ).unsafeRunSync()

    assertEquals(io.status, Status.InternalServerError)
  }

}
