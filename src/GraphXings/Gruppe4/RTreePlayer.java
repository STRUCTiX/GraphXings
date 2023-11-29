package GraphXings.Gruppe4;

import GraphXings.Algorithms.NewPlayer;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import GraphXings.Game.GameState;
import GraphXings.Gruppe4.Common.Helper;
import GraphXings.Gruppe4.Common.TreeHelper;
import GraphXings.Gruppe4.Strategies.MaximizeDiagonalCrossing;
import GraphXings.Gruppe4.Strategies.MaximizePlaceInDenseRegion;
import GraphXings.Gruppe4.Strategies.MinimizePlaceAtBorder;
import GraphXings.Gruppe4.Strategies.MinimizePlaceNextToOpponent;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;
import com.github.davidmoten.rtree2.geometry.internal.PointFloat;
import java.util.*;
import GraphXings.Data.Edge;



public class RTreePlayer implements NewPlayer {

    /**
     * The name of the random player.
     */
    private String name;

    /**
     * The mutable R-Tree structure. Filled with edges.
     */
    private MutableRTree<Edge, LineFloat> tree;

    /**
     * The mutable R-Tree structure filled with the placed vertices.
     */
    private MutableRTree<Vertex, PointFloat> vertexTree;

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

        // Add lines to tree by observing last game move if not empty.
        TreeHelper.additionalLines(g, gs.getVertexCoordinates(), lastMove).ifPresent(lines -> tree.addAll(lines));

        // Add point to the vertex tree by converting the last game move
        TreeHelper.additionalPoint(lastMove).ifPresent(entry -> vertexTree.add(entry));

        // Calculate the game move.
        var maximizer = new MaximizePlaceInDenseRegion(g, gs, tree, vertexTree, width, height);
        Optional<GameMove> move;

        // Check if we've got the first move and must execute the heuristic
        if (gs.getPlacedVertices().isEmpty()) {
            maximizer.executeHeuristic(Optional.ofNullable(lastMove));
        } else {
            maximizer.executeStrategy(lastMove);
        }
        move = maximizer.getGameMove();

        // This is our fallback. If our strategy fails, return a random move
        if (move.isEmpty()) {
            move = Optional.of(Helper.randomMove(g, gs.getUsedCoordinates(), gs.getPlacedVertices(), width, height));
        }

        gs.applyMove(move.get());

        // Add our own move to the trees
        // Add lines to tree by observing last game move if not empty.
        TreeHelper.additionalLines(g, gs.getVertexCoordinates(), lastMove).ifPresent(lines -> tree.addAll(lines));

        // Add point to the vertex tree by converting the last game move
        TreeHelper.additionalPoint(lastMove).ifPresent(entry -> vertexTree.add(entry));

        return move.get();
    }

    @Override
    public GameMove minimizeCrossings(GameMove lastMove)
    {
        if (lastMove != null) {
            gs.applyMove(lastMove);
        }

        // Add lines to tree by observing last game move if not empty.
        TreeHelper.additionalLines(g, gs.getVertexCoordinates(), lastMove).ifPresent(lines -> tree.addAll(lines));

        // Add point to the vertex tree by converting the last game move
        TreeHelper.additionalPoint(lastMove).ifPresent(entry -> vertexTree.add(entry));

        // Calculate the game move.
        var minimizer = new MinimizePlaceAtBorder(g, gs, tree, width, height);
        Optional<GameMove> move;

        // Check if we've got the first move and must execute the heuristic
        if (gs.getPlacedVertices().isEmpty()) {
            minimizer.executeHeuristic(Optional.ofNullable(lastMove));
        } else {
            minimizer.executeStrategy(lastMove);
        }
        move = minimizer.getGameMove();

        // This is our fallback. If our strategy fails, return a random move
        if (move.isEmpty()) {
            move = Optional.of(Helper.randomMove(g, gs.getUsedCoordinates(), gs.getPlacedVertices(), width, height));
        }



        gs.applyMove(move.get());

        // Add our own move to the trees
        // Add lines to tree by observing last game move if not empty.
        TreeHelper.additionalLines(g, gs.getVertexCoordinates(), lastMove).ifPresent(lines -> tree.addAll(lines));

        // Add point to the vertex tree by converting the last game move
        TreeHelper.additionalPoint(lastMove).ifPresent(entry -> vertexTree.add(entry));

        System.out.println("id:" + move.get().getVertex().getId() + ", x:" + move.get().getCoordinate().getX() + ", y:" + move.get().getCoordinate().getY());
        return move.get();
    }

    @Override
    public void initializeNextRound(Graph g, int width, int height, Role role)
    {
        var edges = (HashSet<Edge>) g.getEdges();
        // If we have <10k Edges use the normal R-Tree.
        // Otherwise, use the R*-Tree heuristic.
        if (edges.size() < 10000) {
            tree = new MutableRTree<>(MutableRTree.TreeSetup.SMALL, width, height);
        } else {
            tree = new MutableRTree<>(MutableRTree.TreeSetup.BIG, width, height);
        }

        // Initialize the vertex tree
        var vertices = (HashSet<Vertex>) g.getVertices();
        vertexTree = new MutableRTree<>((vertices.size() < 10000) ? MutableRTree.TreeSetup.SMALL : MutableRTree.TreeSetup.BIG, width, height);

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
