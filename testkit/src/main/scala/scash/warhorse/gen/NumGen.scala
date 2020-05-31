package scash.warhorse.gen

import scash.warhorse.core.blockchain.CompactSizeUint
import zio.test.Gen

trait NumGen {

  /** Creates a generator that generates positive int numbers */
  def posInt = Gen.int(0, Int.MaxValue)

  /** Creates a generator that generates positive int numbers without zero */
  def posIntNoZero = Gen.int(1, Int.MaxValue)

  /** Creates a generator that generates negative int numbers */
  def negInt = Gen.int(0, Int.MaxValue)

  /** Creates a generator that generates positive long numbers */
  def posLong = Gen.long(0, Long.MaxValue)

  /** Creates a generator for positive longs without the number zero */
  def posLongNoZero = Gen.long(1, Long.MaxValue)

  /** Creates a number generator that generates negative long numbers */
  def negLong = Gen.long(Long.MinValue, -1)

  /** Chooses a BigInt in the ranges of 0 <= bigInt < 2^^64 */
  def bigInts = Gen.long(Long.MinValue, Long.MaxValue).map(n => BigInt(n) + BigInt(2).pow(63))

  def positiveBigInts = bigInts.filter(_ >= 0)

  def compactSizeUints = uint64.map(CompactSizeUint(_))
}
