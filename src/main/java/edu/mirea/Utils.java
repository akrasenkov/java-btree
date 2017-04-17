package edu.mirea;

public class Utils {

    public static void assertNotNull(String message, Object object) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertTrue(String message, boolean expression) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    public static int setArrayToNull(Object[] array, int from) {
        if (from >= array.length) {
            return 0;
        }
        int counter = 0;
        for (int pos = from; pos < array.length; pos++) {
            if (array[pos] != null) {
                array[pos] = null;
                counter++;
            }
        }
        return counter;
    }

    public static class ComparsionHelper {

        public static boolean equal(Comparable object, Comparable with) {
            return object.compareTo(with) == 0;
        }

        public static boolean more(Comparable object, Comparable than) {
            return object.compareTo(than) > 0;
        }

        public static boolean less(Comparable object, Comparable than) {
            return object.compareTo(than) < 0;
        }

    }
}
