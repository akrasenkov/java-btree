
/******************************************************************************
 *  Compilation:  javac BTreePrinceton2.java
 *  Execution:    java BTreePrinceton2
 *  Dependencies: StdOut.java
 *
 *  B-tree.
 *
 *  Limitations
 *  -----------
 *   -  Assumes M is even and M >= 4
 *   -  should b be an array of childs or list (it would help with
 *      casting to make it a list)
 *
 ******************************************************************************/

package edu.mirea;

/**
 *  The {@code BTreePrinceton2} class represents an ordered symbol table of generic
 *  key-value pairs.
 *  It supports the <em>add</em>, <em>get</em>, <em>contains</em>,
 *  <em>getSize</em>, and <em>is-empty</em> methods.
 *  A symbol table implements the <em>associative array</em> abstraction:
 *  when associating a value with a key that is already in the symbol table,
 *  the convention is to replace the old value with the new value.
 *  Unlike {@link java.util.Map}, this class uses the convention that
 *  values cannot be {@code null}—setting the
 *  value associated with a key to {@code null} is equivalent to deleting the key
 *  from the symbol table.
 *  <p>
 *  This implementation uses a B-tree. It requires that
 *  the key type implements the {@code Comparable} interface and calls the
 *  {@code compareTo()} and method to compare two keys. It does not call either
 *  {@code equals()} or {@code hashCode()}.
 *  The <em>get</em>, <em>add</em>, and <em>contains</em> operations
 *  each make log<sub><em>childsCount</em></sub>(<em>entriesCount</em>) probes in the worst case,
 *  where <em>entriesCount</em> is the number of key-value pairs
 *  and <em>childsCount</em> is the branching factor.
 *  The <em>getSize</em>, and <em>is-empty</em> operations take constant time.
 *  Construction takes constant time.
 *  <p>
 *  For additional documentation, see
 *  <a href="http://algs4.cs.princeton.edu/62btree">Section 6.2</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 */
public class BTreePrinceton2<Key extends Comparable<Key>, Value>  {
    // max childs per B-tree node = M-1
    // (must be even and greater than 2)
    private static final int M = 4;

    private Node root;       // root of the B-tree
    private int height;      // getHeight of the B-tree
    private int entriesCount;           // number of key-value pairs in the B-tree

    // helper B-tree node data type
    private static final class Node {
        private int childsCount;                             // number of childs
        private Entry[] childs = new Entry[M];   // the array of childs

        // create a node with childsCOunt childs
        private Node(int childsCount) {
            this.childsCount = childsCount;
        }
    }

    // internal nodes: only use key and next
    // external nodes: only use key and value
    private static class Entry {
        private Comparable key;
        private final Object val;
        private Node next;     // helper field to iterate over array entries
        public Entry(Comparable key, Object val, Node next) {
            this.key  = key;
            this.val  = val;
            this.next = next;
        }
    }

    /**
     * Initializes an empty B-tree.
     */
    public BTreePrinceton2() {
        root = new Node(0);
    }

    /**
     * Returns true if this symbol table is empty.
     * @return {@code true} if this symbol table is empty; {@code false} otherwise
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the number of key-value pairs in this symbol table.
     * @return the number of key-value pairs in this symbol table
     */
    public int size() {
        return entriesCount;
    }

    /**
     * Returns the getHeight of this B-tree (for debugging).
     *
     * @return the getHeight of this B-tree
     */
    public int height() {
        return height;
    }


