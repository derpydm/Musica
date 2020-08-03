package sg.edu.tp.seanwong.musica.util;

import java.util.Random;

public class Shuffle {

    private static Random random;

        /**
         * Code from method java.util.Collections.shuffle();
         */
        public static void shuffle(int[] array) {
            if (random == null) random = new Random();
            int count = array.length;
            for (int i = count; i > 1; i--) {
                swap(array, i - 1, random.nextInt(i));
            }
        }

        private static void swap(int[] array, int i, int j) {
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
}
