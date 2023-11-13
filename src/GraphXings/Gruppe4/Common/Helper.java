package GraphXings.Gruppe4.Common;

import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import GraphXings.Gruppe4.MutableRTree;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;

import java.util.Optional;

public class Helper {

    /**
     * Helper function to check whether a coordinate is free or in use.
     * @param usedCoordinates The usedCoordinates array.
     * @param coordinate A coordinate object to test for.
     * @return Returns true if free, false otherwise.
     */
    public static boolean isCoordinateFree(int[][] usedCoordinates, Coordinate coordinate) {
        return usedCoordinates[coordinate.getX()][coordinate.getY()] == 0;
    }

    /**
     * Helper function to check whether a coordinate is free or in use.
     * @param usedCoordinates The usedCoordinates array.
     * @return Returns true if free, false otherwise.
     */
    public static boolean isCoordinateFree(int[][] usedCoordinates, int x, int y) {
        return usedCoordinates[x][y] == 0;
    }


    /**
     * Minimize crossings around a given vertex (coordX, coordY).
     * The given perimeter is the amount of the box checked around the given vertex.
     * @param usedCoordinates Array of the coordinates
     * @param tree the mutable rtree to check crossings
     * @param unplacedVertex the vertex which should be placed with this game move
     * @param coordX x coordinate of the given vertex
     * @param coordY y coordinate of the given vertex
     * @param perimeterX the x perimeter around the vertex -> 1 means one field left and right
     * @param perimeterY the y perimeter around the vertex
     * @return a game move if there's a free coordinate in the given perimeter
     */
    public static Optional<GameMove> minimizeBounds(int[][] usedCoordinates, MutableRTree<Edge, LineFloat> tree, Vertex unplacedVertex, int coordX, int coordY, int perimeterX, int perimeterY) {
        long minCrossings = Long.MAX_VALUE;
        int x = 0;
        int y = 0;
        for (int i = coordX - perimeterX; i <= coordX + perimeterX; i++) {
            for (int k = coordY - perimeterY; k <= coordY + perimeterY; k++) {
                // We're just lazy here. Don't check for bounds, just continue if we're out of bounds
                try {
                    if (isCoordinateFree(usedCoordinates, i, k)) {
                        // Found a free coordinate. Check if this minimizes crossings
                        var cross = tree.getIntersections(LineFloat.create(coordX, coordY, i, k));
                        if (cross < minCrossings) {
                            minCrossings = cross;
                            x = i;
                            y = k;
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    continue;
                }
            }
        }

        // Use the coordinates
        return Optional.of(new GameMove(unplacedVertex, new Coordinate(x, y)));
    }


    public static Optional<GameMove> minimizeBounds(int[][] usedCoordinates, MutableRTree<Edge, LineFloat> tree, Vertex unplacedVertex, Coordinate placedCoordinate, int perimeterX, int perimeterY) {
        return minimizeBounds(usedCoordinates, tree, unplacedVertex, placedCoordinate.getX(), placedCoordinate.getY(), perimeterX, perimeterY);
    }


}