    /**
     * Returns the value associated with the given key.
     *
     * @param  key the key
     * @return the value associated with the given key if the key is in the symbol table
     *         and {@code null} if the key is not in the symbol table
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("argument to get() is null");
        return search(root, key, height);
    }

    private Value search(Node node, Key key, int nodeHeight) {
        Entry[] children = node.childs;


        if (nodeHeight == 0) { // external node
            for (int j = 0; j < node.childsCount; j++) {
                if (eq(key, children[j].key)) {
                    return (Value) children[j].val;
                }
            }
        } else { // internal node
            for (int j = 0; j < node.childsCount; j++) {
                if (j+1 == node.childsCount || less(key, children[j+1].key)) {
                    return search(children[j].next, key, nodeHeight-1);
                }
            }
        }
        return null;
    }


    /**
     * Inserts the key-value pair into the symbol table, overwriting the old value
     * with the new value if the key is already in the symbol table.
     * If the value is {@code null}, this effectively deletes the key from the symbol table.
     *
     * @param  key the key
     * @param  val the value
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public void put(Key key, Value val) {
        if (key == null) throw new IllegalArgumentException("argument key to add() is null");
        Node u = insert(root, key, val, height);
        entriesCount++;
        if (u == null) return;

        // need to split root
        Node t = new Node(2);
        t.childs[0] = new Entry(root.childs[0].key, null, root);
        t.childs[1] = new Entry(u.childs[0].key, null, u);
        root = t;
        height++;
    }

    private Node insert(Node node, Key key, Value val, int height) {
        int pos;
        Entry entry = new Entry(key, val, null);

        if (height == 0) { // лист
            for (pos = 0; pos < node.childsCount; pos++) { // проходим по всем детям
                if (less(key, node.childs[pos].key)) {
                    break; // до тех пор, пока не найдем позицию, на которой значение больше текущего
                }
            }
        } else { // нода
            for (pos = 0; pos < node.childsCount; pos++) { // проходим по всем детям
                if ((pos + 1 == node.childsCount) || less(key, node.childs[pos + 1].key)) {
                    // пока следующая не конец
                    // или не найдем позицию, на которой следующее значение больше текущего
                    Node u = insert(node.childs[pos++].next, key, val, height - 1);
                    if (u == null) {
                        return null;
                    }
                    entry.key = u.childs[0].key;
                    entry.next = u;
                    break;
                }
            }
        }

        for (int i = node.childsCount; i > pos; i--) {
            node.childs[i] = node.childs[i-1];
        }

        node.childs[pos] = entry;
        node.childsCount++;

        if (node.childsCount < M) {
            return null;
        } else {
            return split(node);
        }
    }

    // split node in half
    private Node split(Node h) {
        Node t = new Node(M/2);
        h.childsCount = M/2;
        for (int j = 0; j < M/2; j++) {
            t.childs[j] = h.childs[M/2+j];
        }
        return t;
    }

    /**
     * Returns a string representation of this B-tree (for debugging).
     *
     * @return a string representation of this B-tree.
     */
    public String toString() {
        return toString(root, height, "") + "\n";
    }

    private String toString(Node h, int ht, String indent) {
        StringBuilder s = new StringBuilder();
        Entry[] children = h.childs;

        if (ht == 0) {
            for (int j = 0; j < h.childsCount; j++) {
                s.append(indent + children[j].key + " " + children[j].val + "\n");
            }
        }
        else {
            for (int j = 0; j < h.childsCount; j++) {
                if (j > 0) s.append(indent + "(" + children[j].key + ")\n");
                s.append(toString(children[j].next, ht-1, indent + "     "));
            }
        }
        return s.toString();
    }


    // comparison functions - make Comparable instead of Key to avoid casts
    private boolean less(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) < 0;
    }

    private boolean eq(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) == 0;
    }


    /**
     * Unit tests the {@code BTreePrinceton2} data type.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        BTreePrinceton2<Character, Character> tree = new BTreePrinceton2<>();

        tree.put('T', 't');
        tree.put('X', 'x');
        tree.put('A', 'a');
        tree.put('B', 'b');
        tree.put('J', 'j');
        tree.put('K', 'k');
        tree.put('L', 'l');
        tree.put('N', 'n');
        tree.put('R', 'r');
        tree.put('V', 'v');
        tree.put('Z', 'z');


        System.out.println("T:       " + tree.get('T'));
        System.out.println("X:       " + tree.get('V'));
        System.out.println("V:       " + tree.get('V'));
        System.out.println("R:       " + tree.get('R'));
        System.out.println("Z:       " + tree.get('Z'));
        System.out.println("L:       " + tree.get('L'));
        System.out.println();

        System.out.println("getSize:    " + tree.size());
        System.out.println("treeHeight:  " + tree.height());
        System.out.println(tree);
        System.out.println();
    }

}
