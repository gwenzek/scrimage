package com.sksamuel.scrimage.filter

import com.sksamuel.scrimage._
import com.sksamuel.scrimage.filter.util._
/** Created by guw on 24/11/14.
  */
package object blender {

  object Blender {
    def apply(f: (Color, Color) => Color) = new LocalBlender {
      def blend(c1: Color, c2: Color): Color = f(c1, c2)
    }

    def rgb(f: (RGBColor, RGBColor) => Color) = new RGBBlender {
      def blendRGB(c1: RGBColor, c2: RGBColor): Color = f(c1, c2)
    }

    def apply(
      fRed: (Int, Int) => Int,
      fGreen: (Int, Int) => Int,
      fBlue: (Int, Int) => Int,
      fAlpha: (Int, Int) => Int) = new RGBBlender {
      def blendRGB(c1: RGBColor, c2: RGBColor): Color =
        Color(fRed(c1.red, c2.red), fGreen(c1.green, c2.green), fBlue(c1.blue, c2.blue))
    }

    def apply(
      fRed: (Int, Int) => Int,
      fGreen: (Int, Int) => Int,
      fBlue: (Int, Int) => Int): RGBBlender =
      apply(fRed, fGreen, fBlue, (a1, a2) => math.min(255, a1 + a2))

    def hsl(
      fHue: (Float, Float) => Float,
      fSaturation: (Float, Float) => Float,
      fLightness: (Float, Float) => Float) = new HSLBlender {
      def blendHSL(c1: HSLColor, c2: HSLColor) =
        new HSLColor(fHue(c1.hue, c2.hue),
          fSaturation(c1.saturation, c2.saturation),
          fLightness(c1.lightness, c2.lightness),
          math.min(1f, c1.alpha + c2.alpha))
    }

    def byChannel(f: (Int, Int) => Int) = Blender(f, f, f)

    // mathematical composition
    def add = byChannel((c1, c2) => math.min(255, c1 + c2))

    def average = byChannel((c1, c2) => (c1 + c2) >> 1)

    def difference = byChannel((c1, c2) => math.abs(c1 - c2))

    def multiply = byChannel((c1, c2) => (c1 * c2) >> 8)

    def negation = byChannel((c1, c2) => 255 - math.abs(255 - c1 - c2))

    def subtract = byChannel((c1, c2) => math.max(0, c1 + c2 - 256))

    // TODO check effects of this ones
    def red = Blender.rgb((c1, c2) =>
      Color(c1.red, c2.green, c2.blue, math.min(255, c1.alpha + c2.alpha))
    )
    def green = Blender.rgb((c1, c2) =>
      Color(c2.red, c1.green, c2.blue, math.min(255, c1.alpha + c2.alpha))
    )
    def blue = Blender.rgb((c1, c2) =>
      Color(c2.red, c2.green, c1.blue, math.min(255, c1.alpha + c2.alpha))
    )

    // HSL blending
    def color = hsl((h, _) => h, (s, _) => s, (_, l) => l)

    def hue = hsl((h, _) => h, (_, s) => s, (_, l) => l)
    def saturation = hsl((_, h) => h, (s, _) => s, (_, l) => l)
    def luminosity = hsl((_, h) => h, (_, s) => s, (l, _) => l)

    def colorBurn =
      byChannel((c1, c2) => if (c1 == 0) 0 else math.max(0, 255 - ((255 - c2) << 8) / c1))

    def inverseColorBurn =
      byChannel((c1, c2) => if (c2 == 0) 0 else math.max(0, 255 - ((255 - c1) << 8) / c2))

    def colorDodge =
      byChannel((c1, c2) => if (c1 == 255) 255 else math.min(255, (c2 << 8) / (255 - c1)))

    def inverseColorDodge =
      byChannel((c1, c2) => if (c2 == 255) 255 else math.min(255, (c1 << 8) / (255 - c2)))

    def darken = byChannel(math.min)

    def lighten = byChannel(math.max)

    def exclusion = byChannel((c1, c2) => c1 + c2 - (c1 * c2 >> 7))

    def freeze =
      byChannel((c1, c2) => if (c1 == 0) 0 else math.max(0, 255 - ((255 - c2) * (255 - c2)) / c1))

    def heat =
      byChannel((c1, c2) => if (c2 == 0) 0 else math.max(0, 255 - (255 - c1) * (255 - c1) / c2))

    def glow =
      byChannel((c1, c2) => if (c2 == 255) 255 else math.min(255, c1 * c1 / (255 - c2)))

    def hardLight = byChannel((c1, c2) =>
      if (c1 < 128) c1 * c2 >> 7 else 255 - ((255 - c1) * (255 - c2) >> 7)
    )

    /** reversed hardlight */
    def overlay = byChannel((c1, c2) =>
      if (c2 < 128) c1 * c2 >> 7 else 255 - ((255 - c1) * (255 - c2) >> 7)
    )

    def reflect = byChannel((c1, c2) =>
      if (c1 == 255) 255 else math.min(255, c2 * c2 / (255 - c1))
    )

    def screen = byChannel((c1, c2) => 255 - ((255 - c1) * (255 - c2) >> 8))

    def softLight = byChannel((c1, c2) =>
      if (c2 < 128) c1 * c2 >> 7 else 255 - ((255 - c2) * (255 - c1) >> 7)
    )

    def softBurn = byChannel((c1, c2) =>
      if (c1 + c2 < 256)
        if (c2 == 255) 255 else math.min(255, (c1 << 7) / (255 - c2))
      else math.max(0, 255 - ((255 - c2) << 7) / c1)
    )

    //TODO shouldn't it be inverseSoftBurn ?
    def softDodge = byChannel((c1, c2) =>
      if (c1 + c2 < 256)
        if (c1 == 255) 255 else math.min(255, (c2 << 7) / (255 - c1))
      else math.max(0, 255 - ((255 - c1) << 7) / c2)
    )

    def stamp = byChannel((c1, c2) => math.max(0, math.min(255, c2 + 2 * c1 - 256)))

    trait AbstractBlender extends Blender {
      def blend(img: Image, base: Image, alpha: Float): Image =
        blend(img, base, defaultDst(base), alpha)

      def defaultDst(base: Image): Image

      def blend(img: Image, base: Image, dst: Image, alpha: Float): Image
    }

    trait LineByLineBlender extends AbstractBlender {
      def treatLine(y: Int, src: Raster, base: Raster, dst: Raster, alpha: Float): Unit

      def blend(img: Image, base: Image, dst: Image, alpha: Float) = {
        (0 until math.min(img.height, base.height)).par.foreach(
          treatLine(_, img.raster, base.raster, dst.raster, alpha))
        dst
      }
    }

    /* The pixels can be computed independently */
    trait PixelByPixelBlender extends LineByLineBlender with CopyingFilter {
      def blend(x: Int, y: Int, src: Raster, base: Raster): Color

      def mixAlpha(result: RGBColor, base: RGBColor, alpha: Float) = {
        Color((base.red + (result.red - base.red) * alpha).toInt,
          (base.green + (result.green - base.green) * alpha).toInt,
          (base.blue + (result.blue - base.blue) * alpha).toInt,
          (base.alpha + (result.alpha - base.alpha) * alpha).toInt)
      }

      def treatLine(y: Int, src: Raster, base: Raster, dst: Raster, alpha: Float): Unit = {
        var x = 0
        while (x < math.min(src.width, base.width)) {
          dst.write(x, y, mixAlpha(blend(x, y, src, base), base.read(x, y), alpha))
          x += 1
        }
      }
    }

    trait InPlaceBlender extends AbstractBlender {
      def in_place(img: Image, base: Image, alpha: Float) =
        blend(img, base, base, alpha)
    }

    /* The new pixel depends only on the previous one */
    trait LocalBlender extends PixelByPixelBlender with InPlaceBlender {
      def blend(x: Int, y: Int, src: Raster, base: Raster): Color =
        blend(src.read(x, y), base.read(x, y))

      def blend(c1: Color, c2: Color): Color
    }

    trait RGBBlender extends LocalBlender {
      def blendRGB(c1: RGBColor, c2: RGBColor): Color

      def blend(c1: Color, c2: Color): Color = blendRGB(c1, c2)
    }

    trait HSLBlender extends LocalBlender {
      def blendHSL(c1: HSLColor, c2: HSLColor): HSLColor

      def blend(c1: Color, c2: Color): Color =
        blendHSL(Color.toHSL(c1), Color.toHSL(c2))
    }
  }
}
