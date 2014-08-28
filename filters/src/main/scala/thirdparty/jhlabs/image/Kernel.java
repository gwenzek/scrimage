package thirdparty.jhlabs.image;

public class Kernel{

    public final int width, height, xOrigin, yOrigin;
    public final float[] data;

    public Kernel(int width, int height, float[] data) {
        this.width = width;
        this.height = height;
        this.data = data;
        this.xOrigin = (width - 1) / 2;
        this.yOrigin = (height - 1) / 2;
    }
}
