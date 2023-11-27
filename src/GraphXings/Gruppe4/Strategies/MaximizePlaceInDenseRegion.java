package GraphXings.Gruppe4.Strategies;

import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import GraphXings.Game.GameState;
import GraphXings.Gruppe4.Common.Helper;
import GraphXings.Gruppe4.Common.TreeHelper;
import GraphXings.Gruppe4.Heuristics;
import GraphXings.Gruppe4.MutableRTree;
import GraphXings.Gruppe4.Strategy;
import com.github.davidmoten.rtree2.geometry.Point;
import com.github.davidmoten.rtree2.geometry.Rectangle;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;
import com.github.davidmoten.rtree2.geometry.internal.PointFloat;

import java.util.List;
import java.util.Optional;

public class MaximizePlaceInDenseRegion implements Strategy {

    private final Graph g;
    private final MutableRTree<Edge, LineFloat> tree;
    private final MutableRTree<Vertex, PointFloat> vertexTree;
    private final GameState gs;
    private final int width;
    private final int height;

    private Optional<GameMove> gameMove = Optional.empty();

    private long moveQuality = 0;


    public MaximizePlaceInDenseRegion(Graph g, GameState gs, MutableRTree<Edge, LineFloat> tree, MutableRTree<Vertex, PointFloat> vertexTree, int width, int height) {
        this.g = g;
        this.tree = tree;
        this.vertexTree = vertexTree;
        this.gs = gs;
        this.width = width;
        this.height = height;
    }


    /**
     * Executes the heuristic as the first or second move.
     *
     * @param lastMove Is empty on first move otherwise provides the last opponent game move.
     * @return True on success, false otherwise.
     */
    @Override
    public boolean executeHeuristic(Optional<GameMove> lastMove) {
        gameMove = Heuristics.getFreeGameMoveOnCanvasCenter(g, gs.getUsedCoordinates(), gs.getVertexCoordinates(), null, gs.getPlacedVertices(), width, height);
        return gameMove.isPresent();
    }

    /**
     * Execute the main strategy and calculate a game move.
     * The move must be stored and retrievable by getGameMove.
     *
     * @param lastMove The last opponent move.
     * @return True on success, false otherwise
     */
    @Override
    public boolean executeStrategy(GameMove lastMove) {
        var usedCoordinates = gs.getUsedCoordinates();
        var vertexCoordinates = gs.getVertexCoordinates();
        var placedVertices = gs.getPlacedVertices();

        // Select an unplaced vertex neighbour
        var unplacedVertexOption = Helper.pickIncidentVertex(g, vertexCoordinates, lastMove);
        Vertex unplacedVertex = null;
        if (unplacedVertexOption.isEmpty()) {
            // In this case we have to select any free vertex
            for (var v : g.getVertices()) {
                if (!placedVertices.contains(v)) {
                    unplacedVertex = v;
                    break;
                }
            }
        } else {
            unplacedVertex = unplacedVertexOption.get();
        }

        // Check for a dense edge region
        var rectangleOption = tree.findHighestDensity(TreeHelper.densityGridSize(gs, width, height));

        // Suppress warnings from lambda function
        final Vertex finalUnplacedVertex = unplacedVertex;

        if (rectangleOption.isPresent()) {
            // We've found a rectangle with a high density
            // This is not optimal because we should cross completely through the rectangle
            // to get the max. crossings. But for simplicity we just use this rectangle to search for free coordinates.
            var samples = Helper.randPickFreeCoordinatesPerimeter(gs.getUsedCoordinates(), rectangleOption.get(), 10);
            samples.ifPresent(s -> gameMove = chooseHighestIntersection(tree, finalUnplacedVertex, lastMove.getCoordinate(), s));
        } else {
            // In this case we have no dense edge region. Fallback to a dense vertex region instead.

            // First: get the highest density region of the vertex tree.
            var denseOption = vertexTree.findHighestDensity(TreeHelper.densityGridSize(gs, width, height));

            // Second: place the unplaced vertex inside a dense vertex region.
            // This should cause many intersections in the late game.
            if (denseOption.isPresent()) {
                // Create some samples in that region
                var samples = Helper.randPickFreeCoordinatesPerimeter(gs.getUsedCoordinates(), denseOption.get(), 10);
                samples.ifPresent(s -> gameMove = Optional.of(new GameMove(finalUnplacedVertex, s.getFirst())));
            }
        }

        return gameMove.isPresent();
    }

    /**
     * Retrieve a calculated game move.
     *
     * @return A game move. Empty if execution wasn't successful.
     */
    @Override
    public Optional<GameMove> getGameMove() {
        return gameMove;
    }

    /**
     * Quality of the current game move.
     * This number represents how many crossings can be achieved by a game move.
     * For a maximizer this number should be large.
     *
     * @return Number of crossings.
     */
    @Override
    public long getGameMoveQuality() {
        return moveQuality;
    }

    public Optional<GameMove> chooseHighestIntersection(MutableRTree<Edge, LineFloat> tree, Vertex unplacedVertex, Coordinate maxDistCoordinate, List<Coordinate> samples) {
        Coordinate bestCoord = null;
        var maxCrossings = Long.MIN_VALUE;
        for (var sampleCoord : samples) {

            // Create a line to test for intersections
            var line = LineFloat.create(maxDistCoordinate.getX(), maxDistCoordinate.getY(), sampleCoord.getX(), sampleCoord.getY());
            var numCrossings = tree.getIntersections(line);
            if (numCrossings > maxCrossings) {
                maxCrossings = numCrossings;
                bestCoord = sampleCoord;
            }
        }

        if (maxCrossings <= 0) {
            moveQuality = 0;
            return Optional.empty();
        }

        // Use the best coordinate
        moveQuality = maxCrossings;
        return Optional.of(new GameMove(unplacedVertex, bestCoord));
    }

}