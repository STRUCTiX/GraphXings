package GraphXings.Gruppe4.Strategies;

import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import GraphXings.Game.GameState;
import GraphXings.Gruppe4.CanvasObservations.SampleParameters;
import GraphXings.Gruppe4.Common.Helper;
import GraphXings.Gruppe4.MutableRTree;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;
import com.sun.source.tree.IfTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaximizeGrid extends StrategyClass{

    public MaximizeGrid(Graph g, GameState gs, MutableRTree<Edge, LineFloat> tree, int width, int height, SampleParameters sampleParameters) {
        super(g, gs, tree, width, height, sampleParameters);
        moveQuality = 0;
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
     * Strategy: build a grid with vertices from the border
     *
     * @param lastMove The last opponent move.
     * @return True on success, false otherwise
     */
    @Override
    public boolean executeStrategy(GameMove lastMove) {
        var usedCoordinates = gs.getUsedCoordinates();
        var vertexCoordinates = gs.getVertexCoordinates();
        var placedVertices = gs.getPlacedVertices();

        //search for a placed vertex at the border with free neighbours
        Vertex placedVertex;
        Coordinate vertexCoordinate;
        for (Vertex v : placedVertices){
            vertexCoordinate = vertexCoordinates.get(v);
            if (Helper.isAtBorder(vertexCoordinate, width-1, 0, 0, height-1) && Helper.numIncidentVertices(g, gs, v, true) > 0){
                Vertex neigbourVertex = Helper.pickIncidentVertex(g, vertexCoordinates, v).get(); //should be possible since the number of free neighbours is > 0
                var oppositeCoordinate = findOppositeCoordinate(vertexCoordinate);
                if (oppositeCoordinate.isPresent()){
                    gameMove = Optional.of(new GameMove(neigbourVertex, oppositeCoordinate.get()));
                    return true;
                }

            }
        }

        //if no vertex at the border with free neighbours is found -> set a free vertex at the border
        for (Vertex v : g.getVertices()){
            if (!placedVertices.contains(v) && Helper.numIncidentVertices(g, gs, v, true) > 0){
                var freeCoordinatesAtBorder = Helper.findFreeCoordinatesAtBorder(usedCoordinates, width-1, 0, 0, height-1);
                freeCoordinatesAtBorder.ifPresent(coordinateList -> gameMove = chooseHighestIntersection(List.of(v), coordinateList));
            }
        }



        return gameMove.isPresent();
    }


    /**
     * find the next free coordinate at the opposite border from the given coordinate
     * @param coordinate coordinate at border
     * @return opposite coordinate
     */
    private Optional<Coordinate> findOppositeCoordinate (Coordinate coordinate){
        //coordinate is left
        if (coordinate.getX() == 0){
            for (int i = width-1; i > width/2; i--){
                if (Helper.isCoordinateFree(gs.getUsedCoordinates(), i, coordinate.getY())){
                    return Optional.of(new Coordinate(i, coordinate.getY()));
                }
            }
        }
        //coordinate is right
        if (coordinate.getX() == width-1){
            for (int i = 0; i < width/2; i++){
                if (Helper.isCoordinateFree(gs.getUsedCoordinates(), i, coordinate.getY())){
                    return Optional.of(new Coordinate(i, coordinate.getY()));
                }
            }
        }
        //coordinate is at the top
        if (coordinate.getY() == 0){
            for (int i = 0; i < height/2; i++){
                if (Helper.isCoordinateFree(gs.getUsedCoordinates(), coordinate.getX(), i)){
                    return Optional.of(new Coordinate(coordinate.getX(), i));
                }
            }
        }
        //coordinate is at the bottom
        if (coordinate.getY() == height-1){
            for (int i = height-1; i < height/2; i++){
                if (Helper.isCoordinateFree(gs.getUsedCoordinates(), coordinate.getX(), i)){
                    return Optional.of(new Coordinate(coordinate.getX(), i));
                }
            }
        }
        return Optional.empty();

    }


    /**
     * This should return a fixed strategy name
     * which is used by the GameObserver.
     *
     * @return A strategy name
     */
    @Override
    public StrategyName getStrategyName() {
        return StrategyName.MaximizeGrid;
    }
}