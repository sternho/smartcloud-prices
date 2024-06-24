package prices.config

import cats.effect.kernel.Sync

import pureconfig.ConfigSource
import pureconfig.generic.auto._

case class Config(
    app: Config.AppConfig,
    smartcloud: Config.SmartCloudConfig
)

object Config {

  case class AppConfig(
      host: String,
      port: Int
  )

  case class SmartCloudConfig(
      baseUri: String,
      token: String
  )

  def load[F[_]: Sync]: F[Config] =
    Sync[F].delay(ConfigSource.default.loadOrThrow[Config])

}
