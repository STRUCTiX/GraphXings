package GraphXings.Gruppe4;

import GraphXings.Algorithms.CrossingCalculator;
import GraphXings.Algorithms.Player;
import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import GraphXings.Gruppe4.Common.TreeHelper;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Line;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;

import java.util.*;

import static GraphXings.Gruppe4.Maximize.maximizeMove;
import static GraphXings.Gruppe4.Minimize.*;

public class RTreePlayer implements Player {

    /**
     * The name of the random player.
     */
    private String name;

    /**
     * The immutable R-Tree structure.
     */
    private MutableRTree<Edge, LineFloat> tree;

    /**
     * Creates a random player with the assigned name.
     * @param name
     */
    public RTreePlayer(String name)
    {
        this.name = name;
    }

    @Override
    public GameMove maximizeCrossings(Graph g, HashMap<Vertex, Coordinate> vertexCoordinates, List<GameMove> gameMoves, int[][] usedCoordinates, HashSet<Vertex> placedVertices, int width, int height)
    {
        // Add lines to tree by observing last game move
        var additionalLines = TreeHelper.additionalLines(g, vertexCoordinates, gameMoves);
        additionalLines.ifPresent(entries -> tree.addAll(entries));

        return maximizeMove(g, usedCoordinates, vertexCoordinates, gameMoves, placedVertices, width, height, tree);
    }

    @Override
    public GameMove minimizeCrossings(Graph g, HashMap<Vertex, Coordinate> vertexCoordinates, List<GameMove> gameMoves, int[][] usedCoordinates, HashSet<Vertex> placedVertices, int width, int height)
    {
        // Add lines to tree by observing last game move
        var additionalLines = TreeHelper.additionalLines(g, vertexCoordinates, gameMoves);
        additionalLines.ifPresent(entries -> tree.addAll(entries));

        return minimizeMoveClose(g, usedCoordinates, vertexCoordinates, gameMoves, placedVertices, width, height, tree);
    }

    @Override
    public void initializeNextRound(Graph g, int width, int height, Role role)
    {
        var vertices = (HashSet<Vertex>) g.getVertices();
        // If we have <10k Vertices use the normal R-Tree.
        // Otherwise, use the R*-Tree heuristic.
        if (vertices.size() < 10000) {
            tree = new MutableRTree<>(MutableRTree.TreeSetup.SMALL);
        } else {
            tree = new MutableRTree<>(MutableRTree.TreeSetup.BIG);
        }
    }

    @Override
    public String getName()
    {
        return name;
    }

}
