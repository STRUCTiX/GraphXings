package GraphXings.Gruppe4;

import GraphXings.Algorithms.CrossingCalculator;
import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import GraphXings.Gruppe4.Common.EdgeHelper;
import GraphXings.Gruppe4.Common.Helper;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;

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
    public static GameMove maximizeMove(Graph g, int[][] usedCoordinates, HashMap<Vertex, Coordinate> vertexCoordinates, List<GameMove> gameMoves, HashSet<Vertex> placedVertices, int width, int height, MutableRTree<Edge, LineFloat> tree) {
        var heuristicResult = Heuristics.maximizeHeuristic(g, usedCoordinates, vertexCoordinates, gameMoves, placedVertices, width, height);
        if (heuristicResult.isPresent()) {
            return heuristicResult.get();
        }
        // In this case we can compute the max. crossings

        // Find a placed vertex (and a unplaced vertex) with a corresponding edge
        var placed = EdgeHelper.getUsedVertex(g.getEdges(), vertexCoordinates);
        if (placed.isEmpty()) {
            // This case should never happen because we're not able to place anything
            return null;
        }

        // Get the used edge with one placed and one unplaced vertex
        var usedEdge = placed.get().getKey();
        var placedVertex = placed.get().getValue();
        var pCoord = vertexCoordinates.get(placedVertex);


        // Check if we use a heuristic or the actual maximizer
        if (tree.get().isEmpty()) {
            // Use heuristic
            return Heuristics.maximizeHeuristicLateGame(g, usedCoordinates, vertexCoordinates, gameMoves, placedVertices, width, height);
        }

        // Check the max. crossings for an unplaced vertex
        var maxCross = Long.MIN_VALUE;
        int coordX = 0;
        int coordY = 0;
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < height; k++) {
                // Coordinate isn't free -> try the next one
                if (!isCoordinateFree(usedCoordinates, i, k)) {
                    continue;
                }

                // Create a line
                var line = LineFloat.create(pCoord.getX(), pCoord.getY(), i, k);

                // Test for intersections
                var intersections = tree.getIntersections(line);
                if (intersections > maxCross) {
                    maxCross = intersections;
                    coordX = i;
                    coordY = k;
                }
            }
        }

        if (maxCross > 0) {
            // We found crossings so we take the maximized coordinates
            var coord = new Coordinate(coordX, coordY);

            // Return the game move with the unused vertex and the coordinate with max. intersections
            return new GameMove((vertexCoordinates.get(usedEdge.getS()) == null) ? usedEdge.getS() : usedEdge.getT(), coord);
        } else {
            // No intersections found -> use heuristic
            return Heuristics.maximizeHeuristicLateGame(g, usedCoordinates, vertexCoordinates, gameMoves, placedVertices, width, height);
        }
    }


    public static GameMove maximizeMoveOptimize(Graph g, int[][] usedCoordinates, HashMap<Vertex, Coordinate> vertexCoordinates, List<GameMove> gameMoves, HashSet<Vertex> placedVertices, int width, int height, MutableRTree<Edge, LineFloat> tree) {
        var heuristicResult = Heuristics.maximizeHeuristic(g, usedCoordinates, vertexCoordinates, gameMoves, placedVertices, width, height);
        if (heuristicResult.isPresent()) {
            return heuristicResult.get();
        }

        var lastMove = gameMoves.getLast();

        // Test for max. distance between last vertex and the corners of the canvas
        var testCoords = new Coordinate[]{
                new Coordinate(0, 0), // left upper corner
                new Coordinate(width, 0), // right upper corner
                new Coordinate(0, height), // left lower corner
                new Coordinate(width, height) // right lower corner
        };

        var maxDistance = Integer.MIN_VALUE;
        Coordinate maxDistCoordinates = testCoords[0];
        for (var c : testCoords) {

            // Calculate the max. distance between the last move and the corners
            var tempDistance = Heuristics.euclideanDistance(lastMove.getCoordinate(), c);
            if (tempDistance > maxDistance) {
                maxDistCoordinates = c;
                maxDistance = tempDistance;
            }
        }

        // Select an unplaced vertex neighbour
        var unplacedVertexOption = Helper.pickIncidentVertex(g, vertexCoordinates, lastMove);
        Vertex unplacedVertex = null;
        if (unplacedVertexOption.isEmpty()) {
            // In this case we have to select any free vertex
            for (var v : g.getVertices()) {
                if (!placedVertices.contains(v)) {
                    unplacedVertex = v;
                    break;
                }
            }
        } else {
            unplacedVertex = unplacedVertexOption.get();
        }

        // Pick 10 random coordinates out of a perimeter and test for the max. crossings
        // The perimeter is 1/3 of the width/height
        var samples = Helper.randPickFreeCoordinatesPerimeter(usedCoordinates, maxDistCoordinates, width / 3, height / 3, 10);

        // Test for max. crossings
        if (samples.isPresent()) {
            Coordinate bestCoord = null;
            var maxCrossings = Long.MIN_VALUE;
            for (var sampleCoord : samples.get()) {

                // Create a line to test for intersections
                var line = LineFloat.create(maxDistCoordinates.getX(), maxDistCoordinates.getY(), sampleCoord.getX(), sampleCoord.getY());
                var numCrossings = tree.getIntersections(line);
                if (numCrossings > maxCrossings) {
                    maxCrossings = numCrossings;
                    bestCoord = sampleCoord;
                }
            }

            // Use the best coordinate
            return new GameMove(unplacedVertex, bestCoord);
        }

        // TODO: Edge case if no sample was found.
        // In this case we currently just pick a random vertex but this isn't optimal.

        return Helper.randomMove(g, usedCoordinates, placedVertices, width, height);
    }

}
