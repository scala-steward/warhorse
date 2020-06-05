package scash.warhorse.gen.crypto

import scash.warhorse.core.number.{ Uint32, Uint64, Uint8 }
import scash.warhorse.gen.positiveBigInts
import zio.test.Gen

trait UintGen {

  /** Generates a random uint8 */
  def uint8 = Gen.int(Uint8.min.num, Uint8.max.num).map(Uint8(_))

  /** Generates a random uint32 */
  def uint32 = Gen.long(Uint32.min.num, Uint32.max.num).map(Uint32(_))

  def uint32(min: Uint32, max: Uint32) = Gen.long(min.num, max.num).map(Uint32(_))

  /** Generates a random uint64 */
  def uint64 = positiveBigInts.filter(_ < (BigInt(1) << 64)).map(Uint64(_))

}
