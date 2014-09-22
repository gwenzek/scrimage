/*
   Copyright 2013 Stephen K Samuel

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

package com.sksamuel.scrimage.io

import java.io.OutputStream
import javax.imageio.metadata.IIOMetadata
import javax.imageio.stream.MemoryCacheImageOutputStream
import javax.imageio.{IIOImage, ImageIO, ImageWriteParam}

import com.sksamuel.scrimage._
import org.apache.commons.io.IOUtils

/** @author Stephen Samuel */
class JpegWriter(image: ImageLike, compression: Int, progressive: Boolean) extends ImageWriter {

  def withCompression(compression: Int): JpegWriter = {
    require(compression >= 0)
    require(compression <= 100)
    new JpegWriter(image, compression, progressive)
  }
  def withProgressive(progressive: Boolean): JpegWriter = new JpegWriter(image, compression, progressive)

  def write(out: OutputStream) {

    val writer = ImageIO.getImageWritersByFormatName("jpeg").next()
    val params = writer.getDefaultWriteParam
    params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT)
    params.setCompressionQuality(compression / 100f)
    if (progressive)
      params.setProgressiveMode(ImageWriteParam.MODE_DEFAULT)
    else
      params.setProgressiveMode(ImageWriteParam.MODE_DISABLED)

    // jpegs cannot write out transparency. The java version will break
    // see http://stackoverflow.com/questions/464825/converting-transparent-gif-png-to-jpeg-using-java
    // so have to remove alpha
    val noAlpha = image.removeTransparency(X11Colorlist.White)

    val output = new MemoryCacheImageOutputStream(out)
    writer.setOutput(output)
    val metadata: IIOMetadata = image match {
      case i: Metadated => i.metadata
      case _ => null
    }
    writer.write(metadata, new IIOImage(noAlpha.toBufferedImage, null, metadata), params)
    writer.dispose()
    output.close()
    IOUtils.closeQuietly(out)
  }
}

object JpegWriter {
  def apply(image: Image) = new JpegWriter(image, 80, false)
}