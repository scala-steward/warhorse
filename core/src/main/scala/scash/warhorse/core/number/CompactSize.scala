package scash.warhorse.core.number

import scash.warhorse.core.typeclass.Serde

import scodec.Attempt.Successful
import scodec.Codec
import scodec.bits.BitVector
import scodec.codecs.{ uint16L, uint32L, uint8L, listOfN => slistOfN, variableSizeBytes => sVarSize, bytes => sbytes }

case class CompactSize(num: Long)

object CompactSize {

  def bytes                                       = {
    val intCodec = compactSizeSerde.xmap(_.num.toInt, (i: Int) => CompactSize(i.toLong))
    sVarSize(intCodec.codec, sbytes)
  }
  def listOfN[A](serde: Serde[A]): Codec[List[A]] = {
    val intCodec = compactSizeSerde.xmap(_.num.toInt, (i: Int) => CompactSize(i.toLong))
    slistOfN(intCodec.codec, serde.codec)
  }

  val compactSizeSerde: Serde[CompactSize] = Serde(
    Codec[CompactSize](
      (n: CompactSize) =>
        n.num match {
          case i if (i < 0xfd)        =>
            uint8L.encode(i.toInt)
          case i if (i < 0xffff)      =>
            for {
              a <- uint8L.encode(0xfd)
              b <- uint16L.encode(i.toInt)
            } yield a ++ b
          case i if (i < 0xffffffffL) =>
            for {
              a <- uint8L.encode(0xfe)
              b <- uint32L.encode(i)
            } yield a ++ b
          case i                      =>
            for {
              a <- uint8L.encode(0xff)
              b <- Serde[Uint64].encode(Uint64(BigInt(i))).map(_.bits).toAttempt
            } yield a ++ b
        },
      (buf: BitVector) =>
        uint8L
          .decode(buf)
          .flatMap(byte =>
            byte.value match {
              case 0xff =>
                Serde[Uint64].decode(byte.remainder.toByteVector).map(_.map(s => CompactSize(s.num.toLong))).toAttempt
              case 0xfe =>
                uint32L.decode(byte.remainder).map(_.map(CompactSize(_)))
              case 0xfd =>
                uint16L.decode(byte.remainder).map(_.map(n => CompactSize(n.toLong)))
              case _    =>
                Successful(scodec.DecodeResult(CompactSize(byte.value.toLong), byte.remainder))
            }
          )
    )
  )
}
