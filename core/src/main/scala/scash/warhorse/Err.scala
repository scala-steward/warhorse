package scash.warhorse

trait Err {

  /** Gets a description of the error. */
  def message: String

  override def toString = message
}

/** Companion for [[Err]]. */
object Err {

  final case class General(message: String, context: List[String]) extends Err {
    def this(message: String) = this(message, Nil)
    def pushContext(ctx: String) = copy(context = ctx :: context)
  }

  final case class BoundsError(name: String, requirement: String, is: String) extends Err {
    def message = s"$name must be $requirement, got: $is"
  }

  def apply(message: String): Err = new General(message)
}
