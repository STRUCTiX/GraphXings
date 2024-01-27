package GraphXings.Gruppe4.Strategies;

import GraphXings.Algorithms.NewPlayer;
import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameMove;
import GraphXings.Game.GameState;
import GraphXings.Gruppe4.Common.Helper;
import GraphXings.Gruppe4.GameObservations.CanvasObservations.SampleParameters;
import GraphXings.Gruppe4.Heuristics;
import GraphXings.Gruppe4.MutableRTree;
import GraphXings.Gruppe4.StrategiesStopWatch;
import com.github.davidmoten.rtree2.geometry.internal.LineFloat;

import java.util.List;
import java.util.Optional;

/**
 * Use this strategy on small canvases
 */
public class BruteforceCrossing extends StrategyClass {

    private NewPlayer.Role role;

    public BruteforceCrossing(Graph g, GameState gs, MutableRTree<Edge, LineFloat> tree, int width, int height, SampleParameters sampleParameters, StrategiesStopWatch strategiesStopWatch, NewPlayer.Role role) {
        super(g, gs, tree, width, height, sampleParameters, strategiesStopWatch.getWatch(StrategyName.Bruteforce));
        moveQuality = 0;
        this.role = role;
    }


    /**
     * Executes the heuristic as the first or second move.
     *
     * Heuristic: Places a vertex into the middle of the match field
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
        super.startExecuteStrategy();

        var unplacedVertex = Helper.pickIncidentVertex(g, gs.getVertexCoordinates(), lastMove);

        GameMove move;
        if (unplacedVertex.isPresent()) {
            move = bruteforce(lastMove, unplacedVertex.get());
        } else {
            // Pick a free vertex
            Vertex vertex = null;
            for (var v : g.getVertices()) {
                if (!gs.getPlacedVertices().contains(v)) {
                    vertex = v;
                    break;
                }
            }
            move = bruteforce(lastMove, vertex);
        }

        gameMove = Optional.of(move);

        super.stopExecuteStrategy();

        return gameMove.isPresent();
    }


    private GameMove bruteforce(GameMove lastMove, Vertex unplacedVertex) {
        var intersections = (role == NewPlayer.Role.MAX) ? Long.MIN_VALUE : Long.MAX_VALUE;
        int x = 0;
        int y = 0;
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                if (!Helper.isCoordinateFree(gs.getUsedCoordinates(), w, h)) {
                    continue;
                }

                var lm = lastMove.getCoordinate();
                var line = LineFloat.create(lm.getX(), lm.getY(), w, h);
                var currentIntersections = tree.getIntersections(line);
                if (role == NewPlayer.Role.MAX && currentIntersections > intersections) {
                    intersections = currentIntersections;
                    x = w;
                    y = h;
                } else if (role == NewPlayer.Role.MIN && currentIntersections < intersections) {
                    intersections = currentIntersections;
                    x = w;
                    y = h;
                }
            }
        }
        moveQuality = intersections;
        return new GameMove(unplacedVertex, new Coordinate(x, y));
    }

    /**
     * This should return a fixed strategy name
     * which is used by the GameObserver.
     *
     * @return A strategy name
     */
    @Override
    public StrategyName getStrategyName() {
        return StrategyName.Bruteforce;
    }

    /**
     * Determine if the strategy should be calculated.
     *
     * @param percentagePlacedMoves Value between 0-100. 100 means game is over.
     * @return True if the strategy is effective and should be calculated.
     */
    @Override
    public boolean activateFunction(double percentagePlacedMoves, int currentMove, int totalMoves) {
        // Use Bruteforce strategy only if we have a small field (< 100x100)
        // and in late game -> we can skip most of the fields so the strategy computes faster.
        return width * height < 10000 && percentagePlacedMoves > 70.0;
    }

}
