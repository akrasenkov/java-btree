package edu.mirea;

public class Triple<A, B, C> {
    private final A left;
    private final B center;
    private final C right;

    public Triple(A left, B center, C right) {
        this.left = left;
        this.center = center;
        this.right = right;
    }

    public A getLeft() {
        return left;
    }

    public B getCenter() {
        return center;
    }

    public C getRight() {
        return right;
    }
}
