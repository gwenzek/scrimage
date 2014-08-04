package com.sksamuel.scrimage

import org.scalatest.FunSuite
import thirdparty.mortennobel.{ResampleFilters, ResampleOp}


class InterpolatorTest extends FunSuite {
      // val colors = Array[Int](0xffff3f00, 0xffefff0f, 0xff004fff, 0xff1fffdf,
  //                         0xffefff0f, 0xff004fff, 0xff00008f, 0xffff3f00,
  //                         0xff7f0000, 0xff1fffdf, 0xffff3f00, 0xff004fff,
  //                         0xff00008f, 0xff004fff, 0xffefff0f, 0xff00008f
  //                         )

  // val colors = Array[Int](0xffff0000, 0xffdd0000, 0xffaa0000, 0xff990000,
  //                         0xffcc0000, 0xffbb0000, 0xff880000, 0xff660000,
  //                         0xff990000, 0xff770000, 0xff550000, 0xff330000,
  //                         0xff660000, 0xff440000, 0xff220000, 0xff000000
  //                       )

  val colors = Array[Int](0xffff0000, 0xff000000, 0xff000000, 0xffff0000,
                          0xffcc0000, 0xffbb0000, 0xff880000, 0xff660000,
                          0xff990000, 0xff770000, 0xff550000, 0xff330000,
                          0xff000000, 0xffff0000, 0xffff0000, 0xff000000
                        )

  val image = new Image(new IntARGBRaster(4, 4, colors))
  image.write(new java.io.File("small.png"))

  test("scale to with NearestNeighbor should works correctly") {
    val scaled = image.scaleTo(200, 200, ScaleMethod.FastScale)
    scaled.write(new java.io.File("small_nn.png"))
    assert(scaled.pixel(10, 10) == image.pixel(0, 0))
    assert(scaled.pixel(60, 55) == image.pixel(1, 1))
    assert(scaled.pixel(190, 120) == image.pixel(3, 2))
  }

  test("scale to with Bilinear should match Mortennobel") {
    val scaled = new BilinearInterpolator(image).scaleTo(200, 200)
    val op = new ResampleOp(Image.SCALE_THREADS, ResampleFilters.triangleFilter, 200, 200)
    val expected = Image(op.filter(image.awt, null))
    expected.write(new java.io.File("small_linear_expected.png"))
    scaled.write(new java.io.File("small_linear.png"))
    // for( (p0, p1) <- expected.raster.model.zip(scaled.raster.model)){
    //   assert(p0 == p1)
    // }
    assert(expected == scaled)

    val resized = scaled.scaleTo(4, 4, ScaleMethod.Bilinear)
    for( (p0, p1) <- image.raster.model.zip(resized.raster.model)){
      assert(p0 == p1)
    }
  }

  test("scale to with Bicubic should match Mortennobel") {
    val scaled = new BicubicInterpolator(image).scaleTo(200, 200)
    val op = new ResampleOp(Image.SCALE_THREADS, ResampleFilters.biCubicFilter, 200, 200)
    val expected = Image(op.filter(image.awt, null))
    expected.write(new java.io.File("small_cubic_expected.png"))
    scaled.write(new java.io.File("small_cubic.png"))
    assert(expected == scaled)

    // val resized = scaled.scaleTo(4, 4, ScaleMethod.Bicubic)
    // resized.write(new java.io.File("small_small_cubic.png"))
    // for( (p0, p1) <- image.raster.model.zip(resized.raster.model)){
    //   assert(p0 == p1)
    // }
  }

  val bigImage = Image(getClass.getResourceAsStream("/com/sksamuel/scrimage/bird.jpg"))

  test("scale to with Bilinear should match Mortennobel with bird.jpg") {
    val scaled = new BilinearInterpolator(bigImage).scaleTo(600, 400)
    val op = new ResampleOp(Image.SCALE_THREADS, ResampleFilters.triangleFilter, 600, 400)
    val expected = Image(op.filter(bigImage.awt, null))
    expected.write(new java.io.File("big_linear_expected.png"))
    scaled.write(new java.io.File("big_linear.png"))
    assert(expected == scaled)
  }

  test("scale to with Bicubic should still match Mortennobel with bird.jpg") {
    val scaled = new BicubicInterpolator(bigImage).scaleTo(600, 400)
    val op = new ResampleOp(Image.SCALE_THREADS, ResampleFilters.biCubicFilter, 600, 400)
    val expected = Image(op.filter(bigImage.awt, null))
    expected.write(new java.io.File("big_cubic_expected.png"))
    scaled.write(new java.io.File("big_cubic.png"))
    assert(expected == scaled)
  }
}
