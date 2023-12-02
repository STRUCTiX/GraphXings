package GraphXings.Gruppe4.Strategies;

import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import GraphXings.Game.GameState;
import GraphXings.Gruppe4.MutableRTree;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;

import java.util.List;
import java.util.Optional;

public abstract class StrategyClass implements GraphXings.Gruppe4.Strategy {

    protected final Graph g;
    protected final MutableRTree<Edge, LineFloat> tree;
    protected final GameState gs;
    protected final int width;
    protected final int height;

    protected Optional<GameMove> gameMove =Optional.empty();

    protected long moveQuality;

    public StrategyClass(Graph g, GameState gs, MutableRTree<Edge, LineFloat> tree, int width, int height) {
        this.g = g;
        this.tree = tree;
        this.gs = gs;
        this.width = width;
        this.height = height;
    }

    /**
     * Executes the heuristic as the first or second move.
     *
     * @param lastMove Is empty on first move otherwise provides the last opponent game move.
     * @return True on success, false otherwise.
     */
    @Override
    public boolean executeHeuristic(Optional<GameMove> lastMove) {
        return false;
    }


    /**
     * Execute the main strategy and calculate a game move.
     * The move must be stored and retrievable by getGameMove.
     *
     * @param lastMove The last opponent move.
     * @return True on success, false otherwise
     */
    @Override
    public boolean executeStrategy(GameMove lastMove) {
        return false;
    }


    /**
     * Retrieve a calculated game move.
     *
     * @return A game move. Empty if execution wasn't successful.
     */
    @Override
    public Optional<GameMove> getGameMove() {
        return gameMove;
    }


    /**
     * Quality of the current game move.
     * This number represents how many crossings can be achieved by a game move.
     * For a maximizer this number should be large.
     *
     * @return Number of crossings.
     */
    @Override
    public long getGameMoveQuality() {
        return moveQuality;
    }


    /**
     * choose the coordinate with the highest Intersection for a given vertex
     * and builds a new game move
     * @param unplacedVertex
     * @param samples set of vertices to choose from
     * @return game move if present
     */
    public Optional<GameMove> chooseHighestIntersection(Vertex unplacedVertex, List<Coordinate> samples) {
        Coordinate bestCoord = null;
        long maxCrossings = 0;
        for (var sampleCoord : samples) {

            long numCrossings = computeMoveQuality(unplacedVertex, sampleCoord);
            if (numCrossings > maxCrossings) {
                maxCrossings = numCrossings;
                bestCoord = sampleCoord;
            }
        }

        if (maxCrossings <= 0) {
            moveQuality = 0;
            return Optional.empty();
        }

        // Use the best coordinate
        moveQuality = maxCrossings;
        return Optional.of(new GameMove(unplacedVertex, bestCoord));
    }

    /**
     * computes move quality by computing the number of crossings
     * for all edges that are created by placing the given vertex
     * @param vertex to place
     * @param coordinate at which the vertex should be placed
     * @return number of crossings
     */
    public long computeMoveQuality (Vertex vertex, Coordinate coordinate){
        var placedVertices = gs.getPlacedVertices();
        var vertexCoordinates = gs.getVertexCoordinates();
        var incidentEdges = g.getIncidentEdges(vertex);
        long current_move_quality = 0;

        //check for all edges that the vertex has, if they are already existing
        for (Edge e : incidentEdges) {
            if(placedVertices.contains(e.getS()) || placedVertices.contains(e.getT())){
                LineFloat edge;
                if (e.getT().equals(vertex)){
                    edge = LineFloat.create(vertexCoordinates.get(e.getS()).getX(), vertexCoordinates.get(e.getS()).getY(), coordinate.getX(), coordinate.getY());
                } else {
                    edge = LineFloat.create(coordinate.getX(), coordinate.getY(), vertexCoordinates.get(e.getT()).getX(), vertexCoordinates.get(e.getT()).getY());

                }
                current_move_quality += tree.getIntersections(edge);
            }
        }

        //additionally add all crossings that will be created by the free neighbour edges
        return current_move_quality;
    }
}
