package GraphXings.Gruppe4;

import GraphXings.Algorithms.NewPlayer;
import GraphXings.Data.Coordinate;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import GraphXings.Game.GameState;
import GraphXings.Gruppe4.Common.Helper;
import GraphXings.Gruppe4.Common.TreeHelper;
import GraphXings.Gruppe4.Strategies.*;
import GraphXings.Gruppe4.Gui.GuiExport;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;
import com.github.davidmoten.rtree2.geometry.internal.PointFloat;

import java.io.IOException;
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

    public GuiExport getGuiExport() {
        return guiExport;
    }

    private GuiExport guiExport;

    private GameObserver gameObserver;

    // Set to true if you'd like to export data
    private boolean enableExport = true;

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
        gameObserver.startTimer();

        if (lastMove != null) {
            gs.applyMove(lastMove);
            gameObserver.addOpponentGameMove(lastMove);

            // Last move must have been a minimize move.
            // Therefore, export the move from opponent.
            if (enableExport) {
                try {
                    guiExport.exportVertexPlacement(lastMove, Role.MIN);
                } catch (IOException e) {
                    enableExport = false;
                }
            }
        }

        // Add lines to tree by observing last game move if not empty.
        TreeHelper.additionalLines(g, gs.getVertexCoordinates(), lastMove).ifPresent(lines -> tree.addAll(lines));

        // Add point to the vertex tree by converting the last game move
        TreeHelper.additionalPoint(lastMove).ifPresent(entry -> vertexTree.add(entry));

        // Get the estimated SampleParameters
        var sampleParameters = gameObserver.calculateSampleSizeParameters();

        // Instantiate the strategies
        Strategy[] maximizer = {
                new MaximizePlaceVertexOnEdge(g, gs, tree, width, height, sampleParameters),
                new MaximizePlaceInDenseRegion(g, gs, tree, vertexTree, width, height, sampleParameters),
                new MaximizeDiagonalCrossing(g, gs, tree, width, height, sampleParameters),
                new MaximizePointReflection(g, gs, tree, width, height, sampleParameters),
                new MaximizePointReflectionFromBorder(g, gs, tree, width, height, sampleParameters),
                new MaximizeGrid(g, gs, tree, width, height, sampleParameters),
                new RandomSampleMove(g, gs, tree, width, height, Role.MAX, sampleParameters),
        };

        var threads = new ArrayList<Thread>(4);
        for (var strat : maximizer) {

            threads.add(Thread.ofVirtual().start(() -> {
                // Check if we've got the first move and must execute the heuristic
                if (gs.getPlacedVertices().isEmpty()) {
                    strat.executeHeuristic(Optional.ofNullable(lastMove));
                } else {
                    strat.executeStrategy(lastMove);
                }
            }));
        }

        // This is our fallback. If our strategy fails, return a random move
        var randomMove = new RandomMove(g, gs, tree, width, height, sampleParameters);
        randomMove.executeHeuristic(Optional.ofNullable(lastMove));

        // Calculate the game move.
        Optional<GameMove> move = randomMove.getGameMove();
        long moveQuality = randomMove.getGameMoveQuality();
        StrategyName usedStrategy = randomMove.getStrategyName();

        // Wait for the threads to finish
        for (var t : threads) {
            try {
                t.join(gameObserver.getSingleGameMoveTime());
            } catch (InterruptedException e) {
                // TODO Notify the game observer
            }
        }

        for (var strat : maximizer) {
            // Check the quality
            var currentMove = strat.getGameMove();
            var currentQuality = strat.getGameMoveQuality();

            if (currentMove.isPresent() && currentQuality > moveQuality) {
                moveQuality = currentQuality;
                move = currentMove;
                usedStrategy = strat.getStrategyName();
            }
        }


        gs.applyMove(move.get());
        gameObserver.addOwnGameMove(move.get(), usedStrategy);

        if (enableExport) {
            try {
                guiExport.exportVertexPlacement(move.get(), Role.MAX);
            } catch (IOException e) {
                enableExport = false;
            }
        }

        // Add our own move to the trees
        // Add lines to tree by observing last game move if not empty.
        TreeHelper.additionalLines(g, gs.getVertexCoordinates(), lastMove).ifPresent(lines -> tree.addAll(lines));

        // Add point to the vertex tree by converting the last game move
        TreeHelper.additionalPoint(lastMove).ifPresent(entry -> vertexTree.add(entry));

        gameObserver.stopTimer();
        return move.get();
    }

    @Override
    public GameMove minimizeCrossings(GameMove lastMove)
    {
        gameObserver.startTimer();
        if (lastMove != null) {
            gs.applyMove(lastMove);

            // Last move must have been a maximize move.
            // Therefore, export the move from opponent.
            if (enableExport) {
                try {
                    guiExport.exportVertexPlacement(lastMove, Role.MAX);
                } catch (IOException e) {
                    enableExport = false;
                }
            }
        }

        // Add lines to tree by observing last game move if not empty.
        TreeHelper.additionalLines(g, gs.getVertexCoordinates(), lastMove).ifPresent(lines -> tree.addAll(lines));

        // Add point to the vertex tree by converting the last game move
        TreeHelper.additionalPoint(lastMove).ifPresent(entry -> vertexTree.add(entry));

        // Get the estimated SampleParameters
        var sampleParameters = gameObserver.calculateSampleSizeParameters();

        // Instantiate the strategies
        Strategy[] minimizer = {
                new MinimizePlaceNextToOpponent(g, gs, tree, width, height, sampleParameters),
                new MinimizePlaceAtBorder(g, gs, tree, width, height, sampleParameters),
                new RandomSampleMove(g, gs, tree, width, height, Role.MIN, sampleParameters),
        };

        var threads = new ArrayList<Thread>(4);
        for (var strat : minimizer) {
            threads.add(Thread.ofVirtual().start(() -> {
                // Check if we've got the first move and must execute the heuristic
                if (gs.getPlacedVertices().isEmpty()) {
                    strat.executeHeuristic(Optional.ofNullable(lastMove));
                } else {
                    strat.executeStrategy(lastMove);
                }
            }));
        }

        // This is our fallback. If our strategy fails, return a random move
        var randomMove = new RandomMove(g, gs, tree, width, height, sampleParameters);
        randomMove.executeHeuristic(Optional.ofNullable(lastMove));

        // Calculate the game move.
        Optional<GameMove> move = randomMove.getGameMove();
        long moveQuality = randomMove.getGameMoveQuality();

        // Join the threads
        for (var t : threads) {
            try {
                t.join(gameObserver.getSingleGameMoveTime());
            } catch (InterruptedException e) {
                // TODO Notify the game observer
            }
        }

        // Calculate best move
        for (var strat : minimizer) {
            // Check the quality
            var currentMove = strat.getGameMove();
            var currentQuality = strat.getGameMoveQuality();

            if (currentMove.isPresent() && currentQuality < moveQuality) {
                moveQuality = currentQuality;
                move = currentMove;
            }

        }

        if (move.isPresent()) {
            gs.applyMove(move.get());
        } else {
            // This should never happen but just in case, execute another random move
            var panicMove = new RandomMove(g, gs, tree, width, height, sampleParameters);
            panicMove.executeHeuristic(Optional.ofNullable(lastMove));
            gs.applyMove(panicMove.getGameMove().get());
        }

        if (enableExport) {
            try {
                guiExport.exportVertexPlacement(move.get(), Role.MIN);
            } catch (IOException e) {
                enableExport = false;
            }
        }

        // Add our own move to the trees
        // Add lines to tree by observing last game move if not empty.
        TreeHelper.additionalLines(g, gs.getVertexCoordinates(), lastMove).ifPresent(lines -> tree.addAll(lines));

        // Add point to the vertex tree by converting the last game move
        TreeHelper.additionalPoint(lastMove).ifPresent(entry -> vertexTree.add(entry));

        gameObserver.stopTimer();

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
        gs = new GameState(g, width, height);

        this.gameObserver = new GameObserver(g, role, width, height);

        if (enableExport) {
            try {
                if (guiExport != null) {
                    guiExport.close();
                }
                guiExport = new GuiExport();

                // Export the initial graph
                guiExport.exportGraphStructure(g, role, name);
            } catch (IOException e) {
                enableExport = false;
            }
        }
    }

    @Override
    public String getName()
    {
        return name;
    }

    public long computeMoveQuality (Vertex vertex, Coordinate coordinate){
        var placedVertices = gs.getPlacedVertices();
        var vertexCoordinates = gs.getVertexCoordinates();
        var incidentEdges = g.getIncidentEdges(vertex);
        long current_move_quality = 0;

        //check for all edges that the vertex has, if they are already existing
        for (Edge e : incidentEdges) {
            if(placedVertices.contains(e.getS()) || placedVertices.contains(e.getT())){
                LineFloat edge = null;
                if (e.getT().equals(vertex)){
                    edge = LineFloat.create(vertexCoordinates.get(e.getS()).getX(), vertexCoordinates.get(e.getS()).getY(), coordinate.getX(), coordinate.getY());
                } else {
                    edge = LineFloat.create(coordinate.getX(), coordinate.getY(), coordinate.getX(), coordinate.getY());

                }
                current_move_quality += tree.getIntersections(edge);
            }
        }

        //additionally add all crossings that will be created by the free neighbour edges
        return current_move_quality;
    }

    /**
     * Get the GameObserver instance
     * @return GameObserver instance
     */
    public GameObserver getGameObserver() {
        return gameObserver;
    }

}
