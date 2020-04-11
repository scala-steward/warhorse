package scash.warhorse

import scodec.Attempt

import scala.util.Try
import scala.util.control.NonFatal

import scodec.Attempt.{ Successful => SSuccessful, Failure => SFailure }

/**
 * Right biased `Either[Err, A]`.
 *
 * An `Result` is either an `Result.Successful` or an `Result.Failure`. Results can be created
 * by calling `Result.successful` or `Result.failure`, as well as converting from an `Option` via
 * `fromOption`.
 * This encoding is used in `Attempt[A]` by scodec.
 * **/
sealed trait Result[+A] extends Product with Serializable {

  /** Maps the supplied function over the successful value, if present. */
  def map[B](f: A => B): Result[B]

  /** Maps the supplied function over the failure error, if present. */
  def mapErr(f: Err => Err): Result[A]

  /** Maps the supplied function over the successful value, if present. */
  def flatMap[B](f: A => Result[B]): Result[B]

  /** Converts an `Result[Result[X]]` in to an `Result[X]`. */
  //def flatten[B](implicit ev: A <:< Result[B]): Result[B]

  /** Transforms this attempt to a value of type `B` using the supplied functions. */
  def fold[B](ifFailure: Err => B, ifSuccessful: A => B): B

  /** Returns the sucessful value if successful, otherwise the supplied default. */
  def getOrElse[B >: A](default: => B): B

  /** Returns this attempt if successful, otherwise the fallback attempt. */
  def orElse[B >: A](fallback: => Result[B]): Result[B]

  /**
   * If this attempt is a failure, and the supplied partial function is defined for the cause of the failure,
   * a successful attempt is returned. If this attempt is successful or the supplied function is not defined
   * for the cause of the failure, this attempt is returned unmodified.
   */
  def recover[B >: A](f: PartialFunction[Err, B]): Result[B]

  /**
   * If this attempt is a failure, and the supplied partial function is defined for the cause of the failure,
   * the result of applying that function is returned. If this attempt is successful or the supplied
   * function is not defined for the cause of the failure, this attempt is returned unmodified.
   */
  def recoverWith[B >: A](f: PartialFunction[Err, Result[B]]): Result[B]

  /** Returns the successful value if present; otherwise throws an `IllegalArgumentException`. */
  def require: A

  /** True if attempt was successful. */
  def isSuccessful: Boolean

  /** True if attempt was not successful. */
  def isFailure: Boolean = !isSuccessful

  /** Converts to an option, discarding the error value. */
  def toOption: Option[A]

  /** Converts to an either. */
  def toEither: Either[Err, A]

  /** Converts to a try. */
  def toTry: Try[A]
}

object Result {

  /** Creates a successful attempt. */
  def successful[A](a: A): Result[A] = Successful(a)

  /** Creates an unsuccessful attempt. */
  def failure[A](err: Err): Result[A] = Failure(err)

  /** Creates a successful attempt if the condition succeeds otherwise create a unsuccessful attempt. */
  def guard(condition: => Boolean, err: String): Result[Unit] =
    if (condition) successful(()) else failure(Err(err))

  /** Creates a successful attempt if the condition succeeds otherwise create a unsuccessful attempt. */
  def guard(condition: => Boolean, err: => Err): Result[Unit] =
    if (condition) successful(()) else failure(err)

  /** Creates a attempt from a try. */
  def fromTry[A](t: Try[A]): Result[A] = t match {
    case scala.util.Success(value)        => successful(value)
    case scala.util.Failure(NonFatal(ex)) => failure(Err(ex.getMessage))
  }

  /** Creates an attempt from the supplied option. The `ifNone` value is used as the error message if `opt` is `None`. */
  def fromOption[A](opt: Option[A], ifNone: => Err): Result[A] =
    opt.fold(failure[A](ifNone))(successful)

  /** Creates an attempt from the supplied option. The `ifNone` value is used as the success value if `opt` is `None`. */
  def fromErrOption[A](opt: Option[Err], ifNone: => A): Result[A] =
    opt.fold(successful(ifNone))(failure)

  /** Creates an attempt from the supplied either. */
  def fromEither[A](e: Either[Err, A]): Result[A] =
    e.fold(failure, successful)

  def fromAttempt[A](a: Attempt[A]): Result[A] = a match {
    case SSuccessful(a) => Successful(a)
    case SFailure(m)    => Failure(Err(m.message))
  }

  def toAttempt[A](a: Result[A]): Attempt[A] = a match {
    case Successful(a) => SSuccessful(a)
    case Failure(m)    => SFailure(scodec.Err(m.message))
  }

  /** Successful attempt. */
  final case class Successful[A](value: A) extends Result[A] {
    def map[B](f: A => B): Result[B]             = Successful(f(value))
    def mapErr(f: Err => Err): Result[A]         = this
    def flatMap[B](f: A => Result[B]): Result[B] = f(value)
    //def flatten[B](implicit ev: A <:< Result[B]): Result[B]                = value
    def fold[B](ifFailure: Err => B, ifSuccessful: A => B): B              = ifSuccessful(value)
    def getOrElse[B >: A](default: => B): B                                = value
    def orElse[B >: A](fallback: => Result[B])                             = this
    def recover[B >: A](f: PartialFunction[Err, B]): Result[B]             = this
    def recoverWith[B >: A](f: PartialFunction[Err, Result[B]]): Result[B] = this
    def require: A                                                         = value
    def isSuccessful: Boolean                                              = true
    def toOption: Some[A]                                                  = Some(value)
    def toEither: Right[Err, A]                                            = Right(value)
    def toTry: Try[A]                                                      = scala.util.Success(value)
  }

  /** Failed attempt. */
  final case class Failure(cause: Err) extends Result[Nothing] {
    def map[B](f: Nothing => B): Result[B]             = this
    def mapErr(f: Err => Err): Result[Nothing]         = Failure(f(cause))
    def flatMap[B](f: Nothing => Result[B]): Result[B] = this
    //def flatten[B](implicit ev: Nothing <:< Result[B]): Result[B]   = this
    def fold[B](ifFailure: Err => B, ifSuccessful: Nothing => B): B = ifFailure(cause)
    def getOrElse[B >: Nothing](default: => B): B                   = default
    def orElse[B >: Nothing](fallback: => Result[B])                = fallback
    def recover[B >: Nothing](f: PartialFunction[Err, B]): Result[B] =
      if (f.isDefinedAt(cause)) Result.successful(f(cause))
      else this
    def recoverWith[B >: Nothing](f: PartialFunction[Err, Result[B]]): Result[B] =
      if (f.isDefinedAt(cause)) f(cause)
      else this
    def require: Nothing             = throw new IllegalArgumentException(cause.message)
    def isSuccessful: Boolean        = false
    def toOption: None.type          = None
    def toEither: Left[Err, Nothing] = Left(cause)
    def toTry: Try[Nothing] =
      scala.util.Failure(new Exception(s"Error occurred: ${cause.message}"))
  }
}
