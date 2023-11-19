package GraphXings.Gruppe4;

import GraphXings.Data.Coordinate;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.Iterables;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Line;
import com.github.davidmoten.rtree2.geometry.Rectangle;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;
import com.github.davidmoten.rtree2.geometry.internal.RectangleFloat;

import java.util.*;
import java.util.stream.StreamSupport;

public class MutableRTree<T, S extends Geometry> {

    private RTree<T, S> tree;
    private TreeSetup setup;
    private int width = 0;
    private int height = 0;

    public enum TreeSetup {
        // Less than 10k entries
        SMALL,

        // Greater than 10k entries
        BIG
    }

    public MutableRTree(TreeSetup size, int width, int height) {
        setup = size;
        initRTree(width, height);
    }

    private void initRTree(int width, int height) {
        this.width = width;
        this.height = height;
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

    public void reset(int width, int height) {
        initRTree(width, height);
    }

    public long getIntersections(S geometry) {
        // retrieve the minimal bounding box from the geometry
        var rectBB = geometry.mbr();

        // Get all potential intersections with the given bounding box
        Iterable<Entry<T, S>> potentialIntersections = tree.search(rectBB);

        // Count all elements
        return Iterables.size(potentialIntersections);
    }

    public Optional<Rectangle> findHighestDensity(int tiling) {
        int w = width / tiling;
        int h = height / tiling;

        Rectangle highestDensity = null;
        long maxCrossings = Long.MIN_VALUE;
        for (int i = 0; i < tiling - 1; i++) {
            for (int k = 0; k < tiling - 1; k++) {
                var rect = RectangleFloat.create(i * w, k * h, (i + 1) * w, (k + 1) * h);
                var crossings = Iterables.size(tree.search(rect));
                if (crossings > maxCrossings) {
                    highestDensity = rect;
                    maxCrossings = crossings;
                }
            }
        }

        if (highestDensity == null || maxCrossings <= 0) {
            return Optional.empty();
        }
        return Optional.of(highestDensity);
    }

}
