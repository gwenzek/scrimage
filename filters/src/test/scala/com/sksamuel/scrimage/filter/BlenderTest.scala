package com.sksamuel.scrimage.filter

import java.awt.Graphics2D

import com.sksamuel.scrimage.{ Image, Blender }
import org.scalatest.FunSuite
import thirdparty.romainguy.BlendComposite
import thirdparty.romainguy.BlendComposite.BlendingMode

class BlenderTest extends FunSuite {

  val image = Image(getClass.getResourceAsStream("/bird_small.png"))
  val applicative = Image(getClass.getResourceAsStream("/palm.jpg")).scaleTo(image.width, image.height)
  val alpha = 0.6f

  def blend(src: Image, applicative: Image, mode: BlendingMode, alpha: Float) = {
    val buff = src.toBufferedImage
    val g2 = buff.getGraphics.asInstanceOf[Graphics2D]
    g2.setComposite(BlendComposite.getInstance(mode, alpha))
    g2.drawImage(applicative.awt, 0, 0, null)
    g2.dispose()
    Image(buff)
  }

  def compare(blender: Blender, mode: BlendingMode) = {
    val oldVersion = blend(image, applicative, mode, alpha)
    val newVersion = applicative.blendTo(image, blender, alpha)
    (oldVersion, newVersion)
  }

  test("Average blenders correspond") {
    val (o, n) = compare(Blenders.average, BlendingMode.AVERAGE)
    assert(o == n)
  }

  test("Multiply blenders correspond") {
    val (o, n) = compare(Blenders.multiply, BlendingMode.MULTIPLY)
    assert(o == n)
  }

  test("Screen blenders correspond") {
    val (o, n) = compare(Blenders.screen, BlendingMode.SCREEN)
    assert(o == n)
  }

  test("Darken blenders correspond") {
    val (o, n) = compare(Blenders.darken, BlendingMode.DARKEN)
    assert(o == n)
  }

  test("Lighten blenders correspond") {
    val (o, n) = compare(Blenders.lighten, BlendingMode.LIGHTEN)
    assert(o == n)
  }

  test("Overlay blenders correspond") {
    val (o, n) = compare(Blenders.overlay, BlendingMode.OVERLAY)
    assert(o == n)
  }

  test("Hardlight blenders correspond") {
    val (o, n) = compare(Blenders.hardLight, BlendingMode.HARD_LIGHT)
    assert(o == n)
  }

  test("Difference blenders correspond") {
    val (o, n) = compare(Blenders.difference, BlendingMode.DIFFERENCE)
    assert(o == n)
  }

  test("Negation blenders correspond") {
    val (o, n) = compare(Blenders.negation, BlendingMode.NEGATION)
    assert(o == n)
  }

  test("Exclusion blenders correspond") {
    val (o, n) = compare(Blenders.exclusion, BlendingMode.EXCLUSION)
    assert(o == n)
  }

  test("Colordodge blenders correspond") {
    val (o, n) = compare(Blenders.colorDodge, BlendingMode.COLOR_DODGE)
    assert(o == n)
  }

  test("InverseColorDodge blenders correspond") {
    val (o, n) = compare(Blenders.inverseColorDodge, BlendingMode.INVERSE_COLOR_DODGE)
    assert(o == n)
  }

  test("Softdodge blenders correspond") {
    val (o, n) = compare(Blenders.softDodge, BlendingMode.SOFT_DODGE)
    assert(o == n)
  }

  test("Colorburn blenders correspond") {
    val (o, n) = compare(Blenders.colorBurn, BlendingMode.COLOR_BURN)
    assert(o == n)
  }

  test("InverseColorBurn blenders correspond") {
    val (o, n) = compare(Blenders.inverseColorBurn, BlendingMode.INVERSE_COLOR_BURN)
    assert(o == n)
  }

  test("Softburn blenders correspond") {
    val (o, n) = compare(Blenders.softBurn, BlendingMode.SOFT_BURN)
    assert(o == n)
  }

  test("Reflect blenders correspond") {
    val (o, n) = compare(Blenders.reflect, BlendingMode.REFLECT)
    assert(o == n)
  }

  test("Glow blenders correspond") {
    val (o, n) = compare(Blenders.glow, BlendingMode.GLOW)
    assert(o == n)
  }

  test("Freeze blenders correspond") {
    val (o, n) = compare(Blenders.freeze, BlendingMode.FREEZE)
    assert(o == n)
  }

  test("Heat blenders correspond") {
    val (o, n) = compare(Blenders.heat, BlendingMode.HEAT)
    assert(o == n)
  }

  test("Add blenders correspond") {
    val (o, n) = compare(Blenders.add, BlendingMode.ADD)
    assert(o == n)
  }

  test("Subtract blenders correspond") {
    val (o, n) = compare(Blenders.subtract, BlendingMode.SUBTRACT)
    assert(o == n)
  }

  test("Stamp blenders correspond") {
    val (o, n) = compare(Blenders.stamp, BlendingMode.STAMP)
    assert(o == n)
  }

  test("Red blenders correspond") {
    val (o, n) = compare(Blenders.red, BlendingMode.RED)
    assert(o == n)
  }

  test("Green blender correspond to old Blue blender") {
    val (o, n) = compare(Blenders.green, BlendingMode.BLUE)
    assert(o == n)
  }

  test("Blue blenders correspond to old Gren Blenders") {
    val (o, n) = compare(Blenders.blue, BlendingMode.GREEN)
    assert(o == n)
  }

  test("Hue blenders correspond") {
    val (o, n) = compare(Blenders.hue, BlendingMode.HUE)
    o.write("hue_old.png")
    n.write("hue_new.png")
    assert(o == n)
  }

  test("Saturation blenders correspond") {
    val (o, n) = compare(Blenders.saturation, BlendingMode.SATURATION)
    o.write("saturation_old.png")
    n.write("saturation_new.png")
    assert(o == n)
  }

  test("Color blenders correspond") {
    val (o, n) = compare(Blenders.color, BlendingMode.COLOR)
    o.write("color_old.png")
    n.write("color_new.png")
    assert(o == n)
  }

  test("Luminosity blenders correspond") {
    val (o, n) = compare(Blenders.luminosity, BlendingMode.LUMINOSITY)
    o.write("luminosity_old.png")
    n.write("luminosity_new.png")
    assert(o == n)
  }
}
