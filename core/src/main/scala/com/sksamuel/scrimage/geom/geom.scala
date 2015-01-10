package com.sksamuel.scrimage.geom

case class Point2D(x: Int, y: Int) {
  def +(dxdy: (Int, Int)) = Point2D(x + dxdy._1, y + dxdy._2)
  def -(dxdy: (Int, Int)) = Point2D(x - dxdy._1, y - dxdy._2)

  def *(s: Double) = Point2D((x * s).toInt, (y * s).toInt)
  def *(s: Float) = Point2D((x * s).toInt, (y * s).toInt)
  def *(s: Int) = Point2D(x * s, y * s)

  def rot(alpha: Double) = {
    if (alpha == 0.0) this
    else {
      val ca = math.cos(alpha)
      val sa = math.sin(alpha)
      Point2D((ca * x + sa * y).toInt, (ca * y - sa * x).toInt)
    }
  }

  def rotD(a: Int) = {
    if (a == 0 || a == 360) this
    else if (a == 90) Point2D(-y, x)
    else if (a == 180) Point2D(-x, -y)
    else if (a == 270) Point2D(y, -x)
  }

  def in(x0: Int, y0: Int, x1: Int, y1: Int) =
    x0 <= x && x < x1 && y0 <= y && y < y1

  def in(x0y0x1y1: (Int, Int, Int, Int)) = {
    val (x0, y0, x1, y1) = x0y0x1y1
    x0 <= x && x < x1 && y0 <= y && y < y1
  }

  def scale(sx: Double, sy: Double) = Point2D((x * sx).toInt, (y * sy).toInt)

  def toDouble = Point2D(x.toDouble, y.toDouble)
}

class Point2DDouble(val x: Double, val y: Double) {
  def +(dxdy: (Double, Double)) = Point2D(x + dxdy._1, y + dxdy._2)
  def -(dxdy: (Double, Double)) = Point2D(x - dxdy._1, y - dxdy._2)

  def *(s: Double) = Point2D(x * s, y * s)
  def *(s: Float) = Point2D(x * s, y * s)
  def *(s: Int) = Point2D(x * s, y * s)

  def rot(alpha: Double) = {
    if (alpha == 0.0) this
    else {
      val ca = math.cos(alpha)
      val sa = math.sin(alpha)
      Point2D((ca * x + sa * y), (ca * y - sa * x))
    }
  }

  def rotD(a: Int) = {
    if (a == 0 || a == 360) this
    else if (a == 90) Point2D(-y, x)
    else if (a == 180) Point2D(-x, -y)
    else if (a == 270) Point2D(y, -x)
  }

  def in(x0: Double, y0: Double, x1: Double, y1: Double) =
    x0 <= x && x < x1 && y0 <= y && y < y1

  def scale(sx: Double, sy: Double) = Point2D((x * sx), (y * sy))

  def toInt = Point2D(x.toInt, y.toInt)
}

object Point2D {
  def apply(x: Double, y: Double) = new Point2DDouble(x, y)
}

object Point2DDouble {
  def apply(x: Double, y: Double) = new Point2DDouble(x, y)

  def unapply(p: Point2DDouble) = (p.x, p.y)
}

case class Rectangle(x: Int, y: Int, width: Int, height: Int)
