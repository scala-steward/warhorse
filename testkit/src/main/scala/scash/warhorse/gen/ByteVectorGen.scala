package scash.warhorse.gen

import scodec.bits.ByteVector

import zio.test.Gen

trait ByteVectorGen {
  def byteVectorN(size: Int) =
    Gen.listOfN(size)(Gen.anyByte).map(ByteVector(_))

  def byteVectorBounded(min: Int, max: Int) =
    Gen.listOfBounded(min, max)(Gen.anyByte).map(ByteVector(_))
}
