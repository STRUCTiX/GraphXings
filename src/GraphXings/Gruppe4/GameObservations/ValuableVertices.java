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
    private ArrayList<Map.Entry<Vertex, Long>> vertexIncidentEdges;


    public ValuableVertices(Graph g, GameState gs) {
        this.g = g;
        this.gs = gs;
        vertexIncidentEdges = new ArrayList<>(((HashSet<Vertex>)g.getVertices()).size());
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
     * Bresenham circle algorithm.
     * @param xm Middle coordinate in x direction
     * @param ym Middle coordinate in x direction
     * @param a
     * @param b
     * @return
     */
    private ArrayList<Coordinate> ellipse(int xm, int ym, int a, int b) {
        int dx = 0, dy = b; /* im I. Quadranten von links oben nach rechts unten */
        long a2 = a * (long)a, b2 = b * (long)b;
        long err = b2 - (2 * (long)b - 1) * a2, e2; /* Fehler im 1. Schritt */

        ArrayList<Coordinate> coordinates = new ArrayList<>();

        do {
            coordinates.add(new Coordinate(xm + dx, ym + dy)); /* I. Quadrant */
            coordinates.add(new Coordinate(xm - dx, ym + dy)); /* II. Quadrant */
            coordinates.add(new Coordinate(xm - dx, ym - dy)); /* III. Quadrant */
            coordinates.add(new Coordinate(xm + dx, ym - dy)); /* IV. Quadrant */
            e2 = 2 * err;

            if (e2 <  (2 * (long)dx + 1) * b2) {
                ++dx; err += (2 * (long)dx + 1) * b2;
            }
            if (e2 > -(2 * (long)dy - 1) * a2) {
                --dy; err -= (2 * (long)dy - 1) * a2;
            }
        } while (dy >= 0);

        while (dx++ < a) {
            /* fehlerhafter Abbruch bei flachen Ellipsen (b=1) */
            coordinates.add(new Coordinate(xm+dx, ym)); /* -> Spitze der Ellipse vollenden */
            coordinates.add(new Coordinate(xm-dx, ym));
        }

        return coordinates;
    }
}
