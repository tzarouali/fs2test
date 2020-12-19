package challenge.model

import io.circe._
import io.circe.generic.semiauto._

final case class ProcessingItem(
  id: Int,
  text: String,
  words_count: Int,
  characters_count: Int,
  keywords: List[String],
  facebook_shares: Int,
  twitter_shares: Int,
  reddit_shares: Int,
  sum_shares: Int
)

object ProcessingItem {
  implicit val e: Encoder[ProcessingItem] = deriveEncoder
}
