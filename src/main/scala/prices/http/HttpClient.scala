package prices.http

import cats.effect.kernel.Async
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.{Request, Response}

class HttpClient[F[_]: Async] {

  private val resource = EmberClientBuilder.default[F].build

  def send[B](request: Request[F])(f: Response[F] => F[B]): F[B] =
    resource
      .map { client => client.run(request) }
      .use { client => client.use { f } }

}
