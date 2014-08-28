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

public class ShineFilter extends AbstractImageFilter {

	private float radius = 5;
	private float angle = (float)Math.PI*7/4;
	private float distance = 5;
	private float bevel = 0.5f;
	private boolean shadowOnly = false;
	private int shineColor = 0xffffffff;
	private float brightness = 0.2f;
	private float softness = 0;

	public ShineFilter() {
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public float getAngle() {
		return angle;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public float getDistance() {
		return distance;
	}

	/**
	 * Set the radius of the kernel, and hence the amount of blur. The bigger the radius, the longer this filter will take.
	 * @param radius the radius of the blur in pixels.
	 */
	public void setRadius(float radius) {
		this.radius = radius;
	}

	/**
	 * Get the radius of the kernel.
	 * @return the radius
	 */
	public float getRadius() {
		return radius;
	}

	public void setBevel(float bevel) {
		this.bevel = bevel;
	}

	public float getBevel() {
		return bevel;
	}

	public void setShineColor(int shineColor) {
		this.shineColor = shineColor;
	}

	public int getShineColor() {
		return shineColor;
	}

	public void setShadowOnly(boolean shadowOnly) {
		this.shadowOnly = shadowOnly;
	}

	public boolean getShadowOnly() {
		return shadowOnly;
	}

	public void setBrightness(float brightness) {
		this.brightness = brightness;
	}

	public float getBrightness() {
		return brightness;
	}

	public void setSoftness(float softness) {
		this.softness = softness;
	}

	public float getSoftness() {
		return softness;
	}

    public Image filter( Image src, Image dst ) {
        int width = src.width();
        int height = src.height();

        if ( dst == null )
            dst = createCompatibleDestImage( src );

		float xOffset = distance*(float)Math.cos(angle);
		float yOffset = -distance*(float)Math.sin(angle);

        Image matte = new Image(ARGBRaster.apply(width, height));
        ErodeAlphaFilter s = new ErodeAlphaFilter( bevel * 10, 0.75f, 0.1f );
        matte = s.filter( src, null );

        Image shineLayer = new Image(ARGBRaster.apply(width, height));

        //TODO
//		Graphics2D g = shineLayer.createGraphics();
//		g.setColor( new Color( shineColor ) );
//        g.fillRect( 0, 0, width, height );
//        g.setComposite( AlphaComposite.DstIn );
//        g.drawRenderedImage( matte, null );
//        g.setComposite( AlphaComposite.DstOut );
//        g.translate( xOffset, yOffset );
//        g.drawRenderedImage( matte, null );
//		g.dispose();
//        shineLayer = new GaussianFilter( radius ).filter( shineLayer, null );
//        shineLayer = new RescaleFilter( 3*brightness ).filter( shineLayer, shineLayer );
//
//		g = dst.createGraphics();
//        g.drawRenderedImage( src, null );
//        g.setComposite( new AddComposite( 1.0f ) );
//        g.drawRenderedImage( shineLayer, null );
//		g.dispose();

        return dst;
	}

	public String toString() {
		return "Stylize/Shine...";
	}
}
