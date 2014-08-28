/*
Copyright 2006 Jerry Huxtable

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

package thirdparty.jhlabs.image;

import com.sksamuel.scrimage.ARGBRaster;
import com.sksamuel.scrimage.Image;
import com.sksamuel.scrimage.Raster;
import com.sksamuel.scrimage.geom.*;

/**
 * A class containing some static utility methods for dealing with BufferedImages.
 */
public abstract class ImageUtils {

	private static Image backgroundImage = null;

	/**
     * Cretae a Image from an ImageProducer.
     * @param producer the ImageProducer
     * @return a new TYPE_INT_ARGB Image
     */
 //    public static Image createImage(ImageProducer producer) {
	// 	PixelGrabber pg = new PixelGrabber(producer, 0, 0, -1, -1, null, 0, 0);
	// 	try {
	// 		pg.grabPixels();
	// 	} catch (InterruptedException e) {
	// 		throw new RuntimeException("Image fetch interrupted");
	// 	}
	// 	if ((pg.status() & ImageObserver.ABORT) != 0)
	// 		throw new RuntimeException("Image fetch aborted");
	// 	if ((pg.status() & ImageObserver.ERROR) != 0)
	// 		throw new RuntimeException("Image fetch error");
	// 	Image p = new Image(pg.width(), pg.height(), Image.TYPE_INT_ARGB);
	// 	p.setRGB(0, 0, pg.width(), pg.height(), (int[])pg.getPixels(), 0, pg.width());
	// 	return p;
	// }

	/**
	 * Convert an Image into a TYPE_INT_ARGB Image. If the image is already of this type, the original image is returned unchanged.
     * @param image the image to convert
     * @return the converted image
	 */
	public static Image convertImageToARGB( Image image ) {
		if ( image.raster() instanceof ARGBRaster)
			return image;
		else
            return new Image(ARGBRaster.apply(image.width(), image.height(), image.raster().read()));
	}

	/**
	 * Paint a check pattern, used for a background to indicate image transparency.
     * @param c the component to draw into
     * @param g the Graphics objects
     * @param x the x position
     * @param y the y position
     * @param width the width
     * @param height the height
	 */
	// public static void paintCheckedBackground(Component c, Graphics g, int x, int y, int width, int height) {
	// 	if ( backgroundImage == null ) {
	// 		backgroundImage = new Image( 64, 64, Image.TYPE_INT_ARGB );
	// 		Graphics bg = backgroundImage.createGraphics();
	// 		for ( int by = 0; by < 64; by += 8 ) {
	// 			for ( int bx = 0; bx < 64; bx += 8 ) {
	// 				bg.setColor( ((bx^by) & 8) != 0 ? Color.lightGray : Color.white );
	// 				bg.fillRect( bx, by, 8, 8 );
	// 			}
	// 		}
	// 		bg.dispose();
	// 	}

	// 	if ( backgroundImage != null ) {
	// 		Shape saveClip = g.getClip();
	// 		Rectangle r = g.getClipBounds();
	// 		if (r == null)
	// 			r = new Rectangle(c.getSize());
	// 		r = r.intersection(new Rectangle(x, y, width, height));
	// 		g.setClip(r);
	// 		int w = backgroundImage.width();
	// 		int h = backgroundImage.height();
	// 		if (w != -1 && h != -1) {
	// 			int x1 = (r.x / w) * w;
	// 			int y1 = (r.y / h) * h;
	// 			int x2 = ((r.x + r.width + w - 1) / w) * w;
	// 			int y2 = ((r.y + r.height + h - 1) / h) * h;
	// 			for (y = y1; y < y2; y += h)
	// 				for (x = x1; x < x2; x += w)
	// 					g.drawImage(backgroundImage, x, y, c);
	// 		}
	// 		g.setClip(saveClip);
	// 	}
	// }

    /**
     * Calculates the bounds of the non-transparent parts of the given image.
     * @param p the image
     * @return the bounds of the non-transparent area
     */
	public static Rectangle getSelectedBounds(Image p) {
		int width = p.width();
        int height = p.height();
		int maxX = 0, maxY = 0, minX = width, minY = height;
		boolean anySelected = false;
		int y1;
		int [] pixels = null;

		for (y1 = height-1; y1 >= 0; y1--) {
			pixels = p.raster().getRGB(0, y1, width, 1, pixels);
			for (int x = 0; x < minX; x++) {
				if ((pixels[x] & 0xff000000) != 0) {
					minX = x;
					maxY = y1;
					anySelected = true;
					break;
				}
			}
			for (int x = width-1; x >= maxX; x--) {
				if ((pixels[x] & 0xff000000) != 0) {
					maxX = x;
					maxY = y1;
					anySelected = true;
					break;
				}
			}
			if ( anySelected )
				break;
		}
		pixels = null;
		for (int y = 0; y < y1; y++) {
			pixels = p.raster().getRGB(0, y, width, 1, pixels);
			for (int x = 0; x < minX; x++) {
				if ((pixels[x] & 0xff000000) != 0) {
					minX = x;
					if ( y < minY )
						minY = y;
					anySelected = true;
					break;
				}
			}
			for (int x = width-1; x >= maxX; x--) {
				if ((pixels[x] & 0xff000000) != 0) {
					maxX = x;
					if ( y < minY )
						minY = y;
					anySelected = true;
					break;
				}
			}
		}
		if ( anySelected )
			return new Rectangle( minX, minY, maxX-minX+1, maxY-minY+1 );
		return null;
	}

	/**
	 * Compose src onto dst using the alpha of sel to interpolate between the two.
	 * I can't think of a way to do this using AlphaComposite.
     * @param src the source raster
     * @param dst the destination raster
     * @param sel the mask raster
	 */
	public static void composeThroughMask(Raster src, Raster dst, Raster sel) {
		int x = 0;
		int y = 0;
		int w = src.width();
		int h = src.height();

		int srcRGB[] = null;
		int selRGB[] = null;
		int dstRGB[] = null;

		for ( int i = 0; i < h; i++ ) {
			srcRGB = src.getRGB(x, y, w, 1, srcRGB);
			selRGB = sel.getRGB(x, y, w, 1, selRGB);
			dstRGB = dst.getRGB(x, y, w, 1, dstRGB);

			int k = x;
			for ( int j = 0; j < w; j++ ) {
				int sr = srcRGB[k];
				int dir = dstRGB[k];
				int sg = srcRGB[k+1];
				int dig = dstRGB[k+1];
				int sb = srcRGB[k+2];
				int dib = dstRGB[k+2];
				int sa = srcRGB[k+3];
				int dia = dstRGB[k+3];

				float a = selRGB[k+3]/255f;
				float ac = 1-a;

				dstRGB[k] = (int)(a*sr + ac*dir);
				dstRGB[k+1] = (int)(a*sg + ac*dig);
				dstRGB[k+2] = (int)(a*sb + ac*dib);
				dstRGB[k+3] = (int)(a*sa + ac*dia);
				k += 4;
			}

			dst.setRGB(x, y, w, 1, dstRGB);
			y++;
		}
	}


}

