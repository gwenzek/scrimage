package com.sksamuel.scrimage

import javax.imageio.ImageIO

import org.scalatest.{BeforeAndAfter, FunSuite, OneInstancePerTest}
import org.w3c.dom.Node

/** @author Stephen Samuel */
class MetadataTest extends FunSuite with BeforeAndAfter with OneInstancePerTest {

  val input = getClass.getResourceAsStream("/com/sksamuel/scrimage/bird.jpg")

  test("EXIF metadata is wrapped with the image") {
    val image = Image(input)
    assert(image.isInstanceOf[ImageLikeWithMeta[_]])
  }

  test("EXIF metadata can be read") {

    val iis = ImageIO.createImageInputStream(input)
    val readers = ImageIO.getImageReaders(iis)
    val reader = readers.next
    reader.setInput(iis, true)
    val metadata = reader.getImageMetadata(0)
    metadata.getMetadataFormatNames.foreach(format => {
      val node = metadata.getAsTree(format)
      displayMetadata(node, 0)
      println(node.getNodeName)
    })

    def displayMetadata(node: Node, level: Integer) {
      print("<" + node.getNodeName)
      val map = node.getAttributes
      if (map != null) {

        // print attribute values
        val length = map.getLength
        for (i <- 0 until length) {
          val attr = map.item(i)
          print(" " + attr.getNodeName + "=\"" + attr.getNodeValue + "\"")
        }
      }

      var child = node.getFirstChild
      if (child == null) {
        System.out.println("/>")
      } else {
        System.out.println(">")
        while (child != null) {
          displayMetadata(child, level + 1)
          child = child.getNextSibling
        }
        println("</" + node.getNodeName + ">")
      }
    }
  }
}
