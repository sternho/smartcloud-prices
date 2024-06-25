package prices.services

import cats.Monad
import cats.effect._
import cats.implicits._
import org.http4s.Status.Successful
import org.http4s._
import org.http4s.circe._
import prices.data._
import prices.http.SmartCloudHttpClient.SmartCloudHttpClient

object SmartCloudInstanceKindService {

  def make[F[_] : Concurrent](httpClient: SmartCloudHttpClient[F]): SmartCloudInstanceKindService[F] = new SmartCloudInstanceKindService(httpClient)

  final class SmartCloudInstanceKindService[F[_] : Concurrent](
                                                                        httpClient: SmartCloudHttpClient[F]
                                                                      ) extends InstanceKindService[F] {

    implicit val instanceKindsEntityDecoder: EntityDecoder[F, List[String]] = jsonOf[F, List[String]]

    override def getAll(): F[Either[InstanceKindService.Exception, List[InstanceKind]]] =
      httpClient.get("instances") { handleInstanceKindResponse }

    val handleInstanceKindResponse: Response[F] => F[Either[InstanceKindService.Exception, List[InstanceKind]]] = {
      case Successful(response) =>
        response.as[List[String]].map((messages: List[String]) => Right(messages.map(InstanceKind)))
      case response =>
        Monad[F].pure(Left(
          InstanceKindService.Exception.APICallFailure(response.status.toString())
        ))
    }

  }

}
