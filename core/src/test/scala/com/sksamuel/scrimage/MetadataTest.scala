package com.sksamuel.scrimage

import javax.imageio.ImageIO

import org.scalatest.{BeforeAndAfter, FunSuite, OneInstancePerTest}
import org.w3c.dom.Node

/** @author Stephen Samuel */
class MetadataTest extends FunSuite with BeforeAndAfter with OneInstancePerTest {

  val input = getClass.getResourceAsStream("/com/sksamuel/scrimage/bird.jpg")

  test("EXIF metadata is wrapped with the image") {
    val image = Image(input)
    val metadata = image match {
      case i : ImageLikeWithMeta[_] => i.metadata
      case _ => null
    }
    assert(metadata != null)
    image.write("core/src/test/resources/bird_copied.jpg")
    val loadBack = Image.fromPath("/bird_copied.jpg")
    val meta2 = loadBack match {
      case i : ImageLikeWithMeta[_] => i.metadata
      case _ => null
    }
    metadata.getMetadataFormatNames.foreach(format => {
      val node = metadata.getAsTree(format)
      val node2 = metadata.getAsTree(format)
      print(node.getNodeName)
      print(" === ")
      println(node2.getNodeName)
      assert(node.getNodeName === node2.getNodeName)
      displayMetadata(node, 0)
      displayMetadata(node2, 0)
    })
    assert(metadata.equals(meta2))
  }

  ignore("EXIF metadata can be read") {

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
  }

  def printIndent(level: Integer): Unit = {
    (1 to level).foreach(_ -> print("  "))
  }

  def displayMetadata(node: Node, level: Integer) {
    printIndent(level)
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
      printIndent(level)
      println("</" + node.getNodeName + ">")
    }
  }
}
