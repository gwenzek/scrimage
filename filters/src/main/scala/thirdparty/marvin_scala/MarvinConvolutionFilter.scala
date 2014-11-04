package thirdparty.marvin_scala

import com.sksamuel.scrimage._

/** Created by guw on 03/10/14.
  */
trait MarvinConvolutionFilter extends LineByLine with CopyingFilter {
  val matrix: Array[Array[Double]]
  val xC: Int
  val yC: Int
  val matWidth: Int
  val matHeight: Int
  val treatAlpha: Boolean

  def treatLine(y: Int, src: Raster, dst: Raster): Unit = {
    var x, c = 0
    val channels = if (treatAlpha) src.n_channel else src.n_real_channel
    while (x < src.width) {
      c = 0
      while (c < channels) {
        apply(x, y, c, src, dst)
        c += 1
      }
      x += 1
    }
  }

  def apply(x: Int, y: Int, c: Int, src: Raster, dst: Raster) = {
    var i, j, nx, ny = 0
    var result = 0.0

    while (i < matWidth) {
      j = 0
      while (j < matHeight) {
        if (matrix(j)(i) != 0) {
          nx = x + (i - xC)
          ny = y + (j - yC)
          if (nx >= 0 && nx < src.width && ny >= 0 && ny < src.height) {
            result += matrix(j)(i) * src.readChannel(nx, ny, c)
          }
        }
        j += 1
      }
      i += 1
    }

    result = math.abs(result)
    result += dst.readChannel(x, y, c)
    dst.writeChannel(x, y, c, truncate(result.toInt))
  }
}

class CenteredConvolutionFilter(val matrix: Array[Array[Double]], val treatAlpha: Boolean = false) extends MarvinConvolutionFilter {
  final val matWidth: Int = matrix(0).length
  final val matHeight: Int = matrix.length
  final val xC: Int = matWidth / 2
  final val yC: Int = matHeight / 2
}

class XYConvolution(val matrixX: Array[Array[Double]], val matrixY: Array[Array[Double]], val treatAlpha: Boolean = false) extends Filter {
  val convolX = new CenteredConvolutionFilter(matrixX, treatAlpha)
  val convolY = new CenteredConvolutionFilter(matrixY, treatAlpha)

  def apply(srcImage: Image) = {
    val dstImage = convolX(srcImage)
    convolY.filter(srcImage, dstImage)
  }
}