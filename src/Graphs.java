import java.util.LinkedList;

/**
 * @author Pavan Poudel
 * Date - 2018/08/03
 */
public class Graphs {
    private int node_id;
    private int value;
    LinkedList<Node> neighbors;

    public Graphs(int node_id) {
        this.node_id = node_id;
    }

    public int getNode_id() {
        return node_id;
    }

    public void setNode_id(int node_id) {
        this.node_id = node_id;
    }
}