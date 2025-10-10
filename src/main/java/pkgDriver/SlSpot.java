package pkgDriver;

public class SlSpot {
    private int row;
    private int col;
    private float size;

    public SlSpot(int row, int col, float size) {
        this.row = row;
        this.col = col;
        this.size = size;
    }

    public float[] getVertices(float originX, float originY) {
        float x0 = originX + col * size;
        float y0 = originY - row * size;

        return new float[]{
                x0, y0,                 // top-left
                x0 + size, y0,          // top-right
                x0 + size, y0 - size,   // bottom-right
                x0, y0 - size           // bottom-left
        };
    }
}
