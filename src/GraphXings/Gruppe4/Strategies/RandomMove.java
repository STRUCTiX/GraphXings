package GraphXings.Gruppe4.Strategies;

import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Game.GameMove;
import GraphXings.Game.GameState;
import GraphXings.Gruppe4.GameObservations.CanvasObservations.SampleParameters;
import GraphXings.Gruppe4.Common.Helper;
import GraphXings.Gruppe4.MutableRTree;
import GraphXings.Gruppe4.StrategiesStopWatch;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;

import java.util.Optional;

public class RandomMove extends StrategyClass {

    public RandomMove(Graph g, GameState gs, MutableRTree<Edge, LineFloat> tree, int width, int height, SampleParameters sampleParameters, StrategiesStopWatch strategiesStopWatch) {
        super(g, gs, tree, width, height, sampleParameters, strategiesStopWatch.getWatch(StrategyName.RandomMove));
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
        gameMove = Optional.of(Helper.randomMove(g, gs.getUsedCoordinates(), gs.getPlacedVertices(), width, height));
        moveQuality = computeMoveQuality(gameMove.get().getVertex(), gameMove.get().getCoordinate());
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

        gameMove = Optional.of(Helper.randomMove(g, gs.getUsedCoordinates(), gs.getPlacedVertices(), width, height));
        moveQuality = computeMoveQuality(gameMove.get().getVertex(), gameMove.get().getCoordinate());

        super.stopExecuteStrategy();
        return true;
    }


    /**
     * This should return a fixed strategy name
     * which is used by the GameObserver.
     *
     * @return A strategy name
     */
    @Override
    public StrategyName getStrategyName() {
        return StrategyName.RandomMove;
    }

    /**
     * Determine if the strategy should be calculated.
     *
     * @param percentagePlacedMoves Value between 0-100. 100 means game is over.
     * @return True if the strategy is effective and should be calculated.
     */
    @Override
    public boolean activateFunction(double percentagePlacedMoves, int currentMove, int totalMoves) {
        return true;
    }
}
