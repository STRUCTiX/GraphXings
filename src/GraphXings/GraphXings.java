package GraphXings;
import GraphXings.Algorithms.NewPlayer;
import GraphXings.Algorithms.NewRandomPlayer;
import GraphXings.Data.Coordinate;
import GraphXings.Data.Segment;
import GraphXings.Game.GameInstance.GameInstanceFactory;
import GraphXings.Game.GameInstance.PlanarGameInstanceFactory;
import GraphXings.Game.League.NewLeague;
import GraphXings.Game.League.NewLeagueResult;
import GraphXings.Game.Match.NewMatch;
import GraphXings.Game.Match.NewMatchResult;
import GraphXings.Gruppe4.RTreePlayer;

import java.util.ArrayList;
import java.util.Random;


public class GraphXings
{
    public static void main (String[] args)
    {
        ArrayList<NewPlayer> players = new ArrayList<>();
        //TODO: add players here
        players.add(new NewRandomPlayer("R1"));
        var rTreePlayer = new RTreePlayer("RTree");
        players.add(rTreePlayer);
        long timeLimit = 300000000000l;
        long seed = 27081883;
        int bestOf = 10;
        NewMatch.MatchType matchType = NewMatch.MatchType.CROSSING_ANGLE;
        PlanarGameInstanceFactory factory = new PlanarGameInstanceFactory(seed);
        runLeague(players,bestOf,timeLimit,factory,matchType,seed);
        //runRemainingMatches(player,players,bestOf,timeLimit,factory);

        rTreePlayer.getGuiExport().close();
    }

    public static void runLeague(ArrayList<NewPlayer> players, int bestOf, long timeLimit, GameInstanceFactory factory, NewMatch.MatchType matchType, long seed)
    {
        NewLeague l = new NewLeague(players,bestOf,timeLimit,factory,matchType,seed);
        NewLeagueResult lr = l.runLeague();
        System.out.println(lr.announceResults());

    }
    public static void runRemainingMatches(NewPlayer p1, ArrayList<NewPlayer> opponents, int bestOf, long timeLimit, GameInstanceFactory factory, NewMatch.MatchType matchType, long seed)
    {
        int i = 1;
        for (NewPlayer opponent : opponents)
        {
            NewMatch m = new NewMatch(p1,opponent,factory,bestOf,timeLimit,matchType,seed);
            NewMatchResult mr = m.play();
            System.out.println("Match " + i++ + ": " + mr.announceResult());
        }
    }
}
