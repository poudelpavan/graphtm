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
    private static int total_nodes = 36;
    private static int grid_size = 6;
    private static int sub_grid = 2;

    private static int total_objs = 16;
    private static int total_txs = 10;
    private static int update_rate = 20;
    private static int rwset_size = 10;

    private static ArrayList<Objects> objs = new ArrayList<Objects>(total_objs);
    private static ArrayList<Transaction> txs = new ArrayList<Transaction>(total_txs);
    private static ArrayList<ArrayList<Transaction>> nodal_txs = new ArrayList<ArrayList<Transaction>>(total_nodes);
    private static int[] priority_queue = new int[total_nodes];

    /*
     * Calculate distance (communication cost) between two nodes in grid graph.
     */
    public static int getCommCostGrid(Node a, Node b){
        return (abs(a.getX() - b.getX()) + abs(a.getY() - b.getY()));
    }

    /*
     * Calculate distance (communication cost) between two nodes in clique.
     */
    public static int getCommCostClique(Node a, Node b){
        return 1;
    }

    /*
     * Calculate distance (communication cost) between two nodes in line graph.
     */
    public static int getCommCostLine(Node a, Node b){
        return (abs(a.getX() - b.getX()));
    }

    /*
     * Calculate distance (communication cost) between two nodes in star graph.
     */
    public static int getCommCostStar(Node a, Node b, int ray_size){
        int cost_a = 0, cost_b = 0;
        if(a.getX() == 0){
            cost_a = 0;
        }
        else if(a.getX() % ray_size == 0){
            cost_a = ray_size;
        }
        else{
            cost_a = a.getX() % ray_size;
        }
        if(b.getX() == 0){
            cost_b = 0;
        }
        else if(b.getX() % ray_size == 0){
            cost_b = ray_size;
        }
        else{
            cost_b = b.getX() % ray_size;
        }
        int ray_a = a.getX()/ray_size;
        int ray_b = b.getX()/ray_size;
        if(ray_a == ray_b){
            return (abs(cost_a - cost_b));
        }
        else {
            return (cost_a + cost_b);
        }
    }

    /*
     * Calculate distance (communication cost) between two nodes in cluster graph.
     */
    public static int getCommCostCluster(Node a, Node b, int cluster_size){
        int cost_a = 0, cost_b = 0,cost=0;
        int clust_a = a.getX()/cluster_size;
        int clust_b = b.getX()/cluster_size;
        if(clust_a == clust_b){
            cost = 1;
        }
        else{
            if((a.getX() % cluster_size) > 0){
                cost_a = 1;
            }
            if((b.getX() % cluster_size) > 0){
                cost_b = 1;
            }
            cost = cluster_size + cost_a + cost_b;
        }

        return cost;
    }

    /*
     * Generate a priority queue for transaction execution in Grid graph.
     */
    public static void generatePriorityQueueGrid(int gridsize, int subgrid){
        priority_queue = new int[total_nodes];
        int subgridsize = gridsize/subgrid;
        int count=0,i=0,j=0,k=0,l=0,m=0,n=0;
        int hor=0, ver=0,totalgrid=0;
        while(count<total_nodes) {
            totalgrid +=subgridsize;
            if(totalgrid > gridsize){
                subgridsize = subgridsize - (totalgrid - gridsize);
            }
            if(ver == 0) {
                for (i = 0; i < gridsize; i++) {
                    if (hor == 0) {
                        for (j = 0; j < subgridsize; j++) {
                            priority_queue[k] = i * gridsize + j + l;
                            k++;
                            count++;
                        }
                        hor = 1;
                    } else if (hor == 1) {
                        for (j = subgridsize - 1; j >= 0; j--) {
                            priority_queue[k] = i * gridsize + j + l;
                            k++;
                            count++;
                        }
                        hor = 0;
                    }
                }
                n++;
                m = n*subgridsize;
                hor = 0;
                ver = 1;
            }
            else{
                for(i = gridsize-1; i >= 0; i--) {
                    if (hor == 0) {
                        for (j = 0; j < subgridsize; j++) {
                            priority_queue[k] = i * gridsize + j + m;
                            k++;
                            count++;
                        }
                        hor = 1;
                    } else if (hor == 1) {
                        for (j = subgridsize - 1; j >= 0; j--) {
                            priority_queue[k] = i * gridsize + j + m;
                            k++;
                            count++;
                        }
                        hor = 0;
                    }
                }
                n++;
                l=n*subgridsize;
                ver = 0;
                hor = 0;
            }
        }

    }

    /*
     * Generate a priority queue for transaction execution in Grid graph.
     */
    public static void generatePriorityQueueClique(int cliquesize){
        priority_queue = new int[total_nodes];
        ArrayList<Integer> lst = getRandList(cliquesize,0,total_nodes-1);
        for(int i=0;i<total_nodes;i++){
            priority_queue[i] = lst.get(i);
        }
    }

    /*
     * Generate a priority queue for transaction execution in Line graph.
     */
    public static void generatePriorityQueueLine(int linesize, int sublinesize){
        priority_queue = new int[total_nodes];
//        ArrayList<Integer> lst = getRandList(cliquesize,0,total_nodes-1);
        for(int i=0;i<total_nodes;i++){
            priority_queue[i] = i;
        }
    }

    /*
     * Generate a priority queue for transaction execution in Star graph.
     */
    public static void generatePriorityQueueStar(int totalrays, int raysize){
        priority_queue = new int[total_nodes];
        priority_queue[0]=0;
        int count = 0,roundsize = 0,round_total=0,k=0;
        while(count < total_nodes-1) {
            roundsize = (int)Math.pow(2,k);
            if(round_total + roundsize > raysize){
                roundsize = raysize - round_total;
            }
            for(int i = 0; i < totalrays; i++) {
                for(int j = 1; j <= roundsize;j++){
//                    System.out.println((i*raysize + j + round_total)+",");
                    priority_queue[count+1] = i*raysize + j + round_total;
                    count++;
                }
//                System.out.print("\n");

            }
            k++;
            round_total += roundsize;
        }
    }

    /*
     * Generate a priority queue for transaction execution in Cluster graph.
     */
    public static void generatePriorityQueueCluster(int totalclusters, int clustersize){
        priority_queue = new int[total_nodes];
        ArrayList<Integer> lst = getRandList(totalclusters,0,totalclusters-1);
        for(int i=0;i<totalclusters;i++){
            System.out.print(lst.get(i)+",");
        }
        System.out.println("");
        int k=0;
        for(int i=0;i<totalclusters;i++){
            ArrayList<Integer> rnd = getRandList(clustersize-1,1,clustersize-1);
            for(int j=0;j<clustersize-1;j++){
                System.out.print(rnd.get(j)+1+",");
            }
            System.out.println("");
            priority_queue[k] = lst.get(i)*clustersize;
            k++;
            for(int j=0;j<clustersize-1;j++) {
                priority_queue[k] = lst.get(i)*clustersize + rnd.get(j) + 1;
                k++;
            }
        }
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
            Transaction tx = new Transaction(i + 1, rws_size, updt_rate, rset, wset,"IDLE",0,0,0,0);

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
            Transaction tx = new Transaction(i + 1, rws_size, updt_rate, rset, wset,"IDLE",0,0,0,0);

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

        for(int i = 0;i<total_nodes;i++) {
            ArrayList<Integer> dependent = new ArrayList<>(total_nodes);
            for (int j = 0; j < total_nodes; j++) {
                dependent.add(0);
            }
            adjMatrix.add(dependent);
        }

        for(int i = 0;i<total_nodes;i++){
            List<Objects> rs = txs.get(priority_queue[i]).get(tx_num).getRset();
            List<Objects> ws = txs.get(priority_queue[i]).get(tx_num).getWset();

            ArrayList<Integer> dependent = new ArrayList<>(total_nodes);
            for(int j=0;j<total_nodes;j++){
                dependent.add(0);
            }
            for(int j=0;j<i;j++){
                boolean depends = false;
                List<Objects> rs1 = txs.get(priority_queue[j]).get(tx_num).getRset();
                List<Objects> ws1 = txs.get(priority_queue[j]).get(tx_num).getWset();
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
                    dependent.set(priority_queue[j],1);
                }
            }
            adjMatrix.set(priority_queue[i],dependent);
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
    private static int[] executeTxGrid(Transaction t, Node n, Graphs g){
        int total_time = 0,commcost =0;
        List<Objects> rs = t.getRset();
        List<Objects> ws = t.getWset();
        for(int i = 0;i<rs.size();i++){
            int node_id = rs.get(i).getNode();
            Node nd = getNode(node_id,g);
            int access_cost = getCommCostGrid(n, nd);
            if(total_time < access_cost){
                total_time = access_cost;
            }
            commcost += access_cost;
        }
        for(int i = 0;i<ws.size();i++){
            int node_id = ws.get(i).getNode();
            Node nd =getNode(node_id,g);
            int access_cost = getCommCostGrid(n, nd);
            if(total_time < access_cost){
                total_time = access_cost;
            }
            commcost += access_cost;
            Objects obj = ws.get(i);
            obj.setNode(n.getNode_id());
            objs.set(obj.getObj_id()-1,obj);
        }
        int[] exec = {total_time,commcost};
        return exec;
    }


    /*
     * Execute transaction and return execution time based on the objects in read set and write set and its position on clique.
     */
    private static int[] executeTxClique(Transaction t, Node n, Graphs g){
        int total_time = 0,commcost =0;
        List<Objects> rs = t.getRset();
        List<Objects> ws = t.getWset();
        for(int i = 0;i<rs.size();i++){
            int node_id = rs.get(i).getNode();
            Node nd = getNode(node_id,g);
            int access_cost = getCommCostClique(n, nd);
            if(total_time < access_cost){
                total_time = access_cost;
            }
            commcost += access_cost;
        }
        for(int i = 0;i<ws.size();i++){
            int node_id = ws.get(i).getNode();
            Node nd =getNode(node_id,g);
            int access_cost = getCommCostClique(n, nd);
            if(total_time < access_cost){
                total_time = access_cost;
            }
            commcost += access_cost;
            Objects obj = ws.get(i);
            obj.setNode(n.getNode_id());
            objs.set(obj.getObj_id()-1,obj);
        }
        int[] exec = {total_time,commcost};
        return exec;
    }

    /*
     * Execute transaction and return execution time based on the objects in read set and write set and its position on line.
     */
    private static int[] executeTxLine(Transaction t, Node n, Graphs g){
        int total_time = 0,commcost =0;
        List<Objects> rs = t.getRset();
        List<Objects> ws = t.getWset();
        for(int i = 0;i<rs.size();i++){
            int node_id = rs.get(i).getNode();
            Node nd = getNode(node_id,g);
            int access_cost = getCommCostLine(n, nd);
            if(total_time < access_cost){
                total_time = access_cost;
            }
            commcost += access_cost;
        }
        for(int i = 0;i<ws.size();i++){
            int node_id = ws.get(i).getNode();
            Node nd =getNode(node_id,g);
            int access_cost = getCommCostLine(n, nd);
            if(total_time < access_cost){
                total_time = access_cost;
            }
            commcost += access_cost;
            Objects obj = ws.get(i);
            obj.setNode(n.getNode_id());
            objs.set(obj.getObj_id()-1,obj);
        }
        int[] exec = {total_time,commcost};
        return exec;
    }

    /*
     * Execute transaction and return execution time based on the objects in read set and write set and its position on star.
     */
    private static int[] executeTxStar(Transaction t, Node n, Graphs g, int ray_size){
        int total_time = 0,commcost =0;
        List<Objects> rs = t.getRset();
        List<Objects> ws = t.getWset();
        for(int i = 0;i<rs.size();i++){
            int node_id = rs.get(i).getNode();
            Node nd = getNode(node_id,g);
            int access_cost = getCommCostStar(n, nd,ray_size);
            if(total_time < access_cost){
                total_time = access_cost;
            }
            commcost += access_cost;
        }
        for(int i = 0;i<ws.size();i++){
            int node_id = ws.get(i).getNode();
            Node nd =getNode(node_id,g);
            int access_cost = getCommCostStar(n, nd,ray_size);
            if(total_time < access_cost){
                total_time = access_cost;
            }
            commcost += access_cost;
            Objects obj = ws.get(i);
            obj.setNode(n.getNode_id());
            objs.set(obj.getObj_id()-1,obj);
        }
        int[] exec = {total_time,commcost};
        return exec;
    }

    /*
     * Execute transaction and return execution time based on the objects in read set and write set and its position on cluster.
     */
    private static int[] executeTxCluster(Transaction t, Node n, Graphs g, int cluster_size){
        int total_time = 0,commcost =0;
        List<Objects> rs = t.getRset();
        List<Objects> ws = t.getWset();
        for(int i = 0;i<rs.size();i++){
            int node_id = rs.get(i).getNode();
            Node nd = getNode(node_id,g);
            int access_cost = getCommCostCluster(n, nd,cluster_size);
            if(total_time < access_cost){
                total_time = access_cost;
            }
            commcost += access_cost;
        }
        for(int i = 0;i<ws.size();i++){
            int node_id = ws.get(i).getNode();
            Node nd =getNode(node_id,g);
            int access_cost = getCommCostCluster(n, nd,cluster_size);
            if(total_time < access_cost){
                total_time = access_cost;
            }
            commcost += access_cost;
            Objects obj = ws.get(i);
            obj.setNode(n.getNode_id());
            objs.set(obj.getObj_id()-1,obj);
        }
        int[] exec = {total_time,commcost};
        return exec;
    }
    /*
     * Execute transaction for grid grpah
     */
    public static void executeGrid(Graphs grid){
        int round=0, cumulative_rt=0,wait_time = 0;
        ArrayList<ArrayList<Integer>> dependtx = new ArrayList<>();
        while(round < total_txs) {
            System.out.println("Round "+round);
            boolean conflictstatus = false;
            int j=0;
            ArrayList<ArrayList<Transaction>> all_txs = new ArrayList<>();
            all_txs = nodal_txs;
            wait_time = 0;
            int new_cum_time = 0;

            for(int i=0;i<total_nodes;i++){
                int initcost = 0,commcost=0;
                if(i==0){
                    for(int x=0;x<total_objs;x++){
                        int cost = getCommCostGrid(getNode(priority_queue[i], grid),getNode(objs.get(x).getNode(),grid));
                        if(cost > initcost){
                            initcost = cost;
                        }
                    }
                }
                int count = 0;
                dependtx = generateConflictGraph(all_txs,total_nodes,round);
                Transaction t = all_txs.get(i).get(round);
                ArrayList<Integer> conflictlist = dependtx.get(priority_queue[i]);
                for(int k=0;k<conflictlist.size();k++){
                    int conflict = conflictlist.get(k);
                    if(conflict == 1){
                        count=k+1;
                        //System.out.println("Conflict, status = "+all_txs.get(k).get(j).getStatus());
                        if(all_txs.get(k).get(round).getStatus()=="COMMITTED"){
                            int movecost = getCommCostGrid(getNode(priority_queue[i],grid), getNode(k, grid));
                            ArrayList<Transaction> arr = all_txs.get(priority_queue[i]);
                            Transaction t1 = arr.get(round);
                            if(all_txs.get(k).get(round).getExecution_time() + movecost > t1.getExecution_time()){
                                t1.setExecution_time(all_txs.get(k).get(round).getExecution_time() + movecost);

                            }
                            arr.set(round,t1);
                            nodal_txs.set(priority_queue[i],arr);
                            count = all_txs.get(k).get(round).getWaited_for()+1;
                        }
                        else{
                            conflictstatus = true;

                        }
                    }
                }
                if(conflictstatus == false){
                    Transaction t1 = nodal_txs.get(priority_queue[i]).get(round);
                    int[] exec = executeTxGrid(t1,getNode(priority_queue[i],grid),grid);
                    int exec_time = exec[0];
                    int comm_cost = exec[1];
                    ArrayList<Transaction> arr = nodal_txs.get(priority_queue[i]);
                    if(exec_time > t1.getExecution_time()) {
                        t1.setExecution_time(exec_time);
                    }
                    else{
                        exec_time = t1.getExecution_time();
                    }
                    t1.setStatus("COMMITTED");
                    t1.setWaited_for(count);
                    t1.setConflicts(count);
                    t1.setComm_cost(comm_cost);
                    arr.set(round,t1);
                    nodal_txs.set(priority_queue[i],arr);
                    System.out.print("T("+priority_queue[i]+","+round+")\t=> ");
                    for(int x=0;x<count;x++) {
                        if(x==0) {
                            System.out.print("|----|");
                        }
                        else{
                            System.out.print("----|");
                        }
                    }
                    if((cumulative_rt + exec_time) < 10) {
                        System.out.print("  " +(cumulative_rt + exec_time) + "\n");
                    }
                    else{
                        System.out.print(" " + (cumulative_rt + exec_time) + "\n");
                    }

                    if((cumulative_rt + exec_time)>new_cum_time){
                        new_cum_time = cumulative_rt + exec_time;
                    }
                }
            }
            cumulative_rt = new_cum_time;
            round++;
        }
    }

    /*
     * Execute transaction for clique grpah
     */
    public static void executeClique(Graphs clique){
        int round=0, cumulative_rt=0,wait_time = 0;
        ArrayList<ArrayList<Integer>> dependtx = new ArrayList<>();
        while(round < total_txs) {
            System.out.println("Round "+round);
            boolean conflictstatus = false;
            int j=0;
            ArrayList<ArrayList<Transaction>> all_txs = new ArrayList<>();
            all_txs = nodal_txs;
            wait_time = 0;
            int new_cum_time = 0;

            for(int i=0;i<total_nodes;i++){
                int initcost = 0,commcost=0;
                if(i==0){
                    for(int x=0;x<total_objs;x++){
                        int cost = getCommCostClique(getNode(priority_queue[i], clique),getNode(objs.get(x).getNode(),clique));
                        if(cost > initcost){
                            initcost = cost;
                        }
                    }
                }
                int count = 0;
                dependtx = generateConflictGraph(all_txs,total_nodes,round);
                Transaction t = all_txs.get(i).get(round);
                ArrayList<Integer> conflictlist = dependtx.get(priority_queue[i]);
                for(int k=0;k<conflictlist.size();k++){
                    int conflict = conflictlist.get(k);
                    if(conflict == 1){
                        count=k+1;
                        //System.out.println("Conflict, status = "+all_txs.get(k).get(j).getStatus());
                        if(all_txs.get(k).get(round).getStatus()=="COMMITTED"){
                            int movecost = getCommCostClique(getNode(priority_queue[i],clique), getNode(k, clique));
                            ArrayList<Transaction> arr = all_txs.get(priority_queue[i]);
                            Transaction t1 = arr.get(round);
                            if(all_txs.get(k).get(round).getExecution_time() + movecost > t1.getExecution_time()){
                                t1.setExecution_time(all_txs.get(k).get(round).getExecution_time() + movecost);

                            }
                            arr.set(round,t1);
                            nodal_txs.set(priority_queue[i],arr);
                            count = all_txs.get(k).get(round).getWaited_for()+1;
                        }
                        else{
                            conflictstatus = true;

                        }
                    }
                }
                if(conflictstatus == false){
                    Transaction t1 = nodal_txs.get(priority_queue[i]).get(round);
                    int[] exec = executeTxClique(t1,getNode(priority_queue[i],clique),clique);
                    int exec_time = exec[0];
                    int comm_cost = exec[1];
                    ArrayList<Transaction> arr = nodal_txs.get(priority_queue[i]);
                    if(exec_time > t1.getExecution_time()) {
                        t1.setExecution_time(exec_time);
                    }
                    else{
                        exec_time = t1.getExecution_time();
                    }
                    t1.setStatus("COMMITTED");
                    t1.setWaited_for(count);
                    t1.setConflicts(count);
                    t1.setComm_cost(comm_cost);
                    arr.set(round,t1);
                    nodal_txs.set(priority_queue[i],arr);
                    System.out.print("T("+priority_queue[i]+","+round+")\t=> ");
                    for(int x=0;x<count;x++) {
                        if(x==0) {
                            System.out.print("|----|");
                        }
                        else{
                            System.out.print("----|");
                        }
                    }
                    if((cumulative_rt + exec_time) < 10) {
                        System.out.print("  " +(cumulative_rt + exec_time) + "\n");
                    }
                    else{
                        System.out.print(" " + (cumulative_rt + exec_time) + "\n");
                    }

                    if((cumulative_rt + exec_time)>new_cum_time){
                        new_cum_time = cumulative_rt + exec_time;
                    }
                }
            }
            cumulative_rt = new_cum_time;
            round++;
        }
    }

    /*
     * Execute transaction for line grpah
     */
    public static void executeLine(Graphs line){
        int round=0, cumulative_rt=0,wait_time = 0;
        ArrayList<ArrayList<Integer>> dependtx = new ArrayList<>();
        while(round < total_txs) {
            System.out.println("Round "+round);
            boolean conflictstatus = false;
            int j=0;
            ArrayList<ArrayList<Transaction>> all_txs = new ArrayList<>();
            all_txs = nodal_txs;
            wait_time = 0;
            int new_cum_time = 0;

            for(int i=0;i<total_nodes;i++){
                int initcost = 0,commcost=0;
                if(i==0){
                    for(int x=0;x<total_objs;x++){
                        int cost = getCommCostLine(getNode(priority_queue[i], line),getNode(objs.get(x).getNode(),line));
                        if(cost > initcost){
                            initcost = cost;
                        }
                    }
                }
                int count = 0;
                dependtx = generateConflictGraph(all_txs,total_nodes,round);
                Transaction t = all_txs.get(i).get(round);
                ArrayList<Integer> conflictlist = dependtx.get(priority_queue[i]);
                for(int k=0;k<conflictlist.size();k++){
                    int conflict = conflictlist.get(k);
                    if(conflict == 1){
                        count=k+1;
                        if(all_txs.get(k).get(round).getStatus()=="COMMITTED"){
                            int movecost = getCommCostLine(getNode(priority_queue[i],line), getNode(k, line));
                            ArrayList<Transaction> arr = all_txs.get(priority_queue[i]);
                            Transaction t1 = arr.get(round);
                            if(all_txs.get(k).get(round).getExecution_time() + movecost > t1.getExecution_time()){
                                t1.setExecution_time(all_txs.get(k).get(round).getExecution_time() + movecost);

                            }
                            arr.set(round,t1);
                            nodal_txs.set(priority_queue[i],arr);
                            count = all_txs.get(k).get(round).getWaited_for()+1;
                        }
                        else{
                            conflictstatus = true;

                        }
                    }
                }
                if(conflictstatus == false){
                    Transaction t1 = nodal_txs.get(priority_queue[i]).get(round);
                    int[] exec = executeTxLine(t1,getNode(priority_queue[i],line),line);
                    int exec_time = exec[0];
                    int comm_cost = exec[1];
                    ArrayList<Transaction> arr = nodal_txs.get(priority_queue[i]);
                    if(exec_time > t1.getExecution_time()) {
                        t1.setExecution_time(exec_time);
                    }
                    else{
                        exec_time = t1.getExecution_time();
                    }
                    t1.setStatus("COMMITTED");
                    t1.setWaited_for(count);
                    t1.setConflicts(count);
                    t1.setComm_cost(comm_cost);
                    arr.set(round,t1);
                    nodal_txs.set(priority_queue[i],arr);
                    System.out.print("T("+priority_queue[i]+","+round+")\t=> ");
                    for(int x=0;x<count;x++) {
                        if(x==0) {
                            System.out.print("|----|");
                        }
                        else{
                            System.out.print("----|");
                        }
                    }
                    if((cumulative_rt + exec_time) < 10) {
                        System.out.print("  " +(cumulative_rt + exec_time) + "\n");
                    }
                    else{
                        System.out.print(" " + (cumulative_rt + exec_time) + "\n");
                    }

                    if((cumulative_rt + exec_time)>new_cum_time){
                        new_cum_time = cumulative_rt + exec_time;
                    }
                }
            }
            cumulative_rt = new_cum_time;
            round++;
        }
    }

    /*
     * Execute transaction for star grpah
     */
    public static void executeStar(Graphs star, int ray_size){
        int round=0, cumulative_rt=0,wait_time = 0;
        ArrayList<ArrayList<Integer>> dependtx = new ArrayList<>();
        while(round < total_txs) {
            System.out.println("Round "+round);
            boolean conflictstatus = false;
            int j=0;
            ArrayList<ArrayList<Transaction>> all_txs = new ArrayList<>();
            all_txs = nodal_txs;
            wait_time = 0;
            int new_cum_time = 0;

            for(int i=0;i<total_nodes;i++){
                int initcost = 0,commcost=0;
                if(i==0){
                    for(int x=0;x<total_objs;x++){
                        int cost = getCommCostStar(getNode(priority_queue[i], star),getNode(objs.get(x).getNode(),star),ray_size);
                        if(cost > initcost){
                            initcost = cost;
                        }
                    }
                }
                int count = 0;
                dependtx = generateConflictGraph(all_txs,total_nodes,round);
                Transaction t = all_txs.get(i).get(round);
                ArrayList<Integer> conflictlist = dependtx.get(priority_queue[i]);
                for(int k=0;k<conflictlist.size();k++){
                    int conflict = conflictlist.get(k);
                    if(conflict == 1){
                        count=k+1;
                        if(all_txs.get(k).get(round).getStatus()=="COMMITTED"){
                            int movecost = getCommCostStar(getNode(priority_queue[i],star), getNode(k, star),ray_size);
                            ArrayList<Transaction> arr = all_txs.get(priority_queue[i]);
                            Transaction t1 = arr.get(round);
                            if(all_txs.get(k).get(round).getExecution_time() + movecost > t1.getExecution_time()){
                                t1.setExecution_time(all_txs.get(k).get(round).getExecution_time() + movecost);

                            }
                            arr.set(round,t1);
                            nodal_txs.set(priority_queue[i],arr);
                            count = all_txs.get(k).get(round).getWaited_for()+1;
                        }
                        else{
                            conflictstatus = true;

                        }
                    }
                }
                if(conflictstatus == false){
                    Transaction t1 = nodal_txs.get(priority_queue[i]).get(round);
                    int[] exec = executeTxStar(t1,getNode(priority_queue[i],star),star,ray_size);
                    int exec_time = exec[0];
                    int comm_cost = exec[1];
                    ArrayList<Transaction> arr = nodal_txs.get(priority_queue[i]);
                    if(exec_time > t1.getExecution_time()) {
                        t1.setExecution_time(exec_time);
                    }
                    else{
                        exec_time = t1.getExecution_time();
                    }
                    t1.setStatus("COMMITTED");
                    t1.setWaited_for(count);
                    t1.setConflicts(count);
                    t1.setComm_cost(comm_cost);
                    arr.set(round,t1);
                    nodal_txs.set(priority_queue[i],arr);
                    System.out.print("T("+priority_queue[i]+","+round+")\t=> ");
                    for(int x=0;x<count;x++) {
                        if(x==0) {
                            System.out.print("|----|");
                        }
                        else{
                            System.out.print("----|");
                        }
                    }
                    if((cumulative_rt + exec_time) < 10) {
                        System.out.print("  " +(cumulative_rt + exec_time) + "\n");
                    }
                    else{
                        System.out.print(" " + (cumulative_rt + exec_time) + "\n");
                    }

                    if((cumulative_rt + exec_time)>new_cum_time){
                        new_cum_time = cumulative_rt + exec_time;
                    }
                }
            }
            cumulative_rt = new_cum_time;
            round++;
        }
    }

    /*
     * Execute transaction for cluster grpah
     */
    public static void executeCluster(Graphs cluster, int cluster_size){
        int round=0, cumulative_rt=0,wait_time = 0;
        ArrayList<ArrayList<Integer>> dependtx = new ArrayList<>();
        while(round < total_txs) {
            System.out.println("Round "+round);
            boolean conflictstatus = false;
            int j=0;
            ArrayList<ArrayList<Transaction>> all_txs = new ArrayList<>();
            all_txs = nodal_txs;
            wait_time = 0;
            int new_cum_time = 0;

            for(int i=0;i<total_nodes;i++){
                int initcost = 0,commcost=0;
                if(i==0){
                    for(int x=0;x<total_objs;x++){
                        int cost = getCommCostCluster(getNode(priority_queue[i], cluster),getNode(objs.get(x).getNode(),cluster),cluster_size);
                        if(cost > initcost){
                            initcost = cost;
                        }
                    }
                }
                int count = 0;
                dependtx = generateConflictGraph(all_txs,total_nodes,round);
                Transaction t = all_txs.get(i).get(round);
                ArrayList<Integer> conflictlist = dependtx.get(priority_queue[i]);
                for(int k=0;k<conflictlist.size();k++){
                    int conflict = conflictlist.get(k);
                    if(conflict == 1){
                        count=k+1;
                        if(all_txs.get(k).get(round).getStatus()=="COMMITTED"){
                            int movecost = getCommCostCluster(getNode(priority_queue[i],cluster), getNode(k, cluster),cluster_size);
                            ArrayList<Transaction> arr = all_txs.get(priority_queue[i]);
                            Transaction t1 = arr.get(round);
                            if(all_txs.get(k).get(round).getExecution_time() + movecost > t1.getExecution_time()){
                                t1.setExecution_time(all_txs.get(k).get(round).getExecution_time() + movecost);

                            }
                            arr.set(round,t1);
                            nodal_txs.set(priority_queue[i],arr);
                            count = all_txs.get(k).get(round).getWaited_for()+1;
                        }
                        else{
                            conflictstatus = true;

                        }
                    }
                }
                if(conflictstatus == false){
                    Transaction t1 = nodal_txs.get(priority_queue[i]).get(round);
                    int[] exec = executeTxCluster(t1,getNode(priority_queue[i],cluster),cluster,cluster_size);
                    int exec_time = exec[0];
                    int comm_cost = exec[1];
                    ArrayList<Transaction> arr = nodal_txs.get(priority_queue[i]);
                    if(exec_time > t1.getExecution_time()) {
                        t1.setExecution_time(exec_time);
                    }
                    else{
                        exec_time = t1.getExecution_time();
                    }
                    t1.setStatus("COMMITTED");
                    t1.setWaited_for(count);
                    t1.setConflicts(count);
                    t1.setComm_cost(comm_cost);
                    arr.set(round,t1);
                    nodal_txs.set(priority_queue[i],arr);
                    System.out.print("T("+priority_queue[i]+","+round+")\t=> ");
                    for(int x=0;x<count;x++) {
                        if(x==0) {
                            System.out.print("|----|");
                        }
                        else{
                            System.out.print("----|");
                        }
                    }
                    if((cumulative_rt + exec_time) < 10) {
                        System.out.print("  " +(cumulative_rt + exec_time) + "\n");
                    }
                    else{
                        System.out.print(" " + (cumulative_rt + exec_time) + "\n");
                    }

                    if((cumulative_rt + exec_time)>new_cum_time){
                        new_cum_time = cumulative_rt + exec_time;
                    }
                }
            }
            cumulative_rt = new_cum_time;
            round++;
        }
    }


    public static void main(String[] args) {
//        System.out.println("Hello World!");
        Scanner reader = new Scanner(System.in);
        int subgraph_line=0, subgraph_cluster=0, cluster_size=0, subgraph_star=0, ray_nodes=0;

        System.out.println("\n*** ----------------------------- ***\n");

        System.out.print("Choose Graph type: \n1->Line, 2->Clique, 3->Grid, 4->CLuster, 5->Star: ");
        int graph_type = reader.nextInt();
        if(graph_type == 1){
            System.out.println("Provide the sub-graph length (l): ");
            subgraph_line = reader.nextInt();
            System.out.print("\nProvide total number of nodes: ");
            total_nodes = reader.nextInt();
        }
        else if(graph_type == 2){
            System.out.print("\nProvide total number of nodes: ");
            total_nodes = reader.nextInt();
        }
        else if(graph_type == 3) {
            grid_size = (int) Math.sqrt(total_nodes);
            System.out.print("\nProvide Sub-grid size (n*n; n = N/k), k = ");
            sub_grid = reader.nextInt();
            System.out.print("\nProvide total number of nodes: ");
            total_nodes = reader.nextInt();
        }
        else if(graph_type == 4){
            System.out.println("Provide the total number of clusters: ");
            subgraph_cluster = reader.nextInt();
            System.out.println("Provide the size of each cluster (complete graph): ");
            cluster_size = reader.nextInt();
            total_nodes = subgraph_cluster * cluster_size;
        }
        else if(graph_type == 5){
            System.out.println("Provide the total number of rays: ");
            subgraph_star = reader.nextInt();
            System.out.println("Provide the number of nodes on each ray: ");
            ray_nodes = reader.nextInt();
            total_nodes = subgraph_star * ray_nodes + 1;
        }
        else{
            System.out.println("Invalid option.");
            System.exit(1);
        }
        System.out.print("\nProvide total number of objects: ");
        total_objs = reader.nextInt();

        System.out.print("\nProvide total transactions per node: ");
        total_txs = reader.nextInt();


        System.out.println("\n\tCase 1: Read-Write set size for a TX is fixed.");
        System.out.println("\tCase 2: Read-Write set size for a TX is random.");
        System.out.print("Choose your option (1/2): ");
        int option = reader.nextInt();


        ArrayList<Integer> obj_home = getRandList(total_objs, 1, total_nodes);
        for (int i = 0; i < total_objs; i++) {
            Objects obj = new Objects(i + 1, 1, obj_home.get(i));
            objs.add(obj);
        }


        if (option == 1) {
            for (int x = 0; x < total_nodes; x++) {
                txs = generateTransactions(total_objs, total_txs, update_rate, rwset_size);
                nodal_txs.add(txs);
            }
        }
        else if (option == 2) {
            for (int x = 0; x < total_nodes; x++) {
                txs = generateTransactions(total_objs, total_txs, update_rate);
                nodal_txs.add(txs);
            }
        }
        else {
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
            Transaction tx = new Transaction(i+1, rwset_size, update_rate,rset,wset,"IDLE",0,0,0,0);

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
        Graphs grid = new Graphs();
        Graphs clique = new Graphs();
        Graphs line = new Graphs();
        Graphs star = new Graphs();
        Graphs cluster = new Graphs();
        if(graph_type == 1){
            line = Graphs.generateLineGraph(total_nodes);
            generatePriorityQueueLine(total_nodes,total_nodes);
            executeLine(line);
        }
        else if(graph_type == 2){
            clique = Graphs.generateCliqueGraph(total_nodes);
            generatePriorityQueueClique(total_nodes);
            executeClique(clique);
        }
        else if(graph_type == 3) {
            grid = Graphs.generateGridGraph(grid_size);

            System.out.println("\n-----------------------------------------------\n\t  Grid graph of grid size (" + grid_size + " x " + grid_size + ")\n-----------------------------------------------");
            for (int i = 0; i < grid_size; i++) {
                for (int j = 0; j < grid_size; j++) {
                    System.out.print(grid.getNodes().get(i * grid_size + j).getValue() + "\t");
                }
                System.out.println("\n");
            }

            System.out.println("\n-----------------------------------------------\n\t  Initial distribution of objects in grid\n-----------------------------------------------");
            for (int i = 0; i < grid_size; i++) {
                for (int j = 0; j < grid_size; j++) {
                    int home = -1;
                    for (int k = 0; k < total_objs; k++) {
                        int nd = objs.get(k).getNode();
                        if (nd == i * grid_size + j) {
                            home = objs.get(k).getObj_id();
                        }
                    }
                    System.out.print(home + "\t");
                }
                System.out.println("\n");
            }

            generatePriorityQueueGrid(grid_size, sub_grid);
            System.out.println("Priority queue:");
            for (int i = 0; i < total_nodes; i++) {
                System.out.print(priority_queue[i] + " ");
            }

        /*System.out.println("\n-----------------------------------------------\n\t  Dependency Graph (Adjancency matrix)\n-----------------------------------------------");
        ArrayList<ArrayList<Integer>> depend = new ArrayList<>();
        for(int i=0;i<total_nodes;i++) {
            depend = generateDependencyGraph(nodal_txs,total_nodes,0);
        }
        for(int i=0;i<total_nodes;i++){
            for(int j=0;j<total_nodes;j++){
                System.out.print(depend.get(i).get(j) + " ");
            }
            System.out.println("\n");
        }*/

            System.out.println("\n-----------------------------------------------\nTransaction Conflict Graph\n-----------------------------------------------");
            ArrayList<ArrayList<Integer>> dependtx = new ArrayList<>();
            for (int i = 0; i < total_nodes; i++) {
                dependtx = generateConflictGraph(nodal_txs, total_nodes, 0);
            }
            for (int i = 0; i < total_nodes; i++) {
                for (int j = 0; j < total_nodes; j++) {
                    System.out.print(dependtx.get(i).get(j) + " ");
                }
                System.out.println("\n");
            }

            System.out.println("\n-----------------------------------------------\nTransaction execution\n-----------------------------------------------");
            executeGrid(grid);
        }
        else if(graph_type == 4){
            cluster = Graphs.generateClusterGraph(total_nodes,subgraph_cluster,cluster_size);
            generatePriorityQueueCluster(subgraph_cluster,cluster_size);
            System.out.println("Priority queue:");
            for (int i = 0; i < total_nodes; i++) {
                System.out.print(priority_queue[i] + " ");
            }

            System.out.println("\n-----------------------------------------------\nTransaction Conflict Graph\n-----------------------------------------------");
            ArrayList<ArrayList<Integer>> dependtx = new ArrayList<>();
            for (int i = 0; i < total_nodes; i++) {
                dependtx = generateConflictGraph(nodal_txs, total_nodes, 0);
            }
            for (int i = 0; i < total_nodes; i++) {
                for (int j = 0; j < total_nodes; j++) {
                    System.out.print(dependtx.get(i).get(j) + " ");
                }
                System.out.println("\n");
            }

            System.out.println("\n-----------------------------------------------\nTransaction execution\n-----------------------------------------------");
            executeCluster(cluster,cluster_size);
        }
        else if(graph_type == 5){
            star = Graphs.generateStarGraph(total_nodes, subgraph_star, ray_nodes);
            generatePriorityQueueStar(subgraph_star,ray_nodes);
            executeStar(star,ray_nodes);
        }

        System.out.println("Total execution time for each node\nNode\tRW Set\tRSET\tWSET\tCONFLICTS\tExec time\tComm Cost");
        for(int i = 0;i<total_nodes;i++){
            int exec_time = 0,rwsetsize=0,rset = 0, wset = 0,conflict = 0,commcost = 0;
            for(int j =0;j<total_txs;j++){
                exec_time += nodal_txs.get(i).get(j).getExecution_time();
                rwsetsize += nodal_txs.get(i).get(j).getRw_set_size();
                rset += nodal_txs.get(i).get(j).getRset().size();
                wset += nodal_txs.get(i).get(j).getWset().size();
                conflict += nodal_txs.get(i).get(j).getConflicts();
//                if(nodal_txs.get(i).get(j).getComm_cost() > commcost) {
                    commcost += nodal_txs.get(i).get(j).getComm_cost();
//                }
            }
            System.out.println("N"+i+"\t: \t  "+rwsetsize+"\t"+rset+"\t\t"+wset+"\t\t\t"+conflict+"\t\t  "+exec_time+"\t\t\t "+commcost);
        }

        System.out.println("Based on priority:");
        for(int i = 0;i<total_nodes;i++){
            int exec_time = 0,rwsetsize=0, rset = 0, wset = 0,conflict = 0,commcost=0;
            for(int j =0;j<total_txs;j++){
                exec_time += nodal_txs.get(priority_queue[i]).get(j).getExecution_time();
                rwsetsize += nodal_txs.get(priority_queue[i]).get(j).getRw_set_size();
                rset += nodal_txs.get(priority_queue[i]).get(j).getRset().size();
                wset += nodal_txs.get(priority_queue[i]).get(j).getWset().size();
                conflict += nodal_txs.get(priority_queue[i]).get(j).getConflicts();
//                if(nodal_txs.get(priority_queue[i]).get(j).getComm_cost() > commcost) {
                    commcost += nodal_txs.get(priority_queue[i]).get(j).getComm_cost();
//                }
            }
            System.out.println("N"+priority_queue[i]+"\t: \t  "+rwsetsize+"\t"+rset+"\t\t"+wset+"\t\t\t"+conflict+"\t\t  "+exec_time+"\t\t\t "+commcost);
        }
    }
}