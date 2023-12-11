package GraphXings.Gruppe4;

import GraphXings.Algorithms.NewPlayer;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import GraphXings.Gruppe4.CanvasObservations.ObserveBorders;
import GraphXings.Gruppe4.CanvasObservations.ObserveOpponentPlacesNeighbours;
import GraphXings.Gruppe4.CanvasObservations.SampleParameters;
import GraphXings.Gruppe4.CanvasObservations.SampleSize;
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
    private SampleParameters sampleParameters = new SampleParameters(SampleSize.Keep, 10, 1);

    private final long timeLimit = 300000000000L;

    // This is a safety measure, so we don't get a timeout
    private final long timeLimitBuffer = 10000000000L; //10s

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

    /**
     * Retrieve the optimal runtime of a single game move in nano seconds
     * @return Game move runtime
     */
    public long getSingleGameMoveTime() {
        // We divide our time limit by the amount of vertices we have to place
        long gameMove = (timeLimit - timeLimitBuffer) / (totalVerticesCount / 2);

        // One game move should at least have 5ms time to compute
        // TODO: These are nano seconds...
        if (gameMove < 5) {
            return 5;
        }
        return gameMove;
    }

    /**
     * Retrieve an estimation if the sample size should increase/decrease
     * @return
     */
    public SampleSize getSampleSizeAdjustment() {
        long timeDiff = getSingleGameMoveTime() - currentGameMoveTime;
        // If we have more than 1s to spend then increment sample size
        if (timeDiff > 1000000000) {
            return SampleSize.Increment;
        } else if (timeDiff < 0) {
            // If difference is negative we would run too long
            return SampleSize.Decrement;
        }

        // We've found a nice sample size
        return SampleSize.Keep;
    }

    /**
     * Return a new SampleParameters record for the current round.
     * This will alter the sample parameters so only use once per round.
     * If you'd like to get the parameters again, use the get method instead.
     * @return A new SampleParameters object
     */
    public SampleParameters calculateSampleSizeParameters() {
        var sampleSize = getSampleSizeAdjustment();
        var samples = sampleParameters.samples();
        var perimeter = sampleParameters.perimeter();

        switch (sampleSize) {
            case Increment -> {
                samples++;
                perimeter++;
            }
            case Decrement -> {
                samples--;
                perimeter--;
            }
            case Keep -> {
                // Do nothing
            }
        }
        if (samples <= 0) {
            samples = 1;
        }
        if (perimeter <= 0) {
            perimeter = 1;
        }
        sampleParameters = new SampleParameters(sampleSize, samples, perimeter);
        return sampleParameters;
    }

    /**
     * Get the SampleParameters without triggering recalculation.
     * @return A SampleParameters record
     */
    public SampleParameters getSampleParameters() {
        return sampleParameters;
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

    /**
     * This routine runs all CanvasObservations and calculates the best
     * strategy for our next move.
     * @return A counter-attack strategy
     */
    public StrategyName observationRunner(Graph g, GameMove lastMove) {
        // Define all available observers
        CanvasObservation[] observers = {
                new ObserveBorders(),
                new ObserveOpponentPlacesNeighbours(g, ourMoves, opponentMoves, ourRole)
        };

        // Choose the observation with the best score (max. = 100)
        CanvasObservation bestChoice = null;
        int bestObservationScore = 0;
        for (var observer : observers) {
            observer.calculateObservation(lastMove);

            if (observer.getObservation() > bestObservationScore) {
                bestChoice = observer;
                bestObservationScore = observer.getObservation();
            }
        }

        // Return the best counter strategy
        if (bestChoice != null) {
            return bestChoice.getEffectiveCounterStrategy();
        }

        // Or return unknown if we don't know what to do :D
        return StrategyName.Unknown;
    }
}
