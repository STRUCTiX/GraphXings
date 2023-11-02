package GraphXings.Gruppe4.Common;

import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;
import com.github.davidmoten.rtree2.internal.EntryDefault;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TreeHelper {

    /**
     * Create a list of lines which can be used by the R-Tree
     * from the edges
     * @param g The graph
     * @param vertexCoordinates Mapping between vertex and coordinates
     * @param placedVertices Already placed vertices
     * @return A list of generated lines
     */
    public static List<Entry<Edge, LineFloat>> createLinesFromPlacedEdges(Graph g, HashMap<Vertex, Coordinate> vertexCoordinates, HashSet<Vertex> placedVertices) {
        return StreamSupport.stream(g.getEdges().spliterator(), false)
                .filter((e) -> placedVertices.contains(e.getS()) && placedVertices.contains(e.getT()))
                .map((e) -> {
                    // We've found a placed edge -> create a line
                    var s = vertexCoordinates.get(e.getS());
                    var t = vertexCoordinates.get(e.getT());

                    // Create a line from coordinates
                    return EntryDefault.entry(e, LineFloat.create(s.getX(), s.getY(), t.getX(), t.getY()));
                })
                .collect(Collectors.toList());
    }
}
