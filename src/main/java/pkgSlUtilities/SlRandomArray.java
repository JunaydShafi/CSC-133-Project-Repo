package pkgSlUtilities;

import java.util.Random;

public class SlRandomArray {

    // Default constructor
    public SlRandomArray() {
    }

    // Fisher–Yates shuffle to randomize an array in-place
    public void randomizeIntegerArray(int[] array) {
        Random rand = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    // Generate a randomized integer array of size n
    public int[] getRandomizedIntegerArray(int n) {
        int[] array = new int[n];
        for (int i = 0; i < n; i++) {
            array[i] = i;
        }
        randomizeIntegerArray(array);
        return array;
    }
}
