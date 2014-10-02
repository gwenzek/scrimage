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
 * An abstract superclass for point filters. The interface is the same as the old RGBImageFilter.
 */
public abstract class PointFilter extends JavaAbstractImageFilter {

	protected boolean canFilterIndexColorModel = false;

    public Image filter( Image src, Image dst ) {
        int width = src.width();
        int height = src.height();
		Raster srcRaster = src.raster();

        if ( dst == null )
            dst = createCompatibleDestImage( src);
		Raster dstRaster = dst.raster();

        setDimensions( width, height);

		int[] inPixels = new int[width];
        for ( int y = 0; y < height; y++ ) {
            srcRaster.getRGB( 0, y, width, 1, inPixels, 0, width );
            for ( int x = 0; x < width; x++ )
                inPixels[x] = filterRGB( x, y, inPixels[x] );
            dstRaster.setRGB( 0, y, width, 1, inPixels, 0, width );
        }

        return dst;
    }

	public void setDimensions(int width, int height) {
	}

	public abstract int filterRGB(int x, int y, int rgb);
}
