package edu.mirea;

/******************************************************************************
 *  Compilation:  javac BTreePrinceton2.java
 *  Execution:    java BTreePrinceton2
 *  Dependencies: StdOut.java
 *
 *  B-tree.
 *
 *  Limitations
 *  -----------
 *   -  Assumes tFactor is even and tFactor >= 4
 *   -  should b be an array of entries or list (it would help with
 *      casting to make it a list)
 *
 ******************************************************************************/

public class BTreePrinceton<Key extends Comparable<Key>, Value>  {
    // max entries per B-tree node = tFactor-1
    // (must be even and greater than 2)
    private final int tFactor;

    private Node root;       // root of the B-tree
    private int treeHeight;      // treeHeight of the B-tree
    private int entryCount;           // number of key-value pairs in the B-tree

    public BTreePrinceton(int tFactor) {
        this.tFactor = tFactor;
        this.root = new Node(0, tFactor);
    }

    private static class Node {
        private int entriesCount;
        private Entry[] entries;

        private Node(int entriesCount, int tFactor) {
            this.entriesCount = entriesCount;
            this.entries = new Entry[tFactor];
        }

        public void truncate() {
            for (int pos = entriesCount; pos < entries.length; pos++) {
                this.entries[pos] = null;
            }
        }

        public int getEntriesCount() {
            return entriesCount;
        }

        public void setEntriesCount(int entriesCount) {
            this.entriesCount = entriesCount;
        }

        public void incChildrenCount() {
            this.entriesCount++;
        }

        public void decChildrenCount() {
            this.entriesCount--;
        }

        public Entry[] getEntries() {
            return entries;
        }

        public void setEntries(Entry[] entries) {
            this.entries = entries;
        }

        @Override
        public String toString() {
            return "Node(" + entriesCount + ")";
        }
    }

    // internal nodes: only use key and nextNode
    // external nodes: only use key and value
    private static class Entry {
        private final Comparable key;
        private Node nextNode;

        public Entry(Comparable key, Node nextNode) {
            this.key = key;
            this.nextNode = nextNode;
        }

        public Comparable getKey() {
            return key;
        }

        public Node getNextNode() {
            return nextNode;
        }

        public void setNextNode(Node nextNode) {
            this.nextNode = nextNode;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Entry{");
            sb.append("key=").append(key);
            sb.append(", nextNode=").append(nextNode);
            sb.append('}');
            return sb.toString();
        }
    }

    public boolean isEmpty() {
        return entryCount == 0;
    }

    public int getSize() {
        return entryCount;
    }

    public int getHeight() {
        return treeHeight;
    }


    public Value get(Key key) {
        if (key == null) {
            throw new IllegalArgumentException("Can not get value for null key!");
        }
        return find(key, root, treeHeight);
    }

    private Value find(Key key, Node targetNode, int searchingHeight) {
        Entry[] children = targetNode.getEntries();

        if (searchingHeight != 0) {
            // Поиск среди ветвей
            for (int pos = 0; pos < targetNode.getEntriesCount(); pos++) {
                int nextPos = pos + 1;
                if (nextPos == targetNode.getEntriesCount() || ComparsionHelper.less(key, children[nextPos].getKey())) {
                    return find(key, children[pos].getNextNode(), searchingHeight - 1);
                }
            }
        } else {
            // Поиск среди листьев
            for (int pos = 0; pos < targetNode.getEntriesCount(); pos++) {
                if (ComparsionHelper.equals(key, children[pos].getKey())) {
                    return (Value) children[pos].getKey();
                }
            }
        }
        return null;
    }

    public void add(Key key) {
        if (key == null) {
            throw new IllegalArgumentException("Null keys are not supported");
        }
        Node emittedNode = insert(root, key, treeHeight);
        entryCount++;
        if (emittedNode != null) {
            // need to splitNode root
            Node newRoot = new Node(2, tFactor);
            newRoot.getEntries()[0] = new Entry(root.getEntries()[0].getKey(), root);
            newRoot.getEntries()[1] = new Entry(emittedNode.getEntries()[0].getKey(), emittedNode);
            root = newRoot;
            treeHeight++;
        }
    }

