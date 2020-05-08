package scash.warhorse.core.crypto

import scash.warhorse.Result
import scodec.bits.ByteVector

trait Signer[A] {
  def sign(msg: ByteVector, privkey: PrivateKey): Result[Signature]

  def verify(msg: ByteVector, sig: ByteVector, pubkey: PublicKey): Result[Boolean]
}

object Signer   {
  def apply[A](implicit signer: Signer[A]): Signer[A] = signer
}
