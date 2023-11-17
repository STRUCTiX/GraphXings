package GraphXings.Gruppe4;

import GraphXings.Algorithms.CrossingCalculator;
import GraphXings.Algorithms.NewPlayer;
import GraphXings.Algorithms.Player;
import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import GraphXings.Game.GameState;
import GraphXings.Gruppe4.Common.TreeHelper;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Line;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;

import java.util.*;

import static GraphXings.Gruppe4.Maximize.maximizeMoveOptimize;
import static GraphXings.Gruppe4.Minimize.*;

public class RTreePlayer implements NewPlayer {

    /**
     * The name of the random player.
     */
    private String name;

    /**
     * The immutable R-Tree structure.
     */
    private MutableRTree<Edge, LineFloat> tree;

    /**
     * The current game state
     */
    private GameState gs;

    private int width;

    private int height;

    private Graph g;

    /**
     * Creates a random player with the assigned name.
     * @param name
     */
    public RTreePlayer(String name)
    {
        this.name = name;
    }

    @Override
    public GameMove maximizeCrossings(GameMove lastMove)
    {
        if (lastMove != null) {
            gs.applyMove(lastMove);
        }

        // Add lines to tree by observing last game move
        var additionalLines = TreeHelper.additionalLines(g, gs.getVertexCoordinates(), lastMove);
        additionalLines.ifPresent(entries -> tree.addAll(entries));

        var move = maximizeMoveOptimize(g, gs.getUsedCoordinates(), gs.getVertexCoordinates(), lastMove, gs.getPlacedVertices(), width, height, tree);
        gs.applyMove(move);
        return move;
    }

    @Override
    public GameMove minimizeCrossings(GameMove lastMove)
    {
        if (lastMove != null) {
            gs.applyMove(lastMove);
        }
        
        // Add lines to tree by observing last game move
        var additionalLines = TreeHelper.additionalLines(g, gs.getVertexCoordinates(), lastMove);
        additionalLines.ifPresent(entries -> tree.addAll(entries));

        var move = minimizeMoveClose(g, gs.getUsedCoordinates(), gs.getVertexCoordinates(), lastMove, gs.getPlacedVertices(), width, height, tree);
        gs.applyMove(move);
        return move;
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
        this.g = g;
        this.width = width;
        this.height = height;
        gs = new GameState(width, height);
    }

    @Override
    public String getName()
    {
        return name;
    }

}
