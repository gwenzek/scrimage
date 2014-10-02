package com.sksamuel.scrimage.filter

import com.sksamuel.scrimage.{Filter, Image, RGBColor}
import org.scalatest.{FunSuite, OneInstancePerTest}

/** @author Stephen Samuel */
class LensBlurFilterTest extends FunSuite with OneInstancePerTest {

  ignore("LensBlurFilter output matches expected") {
    val f_exp = testFilterOn("/bird_small.png", LensBlurFilter(), "/bird_lens_blur.png")
    writeAndCompare("blured", f_exp)
    assert(f_exp._1 ===  f_exp._2)
  }

  ignore("RippleFilter with Triangle output matches expected") {
    val f_exp = testFilterOn("/bird_small.png", RippleFilter(RippleType.Triangle), "/bird_ripple_triangle.png")
    writeAndCompare("ripple_triangle", f_exp)
    assert(f_exp._1 ===  f_exp._2)
  }

  ignore("RippleFilter with Sawtooth output matches expected") {
    val f_exp = testFilterOn("/bird_small.png", RippleFilter(RippleType.Sawtooth), "/bird_ripple_sawtooth.png")
    writeAndCompare("ripple_sawtooth", f_exp)
    assert(f_exp._1 ===  f_exp._2)
  }

  ignore("VintageFilter with Sawtooth output matches expected") {
    val f_exp = testFilterOn("/bird_small.png", VintageFilter, "/com/sksamuel/scrimage/filters/bird_small_vintage.png", verbose = true)
    writeAndCompare("vintage", f_exp)
    assert(f_exp._1 ===  f_exp._2)
  }

  ignore("SummerFilter with Sawtooth output matches expected") {
    val f_exp = testFilterOn("/bird_small.png", SummerFilter(), "/com/sksamuel/scrimage/filters/bird_small_summer.png", verbose = true)
    writeAndCompare("summer", f_exp)
    assert(f_exp._1 ===  f_exp._2)
  }

  test("my EmbossFilter output matches expected") {
    val f_exp = testFilterOn("/bird_small.png", thirdparty.marvin_scala.EmbossFilter, "/com/sksamuel/scrimage/filters/bird_small_emboss.png", verbose = false)
    writeAndCompare("emboss", f_exp)
    assert(f_exp._1 ===  f_exp._2)
  }

  test("my SepiaFilter output matches expected") {
    val f_exp = testFilterOn("/bird_small.png", thirdparty.marvin_scala.Sepia(20), "/com/sksamuel/scrimage/filters/bird_small_sepia.png", verbose = false)
    writeAndCompare("sepia", f_exp)
    assert(f_exp._1 ===  f_exp._2)
  }

  test("my GrayScaleFilter output matches marvin's one") {
    val img =  Image(getClass.getResourceAsStream("/bird_small.png"))
    val exp = img.filter(new MarvinFilter {
      val plugin = new thirdparty.marvin.image.grayScale.GrayScale()
    })
    val f = img.filter(thirdparty.marvin_scala.MarvinGrayScaleFilter)
    printComparaison(f, exp, "GrayScale")
//    printComparaison(f, exp, img)
    writeAndCompare("grayscale", (f, exp))
    assert(f ===  exp)
  }

  test("my ThresholdingFilter output matches marvin's one") {
    val img =  Image(getClass.getResourceAsStream("/bird_small.png"))
    val exp = img.filter(new MarvinFilter {
      val plugin = new thirdparty.marvin.image.color.Thresholding()
    })

    val f = img.filter(thirdparty.marvin_scala.Thresholding())
    printComparaison(f, exp, "Thresholding")
    printComparaison(f, exp, img)
    writeAndCompare("threshold", (f, exp))
    assert(f ===  exp)
  }

  def testFilterOn(originalPath: String, filter: Filter, expectedPath: String, verbose: Boolean = false): (Image, Image) = {
    val filtered = Image(getClass.getResourceAsStream(originalPath)).filter(filter)
    val expected = Image(getClass.getResourceAsStream(expectedPath))
    printComparaison(filtered, expected, expectedPath, verbose)
    (filtered, expected)
  }

  def printComparaison(img1: Image, img2: Image, img3: Image) : Unit = {
    val toGray: RGBColor => Int = thirdparty.marvin_scala.MarvinGrayScaleFilter.toGray(_)

    for(x <- 0 until img1.width; y <- 0 until img1.height){
      val cf = img1.raster.read(x, y).toRGB
      val ce = img2.raster.read(x, y).toRGB
      if(cf != ce)
        println((cf, ce, toGray(img3.raster.read(x, y))))
    }
  }

  def printComparaison(img1: Image, img2: Image, name: String, verbose: Boolean = false) : Unit = {
    var count = 0
    var err = 0

    def error(rgb1: RGBColor, rgb2: RGBColor) = {
      math.abs(rgb1.red - rgb2.red) +
        math.abs(rgb1.green - rgb2.green) +
        math.abs(rgb1.blue - rgb2.blue) +
        math.abs(rgb1.alpha - rgb2.alpha)
    }

    for(x <- 0 until img1.width; y <- 0 until img1.height){
      val cf = img1.raster.read(x, y).toRGB
      val ce = img2.raster.read(x, y).toRGB
      if(cf != ce){
        if(verbose)
          println(((cf, ce), x, y))
        count += 1
        err += error(cf, ce)
        //        assert(math.abs(cf - ce) <= 1)
      }
    }
    val missed = count.toFloat /( img1.width * img1.height * 4) * 100
    println(s"Got $missed% of wrong channel values in $name")
    println(s"Average error of ${err.toFloat / count}")
  }

  def writeAndCompare(path: String, f_exp: (Image, Image)): Unit = {
    f_exp._1.write(path + ".png")
    f_exp._2.write(path + "_expected.png")
    f_exp._1.filter(ErrorSpotterFilter(f_exp._2, 50)).write(path + "_errors.png")
  }
}
