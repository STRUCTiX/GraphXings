package GraphXings.Gruppe4;

import GraphXings.Algorithms.CrossingCalculator;
import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static GraphXings.Gruppe4.Common.Helper.isCoordinateFree;

public class Minimize {
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
                        // Create a test coordinate
                        var tempCoord = new Coordinate(i, k);
                        if (!isCoordinateFree(usedCoordinates, tempCoord)) {
                            continue;
                        }

                        // Get tagret vertex and coordinates of the edge
                        Vertex targetVertex = null;
                        Coordinate t = null;
                        for(var e: g.getEdges()){
                            if(e.getS().equals(v)){
                                targetVertex = e.getT();
                                t = vertexCoordinates.get(targetVertex);
                                if (t == null){
                                    continue;
                                }
                                break;
                            }
                        }

                        //Create Line for the rtree
                        //LineFloat line = null;
                        if (t == null){
                            t = Heuristics.minimizeHeuristic(g, usedCoordinates, width, height, targetVertex).get().getCoordinate();
                        }

                        var line = LineFloat.create(tempCoord.getX(), tempCoord.getY(), t.getX(), t.getY());



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

}
