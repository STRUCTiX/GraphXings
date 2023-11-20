package GraphXings.Gruppe4;

import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import GraphXings.Gruppe4.Common.Helper;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;

import java.util.HashMap;
import java.util.HashSet;

import static GraphXings.Gruppe4.Common.Helper.isCoordinateFree;

public class Maximize {

    public static GameMove maximizeMoveOptimize(Graph g, int[][] usedCoordinates, HashMap<Vertex, Coordinate> vertexCoordinates, GameMove lastMove, HashSet<Vertex> placedVertices, int width, int height, MutableRTree<Edge, LineFloat> tree) {
        var heuristicResult = Heuristics.getFreeGameMoveOnCanvasCenter(g, usedCoordinates, vertexCoordinates, lastMove, placedVertices, width, height);
        if (heuristicResult.isPresent()) {
            return heuristicResult.get();
        }


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
