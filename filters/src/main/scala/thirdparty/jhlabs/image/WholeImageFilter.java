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
import com.sksamuel.scrimage.geom.Rectangle;

/**
 * A filter which acts as a superclass for filters which need to have the whole image in memory
 * to do their stuff.
 */
public abstract class WholeImageFilter extends AbstractImageFilter {

	/**
     * The output image bounds.
     */
    protected Rectangle transformedSpace;

	/**
     * The input image bounds.
     */
	protected Rectangle originalSpace;

	/**
	 * Construct a WholeImageFilter.
	 */
	public WholeImageFilter() {
	}

    public Image filter( Image src, Image dst ) {
        int width = src.width();
        int height = src.height();
		Raster srcRaster = src.raster();

		originalSpace = new Rectangle(0, 0, width, height);
		transformedSpace = new Rectangle(0, 0, width, height);
		transformSpace(transformedSpace);

        if ( dst == null ) {
            dst = new Image((Raster) src.raster().empty(transformedSpace.width(), transformedSpace.height()));
		}
		Raster dstRaster = dst.raster();

		int[] inPixels = src.raster().getRGB( 0, 0, width, height);
		inPixels = filterPixels( width, height, inPixels, transformedSpace );
		dst.raster().setRGB(0, 0, transformedSpace.width(), transformedSpace.height(), inPixels );

        return dst;
    }

	/**
     * Calculate output bounds for given input bounds.
     * @param rect input and output rectangle
     */
	protected void transformSpace(Rectangle rect) {
	}

	/**
     * Actually filter the pixels.
     * @param width the image width
     * @param height the image height
     * @param inPixels the image pixels
     * @param transformedSpace the output bounds
     * @return the output pixels
     */
	protected abstract int[] filterPixels( int width, int height, int[] inPixels, Rectangle transformedSpace );
}

