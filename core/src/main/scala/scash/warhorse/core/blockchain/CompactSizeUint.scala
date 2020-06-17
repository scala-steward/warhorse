package scash.warhorse.core.blockchain

import scash.warhorse.Err
import scash.warhorse.Result.{ Failure, Successful }
import scash.warhorse.core.number.{ Uint32, Uint64, Uint8 }
import scash.warhorse.core.typeclass.Serde
import scash.warhorse.core._
import scodec.Codec
import scodec.bits.BitVector
import scodec.codecs.{ uint16L, uint32L, uint8L, bytes => sbytes, listOfN => slistOfN, variableSizeBytes => sVarSize }

case class CompactSizeUint(num: Uint64)

object CompactSizeUint {

  def apply(u: Uint8): CompactSizeUint = CompactSizeUint(Uint64(u.num))

  def apply(u: Uint32): CompactSizeUint = CompactSizeUint(Uint64(u.num))

  def bytes = {
    val intCodec = compactSizeSerde.xmap(_.num.num.toInt, (i: Int) => CompactSizeUint(Uint64(i)))
    sVarSize(intCodec.codec, sbytes)
  }
  def listOfN[A](serde: Serde[A]): Codec[List[A]] = {
    val intCodec = compactSizeSerde.xmap(_.num.num.toInt, (i: Int) => CompactSizeUint(Uint64(i)))
    slistOfN(intCodec.codec, serde.codec)
  }

  implicit val compactSizeSerde: Serde[CompactSizeUint] = Serde(
    Codec[CompactSizeUint](
      (n: CompactSizeUint) =>
        n.num match {
          case i if i <= Uint8(252) =>
            uint8L.encode(i.num.toInt)
          case i if i <= Uint32(65535) =>
            for {
              a <- uint8L.encode(0xfd)
              b <- uint16L.encode(i.num.toInt)
            } yield a ++ b
          case i if (i <= Uint32.max) =>
            for {
              a <- uint8L.encode(0xfe)
              b <- uint32L.encode(i.num.toLong)
            } yield a ++ b
          case i if i <= Uint64.max =>
            for {
              a <- uint8L.encode(0xff)
              b <- Successful(i.bits).toAttempt
            } yield a ++ b
          case _ => Failure(Err(s"${n.num} is too large to be parsed into CompactSizeUint")).toAttempt
        },
      (buf: BitVector) =>
        uint8L
          .decode(buf)
          .flatMap(byte =>
            byte.value match {
              case 0xff =>
                Serde[Uint64]
                  .decode(byte.remainder.toByteVector)
                  .map(_.map(CompactSizeUint(_)))
                  .toAttempt
              case 0xfe =>
                uint32L.decode(byte.remainder).map(_.map(n => CompactSizeUint(Uint64(n))))
              case 0xfd =>
                uint16L.decode(byte.remainder).map(_.map(n => CompactSizeUint(Uint64(n))))
              case _ =>
                Successful(scodec.DecodeResult(CompactSizeUint(Uint64(byte.value)), byte.remainder)).toAttempt
            }
          )
    )
  )
}
