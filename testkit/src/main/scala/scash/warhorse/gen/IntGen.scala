package scash.warhorse.gen

import scash.warhorse.core.number.{ Int32, Int64 }
import zio.test.Gen

trait IntGen {

  /** Generates a random Int32 */
  def int32 = Gen.int(Int32.min.num, Int32.max.num).map(Int32(_))

  /** Generates a random Int64 */
  def int64 = Gen.long(Int64.min.num, Int64.max.num).map(Int64(_))

  /** Generates a random negative Int64 */
  def negInt32 = negInt.map(Int32(_))

  /** Generates a random positive Int64 */
  def posInt32 = posInt.map(Int32(_))

  /** Generates a random negative Int64 */
  def negInt64 = negLong.map(Int64(_))

  /** Generates a random positive Int64 */
  def posInt64 = posLong.map(Int64(_))
}
