package GraphXings.Gruppe4.Gui;

import GraphXings.Algorithms.NewPlayer;
import GraphXings.Data.Graph;
import GraphXings.Game.GameMove;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class exports the relevant game data to display the graph in a different application.
 * The high level format is specified as follows:
 * - The first part of the txt document contains the graph structure
 * - The following part contains the placement of vertices in the correct order
 * More specific:
 * The graph structure will be exported as follows:
 * 1. Write all vertices delimited by \n to the file
 * 2. Write a delimiter: \\\n (backslash)
 * 3. Write all edge tuples separated with "," and delimited by \n
 * 4. Write a delimiter: \\\n (backslash)
 * Now we're able to write the vertices placement
 * 5. Write a tuple: role (max/min), vertex id, coordinate (x,y) separated by "," and delimited by \n
 */
public class GuiExport {

    private FileWriter file;
    private BufferedWriter buffer;


    public GuiExport() throws IOException {
        // Get the current time after the Unix epoch.
        // This ensures that filenames are unique
        long currentTime = System.currentTimeMillis();
        file = new FileWriter("./" + currentTime + ".txt");
        buffer = new BufferedWriter(file);
    }

    /**
     * This should be called on InitializeRound
     * @param g
     * @throws IOException Can't write to file
     */
    public void exportGraphStructure(Graph g) throws IOException {
        // Export vertices ids
        for (var v : g.getVertices()) {
            buffer.write(v.getId() + "\n");
        }

        // Write a delimiter
        buffer.write("\\\n");

        // Export all edges
        for (var e : g.getEdges()) {
            buffer.write(e.getS() + "," + e.getT() + "\n");
        }

        // Write a delimiter
        buffer.write("\\\n");
    }

    /**
     * This should be executed before and after our move to record the opponent and our move.
     * @param move
     * @param role Which role has executed this move?
     * @throws IOException
     */
    public void exportVertexPlacement(GameMove move, NewPlayer.Role role) throws IOException {
        var vertex = move.getVertex();
        var coordinate = move.getCoordinate();

        // Write tuple
        buffer.write(role.name() + "," + vertex.getId() + "," + coordinate.getX() + "," + coordinate.getY() + "\n");
    }
}
