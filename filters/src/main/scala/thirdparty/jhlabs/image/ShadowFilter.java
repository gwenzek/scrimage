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
import com.sksamuel.scrimage.AbstractImageFilter;
import com.sksamuel.scrimage.Image;
import com.sksamuel.scrimage.Raster;
import com.sksamuel.scrimage.geom.Point2D;
import com.sksamuel.scrimage.geom.Rectangle;

/**
 * A filter which draws a drop shadow based on the alpha channel of the image.
 */
public class ShadowFilter extends AbstractImageFilter {

	private float radius = 5;
	private float angle = (float)Math.PI*6/4;
	private float distance = 5;
	private float opacity = 0.5f;
	private boolean addMargins = false;
	private boolean shadowOnly = false;
	private int shadowColor = 0xff000000;

	/**
     * Construct a ShadowFilter.
     */
    public ShadowFilter() {
	}

	/**
     * Construct a ShadowFilter.
     * @param radius the radius of the shadow
     * @param xOffset the X offset of the shadow
     * @param yOffset the Y offset of the shadow
     * @param opacity the opacity of the shadow
     */
	public ShadowFilter(float radius, float xOffset, float yOffset, float opacity) {
		this.radius = radius;
		this.angle = (float)Math.atan2(yOffset, xOffset);
		this.distance = (float)Math.sqrt(xOffset*xOffset + yOffset*yOffset);
		this.opacity = opacity;
	}

	/**
     * Specifies the angle of the shadow.
     * @param angle the angle of the shadow.
     * @angle
     * @see #getAngle
     */
	public void setAngle(float angle) {
		this.angle = angle;
	}

	/**
     * Returns the angle of the shadow.
     * @return the angle of the shadow.
     * @see #setAngle
     */
	public float getAngle() {
		return angle;
	}

	/**
     * Set the distance of the shadow.
     * @param distance the distance.
     * @see #getDistance
     */
	public void setDistance(float distance) {
		this.distance = distance;
	}

	/**
     * Get the distance of the shadow.
     * @return the distance.
     * @see #setDistance
     */
	public float getDistance() {
		return distance;
	}

	/**
	 * Set the radius of the kernel, and hence the amount of blur. The bigger the radius, the longer this filter will take.
	 * @param radius the radius of the blur in pixels.
     * @see #getRadius
	 */
	public void setRadius(float radius) {
		this.radius = radius;
	}

	/**
	 * Get the radius of the kernel.
	 * @return the radius
     * @see #setRadius
	 */
	public float getRadius() {
		return radius;
	}

	/**
     * Set the opacity of the shadow.
     * @param opacity the opacity.
     * @see #getOpacity
     */
	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}

	/**
     * Get the opacity of the shadow.
     * @return the opacity.
     * @see #setOpacity
     */
	public float getOpacity() {
		return opacity;
	}

	/**
     * Set the color of the shadow.
     * @param shadowColor the color.
     * @see #getShadowColor
     */
	public void setShadowColor(int shadowColor) {
		this.shadowColor = shadowColor;
	}

	/**
     * Get the color of the shadow.
     * @return the color.
     * @see #setShadowColor
     */
	public int getShadowColor() {
		return shadowColor;
	}

	/**
     * Set whether to increase the size of the output image to accomodate the shadow.
     * @param addMargins true to add margins.
     * @see #getAddMargins
     */
	public void setAddMargins(boolean addMargins) {
		this.addMargins = addMargins;
	}

	/**
     * Get whether to increase the size of the output image to accomodate the shadow.
     * @return true to add margins.
     * @see #setAddMargins
     */
	public boolean getAddMargins() {
		return addMargins;
	}

	/**
     * Set whether to only draw the shadow without the original image.
     * @param shadowOnly true to only draw the shadow.
     * @see #getShadowOnly
     */
	public void setShadowOnly(boolean shadowOnly) {
		this.shadowOnly = shadowOnly;
	}

	/**
     * Get whether to only draw the shadow without the original image.
     * @return true to only draw the shadow.
     * @see #setShadowOnly
     */
	public boolean getShadowOnly() {
		return shadowOnly;
	}

    public Rectangle getBounds2D( Image src ) {

		if ( addMargins ) {
			float xOffset = distance*(float)Math.cos(angle);
			float yOffset = -distance*(float)Math.sin(angle);
			int w  =(int)(Math.abs(xOffset)+2*radius);
			int h = (int)(Math.abs(yOffset)+2*radius);
            return new Rectangle(0, 0, src.width() + w , src.height() + h);
		} else
            return new Rectangle(0, 0, src.width(), src.height());
    }

    public Point2D getPoint2D( Point2D srcPt) {
		if ( addMargins ) {
            float xOffset = distance * (float)Math.cos(angle);
            float yOffset = -distance * (float)Math.sin(angle);
			float topShadow = Math.max( 0, radius-yOffset );
			float leftShadow = Math.max( 0, radius-xOffset );
            return new Point2D( (int) (srcPt.x()+leftShadow), (int) (srcPt.y()+topShadow) );
		} else
            return new Point2D( srcPt.x(), srcPt.y() );
    }

    public Image filter( Image src, Image dst ) {
        int width = src.width();
        int height = src.height();

		float xOffset = distance*(float)Math.cos(angle);
		float yOffset = -distance*(float)Math.sin(angle);

        if ( dst == null ) {
            if ( addMargins ) {
				dst = new Image((Raster) src.raster().empty(src.width()+ (int) (Math.abs(xOffset) + radius), src.height()+ (int) (Math.abs(yOffset) + radius)) );
			} else
				dst = createCompatibleDestImage( src);
		}

        float shadowR = ((shadowColor >> 16) & 0xff) / 255f;
        float shadowG = ((shadowColor >> 8) & 0xff) / 255f;
        float shadowB = (shadowColor & 0xff) / 255f;

		// Make a black mask from the image's alpha channel
        float[][] extractAlpha = {
            { 0, 0, 0, shadowR },
            { 0, 0, 0, shadowG },
            { 0, 0, 0, shadowB },
            { 0, 0, 0, opacity }
        };
        Image shadow = new Image(ARGBRaster.apply(width, height));
        //TODO
//        new BandCombineOp( extractAlpha, null ).filter( src.raster, shadow.raster );
//        shadow = new GaussianFilter( radius ).filter( shadow, null );
//
//		Graphics2D g = dst.createGraphics();
//		g.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, opacity ) );
//		if ( addMargins ) {
//			float radius2 = radius/2;
//			float topShadow = Math.max( 0, radius-yOffset );
//			float leftShadow = Math.max( 0, radius-xOffset );
//			g.translate( leftShadow, topShadow );
//		}
//		g.drawRenderedImage( shadow, AffineTransform.getTranslateInstance( xOffset, yOffset ) );
//		if ( !shadowOnly ) {
//			g.setComposite( AlphaComposite.SrcOver );
//			g.drawRenderedImage( src, null );
//		}
//		g.dispose();

        return dst;
	}

	public String toString() {
		return "Stylize/Drop Shadow...";
	}
}
