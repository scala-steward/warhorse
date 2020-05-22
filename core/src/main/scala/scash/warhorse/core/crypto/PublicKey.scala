package scash.warhorse.core.crypto

import scash.warhorse.{ Err, Result }
import scash.warhorse.Result.{ Failure, Successful }
import scash.warhorse.core.typeclass.Serde
import scodec.DecodeResult
import scodec.bits.ByteVector

sealed trait PublicKey { self =>

  def compress: PublicKey =
    self match {
      case c: PublicKey.PKeyCompressed   => c
      case u: PublicKey.PKeyUnCompressed =>
        val prefix =
          if ((u.b(64) & 0xff) % 2 == 0) 0x02.toByte
          else 0x03.toByte
        PublicKey.PKeyCompressed(prefix +: u.b.slice(1, 33))
    }
}

object PublicKey {
  private case class PKeyCompressed(b: ByteVector)   extends PublicKey
  private case class PKeyUnCompressed(b: ByteVector) extends PublicKey

  def apply(b: ByteVector): Result[PublicKey] =
    b.size match {
      case 33 if b(0) == 0x02 || b(0) == 0x03 => Successful(PKeyCompressed(b))
      case 65 if b(0) == 0x04                 => Successful(PKeyUnCompressed(b))
      case _                                  => Failure(Err.BoundsError("publickey", "is not valid", b.toHex))
    }

  implicit val publicKeySerde: Serde[PublicKey] =
    Serde(
      {
        case PKeyCompressed(b)   => Successful(b)
        case PKeyUnCompressed(b) => Successful(b)
      },
      b =>
        if (b.isEmpty) Failure(Err.ParseError("Publickey", s"empty pubkeys are invalid"))
        else if (List(0x02, 0x03).contains(b(0)))
          Result
            .fromEither(b.acquire(33))
            .map(compressed => DecodeResult(PKeyCompressed(compressed), b.drop(33).toBitVector))
        else if (b(0) == 0x04)
          Result
            .fromEither(b.acquire(65))
            .map(full => DecodeResult(PKeyUnCompressed(full), b.drop(65).toBitVector))
        else Failure(Err.ParseError("Publickey", s"is not compressed or uncompressed"))
    )
}
