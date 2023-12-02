package GraphXings.Gruppe4;

import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;

import java.util.HashSet;

public class GameObserver {

    private final int totalVerticesCount;
    private int currentVerticesCount = 0;

    public GameObserver(Graph g) {
        totalVerticesCount = ((HashSet<Vertex>) g.getVertices()).size();


    }

    public void addGameMove(GameMove move) {
        currentVerticesCount += 1;

        // TODO: Do something with the game move
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
}
