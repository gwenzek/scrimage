// /*
// Copyright 2006 Jerry Huxtable

// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

//    http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// */

// package thirdparty.jhlabs.image;

// import java.awt.*;
// import java.awt.geom.*;
// import com.sksamuel.scrimage.Image;

// /**
//  * A convenience class which implements those methods of BufferedImageOp which are rarely changed.
//  */
// public abstract class AbstractBufferedImageOp implements BufferedImageOp, Cloneable {

//     public Image createCompatibleDestImage(Image src, ColorModel dstCM) {
//         if ( dstCM == null )
//             dstCM = src.getColorModel();
//         return new Image(dstCM, dstCM.createCompatibleRaster(src.width, src.height), dstCM.isAlphaPremultiplied(), null);
//     }

//     public Rectangle2D getBounds2D( Image src ) {
//         return new Rectangle(0, 0, src.width, src.height);
//     }

//     public Point2D getPoint2D( Point2D srcPt, Point2D dstPt ) {
//         if ( dstPt == null )
//             dstPt = new Point2D.Double();
//         dstPt.setLocation( srcPt.getX(), srcPt.getY() );
//         return dstPt;
//     }

//     public RenderingHints getRenderingHints() {
//         return null;
//     }

// 	/**
// 	 * A convenience method for getting ARGB pixels from an image. This tries to avoid the performance
// 	 * penalty of Image.getRGB unmanaging the image.
//      * @param image   a Image object
//      * @param x       the left edge of the pixel block
//      * @param y       the right edge of the pixel block
//      * @param width   the width of the pixel arry
//      * @param height  the height of the pixel arry
//      * @param pixels  the array to hold the returned pixels. May be null.
//      * @return the pixels
//      * @see #setRGB
//      */
// 	public int[] getRGB( Image image, int x, int y, int width, int height, int[] pixels ) {
// 		int type = image.getType();
// 		if ( type == Image.TYPE_INT_ARGB || type == Image.TYPE_INT_RGB )
// 			return (int [])image.raster().getDataElements( x, y, width, height, pixels );
// 		return image.getRGB( x, y, width, height, pixels, 0, width );
//     }

// 	/**
// 	 * A convenience method for setting ARGB pixels in an image. This tries to avoid the performance
// 	 * penalty of Image.setRGB unmanaging the image.
//      * @param image   a Image object
//      * @param x       the left edge of the pixel block
//      * @param y       the right edge of the pixel block
//      * @param width   the width of the pixel arry
//      * @param height  the height of the pixel arry
//      * @param pixels  the array of pixels to set
//      * @see #getRGB
// 	 */
// 	public void setRGB( Image image, int x, int y, int width, int height, int[] pixels ) {
// 		int type = image.getType();
// 		if ( type == Image.TYPE_INT_ARGB || type == Image.TYPE_INT_RGB )
// 			image.raster().setDataElements( x, y, width, height, pixels );
// 		else
// 			image.setRGB( x, y, width, height, pixels, 0, width );
//     }

// 	public Object clone() {
// 		try {
// 			return super.clone();
// 		}
// 		catch ( CloneNotSupportedException e ) {
// 			return null;
// 		}
// 	}
// }
