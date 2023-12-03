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
import GraphXings.Gruppe4.Strategies.MaximizeDiagonalCrossing;
import GraphXings.Gruppe4.Strategies.MaximizePlaceInDenseRegion;
import GraphXings.Gruppe4.Strategies.MinimizePlaceAtBorder;
import GraphXings.Gruppe4.Strategies.MinimizePlaceNextToOpponent;
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

    private GuiExport guiExport;

    // Set to true if you'd like to export data
    private boolean enableExport = false;

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

        // Calculate the game move.
        var maximizer_vertexOnEdge = new MaximizePlaceVertexOnEdge(g, gs, tree, width, height);
        var maximizer_denseRegion = new MaximizePlaceInDenseRegion(g, gs, tree, vertexTree, width, height);
        var maximizer_diagonalCrossing = new MaximizeDiagonalCrossing(g, gs, tree, width, height);
        Optional<GameMove> move;

        // Check if we've got the first move and must execute the heuristic
        if (gs.getPlacedVertices().isEmpty()) {
            maximizer_vertexOnEdge.executeHeuristic(Optional.ofNullable(lastMove));
            maximizer_diagonalCrossing.executeHeuristic(Optional.ofNullable(lastMove));
            maximizer_denseRegion.executeHeuristic(Optional.ofNullable(lastMove));
        } else {
            maximizer_vertexOnEdge.executeStrategy(lastMove);
            //maximizer_diagonalCrossing.executeStrategy(lastMove);
            maximizer_denseRegion.executeStrategy(lastMove);
        }
        var move_vertexOnEdge = maximizer_vertexOnEdge.getGameMove();
        var move_denseRegion = maximizer_denseRegion.getGameMove();
        var move_diagonalCrossing = maximizer_diagonalCrossing.getGameMove();
        var random_move = Optional.of(Helper.randomMove(g, gs.getUsedCoordinates(), gs.getPlacedVertices(), width, height));

        long quality_vertexOnEdge = maximizer_vertexOnEdge.getGameMoveQuality();
        long quality_denseRegion = maximizer_denseRegion.getGameMoveQuality();
        long quality_diagonalCrossing = maximizer_diagonalCrossing.getGameMoveQuality();
        long quality_randMove = computeMoveQuality(random_move.get().getVertex(), random_move.get().getCoordinate());

        move = move_denseRegion;
        if (move_vertexOnEdge.isPresent() && quality_vertexOnEdge > quality_denseRegion && quality_vertexOnEdge > quality_diagonalCrossing && quality_vertexOnEdge > quality_randMove){
            move = move_vertexOnEdge;
            //System.out.println("Vertex   - Move Quality:" + quality_vertexOnEdge + "(" + quality_denseRegion + ":" + quality_diagonalCrossing + ":" +  quality_randMove + "), # placed nodes:" + gs.getPlacedVertices().size() + " of " + g.getN() + ", percent: " + gs.getPlacedVertices().size()/(double) g.getN());

        }
        if (move_denseRegion.isPresent() && quality_denseRegion > quality_diagonalCrossing && quality_denseRegion > quality_randMove && quality_denseRegion > quality_vertexOnEdge){
            move = move_denseRegion;
            //System.out.println("dense    - Move Quality:" + quality_denseRegion + "(" + quality_randMove + ":" + quality_diagonalCrossing + ":" +  quality_vertexOnEdge + "), # placed nodes:" + gs.getPlacedVertices().size() + " of " + g.getN() + ", percent: " + gs.getPlacedVertices().size()/(double) g.getN());

        }
        if (move_diagonalCrossing.isPresent() && quality_diagonalCrossing > quality_denseRegion && quality_diagonalCrossing > quality_randMove && quality_diagonalCrossing > quality_vertexOnEdge){
            move = move_diagonalCrossing;
            //System.out.println("diagonal - Move Quality:" + quality_diagonalCrossing + "(" + quality_denseRegion + ":" + quality_randMove + ":" +  quality_vertexOnEdge + "), # placed nodes:" + gs.getPlacedVertices().size() + " of " + g.getN() + ", percent: " + gs.getPlacedVertices().size()/(double) g.getN());

        }
        if (move.isEmpty() || quality_randMove > quality_denseRegion && quality_randMove > quality_diagonalCrossing && quality_randMove > quality_vertexOnEdge){
            //System.out.println("Random   - Move Quality:" + quality_randMove + "(" + quality_denseRegion + ":" + quality_diagonalCrossing + ":" +  quality_vertexOnEdge + "), # placed nodes:" + gs.getPlacedVertices().size() + " of " + g.getN() + ", percent: " + gs.getPlacedVertices().size()/(double) g.getN());
            move = random_move;
        }


        gs.applyMove(move.get());

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
        return move.get();
    }

    @Override
    public GameMove minimizeCrossings(GameMove lastMove)
    {
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

        // Calculate the game move.
        var minimize_placeNextToOpponent = new MinimizePlaceNextToOpponent(g, gs, tree, width, height);
        var minimize_placeAtBorder = new MinimizePlaceAtBorder(g, gs, tree, width, height);
        Optional<GameMove> move;

        // Check if we've got the first move and must execute the heuristic
        if (gs.getPlacedVertices().isEmpty()) {
            minimize_placeNextToOpponent.executeHeuristic(Optional.ofNullable(lastMove));
            minimize_placeAtBorder.executeHeuristic(Optional.ofNullable(lastMove));
        } else {
            minimize_placeNextToOpponent.executeStrategy(lastMove);
            minimize_placeAtBorder.executeStrategy(lastMove);
        }

        var move_nextToOpponent = minimize_placeNextToOpponent.getGameMove();
        var move_placeAtBorder = minimize_placeAtBorder.getGameMove();
        var random_move = Optional.of(Helper.randomMove(g, gs.getUsedCoordinates(), gs.getPlacedVertices(), width, height));
        move = move_placeAtBorder;


        long quality_nextToOpponent = minimize_placeNextToOpponent.getGameMoveQuality();
        long quality_randMove = computeMoveQuality(random_move.get().getVertex(), random_move.get().getCoordinate());
        long quality_placeAtBorder = minimize_placeAtBorder.getGameMoveQuality();



        // This is our fallback. If our strategy fails, return a random move

        if (move_nextToOpponent.isPresent() && quality_nextToOpponent < quality_randMove && quality_nextToOpponent < quality_placeAtBorder){
            //System.out.println("Opponent - Move Quality:" + quality_nextToOpponent + "(" + quality_placeAtBorder + ":" + quality_randMove  + "), # placed nodes:" + gs.getPlacedVertices().size() + " of " + g.getN() + ", percent: " + gs.getPlacedVertices().size()/(double) g.getN());
            move = move_nextToOpponent;
        }
        if (move_placeAtBorder.isPresent() && quality_placeAtBorder < quality_nextToOpponent && quality_placeAtBorder < quality_randMove){
            //System.out.println("Border   - Move Quality:" + quality_placeAtBorder + "(" + quality_nextToOpponent + ":" + quality_randMove  + "), # placed nodes:" + gs.getPlacedVertices().size() + " of " + g.getN() + ", percent: " + gs.getPlacedVertices().size()/(double) g.getN());
            //move = move_placeAtBorder;
        }
        if (move.isEmpty() || quality_randMove < quality_nextToOpponent && quality_randMove < quality_placeAtBorder) {
            //System.out.println("Random   - Move Quality:" + quality_randMove + "(" + quality_nextToOpponent + ":" + quality_placeAtBorder  + "), # placed nodes:" + gs.getPlacedVertices().size() + " of " + g.getN() + ", percent: " + gs.getPlacedVertices().size()/(double) g.getN());
            //System.out.println("minimize - Move Quality:" + minimizer.getGameMoveQuality() + ", # placed nodes:" + gs.getPlacedVertices().size() + " of " + g.getN() + ", percent: " + gs.getPlacedVertices().size()/(double) g.getN());
            //System.out.println("random   - Move Quality:" + rand_move_quality + ", # placed nodes:" + gs.getPlacedVertices().size() + " of " + g.getN() + ", percent: " + gs.getPlacedVertices().size()/(double) g.getN());
            move = random_move;
        }


        gs.applyMove(move.get());

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

        if (enableExport) {
            try {
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

}
