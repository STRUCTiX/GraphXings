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
    /**
     * Tries to minimize the amount of crossings in a graph g by testing for the min. amount of crossings.
     * In the first move we can't find any crossings because we don't have enough coordinates.
     * Therefore, we place the vertex in a corner of the canvas with the max. distance to the already placed vertex.
     *
     * @param g                 The graph object.
     * @param usedCoordinates   This contains an array of the canvas with already used coordinates.
     * @param vertexCoordinates This is a map which outputs the coordinates for a given vertex.
     * @param placedVertices    The already placed vertices.
     * @param width             Width of the canvas.
     * @param height            Height of the canvas.
     * @return A game move of the final decision.
     */
    public static GameMove minimizeMove(Graph g, int[][] usedCoordinates, HashMap<Vertex, Coordinate> vertexCoordinates, List<GameMove> gameMoves, HashSet<Vertex> placedVertices, int width, int height, MutableRTree<Edge, LineFloat> tree) {
        // At the start of the game we can't check for intersections
        //var heuristicsResult = Heuristics.minimizeHeuristic(g, usedCoordinates, vertexCoordinates, gameMoves, placedVertices, width, height);
        //if (heuristicsResult.isPresent()) {
        //  return heuristicsResult.get();
        //}

        // In this case we can compute the minimal crossings


        // Check the minimal crossings for an unplaced vertex
        var minCross = Long.MAX_VALUE;
        Coordinate minCoord = null;
        Vertex unplacedVertex = null;
        for (var v : g.getVertices()) {
            if (!placedVertices.contains(v)) {

                // Test the minimal crossings for each coordinate
                for (int i = 0; i < width; i++) {
                    for (int k = 0; k < height; k++) {

                        // Check if the coordinate is usable
                        if (!isCoordinateFree(usedCoordinates, i, k)) {
                            continue;
                        }

                        // Usable coordinate found, allocate an object
                        var tempCoord = new Coordinate(i, k);


                        // Get target vertex and coordinates of the edge
                        // TODO: Use UnplacedHelper
                        Vertex targetVertex = null;
                        Coordinate t = null;
                        for (var e : g.getEdges()) {
                            if (e.getS().equals(v)) {
                                targetVertex = e.getT();
                                t = vertexCoordinates.get(targetVertex);
                                if (t == null) {
                                    continue;
                                }
                                break;
                            }
                        }

                        //Create Line for the rtree
                        //LineFloat line = null;
                        if (t == null) {
                            t = Heuristics.minimizeHeuristicLateGame(g, usedCoordinates, width, height, targetVertex).get().getCoordinate();
                        }

                        var line = LineFloat.create(i, k, t.getX(), t.getY());


                        //compute number of intersections
                        long crossNum = tree.getIntersections(line);

                        // Test crossings
                        if (crossNum < minCross) {
                            minCross = crossNum;
                            minCoord = tempCoord;
                        }

                    }
                }

                // Currently we check for the first unplaced vertex. Might be optimized by checking all
                unplacedVertex = v;
                break;
            }
        }

        return new GameMove(unplacedVertex, minCoord);
    }


    public static GameMove minimizeMove2(Graph g, int[][] usedCoordinates, HashMap<Vertex, Coordinate> vertexCoordinates, List<GameMove> gameMoves, HashSet<Vertex> placedVertices, int width, int height, MutableRTree<Edge, LineFloat> tree) {
        var heuristicResult = Heuristics.minimizeHeuristic(g, usedCoordinates, vertexCoordinates, gameMoves.getLast(), placedVertices, width, height);
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
            var unplacedVertex = (vertexCoordinates.get(usedEdge.getS()) == null) ? usedEdge.getS() : usedEdge.getT();
            var heuristic = Heuristics.minimizeHeuristicLateGame(g, usedCoordinates, width, height, unplacedVertex);

            // If we're in the null case we'd probably lose anyway
            return heuristic.orElse(null);

        }

        // Check the max. crossings for an unplaced vertex
        var minCross = Long.MAX_VALUE;
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
                if (intersections < minCross) {
                    minCross = intersections;
                    coordX = i;
                    coordY = k;
                }
            }
        }

        // We found crossings so we take the minimized coordinates
        var coord = new Coordinate(coordX, coordY);

        // Return the game move with the unused vertex and the coordinate with max. intersections
        return new GameMove((vertexCoordinates.get(usedEdge.getS()) == null) ? usedEdge.getS() : usedEdge.getT(), coord);
    }


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
