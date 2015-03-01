package com.sksamuel.scrimage.filter

import com.sksamuel.scrimage.{ Filter, Image, RGBColor }
import org.scalatest.{ FunSuite, OneInstancePerTest }

/** @author Stephen Samuel */
class LensBlurFilterTest extends FunSuite with OneInstancePerTest {

  ignore("LensBlurFilter output matches expected") {
    val (f, exp) = testFilterOn("/bird_small.png", LensBlurFilter(), "/bird_lens_blur.png")
    writeAndCompare("blured", f, exp)
    assert(f === exp)
  }

  test("RippleFilter with Triangle output matches expected") {
    val (f, exp) = testFilterOn("/bird_small.png", RippleFilter(RippleType.Triangle), "/bird_ripple_triangle.png")
    writeAndCompare("ripple_triangle", f, exp)
    assert(f === exp)
  }

  test("RippleFilter with Sawtooth output matches expected") {
    val (f, exp) = testFilterOn("/bird_small.png", RippleFilter(RippleType.Sawtooth), "/bird_ripple_sawtooth.png")
    writeAndCompare("ripple_sawtooth", f, exp)
    assert(f === exp)
  }

  ignore("VintageFilter output matches expected") {
    val (f, exp) = testFilterOn("/bird_small.png", VintageFilter, "/com/sksamuel/scrimage/filters/bird_small_vintage.png")
    writeAndCompare("vintage", f, exp)
    assert(f === exp)
  }

  ignore("SummerFilter output matches expected") {
    val (f, exp) = testFilterOn("/bird_small.png", SummerFilter(), "/com/sksamuel/scrimage/filters/bird_small_summer.png")
    writeAndCompare("summer", f, exp)
    assert(f === exp)
  }

  test("CrystallizeFilter output matches expected") {
    val (f, exp) = testFilterOn("/bird_small.png", CrystallizeFilter(randomness = 0.2), "/com/sksamuel/scrimage/filters/bird_small_crystallize.png")
    writeAndCompare("crystallize", f, exp)
    assert(f === exp)
  }

  test("CellularFilter output matches expected") {
    val img = Image(getClass.getResourceAsStream("/bird_small.png"))
    val exp = img.filter(new thirdparty.jhlabs.image.CellularFilter())
    val f = img.filter(CellularFilter())
    writeAndCompare("cellular", f, exp)
    assert(f === exp)
  }

  test("CristallizeFilter randomness 0.4 output matches expected") {
    val img = Image(getClass.getResourceAsStream("/bird_small.png"))
    val filter = new thirdparty.jhlabs.image.CrystallizeFilter()
    filter.setRandomness(0.4f)
    val exp = img.filter(filter)
    val f = img.filter(CrystallizeFilter(randomness = 0.4))
    writeAndCompare("crystallizeRand", f, exp)
    assert(f === exp)
  }

  test("CellularFilter use color output matches expected") {
    val img = Image(getClass.getResourceAsStream("/bird_small.png"))
    val filter = new thirdparty.jhlabs.image.CellularFilter()
    filter.useColor = true
    val exp = img.filter(filter)
    val f = img.filter(CellularFilter(useColor = true))
    writeAndCompare("cellularColor", f, exp)
    assert(f === exp)
  }

  test("DespeckleFilter output matches expected") {
    val img = Image(getClass.getResourceAsStream("/bird_small.png"))
    val filter = new thirdparty.jhlabs.image.DespeckleFilter()
    val exp = img.filter(filter)
    val f = img.filter(DespeckleFilter)
    writeAndCompare("despeckle", f, exp)
    assert(f === exp)
  }

  test("DiffuseFilter output matches expected") {
    val (f, exp) = testFilterOn("/bird_small.png", DiffuseFilter(), "/com/sksamuel/scrimage/filters/bird_small_diffuse.png")
    writeAndCompare("diffuse", f, exp)
    assert(f === exp)
  }

  test("OffsetFilter output matches expected") {
    val (f, exp) = testFilterOn("/bird_small.png", OffsetFilter(40, 60), "/com/sksamuel/scrimage/filters/bird_small_offset.png")
    writeAndCompare("offset", f, exp)
    assert(f === exp)
  }

  test("Correct scaling, factor: 1") {
    val exp = Image(getClass.getResourceAsStream("/bird_small.png"))
    val f = exp.scaleTo(exp.width, exp.height)
    writeAndCompare("scaled", f, exp)
    assert(f === exp)
  }

  ignore("Correct scaling, factor: 1/2") {
    val big = Image(getClass.getResourceAsStream("/bird.jpg"))
    val small = Image(getClass.getResourceAsStream("/bird_small.png"))
    val f = big.scaleTo(small.width, small.height)
    writeAndCompare("scaled_down", f, small)
    assert(f === small)
  }

  test("ShearFilter outputs something") {
    val img = Image(getClass.getResourceAsStream("/bird_small.png"))
    val f = ShearFilter(0.1, 0.5)(img)
    f.write("shear.png")
  }

