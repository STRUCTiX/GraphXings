package GraphXings.Gruppe4;

import GraphXings.Data.*;
import GraphXings.Game.GameMove;
import GraphXings.Game.GameState;
import GraphXings.Gruppe4.Common.Helper;
import GraphXings.Gruppe4.Common.TreeHelper;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;
import com.github.davidmoten.rtree2.geometry.internal.PointFloat;
import GraphXings.Data.Edge;


import static GraphXings.Gruppe4.Common.Helper.isCoordinateFree;

public class Maximize {

    public static GameMove maximizeMoveOptimize(Graph g, GameState gs, GameMove lastMove, int width, int height, MutableRTree<Edge, LineFloat> tree, MutableRTree<Vertex, PointFloat> vertexTree) {
        var usedCoordinates = gs.getUsedCoordinates();
        var vertexCoordinates = gs.getVertexCoordinates();
        var placedVertices = gs.getPlacedVertices();

        var heuristicResult = Heuristics.getFreeGameMoveOnCanvasCenter(g, usedCoordinates, vertexCoordinates, lastMove, placedVertices, width, height);
        if (heuristicResult.isPresent()) {
            return heuristicResult.get();
        }

        // Find highest density area for edges and vertices.
        var vertexDensity = vertexTree.findHighestDensity(TreeHelper.densityGridSize(gs, width, height));
        var edgeDensity = tree.findHighestDensity(TreeHelper.densityGridSize(gs, width, height));

        if (vertexDensity.isPresent()) {

        }


        // TODO: Function refactor / Function rewrite
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

        // TODO: Edge case when no informed solution has been found
        // In this case we currently just pick a random vertex but this isn't optimal.
        return Helper.randomMove(g, usedCoordinates, placedVertices, width, height);
    }

}
