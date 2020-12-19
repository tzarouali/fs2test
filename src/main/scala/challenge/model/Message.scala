package challenge.model

import io.circe._
import io.circe.generic.semiauto._

final case class Messages(messages: List[Message])

object Messages {
  implicit val d: Decoder[Messages] = deriveDecoder
}

final case class Message(
  id: Int,
  title: String,
  body: String,
  keywords: List[String],
  social_shares_counts: List[SocialShareCount]
)

object Message {
  implicit val d: Decoder[Message] = deriveDecoder
}
