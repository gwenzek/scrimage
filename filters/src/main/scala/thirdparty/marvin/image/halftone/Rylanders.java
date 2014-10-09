/**
 Marvin Project <2007-2009>

 Initial version by:

 Danilo Rosetto Munoz
 Fabio Andrijauskas
 Gabriel Ambrosio Archanjo

 site: http://marvinproject.sourceforge.net

 GPL
 Copyright (C) <2007>

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along
 with this program; if not, write to the Free Software Foundation, Inc.,
 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package thirdparty.marvin.image.halftone;

import thirdparty.marvin.image.*;
import thirdparty.marvin.image.grayScale.GrayScale;

/**
 * Halftone Rylanders implementation.
 *
 * @author Gabriel Ambr�sio Archanjo
 * @version 1.0 02/28/2008
 */
public class Rylanders extends MarvinAbstractImagePlugin {
    private static final int DOT_AREA = 4;
    private static final int arrPattern[] = {1, 9, 3, 11,
            5, 13, 7, 15,
            4, 12, 2, 10,
            8, 16, 6, 14
    };

    public void process(MarvinImage a_imageIn,
                        MarvinImage a_imageOut,
                        MarvinAttributes a_attributesOut,
                        MarvinImageMask a_mask,
                        boolean a_previewMode) {
        double l_intensity;
        int real_intesity;

        // Gray
        MarvinImagePlugin l_filter = new GrayScale();
        l_filter.process(a_imageIn, a_imageIn, a_attributesOut, a_mask, a_previewMode);

        examinate(a_imageIn);

        boolean[][] l_arrMask = a_mask.getMaskArray();

        for (int x = 0; x < a_imageIn.getWidth(); x += DOT_AREA) {
            for (int y = 0; y < a_imageIn.getHeight(); y += DOT_AREA) {
                if (l_arrMask != null && !l_arrMask[x][y]) {
                    continue;
                }

                l_intensity = getSquareIntensity(x, y, a_imageIn);
                drawTone(x, y, a_imageIn, a_imageOut, l_intensity);
            }
        }
    }

    private int getRealSquareIntensity(int a_x, int a_y, MarvinImage image) {
        int total = 0;
        for (int y = 0; y < DOT_AREA; y++) {
            for (int x = 0; x < DOT_AREA; x++) {
                if (a_x + x < image.getWidth() && a_y + y < image.getHeight()) {
                    total += image.getIntComponent0(a_x + x, a_y + y);
                }
            }
        }
        return total / 16;
    }

    private void drawTone(int a_x, int a_y, MarvinImage a_imageIn, MarvinImage a_imageOut, double a_intensity) {
        double l_factor = 1.0 / 15;
        int l_toneIntensity = (int) Math.floor(a_intensity / l_factor);
        int l_x;
        int l_y;
        int real_intesity = getRealSquareIntensity(a_x, a_y, a_imageIn);

        for (int x = 0; x < DOT_AREA * DOT_AREA; x++) {
            l_x = x % DOT_AREA;
            l_y = x / DOT_AREA;

            if (a_x + l_x < a_imageIn.getWidth() && a_y + l_y < a_imageIn.getHeight()) {
                if (l_toneIntensity >= arrPattern[x]) {
                    a_imageOut.setIntColor(a_x + l_x, a_y + l_y, 0, 0, 0);
                } else {
                    a_imageOut.setIntColor(a_x + l_x, a_y + l_y, 255, 255, 255);
                }
            }
        }
    }

    private double getSquareIntensity(int a_x, int a_y, MarvinImage image) {
        double l_totalValue = 0;
        double l_factor = 1.0 / 15;
        for (int y = 0; y < DOT_AREA; y++) {
            for (int x = 0; x < DOT_AREA; x++) {
                if (a_x + x < image.getWidth() && a_y + y < image.getHeight()) {
                    l_totalValue += 255 - (image.getIntComponent0(a_x + x, a_y + y));
                }
            }
        }
        return (l_totalValue / (DOT_AREA * DOT_AREA * 255));
    }

    private void examinate(MarvinImage image){
        double l_factor = 1.0 / 15;

        for(int y = 0; y < DOT_AREA; y++) {
            for (int x = 0; x < DOT_AREA; x++) {
                int level = -1;
                int l_toneIntensity;
                double sqIntensity;
                do{
                    level += 1;
                    setBlock(image, level);
                    sqIntensity = getSquareIntensity(0, 0, image);
                    l_toneIntensity = (int) Math.floor(sqIntensity / l_factor);
                } while(l_toneIntensity >= arrPattern[y * 4 + x]);
                System.out.println("("+x+", "+y+") = "+level);

            }
        }
    }

    private void setBlock(MarvinImage image, int level){
        for(int y = 0; y < DOT_AREA; y++) {
            for (int x = 0; x < DOT_AREA; x++) {
                image.setIntColor(x, y, level, level, level);
            }
        }
    }

}