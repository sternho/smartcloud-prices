package prices.http

import cats.effect.kernel.Async
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.headers.{Accept, Authorization}
import org.http4s.{AuthScheme, Credentials, Headers, MediaType, Request, Response, Uri}

class HttpClient[F[_]: Async] {

  private val clientBuilder = EmberClientBuilder.default[F].build

  def send[B](request: Request[F])(f: Response[F] => F[B]): F[B] =
    clientBuilder
      .map { client => client.run(request) }
      .use { client => client.use { f } }

}
