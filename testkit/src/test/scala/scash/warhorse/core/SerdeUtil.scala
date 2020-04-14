package scash.warhorse.core

import scash.warhorse.TestUtil._
import scash.warhorse.core.typeclass.{ CNumeric, Serde }
import scodec.bits.ByteVector

import zio.test.assert

object SerdeUtil {
  def symmetry[A: Serde: CNumeric](u: A) =
    assert(u.bytes.decode[A])(success[A](u))

  def symmetryHex[A: Serde: CNumeric](u: A) =
    assert(ByteVector.fromValidHex(u.hex).decode[A])(success(u))
}
