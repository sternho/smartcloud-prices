package prices.services

import cats.Monad
import cats.effect._
import cats.implicits.toFunctorOps
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import org.http4s.Status.Successful
import org.http4s._
import org.http4s.circe._
import prices.data._
import prices.http.PriceHttpClient.PriceHttpClient

object SmartCloudInstancePricesService {

  def make[F[_] : Concurrent](httpClient: PriceHttpClient[F]): InstancePricesService[F] = new SmartCloudInstancePricesService(httpClient)

  private final class SmartCloudInstancePricesService[F[_] : Concurrent](
                                                                        httpClient: PriceHttpClient[F]
                                                                      ) extends InstancePricesService[F] {

    implicit val instancePriceDecoder: EntityDecoder[F, InstancePrice] = jsonOf[F, InstancePrice]

    override def get(kind: InstanceKind): F[Either[InstanceKindService.Exception, InstancePrice]] =
      httpClient.get(s"instances/${kind.getString}") {
        case Successful(response) =>
          response.as[InstancePrice].map(Right(_))
        case response =>
          Monad[F].pure(Left(
            InstanceKindService.Exception.APICallFailure(response.status.toString())
          ))
      }
  }

}
