package thirdparty.jhlabscala.image

import com.sksamuel.scrimage.Raster
import com.sksamuel.scrimage.filter.util._
import thirdparty.jhlabscala.image.ConvolveFilter._

object ConvolveFilter {

  val ZERO_EDGES = 0

  val CLAMP_EDGES = 1

  val WRAP_EDGES = 2

  def coerce(x: Float, min: Int = 0, max: Int = 255): Int =
    if (x > max) max
    else if (x < min) min
    else (x + 0.5f).toInt

  def apply(matrix: Array[Float]) = new ConvolveFilter(
    CLAMP_EDGES,
    matrix,
    3, 3
  )
}

class ConvolveFilter(val edgeAction: Int,
                     val matrix: Array[Float],
                     val kernelWidth: Int,
                     val kernelHeight: Int)
    extends IndependentPixelByPixel {

  val buffer = Array.ofDim[Int](matrix.length)
  val dx = (kernelWidth / 2)
  val dy = (kernelHeight / 2)

  def apply(x: Int, y: Int, c: Int, src: Raster): Int = {
    convolve(x, y, c, getReadBlock(src))
  }

  def getReadBlock(src: Raster) = {
    if (edgeAction == CLAMP_EDGES)
      (x: Int, y: Int, c: Int) => src.readBlockChannelsWith(c, x - dx, y - dy, kernelWidth, kernelHeight)(src.clampedChannelReader)
    else if (edgeAction == WRAP_EDGES)
      (x: Int, y: Int, c: Int) => src.readBlockChannelsWith(c, x - dx, y - dy, kernelWidth, kernelHeight)(src.wrappedChannelReader)
    else
      (x: Int, y: Int, c: Int) => src.readBlockChannelsWith(c, x - dx, y - dy, kernelWidth, kernelHeight)(src.zeroedChannelReader)
  }

  def convolve(x: Int, y: Int, c: Int, readBlock: (Int, Int, Int) => Array[Int]) = {
    val data = readBlock(x, y, c)
    var i = 0
    var res = 0.0f
    val n = data.length
    while (i < n) {
      res += matrix(i) * data(i)
      i += 1
    }
    coerce(res)
  }

  def convolveV(x: Int, y: Int, c: Int, readBlock: (Int, Int, Int) => Array[Int]) = {
    val rows2 = kernelHeight / 2
    var res = 0.0f
    var row = -rows2
    val data = readBlock(x, y, c)
    while (row <= rows2) {
      res += matrix(rows2 + row) * data(rows2 + row)
      row += 1
    }
    coerce(res)
  }

  def convolveHV(x: Int, y: Int, c: Int, readBlock: (Int, Int, Int) => Array[Int]) = {
    val rows2 = kernelHeight / 2
    val cols2 = kernelWidth / 2
    var res = 0.0f
    var row = -rows2
    val data = readBlock(x, y, c)
    val moffset = kernelWidth * (row + rows2) + cols2

    while (row <= rows2) {
      var col = -cols2
      while (col <= cols2) {
        res += matrix(moffset + col) * data(moffset + col)
        col += 1
      }
      row += 1
    }
    coerce(res)
  }
}
