package GraphXings.Gruppe4.Strategies;

import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import GraphXings.Game.GameState;
import GraphXings.Gruppe4.Common.Helper;
import GraphXings.Gruppe4.Heuristics;
import GraphXings.Gruppe4.MutableRTree;
import GraphXings.Gruppe4.Strategy;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;

import java.util.Optional;

import static GraphXings.Gruppe4.Common.Helper.minimizeBounds;

public class MinimizePlaceNextToOpponent implements Strategy {
    private final Graph g;
    private final MutableRTree<Edge, LineFloat> tree;
    private final GameState gs;
    private final int width;
    private final int height;

    private Optional<GameMove> gameMove = Optional.empty();

    private long moveQuality = Long.MAX_VALUE;

    public MinimizePlaceNextToOpponent(Graph g, GameState gs, MutableRTree<Edge, LineFloat> tree, int width, int height) {
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
        gameMove = Heuristics.getMostDistantGameMoveOnCanvasCorners(g, gs.getUsedCoordinates(), gs.getVertexCoordinates(), lastMove.get(), gs.getPlacedVertices(), width, height);
        return gameMove.isPresent();
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
        var vertexCoordinates = gs.getVertexCoordinates();
        var usedCoordinates = gs.getUsedCoordinates();
        var placedVertices = gs.getPlacedVertices();

        // Try to place the new vertex next to the last placed vertex.
        // This is only possible if one of the adjacent vertices is unplaced.
        var unplacedVertex = Helper.pickIncidentVertex(g, vertexCoordinates, lastMove);

        // If we've found an unplaced vertex -> try to place it next to the last game move vertex
        if (unplacedVertex.isPresent()) {
            var lastCoord = lastMove.getCoordinate();

            var gameMove = minimizeBounds(usedCoordinates, tree, unplacedVertex.get(), lastCoord, 1, 1);
            if (gameMove.isPresent()) {
                this.gameMove = gameMove;
                moveQuality = computeMoveQuality(gameMove.get().getVertex(), gameMove.get().getCoordinate());
                return true;
            }
        }

        // In this case we either don't have an unplaced vertex or the checked perimeter was too small
        // TODO: Currently we just use the heuristic from week 2/3 which doesn't really fit to our new strategy
        // TODO: A better strategy would be to increase the perimeter to width x height and go from the usedCoordinate towards width/height
        if (unplacedVertex.isPresent()) {
            var result = Heuristics.getFirstFreeGameMoveOnCanvasOutline(g, usedCoordinates, width, height, unplacedVertex.get());
            if (result.isPresent()) {
                this.gameMove = result;
                this.moveQuality = computeMoveQuality(result.get().getVertex(), result.get().getCoordinate());
                return true;
            }
        }

        // Find the first unplaced vertex
        for (var v : g.getVertices()) {
            if (!placedVertices.contains(v)) {
                this.gameMove = Heuristics.getFirstFreeGameMoveOnCanvasOutline(g, usedCoordinates, width, height, v);
                // In this case we return a result, or we do a random move.
                gameMove.ifPresent(move -> moveQuality = computeMoveQuality(move.getVertex(), move.getCoordinate()));

                return gameMove.isPresent();
            }
        }
        return gameMove.isPresent();
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
     * computes move quality by computing the number of crossings
     * for all edges that are created by placing the given vertex
     * @param vertex ton place
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
                LineFloat edge = null;
                if (e.getT().equals(vertex)){
                    edge = LineFloat.create(vertexCoordinates.get(e.getS()).getX(), vertexCoordinates.get(e.getS()).getY(), coordinate.getX(), coordinate.getY());
                } else {
                    edge = LineFloat.create(coordinate.getX(), coordinate.getY(), coordinate.getX(), coordinate.getY());

                }
                current_move_quality += tree.getIntersections(edge);
            }
        }

        //additionally add all crossings that will be created by the free neighbour edges
        return current_move_quality;
    }
}
