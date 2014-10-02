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

import com.sksamuel.scrimage.Image;
import com.sksamuel.scrimage.JavaAbstractImageFilter;
import com.sksamuel.scrimage.Raster;

/**
 * A filter which interpolates betwen two images. You can set the interpolation factor outside the range 0 to 1
 * to extrapolate images.
 */
public class InterpolateFilter extends JavaAbstractImageFilter {

	private Image destination;
	private float interpolation;

	public InterpolateFilter() {
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
     * Set the interpolation factor.
     * @param interpolation the interpolation factor
     * @see #getInterpolation
     */
	public void setInterpolation( float interpolation ) {
		this.interpolation = interpolation;
	}

    /**
     * Get the interpolation factor.
     * @return the interpolation factor
     * @see #setInterpolation
     */
	public float getInterpolation() {
		return interpolation;
	}

    public Image filter( Image src, Image dst ) {
        int width = src.width();
        int height = src.height();
		Raster srcRaster = src.raster();

        if ( dst == null )
            dst = createCompatibleDestImage(src);
		Raster dstRaster = dst.raster();

        if ( destination != null ) {
			width = Math.min( width, destination.width() );
			height = Math.min( height, destination.width() );
			int[] pixels1 = new int[width];
			int[] pixels2 = new int[width];

			for (int y = 0; y < height; y++) {
				pixels1 = srcRaster.getRGB(0, y, width, 1, pixels1 );
				pixels2 = destination.raster().getRGB(0, y, width, 1, pixels2 );
				for (int x = 0; x < width; x++) {
					int rgb1 = pixels1[x];
					int rgb2 = pixels2[x];
					int a1 = (rgb1 >> 24) & 0xff;
					int r1 = (rgb1 >> 16) & 0xff;
					int g1 = (rgb1 >> 8) & 0xff;
					int b1 = rgb1 & 0xff;
					int a2 = (rgb2 >> 24) & 0xff;
					int r2 = (rgb2 >> 16) & 0xff;
					int g2 = (rgb2 >> 8) & 0xff;
					int b2 = rgb2 & 0xff;
					r1 = PixelUtils.clamp( ImageMath.lerp( interpolation, r1, r2 ) );
					g1 = PixelUtils.clamp( ImageMath.lerp( interpolation, g1, g2 ) );
					b1 = PixelUtils.clamp( ImageMath.lerp( interpolation, b1, b2 ) );
					pixels1[x] = (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
				}
				dstRaster.setRGB( 0, y, width, 1, pixels1 );
			}
        }

        return dst;
    }

	public String toString() {
		return "Effects/Interpolate...";
	}
}
