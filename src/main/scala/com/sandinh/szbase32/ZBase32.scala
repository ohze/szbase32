package com.sandinh.szbase32

import java.io.ByteArrayOutputStream

/**
  * Implements the <a href="http://philzimmermann.com/docs/human-oriented-base-32-encoding.txt">
  * z-base-32 encoding</a>.
  * @author Haakon Nilsen, Bui Viet Thanh
  */
object ZBase32 {
  private[this] val encTable = "ybndrfg8ejkmcpqxot1uwisza345h769".toArray
  /** compute from the following code:
    * {{{
    * def toDecTable(e: String): Array[Byte] = {
    *   val t = Array.fill[Byte](128)(-1)
    *   def fill(e: String) = e.zipWithIndex.foreach {
    *     case (c, i) => t(c) = i.toByte
    *   }
    *   fill(e.toLowerCase)
    *   fill(e.toUpperCase)
    *   t.reverse.dropWhile(_ == -1).reverse
    * }
    * def fmt(v: Byte): String = {
    *   var s = s"$v,"
    *   while(s.length < 4) s = " " + s
    *   s
    * }
    * def pretty(t: Array[Byte]) = t.zipWithIndex.foreach {
    *   case (v, i) =>
    *     print(fmt(v))
    *     if (i % 16 == 15) println()
    * }
    * val encTable = "ybndrfg8ejkmcpqxot1uwisza345h769"
    * pretty(toDecTable(encTable))
    * }}}
    */
  private[this] val decTable = Array[Byte](
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, 18, -1, 25, 26, 27, 30, 29,  7, 31, -1, -1, -1, -1, -1, -1,
    -1, 24,  1, 12,  3,  8,  5,  6, 28, 21,  9, 10, -1, 11,  2, 16,
    13, 14,  4, 22, 17, 19, -1, 20, 15,  0, 23, -1, -1, -1, -1, -1,
    -1, 24,  1, 12,  3,  8,  5,  6, 28, 21,  9, 10, -1, 11,  2, 16,
    13, 14,  4, 22, 17, 19, -1, 20, 15,  0, 23
  )

  /**
    * BASE32 characters are 5 bits in length.
    * They are formed by taking a block of five octets to form a 40-bit string,
    * which is converted into eight BASE32 characters.
    */
  private[this] val BitsPerEncodedByte = 5
  private[this] val BytesPerEncodedBlock = 8
  private[this] val BytesPerUnencodedBlock = 5
  /** Mask used to extract 8 bits, used in decoding bytes */
  private[this] val Mask8Bit = 0xff
  /** Mask used to extract 5 bits, used when encoding Base32 bytes */
  private[this] val Mask5Bit = 0x1f

  /** Encodes a byte[] containing binary data, into a z-base-32 string */
  def encode(in: Array[Byte]): String = {
    val out = new StringBuilder
    // Writes to the buffer only occur after every 3/5 reads when encoding.
    // This variable helps track that.
    var modulus = 0
    // Place holder for the bytes we're dealing with for our encoding logic.
    // Bitwise operations store and extract the encoding from this variable.
    var lbitWorkArea = 0L
    for (b <- in) {
      modulus = (modulus + 1) % BytesPerUnencodedBlock
      lbitWorkArea = (lbitWorkArea << 8) + b // BitPerByte
      if (b < 0) lbitWorkArea += 256
      if (modulus == 0) { // we have enough bytes to create our output
        for (i <- 35 to 0 by -5) {
          out += encTable((lbitWorkArea >> i).toInt & Mask5Bit)
        }
      }
    }

    modulus match {
      case 1 => // Only 1 octet; take top 5 bits then remainder
        out += encTable((lbitWorkArea >> 3).toInt & Mask5Bit) // 8-1*5 = 3
        out += encTable((lbitWorkArea << 2).toInt & Mask5Bit) // 5-3=2
      case 2 => // 2 octets = 16 bits to use
        out += encTable((lbitWorkArea >> 11).toInt & Mask5Bit) // 16-1*5 = 11
        out += encTable((lbitWorkArea >>  6).toInt & Mask5Bit) // 16-2*5 = 6
        out += encTable((lbitWorkArea >>  1).toInt & Mask5Bit) // 16-3*5 = 1
        out += encTable((lbitWorkArea <<  4).toInt & Mask5Bit) // 5-1 = 4
      case 3 => // 3 octets = 24 bits to use
        out += encTable((lbitWorkArea >> 19).toInt & Mask5Bit)
        out += encTable((lbitWorkArea >> 14).toInt & Mask5Bit)
        out += encTable((lbitWorkArea >>  9).toInt & Mask5Bit)
        out += encTable((lbitWorkArea >>  4).toInt & Mask5Bit)
        out += encTable((lbitWorkArea <<  1).toInt & Mask5Bit)
      case 4 => // 4 octets = 32 bits to use
        out += encTable((lbitWorkArea >> 27).toInt & Mask5Bit)
        out += encTable((lbitWorkArea >> 22).toInt & Mask5Bit)
        out += encTable((lbitWorkArea >> 17).toInt & Mask5Bit)
        out += encTable((lbitWorkArea >> 12).toInt & Mask5Bit)
        out += encTable((lbitWorkArea >>  7).toInt & Mask5Bit)
        out += encTable((lbitWorkArea >>  2).toInt & Mask5Bit)
        out += encTable((lbitWorkArea <<  3).toInt & Mask5Bit)
      case _ => // case 0 => no leftovers to process
    }

    out.result()
  }

