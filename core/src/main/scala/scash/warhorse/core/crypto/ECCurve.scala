package scash.warhorse.core.crypto

import java.math.BigInteger

import org.bouncycastle.math.ec.{ ECCurve => ECC, ECPoint }

trait ECCurve[A] {
  val N: BigInteger
  val G: ECPoint
  val curve: ECC
}
