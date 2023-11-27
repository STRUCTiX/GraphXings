package GraphXings.Gruppe4.Strategies;

import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import GraphXings.Game.GameState;
import GraphXings.Gruppe4.Common.Helper;
import GraphXings.Gruppe4.Heuristics;
import GraphXings.Gruppe4.MutableRTree;
import GraphXings.Gruppe4.Strategy;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;

import java.util.List;
import java.util.Optional;

public class MaximizeDiagonalCrossing implements Strategy {

    private Graph g;
    private MutableRTree<Edge, LineFloat> tree;
    private GameState gs;
    private int width;
    private int height;

    private Optional<GameMove> gameMove;

    private long moveQuality = 0;


    public MaximizeDiagonalCrossing(Graph g, GameState gs, MutableRTree<Edge, LineFloat> tree, int width, int height) {
        this.g = g;
        this.tree = tree;
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

        // TODO: Function refactor / Function rewrite
        // Test for max. distance between last vertex and the corners of the canvas
        var testCoords = new Coordinate[]{
                new Coordinate(0, 0), // left upper corner
                new Coordinate(width, 0), // right upper corner
                new Coordinate(0, height), // left lower corner
                new Coordinate(width, height) // right lower corner
        };

        var maxDistance = Integer.MIN_VALUE;
        Coordinate maxDistCoordinates = testCoords[0];
        for (var c : testCoords) {

            // Calculate the max. distance between the last move and the corners
            var tempDistance = Heuristics.euclideanDistance(lastMove.getCoordinate(), c);
            if (tempDistance > maxDistance) {
                maxDistCoordinates = c;
                maxDistance = tempDistance;
            }
        }

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

        // Pick 10 random coordinates out of a perimeter and test for the max. crossings
        // The perimeter is 1/3 of the width/height
        var samples = Helper.randPickFreeCoordinatesPerimeter(usedCoordinates, maxDistCoordinates, width / 3, height / 3, 10);

        // Test for max. crossings
        if (samples.isPresent()) {
            gameMove = chooseHighestIntersection(tree, unplacedVertex, maxDistCoordinates, samples.get());
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
