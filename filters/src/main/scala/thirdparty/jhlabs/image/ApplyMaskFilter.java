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

import com.sksamuel.scrimage.AbstractImageFilter;
import com.sksamuel.scrimage.Image;
import com.sksamuel.scrimage.Raster;

/**
 * A filter which uses the alpha channel of a "mask" image to interpolate between a source and destination image.
 */
public class ApplyMaskFilter extends AbstractImageFilter {

	private Image destination;
	private Image maskImage;

    /**
     * Construct an ApplyMaskFIlter.
     */
	public ApplyMaskFilter() {
	}

    /**
     * Construct an ApplyMaskFIlter.
     * @param maskImage the mask image
     * @param destination the destination image
     */
	public ApplyMaskFilter( Image maskImage, Image destination ) {
		this.maskImage = maskImage;
		this.destination = destination;
	}

    /**
     * Set the destination image.
     * @param destination the destination image
     * @see #getDestination
     */
	public void setDestination( Image destination ) {
		this.destination = destination;
	}

    /**
     * Get the destination image.
     * @return the destination image
     * @see #setDestination
     */
	public Image getDestination() {
		return destination;
	}

    /**
     * Set the mask image.
     * @param maskImage the mask image
     * @see #getMaskImage
     */
	public void setMaskImage( Image maskImage ) {
		this.maskImage = maskImage;
	}

    /**
     * Get the mask image.
     * @return the mask image
     * @see #setMaskImage
     */
	public Image getMaskImage() {
		return maskImage;
	}

    /**
     * Interpolates between two rasters according to the alpha level of a mask raster.
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

    public Image filter( Image src, Image dst ) {
        int width = src.width();
        int height = src.height();
		Raster srcRaster = src.raster();

        if ( dst == null )
            dst = createCompatibleDestImage(src);
		Raster dstRaster = dst.raster();

        if ( destination != null && maskImage != null )
			composeThroughMask( src.raster(), dst.raster(), maskImage.raster() );

        return dst;
    }

	public String toString() {
		return "Keying/Key...";
	}
}
