package challenge.model

import io.circe._
import io.circe.generic.semiauto._

final case class SocialShareCount(network: String, count: Int)

object SocialShareCount {
  implicit val d: Decoder[SocialShareCount] = deriveDecoder
}
