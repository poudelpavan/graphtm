import java.util.LinkedList;

/**
 * @author Pavan Poudel
 * Date - 2018/08/03
 */
public class Graphs {
    private int graph_id;
    private int numNodes;
    private int numEdges;

    public Graphs() {
    }

    public Graphs(int graph_id, int numNodes, int numEdges) {
        this.graph_id = graph_id;
        this.numNodes = numNodes;
        this.numEdges = numEdges;
    }

    public int getGraph_id() {
        return graph_id;
    }

    public void setGraph_id(int graph_id) {
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
}