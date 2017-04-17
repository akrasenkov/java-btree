package edu.mirea;

public interface Tree<KeyType extends Comparable<KeyType>> {

    Tree addKey(KeyType key);

    Tree deleteKey(KeyType key);

    boolean findKey(KeyType key);

}
