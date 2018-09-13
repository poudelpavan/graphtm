import java.util.ArrayList;

/**
 * @author Pavan Poudel
 * Date - 2018/08/03
 */
public class Graphs {
    private String graph_id;
    private int numNodes;
    private int numEdges;
    private ArrayList<Node> nodes;

    public Graphs() {
    }

    public Graphs(String graph_id, int numNodes, int numEdges, ArrayList<Node> nodes) {
        this.graph_id = graph_id;
        this.numNodes = numNodes;
        this.numEdges = numEdges;
        this.nodes = nodes;
    }

    public String getGraph_id() {
        return graph_id;
    }

    public void setGraph_id(String graph_id) {
        this.graph_id = graph_id;
    }

    public int getNumNodes() {
        return numNodes;
    }

    public void setNumNodes(int numNodes) {
        this.numNodes = numNodes;
    }

    public int getNumEdges() {
        return numEdges;
    }

    public void setNumEdges(int numEdges) {
        this.numEdges = numEdges;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }


    /*
     * Generate a grid graph.
     */
    public static Graphs generateGridGraph(int gridsize){
        Graphs grid = new Graphs();
        grid.setGraph_id("grid"+gridsize);
        grid.setNumNodes(gridsize*gridsize);
        grid.setNumEdges((gridsize*gridsize) - gridsize);
        ArrayList<Node> nodes = Node.generateNodesGrid(gridsize);
        grid.setNodes(nodes);
        return grid;
    }

    /*
     * Generate a clique graph.
     */
    public static Graphs generateCliqueGraph(int cliquesize){
        Graphs clique = new Graphs();
        clique.setGraph_id("clique"+cliquesize);
        clique.setNumNodes(cliquesize);
        clique.setNumEdges((cliquesize*(cliquesize-1))/2);
        ArrayList<Node> nodes = Node.generateNodesClique(cliquesize);
        clique.setNodes(nodes);
        return clique;
    }

    /*
     * Generate a line graph.
     */
    public static Graphs generateLineGraph(int linesize){
        Graphs line = new Graphs();
        line.setGraph_id("line"+linesize);
        line.setNumNodes(linesize);
        line.setNumEdges(linesize-1);
        ArrayList<Node> nodes = Node.generateNodesLine(linesize);
        line.setNodes(nodes);
        return line;
    }

    /*
     * Generate a Star graph.
     */
    public static Graphs generateStarGraph(int starsize){
        Graphs star = new Graphs();
        star.setGraph_id("star"+starsize);
        star.setNumNodes(starsize);
        star.setNumEdges(starsize-1);
        ArrayList<Node> nodes = Node.generateNodesLine(starsize);
        star.setNodes(nodes);
        return star;
    }
}