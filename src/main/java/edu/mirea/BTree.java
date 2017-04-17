package edu.mirea;

import java.security.Key;
import java.util.*;

import static edu.mirea.BTree.ComparsionHelper.equal;
import static edu.mirea.BTree.ComparsionHelper.less;

public class BTree<KeyType> {

    private static final int tFactor = 3;
    private static final int entriesArraySize = 2 * tFactor;
    private static final int childsArraySize = entriesArraySize + 1;
    private static final int maxEntiresPerNode = 2 * tFactor - 1;
    private static final int maxChildsPerNode = maxEntiresPerNode + 1;

    public Node root;
    public int height;

    public BTree() {
        this.root = new Node();
        this.height = 0;
    }

    public BTree addKeys(Comparable<KeyType>... keys) {
        for (Comparable key : keys) {
            addKey(key);
        }
        return this;
    }

    public BTree addKey(Comparable<KeyType> key) {
        Entry entry = new Entry(key);
        return addEntry(entry);
    }

    public BTree deleteKey(Comparable<KeyType> key) {
        Entry<KeyType> entry = new Entry<>(key);
        return deleteEntry(entry);
    }

    public BTree deleteEntry(Entry entry) {
        List<Entry> entries = root.getEntriesRecursive();
        entries.remove(entry);
        root = new Node();
        height = 0;
        addEntries(entries);
        return this;
    }

    public BTree addEntries(Iterable<Entry> entries) {
        for (Entry entry : entries) {
            addEntry(entry);
        }
        return this;
    }

    public BTree addEntry(Entry<KeyType> entry) {
        Triple<Node, Node, Node> emitted = insert(entry, root, height);
        if (emitted != null) {
            root = emitted.getCenter();
            height++;
        }
        return this;
    }

    public Triple<Node, Node, Node> insert(Entry entry, Node node, int height) {
        int insertPos = findPosToInsertOrdered(entry, node.entries, node.getEntryCount());
        if (height != 0) {
            Triple<Node, Node, Node> emitted = insert(entry, node.childs[insertPos], height - 1);
            if (emitted == null) {
                return null;
            } else {
                entry = emitted.getCenter().entries[0];
                insertPos = findPosToInsertOrdered(entry, node.entries, node.getEntryCount());
                for (int i = node.getChildsCount(); i > insertPos; i--) {
                    node.childs[i] = node.childs[i - 1];
                }
                node.childs[insertPos] = emitted.getLeft();
                node.childs[insertPos + 1] = emitted.getRight();
                node.ensureChildsCount();
            }
        }

        for (int i = node.getEntryCount(); i > insertPos; i--) {
            node.entries[i] = node.entries[i - 1];
        }

        node.entries[insertPos] = entry;
        node.incEntryCount();

        if (node.isEntriesOverfill()) {
            return split(node);
        }
        return null;
    }

    public boolean find(Comparable key) {
        return find(key, root);
    }

    private boolean find(Comparable key, Node node) {
        if (node.getEntryCount() == 0) {
            return false;
        }
        if (node.getChildsCount() == 0) {
            for (int pos = 0; pos < node.getEntryCount(); pos++) {
                if (equal(key, node.entries[pos].getKey())) {
                    return true;
                }
            }
        } else {
            for (int pos = 0; pos < node.getEntryCount(); pos++) {
                if (equal(key, node.entries[pos].getKey())) {
                    return true;
                } else if (less(key, node.entries[pos].getKey())) {
                    Node child = node.childs[pos];
                    boolean found = find(key, child);
                    if (found) {
                        return true;
                    }
                }
            }
            Node lastChild = node.childs[node.getEntryCount()];
            return find(key, lastChild);
        }
        return false;
    }

    /**
     * Find pos in entry array to insert entry respecting ascending order.
     * @param entry object to insert
     * @param entries array to insert into
     * @param arrayEffectiveSize number of non-null objects in entry array
     * @return position to insert
     */
    protected static int findPosToInsertOrdered(Entry entry, Entry[] entries, int arrayEffectiveSize) {
        for (int pos = 0; pos < arrayEffectiveSize; pos++) {
            if (less(entry, entries[pos])) {
                return pos;
            }
        }
        return arrayEffectiveSize;
    }

