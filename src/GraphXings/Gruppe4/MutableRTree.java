package GraphXings.Gruppe4;

import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import java.util.Vector;
import java.util.stream.StreamSupport;

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

    public long getIntersections(S geometry) {
        // retrieve the minimal bounding box from the geometry
        var rectBB = geometry.mbr();

        // Get all potential intersections with the given bounding box
        Iterable<Entry<T, S>> potentialIntersections = tree.search(rectBB);

        // Count all elements
        return StreamSupport.stream(potentialIntersections.spliterator(), false).count();
    }

}
