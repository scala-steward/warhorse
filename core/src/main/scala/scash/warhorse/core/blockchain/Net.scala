package scash.warhorse.core.blockchain

sealed trait Net extends Product with Serializable

case object MainNet extends Net
case object TestNet extends Net
case object RegTest extends Net

object Net {

  def apply(str: String): Net =
    str match {
      case "main"    => MainNet
      case "test"    => TestNet
      case "regtest" => RegTest
    }
}
