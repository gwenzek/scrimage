/*
Copyright 2006 Jerry Huxtable

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

package thirdparty.jhlabs.image

import com.sksamuel.scrimage.{ Image, Raster }
import com.sksamuel.scrimage.geom._

/** A convenience class which implements those methods of BufferedImageOp which are rarely changed.
  */
abstract class AbstractImageOp {

  def createCompatibleDestImage(src: Image) = new Image(src.raster.mimic)
  def createCompatibleDestImage(src: Image, colorModel: Raster.RasterType) =
    new Image(Raster(src.width, src.height, colorModel))

  def getBounds2D(src: Image) = Rectangle(0, 0, src.width, src.height)

  /** A convenience method for getting ARGB pixels from an image. This tries to avoid the performance
    * penalty of Image.getRGB unmanaging the image.
    * @param image   a Image object
    * @param x       the left edge of the pixel block
    * @param y       the right edge of the pixel block
    * @param width   the width of the pixel arry
    * @param height  the height of the pixel arry
    * @param pixels  the array to hold the returned pixels. May be null.
    * @return the pixels
    * @see #setRGB
    */
  def getRGB(image: Image, x: Int, y: Int, width: Int, height: Int, pixels: Array[Int]) =
    image.raster.getRGB(x, y, width, height, pixels, 0, width)

  /** A convenience method for setting ARGB pixels in an image. This tries to avoid the performance
    * penalty of Image.setRGB unmanaging the image.
    * @param image   a Image object
    * @param x       the left edge of the pixel block
    * @param y       the right edge of the pixel block
    * @param width   the width of the pixel arry
    * @param height  the height of the pixel arry
    * @param pixels  the array of pixels to set
    * @see #getRGB
    */
  def setRGB(image: Image, x: Int, y: Int, width: Int, height: Int, pixels: Array[Int]) =
    image.raster.setRGB(x, y, width, height, pixels, 0, width)
}
