package scash.warhorse.core

import org.bouncycastle.crypto.digests.SHA256Digest

import scash.warhorse.Result
import scodec.bits.ByteVector
import scash.warhorse.core.crypto.Secp256k1._

package object crypto {
  def genPrivkey: Result[PrivateKey] = secp256k1KeyGen.genPrivkey

  def genPubkey(privateKey: PrivateKey): Result[PublicKey] = secp256k1KeyGen.genPubkey(privateKey)

  def genPubkeyCompressed(privateKey: PrivateKey): Result[PublicKey] = secp256k1KeyGen.genPubkeyCompressed(privateKey)

  def sign[A: Signer](msg: ByteVector, privkey: PrivateKey): Result[Signature] =
    Signer[A].sign(msg, privkey)

  def verify[A: Signer](msg: ByteVector, sig: ByteVector, pubKey: PublicKey): Result[Boolean] =
    Signer[A].verify(msg, sig, pubKey)

  protected[crypto] def nonceRFC6979 = new KGenerator(new SHA256Digest)
}
