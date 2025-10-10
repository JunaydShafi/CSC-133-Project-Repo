/**package pkgDriver;
public class DriverTest {

    public static void main(String[] args) {
        testCreateSquare();
        testGenerateGrid();
        System.out.println("All tests passed!");
    }

    private static void testCreateSquare() {
        Driver driver = new Driver();
       // float[] square = driver.createSquare(0f, 0f, 10f);// the funct is not private in driver so cannot use this anymore

        assert square.length == 8 : "Square length should be 8";
        assert square[0] == -5f : "Top-left X should be -5";
        assert square[1] == -5f : "Top-left Y should be -5";
        assert square[2] == 5f : "Top-right X should be 5";
        assert square[3] == -5f : "Top-right Y should be -5";
    }

    private static void testGenerateGrid() {
        Driver driver = new Driver();
        //float[][] grid = driver.generateGrid(2, 3, 10f, 1f, 0f);

        assert grid.length == 6 : "Grid should have 6 squares (2*3)";
        for (float[] square : grid) {
            assert square.length == 8 : "Each square should have 8 floats";
        }
    }
}
**/