    /**
     * Split node in half.
     * @return triple containing left, center and right nodes
     */
    public Triple<Node, Node, Node> split(Node node) {
        int partsSize = node.getEntryCount() / 2;
        Node centerPart = new Node();
        Node rightPart = new Node();

        int pos2 = 0;
        // перемещаем правую половину entries в новую правую ноду
        for (int pos1 = partsSize + 1; pos1 < node.getEntryCount(); pos1++) {
            rightPart.entries[pos2] = node.entries[pos1];
            pos2++;
        }
        // перемещаем правую половину childs в новую правую ноду
        pos2 = 0;
        for (int pos1 = partsSize + 1; pos1 < node.getChildsCount(); pos1++) {
            rightPart.childs[pos2] = node.childs[pos1];
            pos2++;
        }

        // перемещаем центр в новую центральную ноду
        centerPart.entries[0] = node.entries[partsSize];
        centerPart.childs[0] = node;
        centerPart.childs[1] = rightPart;
        centerPart.setEntryCount(1);
        centerPart.setChildsCount(2);

        // формируем левую ноду
        node.setEntryCount(partsSize);
        node.setChildsCount(node.hasChilds() ? partsSize + 1 : 0); // или ноль, или максимум

        // формируем правую ноду
        rightPart.setEntryCount(partsSize - 1);
        rightPart.setChildsCount(node.hasChilds() ? partsSize + 1 : 0); // или ноль, или максимум

        // обрезаем entries и childs
        node.truncateEntries();
        node.truncateChilds();

        return new Triple<>(node, centerPart, rightPart);
    }

    @Override
    public String toString() {
        return root.toString();
    }

    protected static class Node {
        private int entryCount;
        private int childsCount;
        public Entry[] entries;
        public Node[] childs;

        public Node() {
            this.entryCount = 0;
            this.childsCount = 0;
            this.entries = new Entry[entriesArraySize];
            this.childs = new Node[childsArraySize];
        }

        public Node(int entryCount, int childsCount) {
            this.entryCount = entryCount;
            this.childsCount = childsCount;
            this.entries = new Entry[entriesArraySize];
            this.childs = new Node[childsArraySize];
        }

        public boolean isFullEntries() {
            return entryCount == maxEntiresPerNode;
        }

        public boolean isFullChilds() {
            return childsCount == maxChildsPerNode;
        }

        public boolean isEntriesOverfill() {
            return entryCount == entriesArraySize;
        }

        public boolean isChildsOverfill() {
            return childsCount == childsArraySize;
        }

        public boolean hasChilds() {
            return childsCount != 0;
        }

        public boolean hasEntries() {
            return entryCount != 0;
        }

        public int getEntryCount() {
            return entryCount;
        }

        public int getChildsCount() {
            return childsCount;
        }

        public void setEntryCount(int entryCount) {
            this.entryCount = entryCount;
        }

        public void setChildsCount(int childsCount) {
            this.childsCount = childsCount;
        }

        public int incEntryCount() {
            return entryCount++;
        }

        public int decEntryCount() {
            return entryCount--;
        }

        public int incChildsCount() {
            return childsCount++;
        }

        public int decChildsCount() {
            return childsCount--;
        }

        public int ensureChildsCount() {
            childsCount = 0;
            for (Node child : childs) {
                if (child != null) {
                    childsCount++;
                }
            }
            return childsCount;
        }

        public int truncateEntries() {
            return Utils.setArrayToNull(entries, entryCount);
        }

        public int truncateChilds() {
            return Utils.setArrayToNull(childs, childsCount);
        }

        public List<Entry> getEntriesRecursive() {
            List<Entry> flatEntires = new LinkedList<>();
            for (Entry entry : entries) {
                if (entry != null) {
                    flatEntires.add(entry);
                }
            }
            for (Node child : childs) {
                if (child != null) {
                    flatEntires.addAll(child.getEntriesRecursive());
                }
            }
            return flatEntires;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Node node = (Node) o;

            if (entryCount != node.entryCount) return false;
            if (childsCount != node.childsCount) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            if (!Arrays.equals(entries, node.entries)) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(childs, node.childs);
        }

        @Override
        public int hashCode() {
            int result = entryCount;
            result = 31 * result + childsCount;
            result = 31 * result + Arrays.hashCode(entries);
            result = 31 * result + Arrays.hashCode(childs);
            return result;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("{");
            for (Entry entry : entries) {
                sb.append(' ').append(entry != null ? entry : 'n');
            }
            sb.append(" ").append("}").append(" -> [");
            for (Node child : childs) {
                sb.append(' ').append(child != null ? child : 'n');
            }
            sb.append("]");
            return sb.toString();
        }
    }

    protected static class Entry<KeyType> implements Comparable<Entry<KeyType>> {
        private Comparable key;

        public Entry(Comparable key) {
            this.key = key;
        }

        public Comparable getKey() {
            return key;
        }

        @Override
        public String toString() {
            return key.toString();
        }

        @Override
        public int compareTo(Entry<KeyType> o) {
            if (o == null) {
                return 100;
            }
            return key.compareTo(o.getKey());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry<?> entry = (Entry<?>) o;

            return key != null ? key.equals(entry.key) : entry.key == null;
        }

        @Override
        public int hashCode() {
            return key != null ? key.hashCode() : 0;
        }
    }

    public static class Triple<A, B, C> {
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
