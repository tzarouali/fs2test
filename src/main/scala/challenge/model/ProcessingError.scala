package challenge.model

import scala.util.control.NoStackTrace

case class ProcessingError(msg: String) extends Throwable(msg) with NoStackTrace
