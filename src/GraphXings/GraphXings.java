package GraphXings;
import GraphXings.Algorithms.CrossingCalculator;
import GraphXings.Algorithms.NewPlayer;
import GraphXings.Algorithms.NewRandomPlayer;
import GraphXings.Data.Coordinate;
import GraphXings.Data.Edge;
import GraphXings.Data.Graph;
import GraphXings.Data.Vertex;
import GraphXings.Game.GameInstance.PlanarExampleInstanceFactory;
import GraphXings.Game.GameInstance.RandomCycleFactory;
import GraphXings.Game.League.NewLeague;
import GraphXings.Game.League.NewLeagueResult;
import GraphXings.Gruppe4.RTreePlayer;


import java.util.HashMap;


import java.util.ArrayList;


public class GraphXings
{
    public static void main (String[] args)
    {
        ArrayList<NewPlayer> players = new ArrayList<>();
        players.add(new RTreePlayer("RTreePlayer"));
        players.add(new NewRandomPlayer("RandomPlayer"));
        //players.add(new RTreePlayer("RTreePlayer2"));


        /*
        HashMap<Vertex, Coordinate> vertexCoordinates = new HashMap<>();
        vertexCoordinates.put(v1, new Coordinate(0,3));
        vertexCoordinates.put(v2, new Coordinate(1,3));
        vertexCoordinates.put(v3, new Coordinate(2,3));
        vertexCoordinates.put(v4, new Coordinate(1,2));
        vertexCoordinates.put(v5, new Coordinate(1,4));

        CrossingCalculator c = new CrossingCalculator(g, vertexCoordinates);
        System.out.println(c.computeCrossingNumber());
        */

        //// Play 1000 games so we can see if the new player wins consistently
        //int player1 = 0, player2 = 0;
        //var randPlayer = new RandomPlayer("Player 1");
        //var gamesNum = 10;
        //for (int i = 0; i < gamesNum; i++) {
        //    // Run the game with two players.
        //    Game game = new Game(g, 1000, 1000, randPlayer, new RTreePlayer("Player 2"));
        //    GameResult res = game.play();
        //    // Display the result!
        //    System.out.println(res.announceResult());
        //    if (res.getWinner() == null) {
        //        // Draw
        //        player1++;
        //        player2++;
        //    } else if (res.getWinner().equals(randPlayer)) {
        //        player1++;
        //    } else {
        //        player2++;
        //    }
        //}
        //System.out.println("Total games: " + gamesNum + ", Player 1 wins: " + player1 + ", Player 2 wins: " + player2);

        /*
        var rcf = new RandomCycleFactory(21122012, true);
        var match = new NewMatch(new NewRandomPlayer("RandomPlayer"), new RTreePlayer("RTreePlayer"), rcf, 10);
        var result = match.play();
        System.out.println(result.announceResult());
        */


        //TODO: add players here
        //RandomCycleFactory factory = new RandomCycleFactory(24091869, true);
        PlanarExampleInstanceFactory factory = new PlanarExampleInstanceFactory();
        long timeLimit = 300000000000l;
        NewLeague l = new NewLeague(players,10,timeLimit,factory);
        NewLeagueResult lr = l.runLeague();
        System.out.println(lr.announceResults());


    }
}
