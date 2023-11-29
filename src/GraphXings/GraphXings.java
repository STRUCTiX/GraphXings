package GraphXings;

import GraphXings.Algorithms.NewPlayer;
import GraphXings.Algorithms.NewRandomPlayer;
import GraphXings.Game.GameInstance.RandomCycleFactory;
import GraphXings.Game.League.NewLeague;
import GraphXings.Game.League.NewLeagueResult;
import GraphXings.Gruppe4.RTreePlayer;

import java.util.ArrayList;

public class GraphXings
{
    public static void main (String[] args)
    {
        ArrayList<NewPlayer> players = new ArrayList<>();
        players.add(new RTreePlayer("RTreePlayer"));
        players.add(new NewRandomPlayer("RandomPlayer"));

        //TODO: add players here
        RandomCycleFactory factory = new RandomCycleFactory(24091869, true);
        long timeLimit = 300000000000l;
        NewLeague l = new NewLeague(players,3,timeLimit,factory);
        NewLeagueResult lr = l.runLeague();
        System.out.println(lr.announceResults());
    }
}