  /** Decodes a String containing characters in the z-base-32 alphabet.
    * @note Silence ignore all character `c` in `in` that `!encTable.contains(c)`.
    *       That condition is same as `0 <= c < decTable.length && decTable(c) >= 0` */
  def decode(in: String): Array[Byte] = {
    val out = new ByteArrayOutputStream
    // Writes to the buffer only occur after every 4/8 reads when decoding.
    // This variable helps track that.
    var modulus = 0
    // Place holder for the bytes we're dealing with for our decoding logic.
    // Bitwise operations store and extract the decoding from this variable.
    var lbitWorkArea = 0L
    for (c <- in) {
      if (c >= 0 && c < decTable.length) {
        val i = decTable(c)
        if (i >= 0) {
          modulus = (modulus + 1) % BytesPerEncodedBlock
          // collect decoded bytes
          lbitWorkArea = (lbitWorkArea << BitsPerEncodedByte) + i
          if (modulus == 0) { // we can output the 5 bytes
            out write ((lbitWorkArea >> 32) & Mask8Bit).toByte
            out write ((lbitWorkArea >> 24) & Mask8Bit).toByte
            out write ((lbitWorkArea >> 16) & Mask8Bit).toByte
            out write ((lbitWorkArea >> 8) & Mask8Bit).toByte
            out write (lbitWorkArea & Mask8Bit).toByte
          }
        }
      }
    }

    //  we ignore partial bytes, i.e. only multiples of 8 count
    modulus match {
      case 2 => // 10 bits, drop 2 and output 1 byte
        out write ((lbitWorkArea >> 2) & Mask8Bit).toByte
      case 3 => // 15 bits, drop 7 and output 1 byte
        out write ((lbitWorkArea >> 7) & Mask8Bit).toByte
      case 4 => // 20 bits = 2*8 + 4
        lbitWorkArea >>= 4 // drop 4 bits
        out write ((lbitWorkArea >> 8) & Mask8Bit).toByte
        out write (lbitWorkArea & Mask8Bit).toByte
      case 5 => // 25bits = 3*8 + 1
        lbitWorkArea >>= 1
        out write ((lbitWorkArea >> 16) & Mask8Bit).toByte
        out write ((lbitWorkArea >> 8) & Mask8Bit).toByte
        out write (lbitWorkArea & Mask8Bit).toByte
      case 6 => // 30bits = 3*8 + 6
        lbitWorkArea >>= 6
        out write ((lbitWorkArea >> 16) & Mask8Bit).toByte
        out write ((lbitWorkArea >> 8) & Mask8Bit).toByte
        out write (lbitWorkArea & Mask8Bit).toByte
      case 7 => // 35 = 4*8 +3
        lbitWorkArea >>= 3
        out write ((lbitWorkArea >> 24) & Mask8Bit).toByte
        out write ((lbitWorkArea >> 16) & Mask8Bit).toByte
        out write ((lbitWorkArea >> 8) & Mask8Bit).toByte
        out write (lbitWorkArea & Mask8Bit).toByte
      case _ => // if modulus < 2, nothing to do
    }

    out.toByteArray
  }
}
