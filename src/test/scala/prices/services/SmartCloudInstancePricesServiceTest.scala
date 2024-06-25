package prices.services

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.generic.encoding.DerivedAsObjectEncoder.deriveEncoder
import munit.FunSuite
import org.http4s.circe.jsonEncoderOf
import org.http4s.{EntityEncoder, Response, Status}
import org.mockito.Mockito.mock
import prices.config.Config.SmartCloudConfig
import prices.data.InstancePrice
import prices.http.SmartCloudHttpClient.SmartCloudHttpClient
import prices.http.{HttpClient, SmartCloudHttpClient}
import prices.services.SmartCloudInstancePricesService.SmartCloudInstancePricesService

class SmartCloudInstancePricesServiceTest extends FunSuite {

  implicit val instancePricesResponseEncoder: EntityEncoder[IO, InstancePrice] = jsonEncoderOf[IO, InstancePrice]

  test("Test smartCloud service return successfully result") {
    val httpClient = mock(classOf[HttpClient[IO]])
    val smartCloudHttpClient: SmartCloudHttpClient[IO] = SmartCloudHttpClient.make(SmartCloudConfig("baseUri", "token"), httpClient)
    val smartCloudInstancePricesService: SmartCloudInstancePricesService[IO] = SmartCloudInstancePricesService.make(smartCloudHttpClient)

    val price = InstancePrice("", 0, "")
    val response = Response[IO](Status.Ok).withEntity(price)
    val result = smartCloudInstancePricesService.handleInstancePricesResponse.apply(response).unsafeRunSync()
    assert(result.isRight)
    assertEquals(result.getOrElse(), price)
  }

  test("Test smartCloud service return server error result") {
    val httpClient = mock(classOf[HttpClient[IO]])
    val smartCloudHttpClient: SmartCloudHttpClient[IO] = SmartCloudHttpClient.make(SmartCloudConfig("baseUri", "token"), httpClient)
    val smartCloudInstancePricesService: SmartCloudInstancePricesService[IO] = SmartCloudInstancePricesService.make(smartCloudHttpClient)

    val response = Response[IO](Status.InternalServerError)
    val result = smartCloudInstancePricesService.handleInstancePricesResponse.apply(response).unsafeRunSync()
    assert(result.isLeft)
  }

}
