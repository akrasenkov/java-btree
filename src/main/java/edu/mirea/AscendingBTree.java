package edu.mirea;

import java.util.*;

import static edu.mirea.Utils.ComparsionHelper.equal;
import static edu.mirea.Utils.ComparsionHelper.less;
import static edu.mirea.Utils.assertNotNull;
import static edu.mirea.Utils.assertTrue;

public class AscendingBTree<KeyType extends Comparable<KeyType>> implements Tree<KeyType> {

    private static final int tFactor = 3;
    private static final int entriesArraySize = 2 * tFactor;
    private static final int childsArraySize = entriesArraySize + 1;

    private Node root;
    private int height;

    public AscendingBTree() {
        this.root = new Node();
        this.height = 0;
    }

    public AscendingBTree<KeyType> addKeys(KeyType... keys) {
        for (KeyType key : keys) {
            addKey(key);
        }
        return this;
    }

    @Override
    public AscendingBTree<KeyType> addKey(KeyType key) {
        assertNotNull("Null keys not supported", key);
        Entry entry = new Entry(key);
        return addEntry(entry);
    }

    @Override
    public AscendingBTree<KeyType> deleteKey(KeyType key) {
        assertNotNull("Null deletion not supported", key);
        Entry<KeyType> entry = new Entry<>(key);
        return deleteEntry(entry);
    }

    private AscendingBTree<KeyType> deleteEntry(Entry<KeyType> entry) {
        List<Entry> entries = root.getEntriesRecursive();
        entries.remove(entry);
        root = new Node();
        height = 0;
        addEntries(entries);
        return this;
    }

    private AscendingBTree<KeyType> addEntries(Iterable<Entry> entries) {
        for (Entry entry : entries) {
            addEntry(entry);
        }
        return this;
    }

    private AscendingBTree<KeyType> addEntry(Entry<KeyType> entry) {
        Triple<Node, Node, Node> emitted = insert(entry, root, height);
        if (emitted != null) {
            root = emitted.getCenter();
            height++;
        }
        return this;
    }

    private Triple<Node, Node, Node> insert(Entry entry, Node node, int height) {
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

    @Override
    public boolean findKey(KeyType key) {
        assertNotNull("Null keys not supported", key);
        return findKey(key, root);
    }

    private boolean findKey(KeyType key, Node node) {
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
                    boolean found = findKey(key, child);
                    if (found) {
                        return true;
                    }
                }
            }
            Node lastChild = node.childs[node.getEntryCount()];
            return findKey(key, lastChild);
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
    private static int findPosToInsertOrdered(Entry entry, Entry[] entries, int arrayEffectiveSize) {
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
    private Triple<Node, Node, Node> split(Node node) {
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
        StringBuilder builder = new StringBuilder();
        builder.append("digraph g {").append("\n");
        builder.append("node [shape = record, height= .1];\n");
        builder.append(root.toString("")).append("\n");
        builder.append("}").append("\n");
        return builder.toString();
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

        public boolean isEntriesOverfill() {
            return entryCount == entriesArraySize;
        }

        public boolean hasChilds() {
            return childsCount != 0;
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

        public int incChildsCount() {
            return childsCount++;
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

        public String toString(String parent) {
            String nodeName = "node" + parent;
            final StringBuilder builder = new StringBuilder();
            builder.append(nodeName + "[label = \"<f0>");
            for (int i = 0; i < entryCount; i++) {
                if (entries[i] != null) {
                    builder.append(String.format(" |%s|<f%d>", entries[i], i + 1));
                }
            }
            builder.append("\"];\n");
            for (int i = 0; i < childsCount; i++) {
                if (childs[i] != null) {
                    builder.append(childs[i].toString(nodeName + i)).append("\n");
                }
            }
            for (int i = 0; i < childsCount; i++) {
                if (childs[i] != null) {
                    builder.append(
                            String.format("\"%s\":f%d -> node%s", nodeName, i, nodeName + i)
                    );
                    builder.append("\n");
                }
            }
            return builder.toString();
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

}
