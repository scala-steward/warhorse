package scash.warhorse.core.blockchain

object CashAddr {

  val P2PKHMainNet = 0x00.toByte
  val P2SHMainNet  = 0x08.toByte
  /*
  def polyMod(data: ByteVector): ByteVector = {
    var c = Uint64.one
    data.foreach { d =>
      val c0 = c >> 35;
      (c & Uint64(0x07ffffffffL) << 5) ^ d.toInt

      if (c0 hasBit 0x01) c ^= 0x98f2bc8e61L
      if (c0 hasBit 0x02) c ^= 0x79b76d99e2L
      if (c0 hasBit 0x04) c ^= 0xf33e5fb3c4L
      if (c0 hasBit 0x08) c ^= 0xae2eabe2a8L
      if (c0 hasBit 0x10) c ^= 0x1e4f43e470L

    }
    (c ^ Uint64.one).bytes
  }


  private def cons(net: Net, mainNetByte: Byte, testNetByte: Byte, hash: Hash160) = {
    def genBase32(prefix: Byte): String = {
      val bytes    = prefix +: hash.bytes
      val checksum = Hasher[DoubleSha256].hash(bytes).bytes.take(5)
      (bytes ++ checksum).toBase32
    }
    net match {
      case MainNet => s"bitcoincash:${genBase32(mainNetByte)}"
      case TestNet => s"bchtest:${genBase32(testNetByte)}"
      case RegTest => s"bchreg:${genBase32(testNetByte)}"
    }
  }

   */
}
