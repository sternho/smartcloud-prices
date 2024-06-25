package prices.services

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.generic.encoding.DerivedAsObjectEncoder.deriveEncoder
import munit.FunSuite
import org.http4s.circe.jsonEncoderOf
import org.http4s.{EntityEncoder, Response, Status}
import org.mockito.Mockito.mock
import prices.config.Config.SmartCloudConfig
import prices.data.InstanceKind
import prices.http.SmartCloudHttpClient.SmartCloudHttpClient
import prices.http.{HttpClient, SmartCloudHttpClient}
import prices.services.SmartCloudInstanceKindService.SmartCloudInstanceKindService

class SmartCloudInstanceKindServiceTest extends FunSuite {

  implicit val instancePricesResponseEncoder: EntityEncoder[IO, List[InstanceKind]] = jsonEncoderOf[IO, List[InstanceKind]]

  test("Test smartCloud service return successfully result") {
    val httpClient = mock(classOf[HttpClient[IO]])
    val smartCloudHttpClient: SmartCloudHttpClient[IO] = SmartCloudHttpClient.make(SmartCloudConfig("baseUri", "token"), httpClient)
    val smartCloudInstanceKindService: SmartCloudInstanceKindService[IO] = SmartCloudInstanceKindService.make(smartCloudHttpClient)

    val response = Response[IO](Status.Ok).withEntity("""["sc2-micro","sc2-small","sc2-medium"]""")
    val result = smartCloudInstanceKindService.handleInstanceKindResponse.apply(response).unsafeRunSync()
    assert(result.isRight)
  }

  test("Test smartCloud service return successfully result") {
    val httpClient = mock(classOf[HttpClient[IO]])
    val smartCloudHttpClient: SmartCloudHttpClient[IO] = SmartCloudHttpClient.make(SmartCloudConfig("baseUri", "token"), httpClient)
    val smartCloudInstanceKindService: SmartCloudInstanceKindService[IO] = SmartCloudInstanceKindService.make(smartCloudHttpClient)

    val response = Response[IO](Status.InternalServerError)
    val result = smartCloudInstanceKindService.handleInstanceKindResponse.apply(response).unsafeRunSync()
    assert(result.isLeft)
  }

}
