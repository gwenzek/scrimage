package thirdparty.marvin_scala

import com.sksamuel.scrimage._

/** Created by gwenzek on 10/10/14.
  */

object NoiseReduction {
  def apply() = new NoiseReductionSimple(20)
}

class NoiseReductionSimple(val radius: Int) extends AbstractImageFilter with CopyingFilter {

  val alpha: Int = 1
  val lam: Double = 0
  val dt: Double = 0.4

  def filter(srcImage: Image, dstImage: Image): Image = {
    val src = srcImage.raster
    var mat1 = Array.ofDim[Double](src.width, src.height)
    var mat2 = Array.ofDim[Double](src.width, src.height)
    var mat3: Array[Array[Double]] = null

    var c = 0

    while (c < src.n_real_channel) {
      mat1 = extractChannel(src, c, mat1)
      var i = 0
      while (i < radius) {
        denoiseOnce(mat1, mat2, src.width, src.height)
        mat3 = mat1
        mat1 = mat2
        mat2 = mat3
        i += 1
      }
      writeChannel(mat1, c, dstImage.raster)
      c += 1
    }
    dstImage
  }

  def extractChannel(src: Raster, channel: Int, mat: Array[Array[Double]]): Array[Array[Double]] = {
    (0 until src.height).par.foreach { y =>
      for (x <- 0 until src.width) {
        mat(x)(y) = src.readChannel(x, y, channel)
      }
    }
    mat
  }

  def writeChannel(mat: Array[Array[Double]], channel: Int, dst: Raster): Unit = {

    def toRange(z: Double): Int = {
      if (z > 255) 255
      else if (z < 0) 0
      else z.toInt
    }

    (0 until dst.height).par.foreach { y =>
      for (x <- 0 until dst.width) {
        dst.writeChannel(x, y, channel, toRange(mat(x)(y)))
      }
    }
  }

  def denoiseOnce(matSrc: Array[Array[Double]], matDst: Array[Array[Double]], w: Int, h: Int): Array[Array[Double]] = {
    (0 until h).par.foreach { y =>
      for (x <- 0 until w) {
        matDst(x)(y) = denoise(x, y, matSrc, w, h)
      }
    }
    matDst
  }

  def denoise(x: Int, y: Int, mat: Array[Array[Double]], w: Int, h: Int): Double = {
    val dy = diff_y(x, y, mat, h)
    val dx = diff_x(x, y, mat, w)

    val a: Double = diff_xx(x, y, mat, w) * (alpha + Math.pow(dy, 2))
    val b: Double = 2 * dx * dy * diff_xy(x, y, mat, w, h)
    val c: Double = diff_yy(x, y, mat, h) * (alpha + Math.pow(dx, 2))

    val l_currentNum = a - b + c
    val l_currentDen = Math.pow(alpha + Math.pow(dx, 2) + Math.pow(dy, 2), 1.5)

    mat(x)(y) + dt * (l_currentNum / l_currentDen)
  }

  @inline
  final def fit_in(x: Int, width: Int): Int = {
    if (x < 0) 0
    else if (x >= width) width - 1
    else x
  }

  def diff_x(x: Int, y: Int, mat: Array[Array[Double]], w: Int): Double =
    (mat(fit_in(x + 1, w))(y) - mat(fit_in(x - 1, w))(y)) / 2

  def diff_y(x: Int, y: Int, mat: Array[Array[Double]], h: Int): Double =
    (mat(x)(fit_in(y + 1, h)) - mat(x)(fit_in(y - 1, h))) / 2

  def diff_xx(x: Int, y: Int, mat: Array[Array[Double]], w: Int): Double =
    mat(fit_in(x + 1, w))(y) + mat(fit_in(x - 1, w))(y) - 2 * mat(x)(y)

  def diff_yy(x: Int, y: Int, mat: Array[Array[Double]], h: Int): Double =
    mat(x)(fit_in(y + 1, h)) + mat(x)(fit_in(y - 1, h)) - 2 * mat(x)(y)

  def diff_xy(x: Int, y: Int, mat: Array[Array[Double]], w: Int, h: Int): Double = {
    (mat(fit_in(x + 1, w))(fit_in(y + 1, h))
      + mat(fit_in(x - 1, w))(fit_in(y - 1, h))
      - mat(fit_in(x - 1, w))(fit_in(y + 1, h))
      - mat(fit_in(x + 1, w))(fit_in(y - 1, h))
    ) / 4
  }
}
