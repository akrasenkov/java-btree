package edu.mirea;

/**
 * Created by senik11 on 21.03.17.
 */
public class Utils {

    public static String arrayToString(Object[] array) {
        StringBuffer buffer = new StringBuffer("{");
        for (Object o : array) {
            buffer.append(" ").append(o);
        }
        buffer.append(" ").append("}");
        return buffer.toString();
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

    private static void swipeArrayLeft(Object[] array) {
        Object[] swipedLeft = new Object[array.length];
        int swipedPos = 0;
        for (int arrayPos = 0; arrayPos < array.length; arrayPos++) {
            if (array[arrayPos] != null) {
                swipedLeft[swipedPos] = array[arrayPos];
                swipedPos++;
            }
        }
        array = swipedLeft; // relinking
    }

    /**
     * Поиск первого свободного места в массиве.
     * @return первая свободная ячейка массива, или -1, если массив */
    private static int getFreeArrayPos(Object[] array) {
        for (int pos = 0; pos < array.length; pos++) {
            if (array[pos] == null) {
                return pos;
            }
        }
        return -1;
    }

}
