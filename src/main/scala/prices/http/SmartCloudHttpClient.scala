package prices.http

import cats.effect.kernel.Async
import org.http4s.headers.{Accept, Authorization}
import org.http4s.{AuthScheme, Credentials, Headers, MediaType, Request, Response, Uri}
import prices.config.Config.SmartCloudConfig

object SmartCloudHttpClient {

  def make[F[_]: Async](smartCloudConfig: SmartCloudConfig, httpClient: HttpClient[F]): SmartCloudHttpClient[F] =
    new SmartCloudHttpClient(smartCloudConfig, httpClient)

  class SmartCloudHttpClient[F[_]: Async](smartCloudConfig: SmartCloudConfig, httpClient: HttpClient[F]) {
    implicit val priceHeader: Headers = Headers(
      Accept(MediaType.text.strings),
      Authorization(Credentials.Token(AuthScheme.Bearer, smartCloudConfig.token))
    )

    def get[B](uri: String)(f: Response[F] => F[B]): F[B] = {
      val request = Request[F](
        uri = Uri.unsafeFromString(s"${smartCloudConfig.baseUri}/$uri"),
        headers = priceHeader
      )
      httpClient.send(request)(f)
    }
  }

}
