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
        Vertex firstVertex = g.getVertices().iterator().next();
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
        var placedVertices = gs.getPlacedVertices();

        //it is the third game move and the longest edge still has to be created

        if (gs.getPlacedVertices().size() < 3){
            createLongestEdge();
            return true;
        }

        String longest_edge_type = "";


        //at least 4. game move: longest edge is already drawn

        //find the unplaced vertex with the highest number of neighbours
        Vertex new_vertex = null;
        int num_max_neigbours = 0;
        for (Vertex vertex : g.getVertices()){
            int num_neighbours = Helper.numIncidentVertices(g, gs, vertex, false);
            //check if vertex is already places and for the number neighbours
            if (!placedVertices.contains(vertex) && num_max_neigbours < num_neighbours){
                num_max_neigbours = num_neighbours;
                new_vertex = vertex;
            }
        }

        //find the coordinate for the next move
        var new_coordinate = nextFreeCoordinateOnEdge();
        if (new_coordinate.isPresent() && new_vertex != null){
            gameMove = Optional.of(new GameMove(new_vertex, new_coordinate.get()));
            return true;
        }



        return gameMove.isPresent();
    }

    /**
     * finds the next free coordinate on the largest edge from outside to inside
     * @return free coordinate
     */
    //TODO: how to find not only the next free coordinate, but the best free coordinate????
    private Optional<Coordinate> nextFreeCoordinateOnEdge(){
        String edgeType = getLongestEdgeType();
        int max_length = (edgeType.equals("diagonal")? Math.min(width, height) : Math.max(width, height));

        for (int i = 1; i < max_length/2; i++){
            int x = 0;
            int y = 0;
            int x_end = 0;
            int y_end = 0;
            switch (edgeType) {
                case "diagonal" -> {
                    x = i;
                    y = i;
                    x_end = width-i;
                    y_end = height-i;
                }
                case "vertical" -> {
                    y = i;
                    y_end = height-i;
                }
                case "horizontal" -> {
                    x = i;
                    x_end = width-i;
                }
                default ->
                    throw new IllegalArgumentException("edge type must be one of: diagonal, vertical, horizontal, but it was " + edgeType);
            }
            // check free coordinate at the start of the edge
            if (Helper.isCoordinateFree(gs.getUsedCoordinates(), x, y)){
                return Optional.of(new Coordinate(x, y));
            }
            //test free coordinate at the end of the edge
            if (Helper.isCoordinateFree(gs.getUsedCoordinates(), x_end, y_end)){
                return Optional.of(new Coordinate(x_end, y_end));
            }
        }
        return Optional.empty();
    }

    /**
     * find the longest edge (either the diagonal or the vertical/horizontal line)
     * @return "diagonal"/"vertical"/"horizontal"
     */
    private String getLongestEdgeType(){
        double diagonal_length = Math.sqrt(Math.pow(Math.min(width, height), 2) * 2);
        int max_straight_length = Math.max(width, height);

        if (diagonal_length > max_straight_length){
            return "diagonal";
        } else if (width > height){
            return "horizontal";
        } else {
            return "vertical";
        }

    }


    /**
     * creates the longest possible edge through the field
     * (ether diagonal (with slope 1) or vertical or horizontal)
     *
     * @return returns which one is the longest edge (diagonal, horizontal, vertical)
     */
    private void createLongestEdge(){
        var usedCoordinates = gs.getUsedCoordinates();
        var vertexCoordinates = gs.getVertexCoordinates();

        String edge_type = getLongestEdgeType();

        //the next vertex to place is the neighbour vertex to the firstVertex
        //(there has to be a free neighbour vertex, since the minimizer had only one game move at this point)
        Vertex new_vertex = null;
        for (Vertex v : gs.getPlacedVertices()){
            //check if the vertex is the first vertex
            if (vertexCoordinates.get(v).getX() == 0 && vertexCoordinates.get(v).getY() == 0){
                new_vertex = Helper.pickIncidentVertex(g, vertexCoordinates, v).get();
            }
        }

        int x = width-1;
        int y = height-1;

        switch (edge_type) {
            case "diagonal":
                break;
            case "vertical":
                x = 0;
                break;
            case "horizontal":
                y = 0;
                break;
            default:
                throw new IllegalArgumentException("edge type must be one of: diagonal, vertical, horizontal, but it was " + edge_type);

        }

        if (Helper.isCoordinateFree(usedCoordinates, x, y)){
            //if the diagonal coordinate is free, the diagonal vertex is placed there
            gameMove = Optional.of(new GameMove(new_vertex, new Coordinate(x, y)));
        } else {
            // if the diagonal coordinate is not free, the diagonal vertex is placed on field before/above
            // (at least this coordinate should be free, since the minimizer had only one game move at this point)
            gameMove = Optional.of(new GameMove(new_vertex, new Coordinate(Math.max(0, x-1), Math.max(0, y-1))));
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
