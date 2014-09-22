package com.sksamuel.scrimage

import javax.imageio.metadata.IIOMetadata

import org.scalatest.{FunSuite, OneInstancePerTest}
import org.w3c.dom.Node

/** @author Stephen Samuel */
class MetadataTest extends FunSuite with OneInstancePerTest {

  val image = Image(getClass.getResourceAsStream("/com/sksamuel/scrimage/bird.jpg"))

  test("EXIF metadata can be read") {
    assert(image.getMeta.isDefined)
  }

  test("EXIF metadata can be converted to scala losslessly") {
    val meta1 = Metadata(image.getMeta.get)
    val meta2 = Metadata(meta1.toJava)
    val set1 = meta1.listAttributes.toSet
    val set2 = meta2.listAttributes.toSet
    assert(set1 === set2)
    assert(meta1 == meta2)
  }

  test("EXIF metadata is wrapped with the image") {
    assert(image.getMeta.isDefined)
    val metadata = Metadata(image.getMeta.get)
    image.write("core/src/test/resources/bird_copied.jpg", Format.JPEG)
    val loadedBack = Image(getClass.getResourceAsStream("/bird_copied.jpg"))
    val meta2 = Metadata(loadedBack.getMeta.get)
    assert(metadata.listAttributes.toSet === meta2.listAttributes.toSet)
    assert(metadata === meta2)
  }

  test("EXIF metadata is conserved through operations") {
    val metadata = Metadata(image.getMeta.get)
    val meta2 = Metadata(image.rotateLeft.getMeta.get)
    assert(metadata.listAttributes.toSet === meta2.listAttributes.toSet)
    assert(metadata === meta2)
  }

  def printIndent(level: Integer): Unit = {
    (1 to level).foreach(_ -> print("  "))
  }

  def displayMetadata(metadata: IIOMetadata): Unit = {
    metadata.getMetadataFormatNames.foreach(format => {
      val node = metadata.getAsTree(format)
      displayMetadata(node, 0)
    })
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
      println("/>")
    } else {
      println(">")
      while (child != null) {
        displayMetadata(child, level + 1)
        child = child.getNextSibling
      }
      printIndent(level)
      println("</" + node.getNodeName + ">")
    }
  }
}
