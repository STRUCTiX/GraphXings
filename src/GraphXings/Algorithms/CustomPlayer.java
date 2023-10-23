package GraphXings.Algorithms;

import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;

import java.util.*;

public class CustomPlayer implements Player {

    /**
     * The name of the random player.
     */
    private String name;

    /**
     * Creates a random player with the assigned name.
     * @param name
     */
    public CustomPlayer(String name)
    {
        this.name = name;
    }

    @Override
    public GameMove maximizeCrossings(Graph g, HashMap<Vertex, Coordinate> vertexCoordinates, List<GameMove> gameMoves, int[][] usedCoordinates, HashSet<Vertex> placedVertices, int width, int height)
    {
        return maximizeMove(g, usedCoordinates, vertexCoordinates, placedVertices, width, height);
    }

    @Override
    public GameMove minimizeCrossings(Graph g, HashMap<Vertex, Coordinate> vertexCoordinates, List<GameMove> gameMoves, int[][] usedCoordinates, HashSet<Vertex> placedVertices, int width, int height)
    {
        return randomMove(g,usedCoordinates,placedVertices,width,height);
    }

    @Override
    public void initializeNextRound()
    {

    }

    /**
     * Computes a random valid move.
     * @param g The graph.
     * @param usedCoordinates The used coordinates.
     * @param placedVertices The already placed vertices.
     * @param width The width of the game board.
     * @param height The height of the game board.
     * @return A random valid move.
     */
    private GameMove randomMove(Graph g, int[][] usedCoordinates, HashSet<Vertex> placedVertices, int width, int height)
    {
        Random r = new Random();
        int stillToBePlaced = g.getN()- placedVertices.size();
        int next = r.nextInt(stillToBePlaced);
        int skipped = 0;
        Vertex v=null;
        for (Vertex u : g.getVertices())
        {
            if (!placedVertices.contains(u))
            {
                if (skipped < next)
                {
                    skipped++;
                    continue;
                }
                v=u;
                break;
            }
        }
        Coordinate c = new Coordinate(0,0);
        do
        {
            c = new Coordinate(r.nextInt(width),r.nextInt(height));
        }
        while (usedCoordinates[c.getX()][c.getY()]!=0);
        return new GameMove(v,c);
    }


    private GameMove maximizeMove(Graph g, int[][] usedCoordinates, HashMap<Vertex, Coordinate> vertexCoordinates, HashSet<Vertex> placedVertices, int width, int height) {
        // This list contains the possible coordinates for the next move.
        // We have to check if these coordinates are actually unused,
        // therefore we need multiple candidates.
        var coordinateCandidate = new HashMap<Vertex, Coordinate>();

        // Calculate the middle point of the edge which is connected to its two vertices.
        for (Vertex v : g.getVertices()) {
            if (!placedVertices.contains(v)) {
                var midpoint = getMidpoint(g, vertexCoordinates, v);
                if (midpoint.isEmpty()) {
                    // Invalid vertex, choose another one
                    continue;
                }

                // Move the candidate position closer to the target position
                //var candidatePosition = moveVertexCloser(vertexCoordinates.get(v), midpoint.get());
                coordinateCandidate.put(v, midpoint.get());
            }
        }

        // Check if one of the coordinate candidates are possible game moves
        for (var vertex : coordinateCandidate.keySet()) {
            if (isCoordinateFree(usedCoordinates, coordinateCandidate.get(vertex))) {
                System.out.println("Success :)");
                return new GameMove(vertex, coordinateCandidate.get(vertex));
            }
        }


        System.out.println("Fallback :(");
        // If we haven't found anything yet use fallback strategy
        // -> use the first free coordinate
        var coordinate = new Coordinate(0, 0);
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < height; k++) {
                // Find a free coordinate
                if (usedCoordinates[i][k] == 0) {
                    coordinate = new Coordinate(i, k);
                    break;
                }
            }
        }

        // Find an unplaced vertex
        Vertex vertex = null;
        for (var v : g.getVertices()) {
            if (!placedVertices.contains(v)) {
                vertex = v;
            }
        }
        // This case should never happen
        return new GameMove(vertex, coordinate);
    }

    private Optional<Coordinate> getMidpoint(Graph g, HashMap<Vertex, Coordinate> vertexCoordinates, Vertex selectedVertex) {
        HashMap<Vertex, Coordinate> neighbours = new HashMap<>();
        // Get the adjacent edges of the selected vertex
        // and calculate the middle point of the edges.
        for (Edge e : g.getIncidentEdges(selectedVertex)) {
            Vertex source = e.getS();
            Vertex target = e.getT();

            // Check if source vertex is a neighbour of selected vertex and check if this neighbour has coordinates
            if (!source.equals(selectedVertex) && vertexCoordinates.get(source) != null) {
                neighbours.put(source, vertexCoordinates.get(source));
            }
            // Do the same for the target vertex
            if (!target.equals(selectedVertex) && vertexCoordinates.get(target) != null) {
                neighbours.put(target, vertexCoordinates.get(target));
            }
        }

        // At this point we should have either two neighbours or we can't use the selected vertex
        if (neighbours.values().size() != 2) {
            // Invalid selected vertex
            return Optional.empty();
        }

        // Calculate the middle point coordinate
        int x = neighbours.values().stream().mapToInt(Coordinate::getX).sum();
        int y = neighbours.values().stream().mapToInt(Coordinate::getY).sum();

        // Return the half of each sum to get the middle of x/y
        return Optional.of(new Coordinate(x / 2, y / 2));
    }

    private Coordinate moveVertexCloser(Coordinate vertexPosition, Coordinate targetPosition) {
        // Move the vertex closer to the target position
        int x = vertexPosition.getX() + targetPosition.getX();
        int y = vertexPosition.getY() + targetPosition.getY();
        return new Coordinate(x / 2, y / 2);
    }

    private boolean isCoordinateFree(int[][] usedCoordinates, Coordinate coordinate) {
        return usedCoordinates[coordinate.getX()][coordinate.getY()] == 0;
    }

    @Override
    public String getName()
    {
        return name;
    }
}
