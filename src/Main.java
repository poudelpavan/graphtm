import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import static java.lang.Math.abs;

/**
 * @author Pavan Poudel
 * Date - 2018/08/03
 */
public class Main {
    private static int total_nodes = 25;
    private static int grid_size = 5;

    private static int total_objs = 16;
    private static int total_txs = 10;
    private static int update_rate = 20;
    private static int rwset_size = 10;

    private static ArrayList<Objects> objs = new ArrayList<Objects>(total_objs);
    private static ArrayList<Transaction> txs = new ArrayList<Transaction>(total_txs);
    private static ArrayList<ArrayList<Transaction>> nodal_txs = new ArrayList<ArrayList<Transaction>>(total_nodes);

    /*
     * Generate a grid graph.
     */
    public static Graphs generateGridGraph(int gridsize){
        Graphs grid = new Graphs();
        grid.setGraph_id("grid"+gridsize);
        grid.setNumNodes(gridsize*gridsize);
        grid.setNumEdges((gridsize*gridsize) - gridsize);
        ArrayList<Node> nodes = generateNodes(gridsize);
        grid.setNodes(nodes);
        return grid;
    }

    /*
     * Get neighbors of a node in grid graph.
     */
    public static ArrayList<Integer> getNeighbors(int r, int c, int rows, int cols){
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
     * Generate nodes for grid graph.
     */
    public static ArrayList<Node> generateNodes(int gridsize){
        ArrayList<Node> nodes = new ArrayList<Node>();
        for(int r=0;r<gridsize;r++){
            for(int c=0;c<gridsize;c++){
                Node nd = new Node();
                nd.setNode_id(r*gridsize + c);
                nd.setValue(r*gridsize + c+1);
                nd.setX(r);
                nd.setY(c);
                ArrayList<Integer> neighbors = getNeighbors(r,c,gridsize,gridsize);
                nd.setNeighbors(neighbors);
                nodes.add(nd);
            }
        }
        return nodes;
    }

    /*
     * Calculate distance (communication cost) between two nodes in grid graph.
     */
    public static int getCommCost(Node a, Node b){
        return (abs(a.getX() - b.getX()) + abs(a.getY() - b.getY()));
    }

    /*
     * Generate Read-Write set for a transaction with random size.
     */
    public static ArrayList<Transaction> generateTransactions(int tot_obj, int tot_tx, int updt_rate){
        ArrayList<Transaction> txs = new ArrayList<Transaction>(tot_tx);

        for(int i=0;i<tot_tx;i++) {
            List<Objects> ws = new ArrayList<Objects>();
            List<Objects> rs = new ArrayList<Objects>();

            Random rand = new Random();
            int rws_size = rand.nextInt(tot_obj);
            int ws_size = rws_size * updt_rate / 100;
            int rs_size = rws_size - ws_size;
            int n = 0, sum = 0;

            List<Objects> rwset = setRWSet(rws_size, tot_obj);
            ArrayList<Integer> randList = getRandList(rws_size, 0, rwset.size()-1);

            while (n < randList.size()) {
                if (sum < ws_size) {
                    ws.add(rwset.get(randList.get(n)));
                    sum = sum + rwset.get(randList.get(n)).getObj_size();
                } else {
                    rs.add(rwset.get(randList.get(n)));
                }
                n++;
            }

            List<Objects> rset = rs;
            List<Objects> wset = ws;
            Transaction tx = new Transaction(i + 1, rws_size, updt_rate, rset, wset,"IDLE",0);

            txs.add(tx);
        }
        return txs;
    }

    /*
     * Generate Read-Write set for a transaction with fixed size.
     */
    public static ArrayList<Transaction> generateTransactions(int tot_obj, int tot_tx, int updt_rate, int rws_size){
        ArrayList<Transaction> txs = new ArrayList<Transaction>(tot_tx);

        for(int i=0;i<tot_tx;i++) {
            List<Objects> ws = new ArrayList<Objects>();
            List<Objects> rs = new ArrayList<Objects>();

            int ws_size = rws_size * updt_rate / 100;
            int rs_size = rws_size - ws_size;
            int n = 0, sum = 0;

            List<Objects> rwset = setRWSet(rws_size, tot_obj);
            ArrayList<Integer> randList = getRandList(rws_size, 0, rwset.size()-1);

            while (n < randList.size()) {
                if (sum < ws_size) {
                    ws.add(rwset.get(randList.get(n)));
                    sum = sum + rwset.get(randList.get(n)).getObj_size();
                } else {
                    rs.add(rwset.get(randList.get(n)));
                }
                n++;
            }

            List<Objects> rset = rs;
            List<Objects> wset = ws;
            Transaction tx = new Transaction(i + 1, rws_size, updt_rate, rset, wset,"IDLE",0);

            txs.add(tx);
        }
        return txs;
    }

    /*
     * Generate a list of unique random numbers.
     */
    public static ArrayList<Integer> getRandList(int size, int min, int max){
        Random rand = new Random();
        ArrayList<Integer> randList = new ArrayList<Integer>();
        int range = max - min + 1;
        while (randList.size() < size) {
            int a = rand.nextInt(range);
            if (!randList.contains(a)) {
                randList.add(a);
            }
        }
        return randList;
    }

    /*
     * Generate a random Read-Write set.
     */
    private static List<Objects> setRWSet(int rw_size, int total_objs){
        List<Objects> rw = new ArrayList<>();
        int sum = 0;
        Random rand = new Random();
        ArrayList<Integer> randList = new ArrayList<>();

        while (sum<rw_size) {
            int x = rand.nextInt(total_objs);
            if (!randList.contains(x)) {
                randList.add(x);
                randList.add(x);
                rw.add(objs.get(x));
                sum = sum + objs.get(x).getObj_size();
            }
        }
        return rw;
    }

    /*
     * Generate a transaction dependency graph based on objects positioned on the node.
     */
    private static ArrayList<ArrayList<Integer>> generateDependencyGraph(ArrayList<ArrayList<Transaction>> txs, int total_nodes, int tx_num){
        ArrayList<ArrayList<Integer>> adjMatrix = new ArrayList<>();

        for(int i = 0;i<total_nodes;i++){
            List<Objects> rs = txs.get(i).get(tx_num).getRset();
            List<Objects> ws = txs.get(i).get(tx_num).getWset();

            ArrayList<Integer> dependent = new ArrayList<>();
            for(int j=0;j<total_nodes;j++){
                dependent.add(0);
            }

            if(ws.size() > 0){
                for(int j=0;j<ws.size();j++){
                    Objects obj = ws.get(j);
                    int homenode = obj.getNode();

                    if(homenode !=i){
                        dependent.set(homenode,1);
                    }
                }
            }
            adjMatrix.add(dependent);
        }
        return adjMatrix;
    }

    /*
     * Generate a transaction conflict graph based on read set and write sets of transactions with priority.
     * Transaction at upper node has higher priority than the transaction at lower node.
     */
    private static ArrayList<ArrayList<Integer>> generateConflictGraph(ArrayList<ArrayList<Transaction>> txs, int total_nodes, int tx_num){
        ArrayList<ArrayList<Integer>> adjMatrix = new ArrayList<>();

        for(int i = 0;i<total_nodes;i++){
            List<Objects> rs = txs.get(i).get(tx_num).getRset();
            List<Objects> ws = txs.get(i).get(tx_num).getWset();

            ArrayList<Integer> dependent = new ArrayList<>(total_nodes);
            for(int j=0;j<total_nodes;j++){
                dependent.add(0);
            }
            for(int j=0;j<i;j++){
                boolean depends = false;
                List<Objects> rs1 = txs.get(j).get(tx_num).getRset();
                List<Objects> ws1 = txs.get(j).get(tx_num).getWset();
                for(int k=0;k<ws.size();k++) {
                    Objects obj = ws.get(k);
                    int obj_id = obj.getObj_id();
                    for (int l = 0; l < ws1.size(); l++) {
                        Objects obj1 = ws1.get(l);
                        int obj_id1 = obj1.getObj_id();
                        if (obj_id == obj_id1) {
                            depends = true;
                        }
                    }
                    for (int l = 0; l < rs1.size(); l++) {
                        Objects obj1 = rs1.get(l);
                        int obj_id1 = obj1.getObj_id();
                        if (obj_id == obj_id1) {
                            depends = true;
                        }
                    }
                }
                for(int k=0;k<rs.size();k++) {
                    Objects obj = rs.get(k);
                    int obj_id = obj.getObj_id();
                    for (int l = 0; l < ws1.size(); l++) {
                        Objects obj1 = ws1.get(l);
                        int obj_id1 = obj1.getObj_id();
                        if (obj_id == obj_id1) {
                            depends = true;
                        }
                    }
                }
                if(depends == true) {
                    dependent.set(j,1);
                }
            }
            adjMatrix.add(dependent);
        }
        return adjMatrix;
    }

    /*
     * Retrieve node of a grid with node_id.
     */
    private static Node getNode(int n_id, Graphs g){
        int j=0;
        Node nd = g.getNodes().get(j);
        while(nd.getNode_id() != n_id)
        {
            j++;
            nd = g.getNodes().get(j);
        }
        return nd;
    }

    /*
     * Execute transaction and return execution time based on the objects in read set and write set and its position on grid.
     */
    private static int executeTx(Transaction t, Node n, Graphs g){
        int total_time = 0;
        List<Objects> rs = t.getRset();
        List<Objects> ws = t.getWset();
        for(int i = 0;i<rs.size();i++){
            int node_id = rs.get(i).getNode();
            Node nd = getNode(node_id,g);
            int access_cost = getCommCost(n, nd);
            if(total_time < access_cost){
                total_time = access_cost;
            }
        }
        for(int i = 0;i<ws.size();i++){
            int node_id = ws.get(i).getNode();
            Node nd =getNode(node_id,g);
            int access_cost = getCommCost(n, nd);
            if(total_time < access_cost){
                total_time = access_cost;
            }
            Objects obj = ws.get(i);
            obj.setNode(n.getNode_id());
            objs.set(obj.getObj_id()-1,obj);
        }
        return total_time;
    }

    public static void main(String[] args) {
//        System.out.println("Hello World!");
        Scanner reader = new Scanner(System.in);

        ArrayList<Integer> obj_home = getRandList(total_objs,1,total_nodes);
        for(int i=0;i<total_objs;i++){
            Objects obj = new Objects(i+1, 1, obj_home.get(i));
            objs.add(obj);
        }

        System.out.println("\n*** ----------------------------- ***\n");
        System.out.println("Case 1: Read-Write set size for a tx is fixed.");
        System.out.println("Case 2: Read-Write set size for a tx is random.");
        System.out.print("Choose your option (1/2): ");
        int option = reader.nextInt();

        if(option == 1){
            for(int x=0;x<total_nodes;x++) {
                txs = generateTransactions(total_objs, total_txs, update_rate, rwset_size);
                nodal_txs.add(txs);
            }
        }
        else if(option == 2){
            for(int x=0;x<total_nodes;x++) {
                txs = generateTransactions(total_objs,total_txs,update_rate);
                nodal_txs.add(txs);
            }
        }
        else{
            System.out.println("Invalid choice!");
            System.exit(0);
        }

        for(int i=0;i<total_txs;i++){
            List<Objects> ws = new ArrayList<Objects>();
            List<Objects> rs = new ArrayList<Objects>();
            int ws_size = rwset_size * update_rate/100;
            int rs_size = (rwset_size) - ws_size;
            List<Objects> rwset = setRWSet(rwset_size,total_objs);
            int n = 0, sum = 0;
            Random rand = new Random();
            ArrayList<Integer> randList = new ArrayList<Integer>();
            while (randList.size() < rwset.size()) {
                int a = rand.nextInt(rwset.size());
                if (!randList.contains(a)) {
                    randList.add(a);
                }
            }

            while (n < randList.size()) {
                if(sum < ws_size){
                    ws.add(rwset.get(randList.get(n)));
                    sum = sum + rwset.get(randList.get(n)).getObj_size();
                }
                else{
                    rs.add(rwset.get(randList.get(n)));
                }
                n++;
            }

            List<Objects> rset = rs;
            List<Objects> wset = ws;
            Transaction tx = new Transaction(i+1, rwset_size, update_rate,rset,wset,"IDLE",0);

            txs.add(tx);
        }

        System.out.println("---------------------------------");
        System.out.println("Tx\trw-set-size\tupdate-rate");
        System.out.println("---------------------------------");
        for(int i=0;i<total_txs;i++){
            System.out.print("T"+txs.get(i).getTx_id()+"   \t"+txs.get(i).getRw_set_size()+"\t\t"+txs.get(i).getUpdate_rate()+"\t\tRead Set(Objects) ==> (");
            for(int j=0;j<txs.get(i).getRset().size();j++){
//                System.out.print("o"+txs.get(i).getRset().get(j).getObj_id()+":"+txs.get(i).getRset().get(j).getObj_size()+" ");
                System.out.print(txs.get(i).getRset().get(j).getObj_id());
                if(j<txs.get(i).getRset().size()-1){
                    System.out.print(", ");
                }
            }
            System.out.print(")\n\t\t\t\t\t\tWrite Set(Objects) ==> (");
            for(int j=0;j<txs.get(i).getWset().size();j++){
                System.out.print(txs.get(i).getWset().get(j).getObj_id());
                if(j<txs.get(i).getWset().size()-1){
                    System.out.print(", ");
                }
            }
            System.out.print(")\n");
        }

        System.out.println("\n---------------------------------\nNodes vs. Transactions\n---------------------------------");
        for(int i=0;i<total_nodes;i++){
            System.out.print("N"+(i+1)+"  \tT"+nodal_txs.get(i).get(0).getTx_id()+"   \t"+nodal_txs.get(i).get(0).getRw_set_size()+"\t\t"+nodal_txs.get(i).get(0).getUpdate_rate()+"\t\tRead Set(Objects) ==> (");
            for(int j=0;j<nodal_txs.get(i).get(0).getRset().size();j++){
//                System.out.print("o"+txs.get(i).getRset().get(j).getObj_id()+":"+txs.get(i).getRset().get(j).getObj_size()+" ");
                System.out.print(nodal_txs.get(i).get(0).getRset().get(j).getObj_id());
                if(j<nodal_txs.get(i).get(0).getRset().size()-1){
                    System.out.print(", ");
                }
            }
            System.out.print(")\n\t\t\t\t\t\t\t\tWrite Set(Objects) ==> (");
            for(int j=0;j<nodal_txs.get(i).get(0).getWset().size();j++){
                System.out.print(nodal_txs.get(i).get(0).getWset().get(j).getObj_id());
                if(j<nodal_txs.get(i).get(0).getWset().size()-1){
                    System.out.print(", ");
                }
            }
            System.out.print(")\n");
        }
        Graphs grid = generateGridGraph(grid_size);
        System.out.println("\n-----------------------------------------------\n\t  Grid graph of grid size ("+grid_size+" x "+grid_size+")\n-----------------------------------------------");
        for(int i=0;i<grid_size;i++){
            for(int j=0;j<grid_size;j++){
                System.out.print(grid.getNodes().get(i*grid_size +j).getValue()+"\t");
            }
            System.out.println("\n");
        }

        System.out.println("\n-----------------------------------------------\n\t  Initial distribution of objects in grid\n-----------------------------------------------");
        for(int i=0;i<grid_size;i++){
            for(int j=0;j<grid_size;j++){
                int home = -1;
                for(int k=0;k<total_objs;k++){
                    int nd = objs.get(k).getNode();
                    if(nd == i*grid_size+j){
                        home = objs.get(k).getObj_id();
                    }
                }
                System.out.print(home+"\t");
            }
            System.out.println("\n");
        }

        System.out.println("\n-----------------------------------------------\n\t  Dependency Graph (Adjancency matrix)\n-----------------------------------------------");
        ArrayList<ArrayList<Integer>> depend = new ArrayList<>();
        for(int i=0;i<total_nodes;i++) {
            depend = generateDependencyGraph(nodal_txs,total_nodes,0);
        }
        for(int i=0;i<total_nodes;i++){
            for(int j=0;j<total_nodes;j++){
                System.out.print(depend.get(i).get(j) + " ");
            }
            System.out.println("\n");
        }

        System.out.println("\n-----------------------------------------------\nTransaction Dependency Graph (conflits)\n-----------------------------------------------");
        ArrayList<ArrayList<Integer>> dependtx = new ArrayList<>();
        for(int i=0;i<total_nodes;i++) {
            dependtx = generateConflictGraph(nodal_txs,total_nodes,0);
        }
        for(int i=0;i<total_nodes;i++){
            for(int j=0;j<total_nodes;j++){
                System.out.print(dependtx.get(i).get(j) + " ");
            }
            System.out.println("\n");
        }

        System.out.println("\n-----------------------------------------------\nTransaction execution\n-----------------------------------------------");
        boolean noconflict = false;

        int round=0, cumulative_rt=0;
        while(round < total_txs) {
            System.out.println("Round "+round);
            boolean conflictstatus = false;
            int j=0;
            ArrayList<ArrayList<Transaction>> all_txs = new ArrayList<>();
            all_txs = nodal_txs;

            for(int i=0;i<total_nodes;i++){
                int count = 0;
                dependtx = generateConflictGraph(all_txs,total_nodes,round);
                Transaction t = all_txs.get(i).get(round);
                ArrayList<Integer> conflictlist = dependtx.get(i);
                for(int k=0;k<conflictlist.size();k++){
                    int conflict = conflictlist.get(k);
                    if(conflict == 1){
                        count++;
                        //System.out.println("Conflict, status = "+all_txs.get(k).get(j).getStatus());
                        if(all_txs.get(k).get(round).getStatus()=="COMMITTED"){
                            int movecost = getCommCost(getNode(i,grid), getNode(k, grid));
                            if(all_txs.get(k).get(round).getExecution_time() + movecost > all_txs.get(i).get(round).getExecution_time()){
                                ArrayList<Transaction> arr = all_txs.get(i);
                                Transaction t1 = all_txs.get(i).get(round);
                                t1.setExecution_time(all_txs.get(k).get(round).getExecution_time() + movecost);
                                arr.set(round,t1);
                                nodal_txs.set(i,arr);
                            }
                        }
                        else{
                            conflictstatus = true;

                        }
                    }
                }
                if(conflictstatus == false){
                    Transaction t1 = nodal_txs.get(i).get(round);
                    int exec_time = executeTx(t1,getNode(i,grid),grid);
                    ArrayList<Transaction> arr = nodal_txs.get(i);
                    if(exec_time > t1.getExecution_time()) {
                        t1.setExecution_time(exec_time);
                    }
                    else{
                        exec_time = t1.getExecution_time();
                    }
                    t1.setStatus("COMMITTED");
                    arr.set(round,t1);
                    nodal_txs.set(i,arr);
                    System.out.print("T("+i+","+round+")\t=> ");
                    for(int x=0;x<count;x++) {
                        if(x==0) {
                            System.out.print("|----|");
                        }
                        else{
                            System.out.print("----|");
                        }
                    }
                    if(exec_time < 10) {
                        System.out.print("  " +(cumulative_rt + exec_time) + "\n");
                    }
                    else{
                        System.out.print(" " + (cumulative_rt + exec_time) + "\n");
                    }
                }
            }
            cumulative_rt += nodal_txs.get(total_nodes-1).get(round).getExecution_time();
            round++;
        }
    }
}