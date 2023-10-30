package GraphXings.Gruppe4;

import GraphXings.Algorithms.CrossingCalculator;
import GraphXings.Algorithms.Player;
import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;

import java.util.*;

public class BruteforcePlayer implements Player {

    /**
     * The name of the random player.
     */
    private String name;

    /**
     * Creates a random player with the assigned name.
     * @param name
     */
    public BruteforcePlayer(String name)
    {
        this.name = name;
    }

    @Override
    public GameMove maximizeCrossings(Graph g, HashMap<Vertex, Coordinate> vertexCoordinates, List<GameMove> gameMoves, int[][] usedCoordinates, HashSet<Vertex> placedVertices, int width, int height)
    {
        return maximizeMove2(g, usedCoordinates, vertexCoordinates, gameMoves, placedVertices, width, height);
    }

    @Override
    public GameMove minimizeCrossings(Graph g, HashMap<Vertex, Coordinate> vertexCoordinates, List<GameMove> gameMoves, int[][] usedCoordinates, HashSet<Vertex> placedVertices, int width, int height)
    {
        return minimizeMove(g, usedCoordinates, vertexCoordinates, gameMoves, placedVertices, width, height);
    }

    @Override
    public void initializeNextRound(Graph g, int width, int height, Role role)
    {

    }


