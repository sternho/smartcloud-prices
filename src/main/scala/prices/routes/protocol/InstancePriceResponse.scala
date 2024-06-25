package prices.routes.protocol

import io.circe._
import io.circe.syntax._

import prices.data._

final case class InstancePriceResponse(value: InstancePrice)

object InstancePriceResponse {

  implicit val encoder: Encoder[InstancePriceResponse] =
    Encoder.instance[InstancePriceResponse] {
      case InstancePriceResponse(k) => Json.obj(
        "kind" -> k.kind.asJson,
        "price" -> k.price.asJson,
        "timestamp" -> k.timestamp.asJson,
      )
    }

}
