//package com.sksamuel.scrimage
//
//import org.scalatest.{ Matchers, BeforeAndAfter, FunSuite }
//
///** @author Stephen Samuel */
//
//class RasterTest extends FunSuite with BeforeAndAfter with Matchers {
//
//  val in = getClass.getResourceAsStream("/com/sksamuel/scrimage/bird.jpg")
//  val image = Image(in)
//
//  test("getRGB returns the correct pixels with default") {
//    val pixels = Array.ofDim[Int](100)
//    val awtPixels = Array.ofDim[Int](100)
//    image.raster.getRGB(0, 0, 10, 10, pixels)
//    image.awt.getRGB(0, 0, 10, 10, awtPixels, 0, 10)
//    assert(pixels.toList == awtPixels.toList)
//  }
//
//  test("getRGB returns the correct pixels with shifted start") {
//    val pixels = Array.ofDim[Int](100)
//    val awtPixels = Array.ofDim[Int](100)
//    image.raster.getRGB(4, 5, 10, 10, pixels)
//    image.awt.getRGB(4, 5, 10, 10, awtPixels, 0, 10)
//    assert(pixels.toList == awtPixels.toList)
//  }
//
//  test("getRGB returns the correct pixels with offset") {
//    val pixels = Array.ofDim[Int](150)
//    val awtPixels = Array.ofDim[Int](150)
//    image.raster.getRGB(0, 0, 10, 10, pixels, 50, 10)
//    image.awt.getRGB(0, 0, 10, 10, awtPixels, 50, 10)
//    assert(pixels.toList == awtPixels.toList)
//  }
//
//  test("getRGB returns the correct pixels with scanline") {
//    val pixels = Array.ofDim[Int](300)
//    val awtPixels = Array.ofDim[Int](300)
//    image.raster.getRGB(0, 0, 10, 10, pixels, 50, 20)
//    image.awt.getRGB(0, 0, 10, 10, awtPixels, 50, 20)
//    assert(pixels.toList == awtPixels.toList)
//  }
//
//  test("setRGB writes the correct pixels with default") {
//    val blank = Image.filled(200, 200)
//    val pixels = image.raster.getRGB(0, 0, image.width, image.height)
//    blank.raster.setRGB(0, 0, 10, 10, pixels)
//    blank.awt.setRGB(0, 0, 10, 10, pixels, 0, 10)
//
//    val rasterPixels = blank.raster.getRGB(0, 0, 200, 200)
//    val awtPixels = blank.awt.getRGB(0, 0, 200, 200, null, 0, 200)
//
//    assert(rasterPixels.toList == awtPixels.toList)
//  }
//
//  test("setRGB writes the correct pixels with shifted start") {
//    val blank = Image.filled(200, 200)
//    val pixels = image.raster.getRGB(0, 0, image.width, image.height)
//    blank.raster.setRGB(4, 5, 180, 180, pixels, 10200, image.width)
//    blank.awt.setRGB(4, 5, 180, 180, pixels, 10200, image.width)
//
//    val rasterPixels = blank.raster.getRGB(0, 0, 200, 200)
//    val awtPixels = blank.awt.getRGB(0, 0, 200, 200, null, 0, 200)
//    blank.write(new java.io.File("setted.png"))
//    assert(rasterPixels.toList == awtPixels.toList)
//  }
//
//  test("setRGB writes the correct pixels with offset") {
//    val blank = Image.filled(200, 200)
//    val pixels = image.raster.getRGB(0, 0, image.width, image.height)
//    blank.raster.setRGB(0, 0, 10, 10, pixels, 500, 10)
//    blank.awt.setRGB(0, 0, 10, 10, pixels, 500, 10)
//
//    val rasterPixels = blank.raster.getRGB(0, 0, 200, 200)
//    val awtPixels = blank.awt.getRGB(0, 0, 200, 200, null, 0, 200)
//    assert(rasterPixels.toList == awtPixels.toList)
//  }
//
//  test("setRGB writes the correct pixels with scanline") {
//    val blank = Image.filled(200, 200)
//    val pixels = image.raster.getRGB(0, 0, image.width, image.height)
//    blank.raster.setRGB(0, 0, 200, 200, pixels, 500, 20)
//    blank.awt.setRGB(0, 0, 200, 200, pixels, 500, 20)
//
//    val rasterPixels = blank.raster.getRGB(0, 0, 200, 200)
//    val awtPixels = blank.awt.getRGB(0, 0, 200, 200, null, 0, 200)
//    assert(rasterPixels.toList == awtPixels.toList)
//  }
//}
