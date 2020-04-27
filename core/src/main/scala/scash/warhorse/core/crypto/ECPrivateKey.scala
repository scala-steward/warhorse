package scash.warhorse.core.crypto

import scodec.bits.ByteVector

case class ECPrivateKey(private val hex: ByteVector)

object ECPrivateKey {}
