package edu.mirea;

import java.util.Arrays;

public class Playground {

    public static void main(String[] args) {
        test5();
    }

    private static void test4() {
        BTree tree = new BTree();
        tree.root.entries = new BTree.Entry[]{
                new BTree.Entry(1),
                new BTree.Entry(2),
                new BTree.Entry(3),
                new BTree.Entry(4),
                new BTree.Entry(17)
        };
        tree.root.setEntryCount(5);
    }

    private static void test5() {
        BTree tree = new BTree();
        tree.put(1, 2, 3, 4, 17, 31, 7, 9, 13, 16, 11, 19, 26, 27, 96, 97, 99, 0, 15, 28, 70, 71, 72);
        tree.put(73);
        tree.put(100, 101, -3, -6, -5);
        //tree.put('T', 'X', 'A', 'B', 'J', 'K', 'L', 'N', 'R', 'V', 'Z');
        //tree.put(9, 13, 16, 11, 19, 26, 27, 96, 97, 99);
        System.out.println(tree);
        System.out.println(tree.find(4));
        System.out.println(tree.find(11));
        System.out.println(tree.find(-6));
        System.out.println(tree.find(89));
    }
}
