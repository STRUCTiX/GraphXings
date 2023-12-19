import networkx as nx
import matplotlib.pyplot as plt
import sys
import os

# Data structure of parsed data
class GraphData:
    playername = ""
    playerrole = ""
    vertices = []
    edges = []
    gamemoves = []

    # Parse the content of the file into the data structure
    def parseLines(self, lines):
        parsemode = 0
        for line in lines:
            cleanline = line.strip()
            if cleanline == '\\':
                parsemode += 1
                continue # Skip the current line
            if parsemode == 0:
                self.playername = cleanline
            elif parsemode == 1:
                self.playerrole = cleanline
            elif parsemode == 2:
                self.vertices.append(cleanline)
            elif parsemode == 3:
                v1, v2 = cleanline.split(",")
                self.edges.append((v1, v2))
            elif parsemode == 4:
                # We have the following format for game moves:
                # role, vertex, x, y, player strategy
                role, vertex, x, y, player_strategy = cleanline.split(",")
                self.gamemoves.append((role, vertex, x, y, player_strategy))

    def getGameMoves(self):
        return self.gamemoves

    # Returns a dictionary of vertices and their positions
    def getVerticesPosition(self):
        positions = {}
        for _, vertex, x, y, _ in self.gamemoves:
            positions[vertex] = [int(x), int(y)]
        return positions
    
    def getVerticesColor(self):
        colors = []
        for role, _, _, _, _ in self.gamemoves:
            color = getColorForRole(role)
            colors.append(color)
        return colors
    
    # Read the file and parse the content
    def readFile(self, filename):
        f = open(filename, "r")
        lines = f.readlines()
        self.parseLines(lines)
        #self.printData()
        f.close()

    # Print the data structure for debugging
    def printData(self):
        print("Player Name: " + self.playername)
        print("Player Role: " + self.playerrole)
        print("Vertices: ")
        print(self.vertices)
        print("Edges: ")
        print(self.edges)
        print("Game Moves: ")
        print(self.gamemoves)

def getColorForRole(role):
    return (1,0,0,0.1) if role == "MAX" else (0,0,1,0.1)

def render_gamemove_images(graphData: GraphData, filename_prefix):

    # Add nodes
    node_positions = graphData.getVerticesPosition()
    node_colors = []
    nodes_visible = []
    edges_visible = set()

    game_moves = graphData.getGameMoves()
    for game_move, (role, vertex, x, y, player_strategy) in enumerate(game_moves):
        # Create an empty graph
        G = nx.Graph()

        G.add_nodes_from(node_positions)

        # Add edges
        G.add_edges_from(graphData.edges)

        nodes_visible.append(vertex)
        node_colors.append(getColorForRole(role))

        for (v1, v2) in G.edges():
            if (vertex == v1 or vertex == v2) and v1 in nodes_visible and v2 in nodes_visible:
                edges_visible.add((v1, v2))

        plt.figure()

        plt.grid(True)

        # Draw the graph with assigned colors
        nx.draw_networkx(G, nodelist=nodes_visible, edgelist=edges_visible, pos=node_positions, with_labels=True, node_color=node_colors, node_size=200, font_weight='medium')

        filepath = filename_prefix + f"_{game_move:010d}.png"

        # Display the graph
        plt.savefig(filepath, format="PNG")

# Draw the graph
def show_graph_window(graphData: GraphData):
    # Create an empty graph
    G = nx.Graph()
    
    # Add nodes
    node_colors = graphData.getVerticesColor()
    node_positions = graphData.getVerticesPosition()
    G.add_nodes_from(node_positions)
    
    # Add edges
    G.add_edges_from(graphData.edges)
    
    # Assign colors to nodes based on even/odd
    #node_colors = ['blue' if node % 2 == 0 else 'green' for node in G.nodes()]
    #plt.figure("Game Graph", figsize=(1000, 1000), dpi=1)

    plt.grid(True)

    # Draw the graph with assigned colors
    nx.draw_networkx(G, pos=node_positions, with_labels=True, node_color=node_colors, node_size=200, font_weight='medium')

    # Display the graph
    plt.show()


# Main call
if len(sys.argv) > 1:
    # Access the command-line arguments using sys.argv
    filepath = sys.argv[1]
    gd = GraphData()
    gd.readFile(filepath)

    directory = os.path.dirname(filepath)
    basename = os.path.basename(filepath)
    filename, file_extension = os.path.splitext(basename)
    render_gamemove_images(gd, os.path.join(directory,filename))

    # show_graph_window(gd)
else:
    print("No file-path provided.")
