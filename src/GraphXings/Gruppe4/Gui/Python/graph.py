import networkx as nx
import matplotlib.pyplot as plt

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
                if cleanline.startswith("M"):
                    self.playerrole = cleanline
                else:
                    self.playername = cleanline
            elif parsemode == 1:
                self.vertices.append(cleanline)
            elif parsemode == 2:
                sp = cleanline.split(",")
                self.edges.append((sp[0], sp[1]))
            elif parsemode == 3:
                # We have the following format for game moves:
                # role, vertex, x, y, player strategy
                sp = cleanline.split(",")
                self.gamemoves.append((sp[0], sp[1], sp[2], sp[3], sp[4]))

    # Returns a dictionary of vertices and their positions
    def getVerticesPosition(self):
        pos = {}
        for g in self.gamemoves:
            pos[g[1]] = (int(g[2]), int(g[3]))
        return pos
    
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


# Draw the graph
def draw_graph(graphData: GraphData):
    # Create an empty graph
    G = nx.Graph()
    
    # Add nodes
    G.add_nodes_from(graphData.getVerticesPosition())
    
    # Add edges
    G.add_edges_from(graphData.edges)
    
    # Assign colors to nodes based on even/odd
    #node_colors = ['blue' if node % 2 == 0 else 'green' for node in G.nodes()]
    #plt.figure("Game Graph", figsize=(1000, 1000), dpi=1)

    plt.grid(True)

    # Draw the graph with assigned colors
    nx.draw_networkx(G, with_labels=False, node_color="blue", node_size=200, font_weight='medium')
    #nx.draw(G, with_labels=False, node_color="blue", node_size=200, font_weight='medium')
    
    # Display the graph
    plt.show()


# Main call
gd = GraphData()
gd.readFile("test.txt")
draw_graph(gd)