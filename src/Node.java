import java.util.LinkedList;

/**
 * @author Pavan Poudel
 * Date - 2018/08/07
 */
public class Node {
    private int node_id;
    private int value;
    private int x;
    private int y;
    private LinkedList<Node> neighbors;

    public Node() {
    }

    public Node(int node_id, int value, int x, int y, LinkedList<Node> neighbors) {
        this.node_id = node_id;
        this.value = value;
        this.x = x;
        this.y = y;
        this.neighbors = neighbors;
    }

    public int getNode_id() {
        return node_id;
    }

    public void setNode_id(int node_id) {
        this.node_id = node_id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public LinkedList<Node> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(LinkedList<Node> neighbors) {
        this.neighbors = neighbors;
    }
}
