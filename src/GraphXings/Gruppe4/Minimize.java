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
import java.util.Optional;

import static GraphXings.Gruppe4.Common.Helper.isCoordinateFree;
import static GraphXings.Gruppe4.Common.Helper.minimizeBounds;

public class Minimize {

    public static GameMove minimizeMoveClose(Graph g, int[][] usedCoordinates, HashMap<Vertex, Coordinate> vertexCoordinates, GameMove lastMove, HashSet<Vertex> placedVertices, int width, int height, MutableRTree<Edge, LineFloat> tree) {
        var heuristicResult = Heuristics.minimizeHeuristic(g, usedCoordinates, vertexCoordinates, lastMove, placedVertices, width, height);
        if (heuristicResult.isPresent()) {
            return heuristicResult.get();
        }

        // Try to place the new vertex next to the last placed vertex.
        // This is only possible if one of the adjacent vertices is unplaced.
        var unplacedVertex = Helper.pickIncidentVertex(g, vertexCoordinates, lastMove);

        // If we've found an unplaced vertex -> try to place it next to the last game move vertex
        if (unplacedVertex.isPresent()) {
            var lastCoord = lastMove.getCoordinate();

            var gameMove = minimizeBounds(usedCoordinates, tree, unplacedVertex.get(), lastCoord, 1, 1);
            if (gameMove.isPresent()) {
                return gameMove.get();
            }
        }

        // In this case we either don't have an unplaced vertex or the checked perimeter was too small
        // TODO: Currently we just use the heuristic from last week which doesn't really fit to our new strategy
        // TODO: A better strategy would be to increase the perimeter to width x height and go from the usedCoordinate towards width/height
        if (unplacedVertex.isPresent()) {
            var result = Heuristics.minimizeHeuristicLateGame(g, usedCoordinates, width, height, unplacedVertex.get());
            if (result.isPresent()) {
                return result.get();
            }
        }

        // Find the first unplaced vertex
        for (var v : g.getVertices()) {
            if (!placedVertices.contains(v)) {
                var result = Heuristics.minimizeHeuristicLateGame(g, usedCoordinates, width, height, v);
                // In this case we return a result or we're screwed and just surrender
                return result.orElse(null);
            }
        }

        // This would be the case if we don't find any free vertex -> should not happen
        return null;
    }

}
