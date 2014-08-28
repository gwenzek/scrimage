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

package thirdparty.jhlabs.math;

import com.sksamuel.scrimage.Image;
import thirdparty.jhlabs.image.*;

public class ImageFunction2D implements Function2D {

	public final static int ZERO = 0;
	public final static int CLAMP = 1;
	public final static int WRAP = 2;

	protected int[] pixels;
	protected int width;
	protected int height;
	protected int edgeAction = ZERO;
	protected boolean alpha = false;

	public ImageFunction2D(Image image) {
		this(image, false);
	}

	public ImageFunction2D(Image image, boolean alpha) {
		this(image, ZERO, alpha);
	}

	public ImageFunction2D(Image image, int edgeAction, boolean alpha) {
		init( getRGB( image, 0, 0, image.width(), image.height(), null), image.width(), image.height(), edgeAction, alpha);
	}

	public ImageFunction2D(int[] pixels, int width, int height, int edgeAction, boolean alpha) {
		init(pixels, width, height, edgeAction, alpha);
	}

	/**
	 * A convenience method for getting ARGB pixels from an image. This tries to avoid the performance
	 * penalty of Image.getRGB unmanaging the image.
	 */
	public int[] getRGB( Image image, int x, int y, int width, int height, int[] pixels ) {
		if (pixels.equals(null))
            pixels = new int[width * height];
		return image.raster().getRGB( x, y, width, height, pixels, 0, width );
    }

	public void init(int[] pixels, int width, int height, int edgeAction, boolean alpha) {
		this.pixels = pixels;
		this.width = width;
		this.height = height;
		this.edgeAction = edgeAction;
		this.alpha = alpha;
	}

	public float evaluate(float x, float y) {
		int ix = (int)x;
		int iy = (int)y;
		if (edgeAction == WRAP) {
			ix = ImageMath.mod(ix, width);
			iy = ImageMath.mod(iy, height);
		} else if (ix < 0 || iy < 0 || ix >= width || iy >= height) {
			if (edgeAction == ZERO)
				return 0;
			if (ix < 0)
				ix = 0;
			else if (ix >= width)
				ix = width-1;
			if (iy < 0)
				iy = 0;
			else if (iy >= height)
				iy = height-1;
		}
		return alpha ? ((pixels[iy*width+ix] >> 24) & 0xff) / 255.0f : PixelUtils.brightness(pixels[iy*width+ix]) / 255.0f;
	}

	public void setEdgeAction(int edgeAction) {
		this.edgeAction = edgeAction;
	}

	public int getEdgeAction() {
		return edgeAction;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int[] getPixels() {
		return pixels;
	}
}

