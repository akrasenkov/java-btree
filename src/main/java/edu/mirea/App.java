package edu.mirea;

import static edu.mirea.Utils.assertTrue;

public class App implements Runnable {

    private static Runnable instance;

    public App() {
    }

    public static void main(String[] args) {
        instance = new App();
        instance.run();
    }

    public void run() {
        testNumeric();
        System.out.println();
        testSymbolic();
    }

    private void testNumeric() {
        AscendingBTree<Integer> tree = new AscendingBTree<>();
        tree.addKeys(1, 2, 3, 4, 17, 31, 7, 9, 13, 16, 11, 19, 26, 27, 96, 97, 99, 0, 15, 28, 70, 71, 72);
        tree.addKeys(73);
        tree.addKeys(100, 101, -3, -6, -5);

        int[] trueNums = new int[] {4, 11, -6};
        int[] falseNums = new int[] {89, 1000, -666, 5};
        for (int num : trueNums) {
            assertTrue("Key exists, but not found: " + num, tree.findKey(num));
        }
        for (int num : falseNums) {
            assertTrue("Key not exists, but found: " + num, !tree.findKey(num));
        }

        int[] toDeleteNums = new int[] {4, 11};
        for (int num : toDeleteNums) {
            tree.deleteKey(num);
            assertTrue("Failed key deletion: " + num, !tree.findKey(num));
        }

        System.out.println(tree.toString());
    }

    private void testSymbolic() {
        AscendingBTree<Character> tree = new AscendingBTree<>();
        tree.addKeys('T', 'X', 'A', 'B', 'J', 'K', 'L', 'N', 'R', 'V', 'Z');

        char[] trueChars = new char[] {'X', 'A', 'Z'};
        char[] falseChars = new char[] {'q', 'Q', 'P'};

        for (char ch : trueChars) {
            assertTrue("Key exists, but not found: " + ch, tree.findKey(ch));
        }
        for (char ch : falseChars) {
            assertTrue("Key not exists, but found: " + ch, !tree.findKey(ch));
        }

        char[] toDeleteChars = new char[] {'A', 'R'};
        for (char ch : toDeleteChars) {
            tree.deleteKey(ch);
            assertTrue("Failed key deletion: " + ch, !tree.findKey(ch));
        }

        System.out.println(tree.toString());
    }
}
