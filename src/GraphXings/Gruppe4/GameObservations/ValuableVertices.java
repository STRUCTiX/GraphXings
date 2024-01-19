package GraphXings.Gruppe4.GameObservations;

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

    public void computeRank() {
        for (var vertex : g.getVertices()) {
            var incidentCount = Iterables.size(g.getIncidentEdges(vertex));
            vertexIncidentEdges.add(new AbstractMap.SimpleImmutableEntry<>(vertex, incidentCount));
        }

        vertexIncidentEdges.sort(Map.Entry.comparingByValue());
    }

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
}
