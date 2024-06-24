package prices.services

import scala.util.control.NoStackTrace
import prices.data._
import prices.routes.protocol.InstancePriceResponse

trait InstancePricesService[F[_]] {
  def get(kind: InstanceKind): F[Either[InstanceKindService.Exception, InstancePrice]]
}

object InstancePricesService {

  sealed trait Exception extends NoStackTrace
  object Exception {
    case class APICallFailure(message: String) extends Exception
  }

}
