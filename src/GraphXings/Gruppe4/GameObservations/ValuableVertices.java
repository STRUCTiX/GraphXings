package GraphXings.Gruppe4.GameObservations;

import GraphXings.Data.Coordinate;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameState;
import GraphXings.Gruppe4.Common.Helper;
import com.github.davidmoten.rtree2.Iterables;

import java.util.*;

public class ValuableVertices {
    private Graph g;
    private GameState gs;
    private final ArrayList<Map.Entry<Vertex, Long>> vertexIncidentEdges;
    private final ArrayList<Coordinate> ellipseCoordinates;


    public ValuableVertices(Graph g, GameState gs) {
        this.g = g;
        this.gs = gs;
        vertexIncidentEdges = new ArrayList<>(((HashSet<Vertex>)g.getVertices()).size());
        ellipseCoordinates = new ArrayList<>(100);
    }

    /**
     * This computation must be performed before usage
     */
    public void computeRank() {
        for (var vertex : g.getVertices()) {
            var incidentCount = Iterables.size(g.getIncidentEdges(vertex));
            vertexIncidentEdges.add(new AbstractMap.SimpleImmutableEntry<>(vertex, incidentCount));
        }

        vertexIncidentEdges.sort(Map.Entry.comparingByValue());
    }

    /**
     * Compute the ellipse with the Bresenham algorithm.
     * This will generate a ellipse in the upper right quadrant of the canvas.
     * @param width
     * @param height
     */
    public void computeEllipseCoordinates(int width, int height) {
        // Use the north side (between quadrant 1 and 2) as center-point
        // to avoid getting crossed by diagonal maximizers.
        var middleX = width / 2;
        var middleY = height / 4;

        // Determine the radius
        var a = middleX / 8;
        var b = middleY / 8;

        ellipse(middleX, middleY, a, b);

        // The correct order will create small edge angles
        Collections.shuffle(ellipseCoordinates);
    }

    /**
     * Returns the Vertex with the most incident edges which is currently free to use.
     * @return Vertex
     */
    public Vertex getAndRemoveVertexWithMostEdges() {
        for (int i = vertexIncidentEdges.size() - 1; i >= 0; i--) {
            Vertex currentVertex = vertexIncidentEdges.get(i).getKey();
            if (!gs.getPlacedVertices().contains(currentVertex)) {
                // Found a free vertex. Remove and return it
                return vertexIncidentEdges.remove(i).getKey();
            }

            // Vertex already taken. Remove it
            vertexIncidentEdges.remove(i);
        }
        // This shouldn't happen. In this case the game is already over.
        return null;
    }

    /**
     * Get a free ellipse coordinate if one is available.
     * This function will only return coordinates in MIN_ANGLE mode.
     * @return A coordinate otherwise empty.
     */
    public Optional<Coordinate> getAndRemoveCoordinate() {
        for (int i = ellipseCoordinates.size() - 1; i >= 0; i--) {
            Coordinate coord = ellipseCoordinates.get(i);
            if (Helper.isCoordinateFree(gs.getUsedCoordinates(), coord.getX(), coord.getY())) {
                // Found a free coordinate. Remove it from the list and return it
                return Optional.of(ellipseCoordinates.remove(i));
            }

            // Coordinate isn't free anymore. Remove it
            ellipseCoordinates.remove(i);
        }

        // No coordinate left in the array
        return Optional.empty();
    }

    /**
     * Retrieve the size of the remaining coordinates
     * @return Integer with the size
     */
    public int getCoordinateSize() {
        return ellipseCoordinates.size();
    }

    /**
     * Bresenham ellipse algorithm.
     * Taken from here: https://de.wikipedia.org/wiki/Bresenham-Algorithmus#Ellipsen
     * @param xm Middle coordinate in x direction
     * @param ym Middle coordinate in x direction
     * @param a Radius
     * @param b Radius
     */
    private void ellipse(int xm, int ym, int a, int b) {
        int dx = 0, dy = b; /* im I. Quadranten von links oben nach rechts unten */
        long a2 = a * (long)a, b2 = b * (long)b;
        long err = b2 - (2 * (long)b - 1) * a2, e2; /* Fehler im 1. Schritt */

        do {
            ellipseCoordinates.add(new Coordinate(xm + dx, ym + dy)); /* I. Quadrant */
            ellipseCoordinates.add(new Coordinate(xm - dx, ym + dy)); /* II. Quadrant */
            ellipseCoordinates.add(new Coordinate(xm - dx, ym - dy)); /* III. Quadrant */
            ellipseCoordinates.add(new Coordinate(xm + dx, ym - dy)); /* IV. Quadrant */
            e2 = 2 * err;

            if (e2 <  (2 * (long)dx + 1) * b2) {
                ++dx;
                err += (2 * (long)dx + 1) * b2;
            }
            if (e2 > -(2 * (long)dy - 1) * a2) {
                --dy;
                err -= (2 * (long)dy - 1) * a2;
            }
        } while (dy >= 0);

        while (dx++ < a) {
            /* fehlerhafter Abbruch bei flachen Ellipsen (b=1) */
            ellipseCoordinates.add(new Coordinate(xm+dx, ym)); /* -> Spitze der Ellipse vollenden */
            ellipseCoordinates.add(new Coordinate(xm-dx, ym));
        }

    }
}
