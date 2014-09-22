package com.sksamuel.scrimage

import java.io.PrintStream
import javax.imageio.metadata.{IIOMetadata, IIOMetadataNode}

import org.w3c.dom.Node
/** Created by guw on 03/09/14.
  */

class Metadata(val formats: List[MetadataTree]) { self =>
  def ++(that: Metadata) = new Metadata(formats ++ that.formats)
  def enrich(that: Metadata) = this ++ that
  def getMetadataFormatNames: List[String] = formats.map(_.name)
  def getAsTree(name: String): Option[MetadataTree] = formats.find(_.name == name)

  def toJava: IIOMetadata = new IIOMetadata() {
    def mergeTree(p1: String, p2: Node): Unit = ()

    def isReadOnly: Boolean = true

    def getAsTree(p1: String): Node = (self.getAsTree(p1) map (_.toJavaNode)).orNull

    def reset(): Unit = ()

    override def getMetadataFormatNames = formats.map(_.name).toArray
  }

  def display(out: PrintStream = System.out): Unit = formats.foreach(_.display(out=out))

  def listAttributes: List[(String, String)] = formats.flatMap(_.listAttributes)
}

sealed trait MetadataTree {
  val name: String
  def attributes: Iterable[(String, String)]
  def isLeaf: Boolean
  def isNode: Boolean = !isLeaf
  def ++(that: MetadataTree): MetadataTree
  def toJavaNode: Node
  def display(indent: Int = 0, out: PrintStream = System.out): Unit

  def listAttributes: List[(String, String)]

  protected def printIndent(indent: Int, out: PrintStream): Unit = (1 to indent).foreach(_ => out.print("  "))
}

class MetadataLeaf(val name: String, val attributes: List[(String, String)]) extends MetadataTree{
  val isLeaf: Boolean = true

  def ++(that: MetadataTree) = { System.err.println(s"key collision on $name"); that }

  def toJavaNode: IIOMetadataNode = {
    val node = new IIOMetadataNode(name)
    attributes.foreach(nv => node.setAttribute(nv._1, nv._2))
    node
  }

  def display(indent: Int, out: PrintStream = System.out): Unit = {
    printIndent(indent, out)
    out.print(s"<$name")
    attributes.foreach(nv => out.print(s" ${nv._1}=${nv._2}"))
    out.println("/>")
  }

  def listAttributes = attributes.map(kv => (name + '/' + kv._1, kv._2))

}

class MetadataNode(val name: String, val children: List[MetadataTree], val attributes: List[(String, String)]) extends MetadataTree{
  def ++(that: MetadataTree) = {
    assert(that.name == name)
    that match {
      case node: MetadataNode =>
//        val shared = children.filter(kv => node.children.contains(kv._1)).map(kv => (kv._1, kv._2 ++ node.children(kv._1)))
        new MetadataNode(name, children ++ node.children, attributes ++ that.attributes)
      case _: MetadataLeaf => new MetadataNode(name, children, attributes ++ that.attributes)
    }
  }

  def toJavaNode: IIOMetadataNode = {
    val node = new IIOMetadataNode(name)
    children.foreach {
      case n: MetadataNode => node.appendChild(n.toJavaNode)
      case l: MetadataLeaf => attributes.foreach(nv => node.setAttribute(nv._1, nv._2))
    }
    node
  }

  final val isLeaf: Boolean = false

  def display(indent: Int, out: PrintStream = System.out): Unit = {
    printIndent(indent, out)
    out.print(s"<$name")
    attributes.foreach(nv => out.print(s" ${nv._1}=${nv._2}"))
    out.println(">")
    children.map(_.display(indent + 1, out))
    printIndent(indent, out)
    out.println(s"</$name>")
  }

  def listAttributes = (attributes ++ children.flatMap(_.listAttributes)).map(kv => (name + '/' + kv._1, kv._2))
}

object Metadata{

//  implicit def fromJava(metadata: IIOMetadata): Metadata = apply(metadata)
//  implicit def toJava(metadata: Metadata): IIOMetadata = metadata.toJava

  def apply(metadata: IIOMetadata): Metadata = {
    val formats = for(format <- metadata.getMetadataFormatNames) yield {
      val node = metadata.getAsTree(format)
      apply(node)
    }
    new Metadata(formats.toList)
  }

  def apply(node: Node): MetadataTree = {
    if (node.hasChildNodes)
       new MetadataNode(node.getNodeName, getChildren(node).map(apply).toList, getAttributes(node).toList)
    else
      new MetadataLeaf(node.getNodeName, getAttributes(node).toList)
  }

  def getChildren(node: Node): Iterator[Node] = new Iterator[Node]{
    val children = node.getChildNodes
    var i = 0
    def hasNext: Boolean = i < children.getLength
    def next(): Node = {
      val i0 = i
      i += 1
      children.item(i0)
    }
  }

  def getAttributes(node: Node): Iterator[(String, String)] = new Iterator[(String, String)]{
    val attributes = node.getAttributes
    var i = 0
    def hasNext: Boolean = i < attributes.getLength
    def next(): (String, String) = {
      val i0 = i
      i += 1
      (attributes.item(i0).getNodeName, attributes.item(i0).getNodeValue)
    }
  }
}
