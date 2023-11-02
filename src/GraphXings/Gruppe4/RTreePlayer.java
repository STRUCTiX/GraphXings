package GraphXings.Gruppe4;

import GraphXings.Algorithms.CrossingCalculator;
import GraphXings.Algorithms.Player;
import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Line;

import java.util.*;

public class RTreePlayer implements Player {

    /**
     * The name of the random player.
     */
    private String name;

    /**
     * The immutable R-Tree structure.
     */
    private MutableRTree<Vertex, Line> tree;

    /**
     * Creates a random player with the assigned name.
     * @param name
     */
    public RTreePlayer(String name)
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
        var vertices = (HashSet<Vertex>) g.getVertices();
        // If we have <10k Vertices use the normal R-Tree.
        // Otherwise, use the R*-Tree heuristic.
        if (vertices.size() < 10000) {
            tree = new MutableRTree<>(MutableRTree.TreeSetup.SMALL);
        } else {
            tree = new MutableRTree<>(MutableRTree.TreeSetup.BIG);
        }
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
        var heuristicsResult = Heuristics.minimizeHeuristic(g, usedCoordinates, vertexCoordinates, gameMoves, placedVertices, width, height);
        if (heuristicsResult.isPresent()) {
            return heuristicsResult.get();
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



    @Override
    public String getName()
    {
        return name;
    }

}
