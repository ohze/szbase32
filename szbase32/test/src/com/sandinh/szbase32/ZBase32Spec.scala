package com.sandinh.szbase32

import org.scalatest.{FlatSpec, Matchers}
import szbase32.{ZBase32 => Old}

import scala.util.Random
import ZBase32._

import scala.annotation.tailrec
import scala.collection.immutable.Stream

class ZBase32Spec extends FlatSpec with Matchers {
  "ZBase32" should "bytes -> encode -> decode == bytes, include case bytes.length == 0" in {
    def test(bytesLen: Int) = {
      val bytes = new Array[Byte](bytesLen)
      Random.nextBytes(bytes)
      decode(encode(bytes)) shouldEqual bytes // should contain theSameElementsInOrderAs
    }
    test(0)
    for (_ <- 0 to 20) test(1 + Random.nextInt(100))
  }

  it should
    """do NOT NEED to: s -> decode -> encode == s
      |  because there are >= 1 ways to encode a byte array.""".stripMargin in {
    for((s, s2, bytes) <- Seq(
      ("rj", "re", Array[Byte](34)),
      ("4ramr45", "4ramr4a", Array[Byte](-47,48,-78,107)))
    ) {
      encode(decode(s)) shouldEqual s2 // don't need == s
      decode(s) shouldEqual bytes
      decode(s) shouldEqual decode(s2)
    }
  }

  private val encTbl = "ybndrfg8ejkmcpqxot1uwisza345h769"
  def rndChars: Stream[Char] = {
    def c = encTbl charAt (Random nextInt 32)
    Stream continually c
  }

  it should "en/decode same as in the old implementation" in {
    for (_ <- 0 to 20) {
      val bytes = new Array[Byte](Random.nextInt(100))
      Random.nextBytes(bytes)
      encode(bytes) shouldEqual Old.encode(bytes)

      val s = rndChars.take(Random.nextInt(100)).mkString
      decode(s) shouldEqual Old.decode(s)
    }
  }

  it should "`decode` case-insensitive" in {
    for (_ <- 0 to 20) {
      val s = rndChars.take(Random.nextInt(100)).mkString
      decode(s) shouldEqual decode(s.toUpperCase())
    }
  }

  it should "`decode` don't break on invalid z-base-32 input" in {
    def invalidChar(c: Char) = !encTbl.contains(c.toLower)
    @tailrec def invalidInput(): String = {
      val r = Random.alphanumeric.take(Random.nextInt(100))
      if (r exists invalidChar) r.mkString
      else invalidInput()
    }

    for (_ <- 0 to 20) {
      val s = invalidInput()
//      println(s)
//      println(s.map(c => if (invalidChar(c)) " " else c).mkString)
      decode(s) shouldEqual decode(s filterNot invalidChar)
    }
  }
}
