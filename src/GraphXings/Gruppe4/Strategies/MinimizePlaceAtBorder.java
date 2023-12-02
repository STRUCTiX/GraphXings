package GraphXings.Gruppe4.Strategies;

import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import GraphXings.Game.GameState;
import GraphXings.Gruppe4.Common.Helper;
import GraphXings.Gruppe4.MutableRTree;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;

import java.util.Optional;

public class MinimizePlaceAtBorder extends StrategyClass {

    private int border = 0;

    public MinimizePlaceAtBorder(Graph g, GameState gs, MutableRTree<Edge, LineFloat> tree, int width, int height) {
        super(g, gs, tree,  width, height);
        moveQuality = Long.MAX_VALUE;
    }



    /**
     * Execute the main strategy and calculate a game move.
     * The move must be stored and retrievable by getGameMove.
     *
     * Strategy: search for a vertex at the border and place its neighbour next to it
     * (use the move with the lowest number of crossings)
     * if this is not possible use a free vertex with at least one free neighbour an place it at the border
     * if the border is full: new border goes one coordinate inside
     *
     * @param lastMove The last opponent move.
     * @return True on success, false otherwise
     */
    @Override
    public boolean executeStrategy(GameMove lastMove) {
        var vertexCoordinates = gs.getVertexCoordinates();
        var placedVertices = gs.getPlacedVertices();

        var freeCoordinateAtBorder = pickFreeCoordinateAtBorder(border);
        int maxBorder = Math.min(width, height);
        while (freeCoordinateAtBorder.isEmpty() && border < maxBorder/2){
            border += 1;
            freeCoordinateAtBorder = pickFreeCoordinateAtBorder(border);
        }

        for (var vertex : placedVertices){
            var neighbourVertex = Helper.pickIncidentVertex(g, vertexCoordinates, vertex);
            var vertexCoordinate = vertexCoordinates.get(vertex);
            //check for the vertex, if it is at the border and if it has an unplaced neighbour and if there is a free coordinate at the border
            var coordinate_neighbour = findNextFreeCoordinateAtBorder(vertexCoordinate);
            if (isAtBorder(vertexCoordinate) && neighbourVertex.isPresent() && coordinate_neighbour.isPresent()){
                //compute intersections for the new optional move to check the quality
                long current_move_quality = computeMoveQuality(neighbourVertex.get(), coordinate_neighbour.get());

                //only update game move, if the quality of the optional current move is better than the quality of the best move
                if (this.moveQuality > current_move_quality){
                    gameMove = Optional.of(new GameMove(neighbourVertex.get(), coordinate_neighbour.get()));
                    this.moveQuality = current_move_quality;
                }
            }
            //move with the best quality was found
            if (moveQuality == 0){
                return true;
            }
        }

        //take unused vertex with at least 1 free neighbour and put it on the free coordinate on the border
        for (Vertex vertex : g.getVertices()){
            if (!placedVertices.contains(vertex) && freeCoordinateAtBorder.isPresent() && Helper.numIncidentVertices(g, gs, vertex, true) >= 1){
                long current_move_quality = computeMoveQuality(vertex, freeCoordinateAtBorder.get());
                if (moveQuality > current_move_quality){
                    gameMove = Optional.of(new GameMove(vertex, freeCoordinateAtBorder.get()));
                    moveQuality = current_move_quality;
                }
            }
            if (moveQuality == 0){
                return true;
            }
        }


        return gameMove.isPresent();
    }


    /**
     * picks the first free Coordinate at the border
     * @return the free Coordinate
     */
    //TODO: find the free coordinate that creates less crossings not only the first one that is found
    private Optional<Coordinate> pickFreeCoordinateAtBorder (int border){
        //check top border
        for (int i = border; i < width-border-1; i++){
            if (Helper.isCoordinateFree(gs.getUsedCoordinates(), i,border)){
                return Optional.of(new Coordinate(i, border));
            }
        }
        //check bottom border
        for (int i = border; i < width-border-1; i++){
            if (Helper.isCoordinateFree(gs.getUsedCoordinates(), i, height-border-1)){
                return Optional.of(new Coordinate(i, height-border-1));
            }
        }
        //check left border
        for (int i = border; i < height-border-1; i++){
            if (Helper.isCoordinateFree(gs.getUsedCoordinates(), border, i)){
                return Optional.of(new Coordinate(border,i));
            }
        }
        //check right border
        for (int i = border; i < height-border-1; i++){
            if (Helper.isCoordinateFree(gs.getUsedCoordinates(), width-border-1, i)){
                return Optional.of(new Coordinate(width-border-1, i));
            }
        }

        return Optional.empty();
    }




    /**
     * finds the next free coordinate at the border from the given coordinate
     * @param coordinate
     * @return next free coordinate
     */
    private Optional<Coordinate> findNextFreeCoordinateAtBorder (Coordinate coordinate){
        int height = this.height - border;
        int width = this.width - border;
        int xValue_old = coordinate.getX();
        int yValue_old = coordinate.getY();
        int i = 1;
        if(yValue_old == border || yValue_old == height-1){
            //coordinate is at top or bottom border

            while (xValue_old + i < width-1 || xValue_old - i > border) {
                //check field to the right side
                int xValue_new = xValue_old + i;
                if (xValue_new < width-1 && Helper.isCoordinateFree(gs.getUsedCoordinates(), xValue_new, yValue_old)){
                    return Optional.of(new Coordinate(xValue_new, yValue_old));
                }

                //check field to the left side
                xValue_new = xValue_old - i;
                if (xValue_new > border && Helper.isCoordinateFree(gs.getUsedCoordinates(), xValue_new, yValue_old)){
                    return Optional.of(new Coordinate(xValue_new, yValue_old));
                }
                i++;
            }

        } else {
           //coordinate is at left or right border
            while (yValue_old + i < height-1 || yValue_old - i > border){
                //check field above
                int yValue_new = yValue_old + i;
                if (yValue_new < height-1 && Helper.isCoordinateFree(gs.getUsedCoordinates(), xValue_old, yValue_new)){
                    return Optional.of(new Coordinate(xValue_old, yValue_new));
                }

                //check field under
                yValue_new = yValue_old - i;
                if (yValue_new > border && Helper.isCoordinateFree(gs.getUsedCoordinates(), xValue_old, yValue_new)){
                    return Optional.of(new Coordinate(xValue_old, yValue_new));
                }

                i++;
            }
        }

        // given coordinate is not at the border or no next coordinate is free
        return Optional.empty();
    }

    /**
     * Checks if a given coordinate is at the border
     * @param coordinate to check
     * @return true or false
     */
    private boolean isAtBorder(Coordinate coordinate){
        return coordinate.getX() == border || coordinate.getX() == width-border-1 || coordinate.getY() == 0 || coordinate.getY() == height-border-1;
    }



}
