import java.util.ArrayList;

/**
 * @author Pavan Poudel
 * Date - 2018/08/07
 */
public class Node {
    private int node_id;
    private int value;
    private int x;
    private int y=-1;
    public ArrayList<Integer> neighbors;
    public ArrayList<Objects> objects;

    public Node() {
    }

    public Node(int node_id, int value, int x, int y, ArrayList<Integer> neighbors, ArrayList<Objects> objects) {
        this.node_id = node_id;
        this.value = value;
        this.x = x;
        this.y = y;
        this.neighbors = neighbors;
        this.objects = objects;
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

    public ArrayList<Integer> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(ArrayList<Integer> neighbors) {
        this.neighbors = neighbors;
    }

    public ArrayList<Objects> getObjects() {
        return objects;
    }

    public void setObjects(ArrayList<Objects> objects) {
        this.objects = objects;
    }

    /*
     * Get neighbors of a node in grid graph.
     */
    public static ArrayList<Integer> getNeighborsGrid(int r, int c, int rows, int cols){
        ArrayList<Integer> neighbors = new ArrayList<Integer>();
        if(r>0){
            neighbors.add(((r-1)*cols)+c);
        }
        if(r<rows-1){
            neighbors.add(((r+1)*cols)+c);
        }
        if(c>0){
            neighbors.add((r*cols)+(c-1));
        }
        if(c<cols-1){
            neighbors.add((r*cols)+(c+1));
        }
        return neighbors;
    }

    /*
     * Get neighbors of a node in clique graph.
     */
    public static ArrayList<Integer> getNeighborsClique(int r, int n){
        ArrayList<Integer> neighbors = new ArrayList<Integer>();
        for(int i = 0;i < n; i++) {
            if(i!=r) {
                neighbors.add(i);
            }
        }
        return neighbors;
    }

    /*
     * Get neighbors of a node in line graph.
     */
    public static ArrayList<Integer> getNeighborsLine(int r, int n){
        ArrayList<Integer> neighbors = new ArrayList<Integer>();
        if(r == 0){
            neighbors.add(1);
        }
        else if(r == n-1){
            neighbors.add(n-2);
        }
        else{
            neighbors.add(r-1);
            neighbors.add(r+1);
        }
        return neighbors;
    }

    /*
     * Get neighbors of a node in star graph.
     */
    public static ArrayList<Integer> getNeighborsStar(int r, int rays, int rays_size){
        ArrayList<Integer> neighbors = new ArrayList<Integer>();

        if(r == 0){
            for(int i=0;i<rays;i++) {
                neighbors.add(i*rays_size+1);
            }
        }
        else if((r%rays_size) == 0){
            neighbors.add(r-1);
        }
        else if((r%rays_size) == 1){
            neighbors.add(0);
            neighbors.add(r+1);
        }
        else{
            neighbors.add(r-1);
            neighbors.add(r+1);
        }
        return neighbors;
    }

    /*
     * Get neighbors of a node in cluster graph.
     */
    public static ArrayList<Integer> getNeighborsCluster(int r, int clusters, int cluster_size){
        ArrayList<Integer> neighbors = new ArrayList<Integer>();
        int clust_id = r/cluster_size;
        for(int i=0;i<cluster_size;i++){
            if((clust_id * cluster_size +i) != r){
                neighbors.add(clust_id * cluster_size +i);
            }
        }
        if(r%cluster_size == 0){
            for(int i=0;i<clusters;i++){
                if(i*cluster_size != r){
                    neighbors.add(i*cluster_size);
                }
            }
        }
        return neighbors;
    }
    /*
     * Generate nodes for grid graph.
     */
    public static ArrayList<Node> generateNodesGrid(int gridsize){
        ArrayList<Node> nodes = new ArrayList<Node>();
        for(int r=0;r<gridsize;r++){
            for(int c=0;c<gridsize;c++){
                Node nd = new Node();
                nd.setNode_id(r*gridsize + c);
                nd.setValue(r*gridsize + c);
                nd.setX(r);
                nd.setY(c);
                ArrayList<Integer> neighbors = getNeighborsGrid(r, c, gridsize, gridsize);
                nd.setNeighbors(neighbors);
                nodes.add(nd);
            }
        }
        return nodes;
    }

    /*
     * Generate nodes for clique graph.
     */
    public static ArrayList<Node> generateNodesClique(int cliquesize){
        ArrayList<Node> nodes = new ArrayList<Node>();
        for(int r=0;r<cliquesize;r++) {
            Node nd = new Node();
            nd.setNode_id(r);
            nd.setValue(r);
            nd.setX(r);
            nd.setY(0);
            ArrayList<Integer> neighbors = getNeighborsClique(r, cliquesize);
            nd.setNeighbors(neighbors);
            nodes.add(nd);
        }
        return nodes;
    }

    /*
     * Generate nodes for line graph.
     */
    public static ArrayList<Node> generateNodesLine(int linesize){
        ArrayList<Node> nodes = new ArrayList<Node>();
        for(int r=0;r<linesize;r++) {
            Node nd = new Node();
            nd.setNode_id(r);
            nd.setValue(r);
            nd.setX(r);
            nd.setY(0);
            ArrayList<Integer> neighbors = getNeighborsLine(r, linesize);
            nd.setNeighbors(neighbors);
            nodes.add(nd);
        }
        return nodes;
    }

    /*
     * Generate nodes for line graph.
     */
    public static ArrayList<Node> generateNodesStar(int starsize,int rays, int rays_size){
        ArrayList<Node> nodes = new ArrayList<Node>();
        for(int r=0;r<starsize;r++) {
            Node nd = new Node();
            nd.setNode_id(r);
            nd.setValue(r);
            nd.setX(r);
            nd.setY(0);
            ArrayList<Integer> neighbors = getNeighborsStar(r,rays,rays_size);
            nd.setNeighbors(neighbors);
            nodes.add(nd);
        }
        return nodes;
    }

    /*
     * Generate nodes for cluster graph.
     */
    public static ArrayList<Node> generateNodesCluster(int totalnodes, int clusters, int cluster_size){
        ArrayList<Node> nodes = new ArrayList<Node>();
        for(int r=0;r<totalnodes;r++) {
            Node nd = new Node();
            nd.setNode_id(r);
            nd.setValue(r);
            nd.setX(r);
            nd.setY(0);
            ArrayList<Integer> neighbors = getNeighborsCluster(r,clusters,cluster_size);
            nd.setNeighbors(neighbors);
            nodes.add(nd);
        }
        return nodes;
    }
}
