package GraphXings.Gruppe4;

import GraphXings.Data.Coordinate;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import GraphXings.Gruppe4.Common.EdgeHelper;
import GraphXings.Gruppe4.Common.Helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class Heuristics {


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
    public static Optional<GameMove> minimizeHeuristic(Graph g, int[][] usedCoordinates, HashMap<Vertex, Coordinate> vertexCoordinates, List<GameMove> gameMoves, HashSet<Vertex> placedVertices, int width, int height) {
        if (placedVertices.size() < 3) {
            // In this case we have to place more or less random coordinates
            // because it's not possible to calculate crossings at this point.

            GameMove lastGameMove = gameMoves.get(gameMoves.size() - 1);

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
                    var distance = euclideanDistance(lastCoordinate, tempCoord);
                    if (distance > maxDistance) {
                        maxDistance = distance;
                        maxCoordinate = tempCoord;
                    }
                }
            }

            return Optional.of(new GameMove(neighbourVertex, maxCoordinate));
        }
        return Optional.empty();
    }

    /**
     * Tries to minimize the amount of crossings in a graph g by testing for the min. amount of crossings.
     * In the first move we can't find any crossings because we don't have enough coordinates.
     * Therefore, we place the vertex in a corner of the canvas with the max. distance to the already placed vertex.
     *
     * @param g The graph object.
     * @param usedCoordinates This contains an array of the canvas with already used coordinates.
     * @param width Width of the canvas.
     * @param height Height of the canvas.
     * @return A game move of the final decision.
     */
    public static Optional<GameMove> minimizeHeuristicLateGame(Graph g, int[][] usedCoordinates, int width, int height, Vertex v) {
        for(int i = 0; i < width; i++){
            if(usedCoordinates[i][0] == 0) {
                return Optional.of(new GameMove(v, new Coordinate(i, 0)));
            } else if (usedCoordinates[i][height-1] == 0) {
                return Optional.of(new GameMove(v, new Coordinate(i, height-1)));
            }
        }

        for(int i = 0; i < height; i++){
            if(usedCoordinates[0][i] == 0) {
                return Optional.of(new GameMove(v, new Coordinate(0, i)));
            } else if (usedCoordinates[width-1][i] == 0) {
                return Optional.of(new GameMove(v, new Coordinate(width-1, i)));
            }
        }

        return Optional.empty();
    }


    /**
     * This heuristic is used if it's our first game move.
     * In this case we just place the vertex in the middle of the canvas.
     * @param g
     * @param usedCoordinates
     * @param vertexCoordinates
     * @param gameMoves
     * @param placedVertices
     * @param width
     * @param height
     * @return
     */
    public static Optional<GameMove> maximizeHeuristic(Graph g, int[][] usedCoordinates, HashMap<Vertex, Coordinate> vertexCoordinates, List<GameMove> gameMoves, HashSet<Vertex> placedVertices, int width, int height) {
        if (placedVertices.isEmpty()) {
            // Maximize is the first move of the game.
            // Therefore, we place the vertex in the middle of the canvas
            var firstVertex = g.getVertices().iterator().next();
            var middleCoordinate = new Coordinate(width / 2, height / 2);
            return Optional.of(new GameMove(firstVertex, middleCoordinate));
        }
        return Optional.empty();
    }

    /**
     * Sometimes there's no existing edge (two placed vertices connected by the predefined edge)
     * on the canvas. In this case we have to use another heuristic to place an adjacent vertex
     * and therefore have a usable edge in the next game move.
     * The strategy remains the same: try to place the vertex near the middle point of the canvas.
     * @param g
     * @param usedCoordinates
     * @param vertexCoordinates
     * @param gameMoves
     * @param placedVertices
     * @param width
     * @param height
     * @return
     */
    public static GameMove maximizeHeuristicLateGame(Graph g, int[][] usedCoordinates, HashMap<Vertex, Coordinate> vertexCoordinates, List<GameMove> gameMoves, HashSet<Vertex> placedVertices, int width, int height) {
        var unusedEdgeVertex = EdgeHelper.getUnusedVertex(g.getEdges(), vertexCoordinates);

        Vertex placeableVertex = null;
        if (unusedEdgeVertex.isEmpty()) {
            // We're in a weird state. No placed and unplaced vertex-pair inside an edge.
            // This shouldn't happen except for the start and end of the game but we better handle
            // this edge case.
            // Just get any unplaced vertex
            for (var v : g.getVertices()) {
                if (!placedVertices.contains(v)) {
                    placeableVertex = v;
                    break;
                }
            }
        } else {
            // In this case we've retrieved an unused vertex
            placeableVertex = unusedEdgeVertex.get().getValue();
        }

        int midX = width / 2;
        int midY = height / 2;

        int unusedX = 0;
        int unusedY = 0;
        for (int i = 0; i < midX; i++) {
            for (int k = 0; k < midY; k++) {
                unusedX = midX + i;
                unusedY = midY + k;
                if (Helper.isCoordinateFree(usedCoordinates, unusedX, unusedY)) {
                    // We've found a free coordinate, return game move
                    var coord = new Coordinate(unusedX, unusedY);
                    return new GameMove(placeableVertex, coord);
                }
            }
        }

        // This case should never happen
        return new GameMove(placeableVertex, new Coordinate(0, 0));
    }




    /**
     * Calculate the euclidean distance between two coordinates as integer value.
     * @param p A coordinate object.
     * @param q A coordinate object.
     * @return The distance as integer.
     */
    public static int euclideanDistance(Coordinate p, Coordinate q) {
        return (int)Math.sqrt(Math.pow(q.getX() - p.getX(), 2) + Math.pow(q.getY() - p.getY(), 2));
    }


    /**
     * Calculate the euclidean distance between two coordinates as integer value.
     * @param p A coordinate object.
     * @param x x coordinate as integer
     * @param y y coordinate as integer
     * @return The distance as integer.
     */
    public static int euclideanDistance(Coordinate p, int x, int y) {
        return (int)Math.sqrt(Math.pow(x - p.getX(), 2) + Math.pow(y - p.getY(), 2));
    }
}