  test("TwirlFilter output matches expected") {
    val (f, exp) = testFilterOn("/bird_small.png", TwirlFilter(150), "/com/sksamuel/scrimage/filters/bird_small_twirl.png")
    writeAndCompare("twirl", f, exp)
    assert(f === exp)
  }

  test("WaterFilter output matches expected") {
    val img = Image(getClass.getResourceAsStream("/bird_small.png"))
    val radius = 100
    val waveLength = 30f
    val filter = new thirdparty.jhlabs.image.WaterFilter()
    filter.setRadius(radius)
    filter.setWavelength(waveLength)
    val exp = img.filter(filter)
    val f = img.filter(WaterFilter(radius, waveLength))
    writeAndCompare("water", f, exp)
    assert(f === exp)
  }

  test("SwimFilter output matches expected") {
    val (f, exp) = testFilterOn("/bird_small.png", SwimFilter(), "/com/sksamuel/scrimage/filters/bird_small_swim.png")
    writeAndCompare("swim", f, exp)
    assert(f === exp)
  }

  // test("SwimFilter stretch output matches expected") {
  //   val img = Image(getClass.getResourceAsStream("/bird_small.png"))
  //   val amount = 6f
  //   val stretch = 2f
  //   val scale = 64
  //   val filter = new thirdparty.jhlabs.image.SwimFilter()
  //   filter.setStretch(stretch)
  //   filter.setAmount(amount)
  //   filter.setScale(scale)
  //   val exp = img.filter(filter)
  //   val f = img.filter(new SwimFilter(stretch = stretch, amount = amount, scale = scale))
  //   writeAndCompare("swim_stretch", f, exp)
  //   assert(f === exp)
  // }

  // test("PinchFilter output matches expected") {
  //   val img = Image(getClass.getResourceAsStream("/bird_small.png"))

  //   val filter = new thirdparty.jhlabs.image.PinchFilter()
  //   val exp = img.filter(filter)
  //   val f = img.filter(PinchFilter())
  //   writeAndCompare("pinch", f, exp)
  //   assert(f === exp)
  // }

  // test("PageCurlFilter output matches expected") {
  //   val img = Image(getClass.getResourceAsStream("/bird_small.png"))
  //   val radius = 10
  //   val transition = 100
  //   val filter = new thirdparty.jhlabs.image.CurlFilter()
  //   filter.setRadius(radius)
  //   filter.setTransition(transition)
  //   val exp = img.filter(filter)
  //   val f = img.filter(PageCurlFilter(radius = radius, transition = transition))
  //   writeAndCompare("page_curl", f, exp)
  //   assert(f === exp)
  // }

  test("OldPhotoFilter output matches expected") {
    val (f, exp) = testFilterOn("/bird_small.png", OldPhotoFilter, "/com/sksamuel/scrimage/filters/bird_small_oldphoto.png")
    writeAndCompare("old_photo", f, exp)

    val daisy = Image.fromFile("daisy.png")
    val daisy2 = Image.fromFile("daisy_expected.png")
    writeAndCompare("daisy", daisy, daisy2)
    assert(f === exp)
  }

  def testFilterOn(originalPath: String, filter: Filter, expectedPath: String, verbose: Boolean = false): (Image, Image) = {
    val filtered = Image(getClass.getResourceAsStream(originalPath)).filter(filter)
    val expected = Image(getClass.getResourceAsStream(expectedPath))
    printComparaison(filtered, expected, expectedPath, verbose)
    (filtered, expected)
  }

  def printComparaison(img1: Image, img2: Image, img3: Image): Unit = {
    val toGray: RGBColor => Int = thirdparty.marvin_scala.MarvinGrayScaleFilter.toGray

    for (x <- 0 until img1.width; y <- 0 until img1.height) {
      val cf = img1.raster.read(x, y).toRGB
      val ce = img2.raster.read(x, y).toRGB
      if (cf != ce)
        println((cf, ce, toGray(img3.raster.read(x, y))))
    }
  }

  def printComparaison(img1: Image, img2: Image, name: String, verbose: Boolean = false): Unit = {
    var count = 0
    var err = 0

    def error(rgb1: RGBColor, rgb2: RGBColor) = {
      math.abs(rgb1.red - rgb2.red) +
        math.abs(rgb1.green - rgb2.green) +
        math.abs(rgb1.blue - rgb2.blue) +
        math.abs(rgb1.alpha - rgb2.alpha)
    }

    for (x <- 0 until img1.width; y <- 0 until img1.height) {
      val cf = img1.raster.read(x, y).toRGB
      val ce = img2.raster.read(x, y).toRGB
      if (cf != ce) {
        if (verbose)
          println(((cf, ce), x, y))
        count += 1
        err += error(cf, ce)
        //        assert(math.abs(cf - ce) <= 1)
      }
    }
    val missed = count.toFloat / (img1.width * img1.height) * 100
    println(s"Got $missed% of wrong pixels in $name")
    println(s"Average error of ${err.toFloat / count / 4}")
  }

  def writeAndCompare(path: String, f: Image, exp: Image): Unit = {
    f.write(path + ".png")
    exp.write(path + "_expected.png")
    f.filter(ErrorSpotterFilter(exp, 50)).write(path + "_errors.png")
  }
}
