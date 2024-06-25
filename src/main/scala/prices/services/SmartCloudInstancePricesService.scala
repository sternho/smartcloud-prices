package prices.services

import cats.Monad
import cats.effect._
import cats.implicits.toFunctorOps
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import org.http4s.Status.Successful
import org.http4s._
import org.http4s.circe._
import prices.data._
import prices.http.SmartCloudHttpClient.SmartCloudHttpClient

object SmartCloudInstancePricesService {

  def make[F[_] : Concurrent](httpClient: SmartCloudHttpClient[F]): SmartCloudInstancePricesService[F] = new SmartCloudInstancePricesService(httpClient)

  final class SmartCloudInstancePricesService[F[_] : Concurrent](
                                                                        httpClient: SmartCloudHttpClient[F]
                                                                      ) extends InstancePricesService[F] {

    implicit val instancePriceDecoder: EntityDecoder[F, InstancePrice] = jsonOf[F, InstancePrice]

    override def get(kind: InstanceKind): F[Either[InstanceKindService.Exception, InstancePrice]] =
      httpClient.get(s"instances/${kind.kind}") { handleInstancePricesResponse }

    val handleInstancePricesResponse: Response[F] => F[Either[InstanceKindService.Exception, InstancePrice]] = {
      case Successful(response) =>
        response.as[InstancePrice].map(Right(_))
      case response =>
        Monad[F].pure(Left(
          InstanceKindService.Exception.APICallFailure(response.status.toString())
        ))
    }
  }

}
