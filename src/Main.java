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
    private static int total_nodes = 100;
    private static int grid_size = 10;

    private static int total_objs = 128;
    private static int total_txs = 100;
    private static int update_rate = 20;
    private static int rwset_size = 16;

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
                nd.setValue(r*gridsize + c);
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
            Transaction tx = new Transaction(i + 1, rws_size, updt_rate, rset, wset);

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
            Transaction tx = new Transaction(i + 1, rws_size, updt_rate, rset, wset);

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
        List<Objects> rw = new ArrayList<Objects>();
        int sum = 0;
        Random rand = new Random();
        ArrayList<Integer> randList = new ArrayList<Integer>();

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

    public static void main(String[] args) {
//        System.out.println("Hello World!");
        Scanner reader = new Scanner(System.in);

        for(int i=0;i<total_objs;i++){
            Objects obj = new Objects(i+1, 1);
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
            int ws_size = rwset_size * update_rate/800;
            int rs_size = (rwset_size/8) - ws_size;
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
            Transaction tx = new Transaction(i+1, rwset_size, update_rate,rset,wset);

            txs.add(tx);
        }

        System.out.println("\n*** ----------------------------- ***\n");
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

        System.out.println("---------------------------------");
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
    }
}