package GraphXings.Gruppe4.Strategies;

import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import GraphXings.Game.GameState;
import GraphXings.Gruppe4.Common.Helper;
import GraphXings.Gruppe4.GameObservations.CanvasObservations.SampleParameters;
import GraphXings.Gruppe4.GameObservations.ValuableVertices;
import GraphXings.Gruppe4.Heuristics;
import GraphXings.Gruppe4.MutableRTree;
import GraphXings.Gruppe4.StrategiesStopWatch;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;

import java.util.List;
import java.util.Optional;

/**
 * This maximizer places the vertices with the most incident edges first.
 * These will be placed in the left upper and right lower corner.
 */
public class MaximizePlaceHighIncidentEdgesAngle extends StrategyClass {

    ValuableVertices valuableVertices;

    public MaximizePlaceHighIncidentEdgesAngle(Graph g, GameState gs, MutableRTree<Edge, LineFloat> tree, int width, int height, SampleParameters sampleParameters, StrategiesStopWatch strategiesStopWatch, ValuableVertices valuableVertices) {
        super(g, gs, tree, width, height, sampleParameters, strategiesStopWatch.getWatch(StrategyName.MaximizePlaceHighIncidentEdgesAngle));
        this.valuableVertices = valuableVertices;
        moveQuality = 0;
    }


    /**
     * Executes the heuristic as the first or second move.
     *
     * Heuristic: Places a vertex into the middle of the match field
     *
     * @param lastMove Is empty on first move otherwise provides the last opponent game move.
     * @return True on success, false otherwise.
     */
    @Override
    public boolean executeHeuristic(Optional<GameMove> lastMove) {
        var vertex = valuableVertices.getAndRemoveVertexWithMostEdges();
        gameMove = Optional.of(new GameMove(vertex, new Coordinate(0, 0)));
        return true;
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
        super.startExecuteStrategy();

        var usedCoordinates = gs.getUsedCoordinates();
        var vertexCoordinates = gs.getVertexCoordinates();
        var placedVertices = gs.getPlacedVertices();

        // Test for max. distance between last vertex and the corners of the canvas
        var testCoords = new Coordinate[]{
                new Coordinate(0, 0), // left upper corner
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
        var valuableUnplacedVertex = valuableVertices.getAndRemoveVertexWithMostEdges();
        Vertex unplacedVertex = null;
        if (valuableUnplacedVertex == null) {
            // In this case we have to select any free vertex
            for (var v : g.getVertices()) {
                if (!placedVertices.contains(v)) {
                    unplacedVertex = v;
                    break;
                }
            }
        } else {
            //
            unplacedVertex = valuableUnplacedVertex;
        }

        // Pick 10/more/less random coordinates out of a perimeter and test for the max. crossings
        // The perimeter is 1/4 of the width/height
        var samples = Helper.randPickFreeCoordinatesPerimeter(usedCoordinates, maxDistCoordinates, width / 4, height / 4, sampleParameters.samples());

        // Test for max. crossings
        if (samples.isPresent() && unplacedVertex != null) {
            gameMove = chooseHighestIntersection(List.of(unplacedVertex), samples.get());
        }

        super.stopExecuteStrategy();

        return gameMove.isPresent();
    }

    /**
     * This should return a fixed strategy name
     * which is used by the GameObserver.
     *
     * @return A strategy name
     */
    @Override
    public StrategyName getStrategyName() {
        return StrategyName.MaximizePlaceHighIncidentEdgesAngle;
    }

    /**
     * Determine if the strategy should be calculated.
     *
     * @param percentagePlacedMoves Value between 0-100. 100 means game is over.
     * @return True if the strategy is effective and should be calculated.
     */
    @Override
    public boolean activateFunction(double percentagePlacedMoves, int currentMove, int totalMoves) {
        // Use this strategy only for the 20 most valuable moves
        return currentMove < 20;
    }
}
