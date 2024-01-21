package GraphXings.Gruppe4.Strategies;

import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import GraphXings.Game.GameState;
import GraphXings.Gruppe4.Common.Helper;
import GraphXings.Gruppe4.Common.TreeHelper;
import GraphXings.Gruppe4.GameObservations.CanvasObservations.SampleParameters;
import GraphXings.Gruppe4.GameObservations.ValuableVertices;
import GraphXings.Gruppe4.MutableRTree;
import GraphXings.Gruppe4.StrategiesStopWatch;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;
import com.github.davidmoten.rtree2.geometry.internal.PointFloat;

import java.util.Optional;

public class MinimizePlaceEllipseAngle extends StrategyClass {

    private final ValuableVertices valuableVertices;

    public MinimizePlaceEllipseAngle(Graph g, GameState gs, MutableRTree<Edge, LineFloat> tree, int width, int height, SampleParameters sampleParameters, StrategiesStopWatch strategiesStopWatch, ValuableVertices valuableVertices) {
        super(g, gs, tree, width, height, sampleParameters, strategiesStopWatch.getWatch(StrategyName.MinimizePlaceEllipseAngle));
        this.valuableVertices = valuableVertices;
        moveQuality = 0;
    }


    /**
     * Puts a vertex at Coordinate (0,0)
     * this should be possible, because it is the first move in the game, since the maximizer begins
     *
     * @param lastMove Is empty on first move otherwise provides the last opponent game move.
     * @return True .
     */
    @Override
    public boolean executeHeuristic(Optional<GameMove> lastMove) {

        var vertex = valuableVertices.getAndRemoveVertexWithMostEdges();
        var coordinate = valuableVertices.getAndRemoveCoordinate();
        coordinate.ifPresent(value -> gameMove = Optional.of(new GameMove(vertex, value)));

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

        // Place the most valuable vertices on a ellipse to maximize crossing angles
        var vertex = valuableVertices.getAndRemoveVertexWithMostEdges();
        var coordinate = valuableVertices.getAndRemoveCoordinate();

        coordinate.ifPresent(value -> gameMove = Optional.of(new GameMove(vertex, value)));


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
        return StrategyName.MinimizePlaceEllipseAngle;
    }

    /**
     * Determine if the strategy should be calculated.
     *
     * @param percentagePlacedMoves Value between 0-100. 100 means game is over.
     * @return True if the strategy is effective and should be calculated.
     */
    @Override
    public boolean activateFunction(double percentagePlacedMoves, int currentMove, int totalMoves) {
        return valuableVertices.getCoordinateSize() > 0;
    }
}
