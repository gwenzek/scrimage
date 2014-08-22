/*
   Copyright 2013 Stephen K Samuel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.sksamuel.scrimage.io

import java.io.InputStream
import javax.imageio.ImageIO
import com.sksamuel.scrimage.{ Raster, Image }
import org.apache.sanselan.Sanselan
import scala.collection.mutable.MapBuilder

/** @author Stephen Samuel */
trait JavaImageIOReader extends ImageReader {
  def read(in: InputStream): Image = Image(ImageIO.read(in))
}

trait SanselanReader extends ImageReader {
  def read(in: InputStream): Image = Image(Sanselan.getBufferedImage(in))
}

protected object ReaderUtil {
  def readInt(in: InputStream): Int = readInt(readBuff(4, in))

  def readInt(a: Array[Byte], off: Int = 0): Int = {
    a(off) << 24 | a(off + 1) << 16 | a(off + 2) << 8 | a(off + 3)
  }

  def readChar(in: InputStream) = {
    in.read().toChar
  }

  def readString(n: Int, in: InputStream) = {
    val buff = Array.ofDim[Byte](n)
    val i = in.read(buff)
    new String(buff).substring(0, i)
  }

  def readBuff(n: Int, in: InputStream) = {
    val buff = Array.ofDim[Byte](n)
    in.read(buff)
    buff
  }
}

class PNGChunk(val name: String, val data: Array[Byte], val crc: Int) {
  def this(length: Int, name: String, in: InputStream) =
    this(name, ReaderUtil.readBuff(length, in), ReaderUtil.readInt(in))

  def isCritical = name(0).isUpper
  def isAncillary = name(0).isLower
  def isPublic = name(1).isUpper
  def isPrivate = name(1).isLower
  def isSafeToCopy = name(3).isUpper

  def apply(i: Int): Int = data(i).toInt
}

class PaletteChunck(val name: String, val data: Array[Byte], val crc: Int) {

}

object PNGReader extends ImageReader with MimeTypeChecker {
  import ReaderUtil._

  def readMimeType(input: InputStream) = {
    try {
      val buff = Array.ofDim[Byte](8)
      input.read(buff)
      val expected = List(0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A) map (_.toByte)
      assert(buff.toList == expected)
      Some(PNGMimeType)
    } catch {
      case _: AssertionError => None
    }
  }

  /** Reads a PNG image. The spec can be read at http://www.libpng.org/pub/png/spec/1.2/PNG-Chunks.html
    */
  def read(in: InputStream) = {
    readMimeType(in).getOrElse(throw new RuntimeException("Unparsable image"))
    val ihdr = readChunck(in)
    assert(ihdr.name == "IDHR")
    val width = readInt(ihdr.data, 0)
    val heigth = readInt(ihdr.data, 4)
    val bitDepth = ihdr(8)
    val paletteNeeded = ihdr(9) % 2 == 1

    var colorType = ihdr(9) - (ihdr(9) % 2)

    val compressionMethod = ihdr(10)
    assert(compressionMethod == 0)

    val filterMethod = ihdr(11)
    assert(filterMethod == 0)

    val interlaceMethod = ihdr(12)
    val useAdam7 = interlaceMethod == 1

    val raster = Raster(width, heigth, colorType)

    val ancillary = new MapBuilder[String, PNGChunk, Map[String, PNGChunk]](Map[String, PNGChunk]())

    var header = readChunckHeader(in)
    var eofReached = false
    while (!eofReached) {
      header._2 match {
        case "PLTE" => ()
        case "IDAT" => ()
        case "IEND" => eofReached = true
      }
      if (!eofReached) header = readChunckHeader(in)
    }

    new Image(raster)
  }

  def readChunck(in: InputStream) = {
    val length = readInt(in)
    val chunkType = readString(4, in)
    val buff = readBuff(length, in)
    val crc = readInt(in)
    new PNGChunk(chunkType, buff, crc)
  }

  def readChunckHeader(in: InputStream) = {
    val length = readInt(in)
    val chunkType = readString(4, in)
    (length, chunkType)
  }

}
