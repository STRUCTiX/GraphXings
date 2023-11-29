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

import java.util.List;
import java.util.Optional;

public class MaximizePlaceVertexOnEdge implements Strategy {
    private final Graph g;
    private final MutableRTree<Edge, LineFloat> tree;
    private final GameState gs;
    private final int width;
    private final int height;

    private Optional<GameMove> gameMove;

    private long moveQuality = 0;
    private Vertex firstVertex;


    public MaximizePlaceVertexOnEdge(Graph g, GameState gs, MutableRTree<Edge, LineFloat> tree, int width, int height) {
        this.g = g;
        this.tree = tree;
        this.gs = gs;
        this.width = width;
        this.height = height;
    }


    /**
     * Puts a vertex at Coordinate (0,0)
     * this should be possible, because it is the first move in the game, since the maximizer begins
     *
     * @param lastMove Is empty on first move otherwise provides the last opponent game move.
     * @return True .
     */
    @Override
    public boolean executeHeuristic(Optional<GameMove> lastMove) {
        firstVertex = g.getVertices().iterator().next();
        gameMove = Optional.of(new GameMove(firstVertex, new Coordinate(0, 0)));
        return true;
    }

    /**
     * Execute the main strategy and calculate a game move.
     * The move must be stored and retrievable by getGameMove.
     *
     * Strategy: create the longest edge in the field
     *           and put the vertices with the highest number of neighbours onto the edge
     *
     * @param lastMove The last opponent move.
     * @return True on success, false otherwise
     */
    @Override
    public boolean executeStrategy(GameMove lastMove) {
        var usedCoordinates = gs.getUsedCoordinates();
        var vertexCoordinates = gs.getVertexCoordinates();
        var placedVertices = gs.getPlacedVertices();

        //it is the third game move and the longest edge still has to be created
        if (gs.getPlacedVertices().size() < 3){
            createLongestEdge();
            return true;
        }

        //at least 4. game move: longest edge is already drawn

        //find the unplaced vertex with the highest number of neighbours
        Vertex new_vertex;
        int num_neigbours = 0;
        for (Vertex vertex : g.getVertices()){
            //if (!placedVertices.contains(vertex) && num_neigbours < Helper.)
        }



        return gameMove.isPresent();
    }

    /**
     * creates the longest possible edge through the field
     * (ether diagonal (with slope 1) or vertical or horizontal)
     */
    private void createLongestEdge(){
        var usedCoordinates = gs.getUsedCoordinates();
        var vertexCoordinates = gs.getVertexCoordinates();

        //find the longest edge (either the diagonal or the vertical/horizontal line)
        double diagonal_length = Math.sqrt(Math.pow(Math.min(width, height), 2) * 2);
        int max_straight_length = Math.max(width, height);

        //the next vertex to place is the neighbour vertex to thr firstVertex
        //(there has to be a free neighbour vertex, since the minimizer had only one game move at this point)
        Vertex new_vertex = Helper.pickIncidentVertex(g, vertexCoordinates, firstVertex).get();

        if (diagonal_length >= max_straight_length){
            // the diagonal is the longest edge
            if (Helper.isCoordinateFree(usedCoordinates, width-1, height-1)){
                //if the diagonal coordinate is free, the diagonal vertex is placed there
                gameMove = Optional.of(new GameMove(new_vertex, new Coordinate(width - 1, height - 1)));
            } else {
                // if the diagonal coordinate is not free, the diagonal vertex is placed on field before/above
                // (at least this coordinate should be free, since the minimizer had only one game move at this point)
                gameMove = Optional.of(new GameMove(new_vertex, new Coordinate(width-2, height-2)));
            }
        } else {
            //the horizontal/vertical is the longest edge
            if (width >= height){
                // vertical line is the longest: edge at the top
                if (Helper.isCoordinateFree(usedCoordinates, width-1, 0)) {
                    //if the last coordinate at the top is free, the vertex is placed there
                    gameMove = Optional.of(new GameMove(new_vertex, new Coordinate(width - 1, 0)));
                } else {
                    //the last coordinate at the top is not free, the diagonal vertex is placed one coordinate before
                    // (at least this coordinate should be free, since the minimizer had only one game move at this point)
                    gameMove = Optional.of(new GameMove(new_vertex, new Coordinate(width - 2, 0)));
                }
            } else {
                // horizontal line is the longest: edge on the left side
                if (Helper.isCoordinateFree(usedCoordinates, 0, height-1)) {
                    //if the last coordinate on the left is free, the vertex is placed there
                    gameMove = Optional.of(new GameMove(new_vertex, new Coordinate(0, height-1)));
                } else {
                    //the last coordinate on the left is not free, the vertex is placed one coordinate before
                    // (at least this coordinate should be free, since the minimizer had only one game move at this point)
                    gameMove = Optional.of(new GameMove(new_vertex, new Coordinate(0, height-2)));
                }
            }
        }
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

    public Optional<GameMove> chooseHighestIntersection(MutableRTree<Edge, LineFloat> tree, Vertex unplacedVertex, Coordinate maxDistCoordinate, List<Coordinate> samples) {
        Coordinate bestCoord = null;
        var maxCrossings = Long.MIN_VALUE;
        for (var sampleCoord : samples) {

            // Create a line to test for intersections
            var line = LineFloat.create(maxDistCoordinate.getX(), maxDistCoordinate.getY(), sampleCoord.getX(), sampleCoord.getY());
            var numCrossings = tree.getIntersections(line);
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
}
