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

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class MinimizePlaceAtBorder implements Strategy {

    private Graph g;
    private MutableRTree<Edge, LineFloat> tree;
    private GameState gs;
    private int width;
    private int height;

    private Optional<GameMove> gameMove;

    private long moveQuality = Long.MAX_VALUE;



    /**
     * Executes the heuristic as the first or second move.
     *
     * @param lastMove Is empty on first move otherwise provides the last opponent game move.
     * @return True on success, false otherwise.
     */
    @Override
    public boolean executeHeuristic(Optional<GameMove> lastMove) {
        gameMove = Heuristics.getFreeGameMoveOnCanvasCenter(g, gs.getUsedCoordinates(), gs.getVertexCoordinates(), null, gs.getPlacedVertices(), width, height);
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
        var placedVertices = gs.getPlacedVertices();

        for (var vertex : placedVertices){
            var neighbourVertex = Helper.pickIncidentVertex(g, vertexCoordinates, vertex);
            var vertexCoordinate = vertexCoordinates.get(vertex);
            //check for the vertex, if it is at the border and if it has an unplaced neighbour and if there is a free coordinate at the border
            var coordinate_neighbour = findNextFreeCoordinateAtBorder(vertexCoordinate);
            if (isAtBorder(vertexCoordinate) && neighbourVertex.isPresent() && coordinate_neighbour.isPresent()){
                var edge = LineFloat.create(vertexCoordinate.getX(), vertexCoordinate.getY(), coordinate_neighbour.get().getX(), coordinate_neighbour.get().getY());
                long current_move_quality = tree.getIntersections(edge);
                if (this.moveQuality > current_move_quality){
                    gameMove = Optional.of(new GameMove(neighbourVertex.get(), coordinate_neighbour.get()));
                    this.moveQuality = current_move_quality;
                }
            }
            if (moveQuality == 0){
                break;
            }
        }

        return gameMove.isPresent();
    }


    /**
     * finds the next free coordinate at the border from the given coordinate
     * @param coordinate border coordinate
     * @return next free coordinate
     */
    private Optional<Coordinate> findNextFreeCoordinateAtBorder (Coordinate coordinate){
        int xValue_old = coordinate.getX();
        int yValue_old = coordinate.getY();
        int i = 1;
        Coordinate returnCoordinate = null;
        if(yValue_old == 0 || yValue_old == height-1){
            //coordinate is at top or bottom border

            while (xValue_old + i < width-1 || xValue_old - i > 0) {
                //check field to the right side
                int xValue_new = xValue_old + i;
                returnCoordinate = new Coordinate(xValue_new, yValue_old);
                if (xValue_new < width-1 && Helper.isCoordinateFree(gs.getUsedCoordinates(), returnCoordinate)){
                    //returnCoordinate = new Coordinate(xValue_new, yValue_old);
                    return Optional.of(returnCoordinate);
                }

                //check field to the left side
                xValue_new = xValue_old - i;
                returnCoordinate = new Coordinate(xValue_new, yValue_old);
                if (xValue_new > 0 && Helper.isCoordinateFree(gs.getUsedCoordinates(), returnCoordinate)){
                    //returnCoordinate = new Coordinate(xValue_new, yValue_old);
                    return Optional.of(returnCoordinate);
                }
                i++;
            }

        } else {
           //coordinate is at left or right border
            while (yValue_old + i < height-1 || yValue_old - i > 0){
                //check field above
                int yValue_new = yValue_old + i;
                returnCoordinate = new Coordinate(xValue_old, yValue_new);
                if (yValue_new < height-1 && Helper.isCoordinateFree(gs.getUsedCoordinates(), returnCoordinate)){
                    return Optional.of(returnCoordinate);
                }

                //check field under
                yValue_new = yValue_old - i;
                returnCoordinate = new Coordinate(xValue_old, yValue_new);
                if (yValue_new > 0 && Helper.isCoordinateFree(gs.getUsedCoordinates(), returnCoordinate)){
                    return Optional.of(returnCoordinate);
                }

                i++;
            }
        }

        return Optional.ofNullable(returnCoordinate);
    }

    /**
     * Checks if the given vertex is at the border
     * @param coordinate to check
     * @return true or false
     */
    private boolean isAtBorder(Coordinate coordinate){
        return coordinate.getX() == 0 || coordinate.getX() == width-1 || coordinate.getY() == 0 || coordinate.getY() == height-1;
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


}
