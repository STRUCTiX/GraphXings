package GraphXings.Gruppe4.CanvasObservations;

import GraphXings.Algorithms.NewPlayer;
import GraphXings.Data.Graph;
import GraphXings.Game.GameMove;
import GraphXings.Game.GameState;
import GraphXings.Gruppe4.CanvasObservation;
import GraphXings.Gruppe4.Common.Helper;
import GraphXings.Gruppe4.Strategies.StrategyName;

import java.util.List;

public class ObserveBorders implements CanvasObservation {

    private final Graph g;
    private final List<GameMove> ourMoves;
    private final List<GameMove> opponentMoves;
    private final NewPlayer.Role ourRole;
    private int width;
    private int height;
    private GameState gs;
    private int observation = 0;

    public ObserveBorders(Graph g, List<GameMove> ourMoves, List<GameMove> opponentMoves, NewPlayer.Role role, GameState gs, int width, int height) {
        this.g = g;
        this.ourMoves = ourMoves;
        this.opponentMoves = opponentMoves;
        this.ourRole = role;
        this.width = width;
        this.height = height;
        this.gs = gs;
    }

    /**
     * Calculate the observation strategy.
     *
     * @param lastMove The last move of the game.
     */
    @Override
    public void calculateObservation(GameMove lastMove) {
        // Check north and south border
        for (int w = 0; w < width; w++) {
            var north = Helper.isCoordinateFree(gs.getUsedCoordinates(), w, 0);
            var south = Helper.isCoordinateFree(gs.getUsedCoordinates(), w, height - 1);

            // TODO: Check if there was an opponent move. We need a datastructure for better performance
        }

        // Check east and west border
        for (int h = 0; h < height; h++) {
            var east = Helper.isCoordinateFree(gs.getUsedCoordinates(), 0, h);
            var west = Helper.isCoordinateFree(gs.getUsedCoordinates(), width - 1, h);
        }
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
