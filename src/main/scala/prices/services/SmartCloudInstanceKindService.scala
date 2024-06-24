package prices.services

import cats.Monad
import cats.effect._
import cats.implicits._
import org.http4s.Status.Successful
import org.http4s._
import org.http4s.circe._
import prices.data._
import prices.http.PriceHttpClient.PriceHttpClient

object SmartCloudInstanceKindService {

  def make[F[_] : Concurrent](httpClient: PriceHttpClient[F]): InstanceKindService[F] = new SmartCloudInstanceKindService(httpClient)

  final class SmartCloudInstanceKindService[F[_] : Concurrent](
                                                                        httpClient: PriceHttpClient[F]
                                                                      ) extends InstanceKindService[F] {

    implicit val instanceKindsEntityDecoder: EntityDecoder[F, List[String]] = jsonOf[F, List[String]]

    override def getAll(): F[Either[InstanceKindService.Exception, List[InstanceKind]]] =
      httpClient.get("instances") {
        case Successful(response) =>
          response.as[List[String]].map((messages: List[String]) => Right(messages.map(InstanceKind)))
        case response =>
          Monad[F].pure(Left(
            InstanceKindService.Exception.APICallFailure(response.status.toString())
          ))
      }

  }

}
