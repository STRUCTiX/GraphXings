package GraphXings.Gruppe4;

import GraphXings.Data.Coordinate;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.Iterables;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Line;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;

import java.util.*;
import java.util.stream.StreamSupport;

public class MutableRTree<T, S extends Geometry> {

    private RTree<T, S> tree;
    private TreeSetup setup;

    public enum TreeSetup {
        // Less than 10k entries
        SMALL,

        // Greater than 10k entries
        BIG
    }

    public MutableRTree(TreeSetup size) {
        setup = size;
        initRTree();
    }

    private void initRTree() {
        if (setup == TreeSetup.SMALL) {
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

    public void addAll(List<Entry<T, S>> entries) {
        tree = tree.add(entries);
    }

    public RTree<T, S> get() {
        return tree;
    }

    public void reset() {
        initRTree();
    }

    public long getIntersections(S geometry) {
        // retrieve the minimal bounding box from the geometry
        var rectBB = geometry.mbr();

        // Get all potential intersections with the given bounding box
        Iterable<Entry<T, S>> potentialIntersections = tree.search(rectBB);

        // Count all elements
        return Iterables.size(potentialIntersections);
    }

}
