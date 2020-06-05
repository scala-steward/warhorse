package scash.warhorse.core.blockchain

sealed trait Net

case object MainNet extends Net
case object TestNet extends Net
case object RegTest extends Net
