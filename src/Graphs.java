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
}