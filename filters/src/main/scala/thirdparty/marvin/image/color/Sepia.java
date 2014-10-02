package thirdparty.marvin.image.color;


import thirdparty.marvin.image.MarvinAbstractImagePlugin;
import thirdparty.marvin.image.MarvinAttributes;
import thirdparty.marvin.image.MarvinImage;
import thirdparty.marvin.image.MarvinImageMask;

/**
 * Sepia plug-in. Reference: {@link http://forum.java.sun.com/thread.jspa?threadID=728795&messageID=4195478}
 *
 * @author Hugo Henrique Slepicka
 * @version 1.0.2 04/10/2008
 */
public class Sepia extends MarvinAbstractImagePlugin {

    private final int depth;

    public Sepia(int depth) {
        this.depth = depth;
    }

    public void process
            (
                    MarvinImage imageIn,
                    MarvinImage imageOut,
                    MarvinAttributes attributesOut,
                    MarvinImageMask mask,
                    boolean previewMode
            ) {
        int r, g, b, corfinal;

        int width = imageIn.getWidth();
        int height = imageIn.getHeight();

        boolean[][] l_arrMask = mask.getMaskArray();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (l_arrMask == null || l_arrMask[x][y]) {
                    //Capture RGB of pixel...
                    r = imageIn.getIntComponent0(x, y);
                    g = imageIn.getIntComponent1(x, y);
                    b = imageIn.getIntComponent2(x, y);

                    //Define a color as average level of channels...
                    corfinal = (r + g + b) / 3;
                    r = g = b = corfinal;

                    r = truncate(r + (depth * 2));
                    g = truncate(g + depth);

                    //Define a nova cor do ponto...
                    imageOut.setIntColor(x, y, imageIn.getAlphaComponent(x, y), r, g, b);
                }
            }
        }
    }

    /**
     * Sets the RGB between 0 and 255
     *
     * @param a
     * @return
     */
    public int truncate(int a) {
        if (a < 0) return 0;
        else if (a > 255) return 255;
        else return a;
    }

}
