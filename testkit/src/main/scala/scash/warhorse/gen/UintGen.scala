package scash.warhorse.gen

import scash.warhorse.core.number.{ Uint32, Uint8 }
import zio.test.Gen

trait UintGen {

  /** Generates a random uint8 */
  def uint8 = Gen.int(Uint8.min.num, Uint8.max.num).map(Uint8(_))

  /** Generates a random uint32 */
  def uint32 = Gen.long(Uint32.min.num, Uint32.max.num).map(Uint32(_))

  /** Generates a random uint64 */
  def uint64 = positiveBigInts.filter(_ < (BigInt(1) << 64)).map(Uint64(_))

}
