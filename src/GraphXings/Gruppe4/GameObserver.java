package GraphXings.Gruppe4;

import GraphXings.Algorithms.NewPlayer;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import GraphXings.Gruppe4.Strategies.StrategyName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GameObserver {

    private final int totalVerticesCount;
    private int currentVerticesCount = 0;
    private NewPlayer.Role ourRole;
    private final HashMap<StrategyName, Integer> strategyNamesCounts;
    private ArrayList<GameMove> ourMoves;
    private ArrayList<GameMove> opponentMoves;
    private long startTime;
    private long totalElapsedTime = 0;
    private long currentGameMoveTime = 0;
    private final long timeLimit = 300000000000L;

    public GameObserver(Graph g, NewPlayer.Role ourRole) {
        totalVerticesCount = ((HashSet<Vertex>) g.getVertices()).size();
        strategyNamesCounts = new HashMap<>(StrategyName.values().length);
        ourMoves = new ArrayList<>(totalVerticesCount / 2);
        opponentMoves = new ArrayList<>(totalVerticesCount / 2);

        this.ourRole = ourRole;
    }

    public void addOwnGameMove(GameMove move, StrategyName strategyName) {
        currentVerticesCount += 1;

        incrementUsedStrategies(strategyName);
        ourMoves.add(move);
    }

    public void addOpponentGameMove(GameMove move) {
        currentVerticesCount += 1;

        opponentMoves.add(move);
    }

    /**
     * Returns how many moves are left before the game ends
     * @return Amount of remaining moves
     */
    public int remainingMoves() {
        return totalVerticesCount - currentVerticesCount;
    }

    /**
     * Returns a double between 0-100% of already placed moves
     * @return A double
     */
    public double percentagePlacedMoves() {
        return currentVerticesCount / (double) totalVerticesCount * 100.0;
    }

    public void startTimer() {
        startTime = System.nanoTime();
    }

    public void stopTimer() {
       var stopTime = System.nanoTime();

       currentGameMoveTime = stopTime - startTime;

       totalElapsedTime += currentGameMoveTime;
    }

    public long getTotalElapsedTime() {
        return totalElapsedTime;
    }

    public long getSingleGameMoveTime() {
        long gameMove = timeLimit / totalVerticesCount;

        // One game move should at least have 5ms time to compute
        if (gameMove < 5) {
            return 5;
        }
        return gameMove;
    }

    /**
     * Count how often we use our own strategies.
     * @param strategyName The strategy name which should get incremented
     */
    private void incrementUsedStrategies(StrategyName strategyName) {
        if (strategyNamesCounts.containsKey(strategyName)) {
            // Counter value is already present. Get, increment, put
            var count = strategyNamesCounts.get(strategyName);
            count += 1;
            strategyNamesCounts.put(strategyName, count);
        } else {
            strategyNamesCounts.put(strategyName, 1);
        }
    }
}
