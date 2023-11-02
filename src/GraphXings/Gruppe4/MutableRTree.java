package GraphXings.Gruppe4;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometry;

public class MutableRTree<T, S extends Geometry> {

    private RTree<T, S> tree;

    public enum TreeSetup {
        // Less than 10k entries
        SMALL,

        // Greater than 10k entries
        BIG
    }

    public MutableRTree(TreeSetup size) {
        if (size == TreeSetup.SMALL) {
            tree = RTree.maxChildren(4).create();
        } else {
            tree = RTree.star().maxChildren(6).create();
        }
    }

    public MutableRTree() {
        // Use a R-Tree by default
        tree = RTree.maxChildren(4).create();
    }

    public void add(T value, S geometry) {
        tree = tree.add(value, geometry);
    }

    public RTree<T, S> get() {
        return tree;
    }

}
