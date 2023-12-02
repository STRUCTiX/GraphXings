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
}
