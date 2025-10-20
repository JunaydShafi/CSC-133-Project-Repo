package pkgSlUtils;

import java.io.*;
import java.util.Scanner;

public class SlIntArray {
    public int[][] arrayData;

    public SlIntArray(int rows, int cols) {
        arrayData = new int[rows][cols];
    }

    public void loadFile(String filename) {
        try (Scanner sc = new Scanner(new File(filename))) {
            int r = 0;
            while (sc.hasNextLine() && r < arrayData.length) {
                String[] parts = sc.nextLine().trim().split("\\s+");
                for (int c = 0; c < parts.length && c < arrayData[0].length; c++) {
                    arrayData[r][c] = Integer.parseInt(parts[c]);
                }
                r++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filename);
        }
    }
}
