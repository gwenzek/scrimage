package com.sksamuel.scrimage.filter

import com.sksamuel.scrimage.{ Image, Filter, Color, RGBColor, YCbCrColor }
// import com.sksamuel.scrimage.Color._
import com.sksamuel.scrimage.filter.util._

import thirdparty.misc.DaisyFilter

/** @author Stephen Samuel */
object OldPhotoFilter extends Filter {

  val film = Image(getClass.getResourceAsStream("/com/sksamuel/scrimage/filter/film1.jpg"))

  def apply(image: Image) = {

    val gray = GrayscaleFilter.filterToARGB(image)
    // gray.write("gray.png")
    val contrast = gray.filter(new YCbCrContrastFilter(0, 15))
    val red = contrast.filter(new RedShift())

    val daisy = Blenders.screen(red, contrast, alpha = 0.6f)
    // daisy.write("daisy.png")

    // val daisy_exp = Image(new DaisyFilter().half_daisy_2(gray.toBufferedImage))
    // daisy_exp.write("daisy_expected.png")

    val film2 = film.scaleTo(image.width, image.height)
    // film2.write("film.png")
    Blenders.inverseColorDodge(film2, daisy, alpha = 0.3f)
  }

  class YCbCrContrastFilter(brightness: Double, contrast: Double, gamma: Double = 0.25) extends YCbCrMapper {

    private[this] val contr =
      if (contrast > 0)
        100 * math.pow(contrast - 1, 1 / gamma) / math.pow(100, 1 / gamma) + 1
      else if (contrast == 0)
        1
      else
        1 / ((100 * math.pow(1 - contrast, 1 / gamma) / math.pow(100, 1 / gamma)) + 1)

    private[this] val br =
      if (brightness > 0)
        (100 * math.pow(brightness, 1 / gamma) / math.pow(100, 1 / gamma)) + 1
      else if (brightness == 0)
        0
      else
        1 / ((100 * math.pow(-brightness, 1 / gamma) / math.pow(100, 1 / gamma)) + 1)

    println("scala brightness: " + br + " contrast: " + contr)

    def apply(c: YCbCrColor) = YCbCrColor(
      (c.Y + br - 127) * contr + 127,
      c.Cb * contr,
      c.Cr * contr,
      c.alpha
    )
  }

  class RedShift(frequence: Double = 2 * math.Pi / 1020) extends PixelMapperFilter {
    def apply(c: RGBColor) = Color(
      clamp(255 * math.sin(frequence * c.red)),
      c.green,
      clamp(255 - 255 * math.cos(frequence * c.blue)),
      c.alpha
    )
  }
}

// import java.awt.Graphics2D
// import thirdparty.romainguy.BlendComposite
// object OldPhotoFilter2 extends Filter {

//   val film = Image(getClass.getResourceAsStream("/com/sksamuel/scrimage/filter/film1.jpg"))

//   def apply(image: Image) = {

//     val buffered = image.toBufferedImage

//     val daisy = new DaisyFilter()
//     val filtered = daisy.filter(buffered)

//     Image(filtered).write("daisy.png")
//     film.write("film.png")

//     val g2 = buffered.getGraphics.asInstanceOf[Graphics2D]
//     g2.drawImage(filtered, 0, 0, null)
//     g2.dispose()

//     g2.setComposite(BlendComposite.getInstance(BlendComposite.BlendingMode.INVERSE_COLOR_DODGE, 0.30f))
//     g2.drawImage(film.scaleTo(image.width, image.height).awt, 0, 0, null)
//     g2.dispose()

//     Image(buffered)
//   }
// }

