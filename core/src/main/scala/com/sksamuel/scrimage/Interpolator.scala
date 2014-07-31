/*
   Copyright 2013 Guillaume Wenzek

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

package com.sksamuel.scrimage

/**
 * An Interpolator is an abstraction over an image that allows access to floating pixels.
 * Interpolators caan be used to scale an image.
 *
 * Usually to interpolate a floating pixel you choose a bunch of surrounding pixels,
 * (with the method extract) and then you apply a function component by component on those pixels.
 * This last part happens in the method interpolateFrom.
 */

trait Interpolator {
    val raster: Raster

    def extract(x: Float, y: Float): Array[Int]
    def interpolateFrom(x: Float, y: Float)(extracted: Array[Int]): Int

    def interpolate(x: Float, y: Float): Int = {
        val extracted : Array[Array[Int]] = extract(x, y).map(raster.listComponents)
        def slice(i: Int): Array[Int] = extracted.map(_.apply(i))
        raster.fromComponents(
            Array.range(0, extracted(0).length)
            .map(slice)
            .map(interpolateFrom(x, y)))
    }

    def scaleTo(targetWidth: Int, targetHeight: Int): Image = {
        val scaled = raster.rasterOfSize(targetWidth, targetHeight)
        val ratioX = 1f * raster.width / targetWidth
        val ratioY = 1f * raster.height / targetHeight
        for(x <- 0 until targetWidth; y <- 0 until targetHeight){
            scaled.write(x, y, interpolate(ratioX * x, ratioY * y))
        }
        new Image(scaled)
    }

    protected def coerce(c: Float, min: Int = 0, max: Int = 255) : Int = {
        if(c >= max) max
        else if(c <= min) min
        else c.toInt
    }
}

/**
 * This interpolator returns the closest real pixel.
 */
class NNInterpolator(val image: Image) extends Interpolator {
    val raster = image.raster

    def extract(x: Float, y: Float) = Array(raster.pixel(x.toInt, y.toInt))

    def interpolateFrom(x: Float, y: Float)(extracted: Array[Int]) = extracted(0)

    override def interpolate(x: Float, y: Float): Int = raster.pixel(x.toInt, y.toInt)
}

/**
 * This interpolator use the 4 closest pixels and use a bilinear interpolation scheme.
 */
class BilinearInterpolator(val image: Image) extends Interpolator {
    val raster = image.raster

    def extract(x: Float, y: Float) = {
        val x0 = coerce(x, 0, raster.width - 1)
        val x1 = coerce(x+1, 0, raster.width - 1)
        val y0 = coerce(y, 0, raster.height - 1)
        val y1 = coerce(y+1, 0, raster.height - 1)
        Array(raster.pixel(x0, y0),
              raster.pixel(x1, y0),
              raster.pixel(x0, y1),
              raster.pixel(x1, y1))
    }

    def interpolateFrom(x: Float, y: Float)(extracted: Array[Int]) = {
        val x1 = x - x.toInt
        val y1 = y - y.toInt
        coerce(
          (1 - x1) * (1 - y1) * extracted(0) +
          x1 * (1 - y1) * extracted(1) +
          (1 - x1) * y1 * extracted(2) +
          x1 * y1 * extracted(3)
        )
    }
}

/**
 * This interpolator use the 16 closest pixels and use a bicubic interpolation scheme.
 */
class BicubicInterpolator(val image: Image) extends Interpolator {
    val raster = image.raster

    def extract(x: Float, y: Float) = {
        val xs = Array(-1, 0, 1, 2).map(dx => coerce(x-0.5f+dx, 0, raster.width - 1))
        val ys = Array(-1, 0, 1, 2).map(dy => coerce(y-0.5f+dy, 0, raster.height - 1))
        ys.flatMap(y => xs.map(x => raster.pixel(x, y)))
    }

    def interpolateFrom(x: Float, y: Float)(extracted: Array[Int]) = {
        val x1 = x - (x-0.5f).toInt - 0.5f
        val x2 = x1 * x1
        val x3 = x2 * x1

        val b_1 = q(x1, x2, x3)(extracted(0), extracted(1), extracted(2), extracted(3))
        val b0 = q(x1, x2, x3)(extracted(4), extracted(5), extracted(6), extracted(7))
        val b1 = q(x1, x2, x3)(extracted(8), extracted(9), extracted(10), extracted(11))
        val b2 = q(x1, x2, x3)(extracted(12), extracted(13), extracted(14), extracted(15))

        val y1 = y - (y-0.5f).toInt - 0.5f
        val y2 = y1 * y1
        val y3 = y2 * y1

        coerce(q(y1, y2, y3)(b_1, b0, b1, b2))
    }

    def q(x1: Float, x2: Float, x3: Float)(a_1: Float, a0: Float, a1: Float, a2: Float) = {
        // (1f*a0 + 0.5f*x1*(a1-a_1)
        //     + x2*(a_1 - 2.5f*a0 + 2f*a1 - 0.5f*a2)
        //     + x3*(0.5f*(a2-a_1) + 1.5f*(a0-a1)))
        ( (0.5f * x3 - x2 + 0.5f * x1) * a_1 +
          (2.5f * x3 - 3.5f * x2 + 1f) * a0 +
          (-2.5f * x3 + 4f*x2 - 0.5f*x1) * a1 +
          (-0.5f * x3 + 0.5f * x2) * a2
        )
    }
}
