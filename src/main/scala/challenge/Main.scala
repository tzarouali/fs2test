package challenge

import java.time.Instant

import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.implicits._
import challenge.model._
import fs2.Stream
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.http4s.Method._
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.dsl.io._
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{EntityDecoder, EntityEncoder}
import retry._

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {

  final val NUMBER_PROCESSING_RETRIES       = 3
  final val MAX_NUMBER_MESSAGES_TO_RETRIEVE = 10

  private def printlnWithTime(msg: String): Unit =
    println(s"***** [${Instant.now()}] [${Thread.currentThread().getName}] $msg")

  implicit def jsonDecoder[
    F[_]: Sync,
    A: Decoder
  ]: EntityDecoder[F, A] = jsonOf[F, A]

  implicit def jsonEncoder[
    F[_]: Sync,
    A: Encoder
  ]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

  private def handleMessagesAsync(msgs: Messages, httpClient: Client[IO]): Unit =
    msgs.messages
      .parTraverse { m =>
        IO {
          val groupedShareCounts = m.social_shares_counts.groupBy(_.network)
          val facebookShares     = groupedShareCounts.get("facebook").map(_.length).getOrElse(0)
          val twitterShares      = groupedShareCounts.get("twitter").map(_.length).getOrElse(0)
          val redditShares       = groupedShareCounts.get("reddit").map(_.length).getOrElse(0)
          ProcessingItem(
            id = m.id,
            text = m.title + m.body,
            words_count = m.body.split(" ").length,
            characters_count = m.body.length,
            keywords = m.keywords,
            facebook_shares = facebookShares,
            twitter_shares = twitterShares,
            reddit_shares = redditShares,
            sum_shares = facebookShares + twitterShares + redditShares
          )
        }
      }
      .flatMap { items =>
        retryingM[Boolean](
          RetryPolicies.limitRetries[IO](NUMBER_PROCESSING_RETRIES),
          (b: Boolean) => b,
          retry.noop[IO, Boolean]
        ) {
          httpClient.successful(POST(items, uri"http://localhost:5000/process"))
        }.ifM(IO(items.map(_.id)), IO(List()))
      }
      .flatMap { itemsIds =>
        if (itemsIds.nonEmpty) {
          printlnWithTime(s"About to ack $itemsIds")
          httpClient.successful(POST(itemsIds.asJson, uri"http://localhost:5000/ack")).void
        } else {
          printlnWithTime(s"Processing failed. Skipping acking")
          IO.unit
        }
      }
      .unsafeRunAsyncAndForget()

  def run(args: List[String]): IO[ExitCode] =
    BlazeClientBuilder[IO](global).resource
      .use { httpClient =>
        Stream
          .eval(
            httpClient.expect[Messages](s"http://localhost:5000/messages?max_messages=$MAX_NUMBER_MESSAGES_TO_RETRIEVE")
          )
          .handleErrorWith { e =>
            printlnWithTime(s"Error retrieving messages: ${e.getMessage}")
            Stream.empty
          }
          .repeat
          .evalMap { messages =>
            printlnWithTime(s"Received ${messages.messages.length} messages")
            handleMessagesAsync(messages, httpClient)
            IO.unit
          }
          .compile
          .drain
          .unsafeRunAsyncAndForget()
        IO.never
      }
      .as(ExitCode.Success)

}