    /**
     * Tries to maximize the amount of crossings in a graph g by moving the vertices near each other.
     * This is currently experimental and therefore we use maximizeMove2.
     *
     * @param g The graph object.
     * @param usedCoordinates This contains an array of the canvas with already used coordinates.
     * @param vertexCoordinates This is a map which outputs the coordinates for a given vertex.
     * @param placedVertices The already placed vertices.
     * @param width Width of the canvas.
     * @param height Height of the canvas.
     * @return A game move of the final decision.
     */
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
                return new GameMove(vertex, coordinateCandidate.get(vertex));
            }
        }


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

    /**
     * Calculate the middle point for a selected vertex and its neighbours.
     * @param g The graph object
     * @param vertexCoordinates This is a map which outputs the coordinates for a given vertex.
     * @param selectedVertex The selected vertex
     * @return Middle point coordinates or none
     */
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


    // Currently not in use. This should be used in the middle point maximizing strategy
    private Coordinate moveVertexCloser(Coordinate vertexPosition, Coordinate targetPosition) {
        // Move the vertex closer to the target position
        int x = vertexPosition.getX() + targetPosition.getX();
        int y = vertexPosition.getY() + targetPosition.getY();
        return new Coordinate(x / 2, y / 2);
    }

    /**
     * Helper function to check whether a coordinate is free or in use.
     * @param usedCoordinates The usedCoordinates array.
     * @param coordinate A coordinate object to test for.
     * @return Returns true if free, false otherwise.
     */
    private boolean isCoordinateFree(int[][] usedCoordinates, Coordinate coordinate) {
        return usedCoordinates[coordinate.getX()][coordinate.getY()] == 0;
    }


    /**
     * Tries to minimize the amount of crossings in a graph g by testing for the min. amount of crossings.
     * In the first move we can't find any crossings because we don't have enough coordinates.
     * Therefore, we place the vertex in a corner of the canvas with the max. distance to the already placed vertex.
     *
     * @param g The graph object.
     * @param usedCoordinates This contains an array of the canvas with already used coordinates.
     * @param vertexCoordinates This is a map which outputs the coordinates for a given vertex.
     * @param placedVertices The already placed vertices.
     * @param width Width of the canvas.
     * @param height Height of the canvas.
     * @return A game move of the final decision.
     */
    private GameMove minimizeMove(Graph g, int[][] usedCoordinates, HashMap<Vertex, Coordinate> vertexCoordinates, List<GameMove> gameMoves, HashSet<Vertex> placedVertices, int width, int height) {
        if (placedVertices.size() < 3) {
            // In this case we have to place more or less random coordinates
            // because it's not possible to calculate crossings at this point.

            GameMove lastGameMove = gameMoves.get(gameMoves.size()-1);

            var lastVertex = lastGameMove.getVertex();
            var lastCoordinate = lastGameMove.getCoordinate();

            // Get a neighbour vertex by iterating through the edges
            Vertex neighbourVertex = null;
            for (var e : g.getEdges()) {
                if (e.getS().equals(lastVertex)) {
                    neighbourVertex = e.getT();
                    break;
                }
            }

            // Place the vertex in a corner of the canvas with
            // maximum distance to the placed vertex.
            int maxDistance = 0;
            Coordinate maxCoordinate = new Coordinate(0, 0);
            for (int i = 0; i < width; i += width - 1) {
                for (int k = 0; k < height; k += height - 1) {
                    var tempCoord = new Coordinate(i, k);
                    var distance = euclidianDistance(lastCoordinate, tempCoord);
                    if (distance > maxDistance) {
                        maxDistance = distance;
                        maxCoordinate = tempCoord;
                    }
                }
            }

            return new GameMove(neighbourVertex, maxCoordinate);
        }

        // In this case we can compute the minimal crossings

        // First create duplicates of the existing graph and vertex coordintes structures
        var graphDuplicate = g.copy();
        var vertexCoordDuplicate = new HashMap<>(vertexCoordinates);


        // Check the minimal crossings for an unplaced vertex
        var minCross = Integer.MAX_VALUE;
        Coordinate minCoord = null;
        Vertex unplacedVertex = null;
        for (var v : g.getVertices()) {
            if (!placedVertices.contains(v)) {

                // Test the minimal crossings for each coordinate
                for (int i = 0; i < width; i++) {
                    for (int k = 0; k < height; k++) {
                        // Create a test coordinate
                        var tempCoord = new Coordinate(i, k);
                        if (!isCoordinateFree(usedCoordinates, tempCoord)) {
                            continue;
                        }

                        // Put it into the hashmap
                        vertexCoordDuplicate.put(v, tempCoord);

                        // Test crossings
                        var crossCalc = new CrossingCalculator(graphDuplicate, vertexCoordDuplicate);
                        var crossNum = crossCalc.computePartialCrossingNumber();
                        if (crossNum < minCross) {
                            minCross = crossNum;
                            minCoord = tempCoord;
                        }

                        // Remove coordinate from hashmap for the next iteration
                        vertexCoordDuplicate.remove(v);
                    }
                }

                // Currently we check for the first unplaced vertex. Might be optimized by checking all
                unplacedVertex = v;
                break;
            }
        }

        return new GameMove(unplacedVertex, minCoord);
    }


    /**
     * Tries to maximize the amount of crossings in a graph g by testing for the max. amount of crossings.
     * In the first move we can't find any crossings because we don't have enough coordinates.
     * Therefore, we place the vertex in the middle of the canvas with the min. distance to the already placed vertex.
     *
     * @param g The graph object.
     * @param usedCoordinates This contains an array of the canvas with already used coordinates.
     * @param vertexCoordinates This is a map which outputs the coordinates for a given vertex.
     * @param placedVertices The already placed vertices.
     * @param width Width of the canvas.
     * @param height Height of the canvas.
     * @return A game move of the final decision.
     */
    private GameMove maximizeMove2(Graph g, int[][] usedCoordinates, HashMap<Vertex, Coordinate> vertexCoordinates, List<GameMove> gameMoves, HashSet<Vertex> placedVertices, int width, int height) {
        if (placedVertices.size() == 0) {
            // Maximize is the first move of the game.
            // Therefore, we place the vertex in the middle of the canvas
            var firstVertex = g.getVertices().iterator().next();
            var middleCoordinate = new Coordinate(width / 2, height / 2);
            return new GameMove(firstVertex, middleCoordinate);
        }

        // In this case we can compute the max. crossings

        // First create duplicates of the existing graph and vertex coordintes structures
        var graphDuplicate = g.copy();
        var vertexCoordDuplicate = new HashMap<>(vertexCoordinates);


        // Check the minimal crossings for an unplaced vertex
        var maxCross = Integer.MIN_VALUE;
        Coordinate maxCoord = null;
        Vertex unplacedVertex = null;
        for (var v : g.getVertices()) {
            if (!placedVertices.contains(v)) {

                // Test the max. crossings for each coordinate
                for (int i = 0; i < width; i++) {
                    for (int k = 0; k < height; k++) {
                        // Create a test coordinate
                        var tempCoord = new Coordinate(i, k);

                        // Coordinate already in use -> skip
                        if (!isCoordinateFree(usedCoordinates, tempCoord)) {
                            continue;
                        }

                        // Put it into the hashmap
                        vertexCoordDuplicate.put(v, tempCoord);

                        // Test crossings
                        var crossCalc = new CrossingCalculator(graphDuplicate, vertexCoordDuplicate);
                        var crossNum = crossCalc.computePartialCrossingNumber();
                        if (crossNum > maxCross) {
                            maxCross = crossNum;
                            maxCoord = tempCoord;
                        }

                        // Remove coordinate from hashmap for the next iteration
                        vertexCoordDuplicate.remove(v);
                    }
                }

                // Currently we check for the first unplaced vertex. Might be optimized by checking all
                unplacedVertex = v;
                break;
            }
        }

        return new GameMove(unplacedVertex, maxCoord);
    }

    /**
     * Calculate the euclidian distance between two coordinates as integer value.
     * @param p A coordinate object.
     * @param q A coordinate object.
     * @return The distance as integer.
     */
    private int euclidianDistance(Coordinate p, Coordinate q) {
        return (int)Math.sqrt(Math.pow(q.getX() - p.getX(), 2) + Math.pow(q.getY() - p.getY(), 2));
    }

    @Override
    public String getName()
    {
        return name;
    }
}
