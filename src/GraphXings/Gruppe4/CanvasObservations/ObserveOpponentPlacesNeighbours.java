package GraphXings.Gruppe4.CanvasObservations;

import GraphXings.Data.Graph;
import GraphXings.Game.GameMove;
import GraphXings.Gruppe4.CanvasObservation;
import GraphXings.Gruppe4.Strategies.StrategyName;

import java.util.List;

public class ObserveOpponentPlacesNeighbours implements CanvasObservation {

    private final Graph g;
    private final List<GameMove> lastMoves;

    public ObserveOpponentPlacesNeighbours(Graph g, List<GameMove> lastMoves) {
        this.g = g;
        this.lastMoves = lastMoves;
    }

    /**
     * Calculate the observation strategy.
     *
     * @param lastMove The last move of the game.
     */
    @Override
    public void calculateObservation(GameMove lastMove) {

    }

    /**
     * Retrieve the observation number between 0-100.
     * This value represents the matching percentage.
     *
     * @return An integer between 0-100
     */
    @Override
    public int getObservation() {
        return 0;
    }

    /**
     * If a strategy was observed get a effective counter-attack.
     *
     * @return a strategy name
     */
    @Override
    public StrategyName getEffectiveCounterStrategy() {
        return null;
    }
}
