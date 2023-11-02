package GraphXings.Gruppe4;

import GraphXings.Algorithms.CrossingCalculator;
import GraphXings.Data.Coordinate;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static GraphXings.Gruppe4.Common.Helper.isCoordinateFree;

public class Maximize {
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
    public static GameMove maximizeMove(Graph g, int[][] usedCoordinates, HashMap<Vertex, Coordinate> vertexCoordinates, List<GameMove> gameMoves, HashSet<Vertex> placedVertices, int width, int height) {
        var heuristicResult = Heuristics.maximizeHeuristic(g, usedCoordinates, vertexCoordinates, gameMoves, placedVertices, width, height);
        if (heuristicResult.isPresent()) {
            return heuristicResult.get();
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

}