    private Node insert(Node target, Key key, int insertingHeight) {
        int pos;
        Entry entry = new Entry(key, null);

        if (insertingHeight == 0) {
            // Вставка среди листьев
            for (pos = 0; pos < target.getEntriesCount(); pos++) {
                Entry leaf = target.getEntries()[pos];
                if (ComparsionHelper.less(key, leaf.getKey())) {
                    break;
                }
            }
        } else {
            // Вставка среди ветвей
            for (pos = 0; pos < target.getEntriesCount(); pos++) {
                int nextPos = pos + 1;
                Entry nextEntry = target.getEntries()[nextPos];
                if ((nextPos == target.getEntriesCount()) || ComparsionHelper.less(key, nextEntry.getKey())) {
                    Node node = insert(nextEntry.getNextNode(), key, insertingHeight - 1);
                    pos = nextPos;
                    if (node == null) {
                        return null;
                    }
                    Entry nodeFirstChild = node.getEntries()[0];
                    entry.setNextNode(node);
                    break;
                }
            }
        }

        Entry[] targetChilds = target.getEntries();
        for (int i = target.getEntriesCount(); i > pos; i--) {
            targetChilds[i] = targetChilds[i - 1];
        }
        targetChilds[pos] = entry;
        target.entriesCount++; // target.incChildrenCount();
        if (target.getEntriesCount() < tFactor) {
            return null;
        } else {
            return splitNode(target);
        }
    }

    // Деление ноды пополам
    private Node splitNode(Node node) {
        int halfNodeChildCount = tFactor / 2;
        Node nextHalf = new Node(halfNodeChildCount, tFactor);
        node.setEntriesCount(halfNodeChildCount);
        for (int pos = 0; pos <= halfNodeChildCount; pos++) {
            Entry[] firstHalfChilds = nextHalf.getEntries();
            firstHalfChilds[pos] = node.getEntries()[halfNodeChildCount + pos];
        }

        node.truncate();

        return nextHalf;
    }

    public String toString() {
        return toString(root, treeHeight, "") + "\n";
    }

    private String toString(Node h, int ht, String indent) {
        StringBuilder s = new StringBuilder();
        Entry[] children = h.entries;

        if (ht == 0) {
            for (int j = 0; j < h.entriesCount; j++) {
                s.append(indent + children[j].key + "\n");
            }
        }
        else {
            for (int j = 0; j < h.entriesCount; j++) {
                if (j > 0) s.append(indent + "(" + children[j].key + ")\n");
                s.append(toString(children[j].nextNode, ht-1, indent + "     "));
            }
        }
        return s.toString();
    }

    private static class ComparsionHelper {

        private static boolean equals(Comparable object, Comparable with) {
            return object.compareTo(with) == 0;
        }

        private static boolean more(Comparable object, Comparable than) {
            return object.compareTo(than) > 0;
        }

        private static boolean less(Comparable object, Comparable than) {
            return object.compareTo(than) < 0;
        }

    }

    /**
     * Unit tests the {@code BTreePrinceton2} data type.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        BTreePrinceton<Character, Character> tree = new BTreePrinceton<>(5);

        // TXABJKLNRVZ

        tree.add('T');
        tree.add('X');
        tree.add('A');
        tree.add('B');
        tree.add('J');
        tree.add('K');
        tree.add('L');
        tree.add('N');
        tree.add('R');
        tree.add('V');
        tree.add('Z');


        System.out.println("T:       " + tree.get('T'));
        System.out.println("X:       " + tree.get('V'));
        System.out.println("V:       " + tree.get('V'));
        System.out.println("R:       " + tree.get('R'));
        System.out.println("Z:       " + tree.get('Z'));
        System.out.println("L:       " + tree.get('L'));
        System.out.println();

        System.out.println("getSize:    " + tree.getSize());
        System.out.println("treeHeight:  " + tree.getHeight());
        System.out.println(tree);
        System.out.println();
    }
}
