import com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList;
import jdk.internal.util.xml.impl.Input;

import javax.print.attribute.standard.NumberUp;
import java.io.*;
import java.util.*;
import java.io.IOException;
import java.lang.ProcessBuilder;
import java.util.ArrayList;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

/**
 * @author Pavan Poudel
 * Date - 2018/08/03
 */
public class Main {
    private static int total_nodes = 100;
    private static int grid_size = 10;
    private static int sub_grid = 1;

    private static int total_objs = 50;
    private static int total_txs = 1000;
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
        if(a.getX() == b.getX()){
            cost = 0;
        }
        else if(clust_a == clust_b){
            cost = 1;
        }
        else{
            if((a.getX() % cluster_size) > 0){
                cost_a = 1;
            }
            if((b.getX() % cluster_size) > 0){
                cost_b = 1;
            }
            cost = cost_a + cost_b + 1;
        }

        return cost;
    }

    /*
    * Reverse a priority queue.
    */
    public static void reversePriorityQueue(){
        int[] temp = new int[total_nodes];
        for(int i=0;i<total_nodes;i++){
            temp[i] = priority_queue[total_nodes - 1 - i];
        }
        priority_queue = temp;
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
     * Find index of an element in an array.
     */
    public static int indexOf(int prq[], int value){
        int index = 0;
        for(int x = 0; x < prq.length; x++){
            if(prq[x] == value){
                index = x;
                break;
            }
        }
        return index;
    }

    /*
     * Generate a priority queue for transaction execution in Grid graph-online.
     */
    public static void generatePriorityQueueOnlineGrid(ArrayList<ArrayList<Integer>> components){
        priority_queue = new int[total_nodes];

        int count = 0;

        //find snake-path in grid
        int gridsize = (int) Math.sqrt(total_nodes), k = 0;
        int prq[] = new int[total_nodes];
        for(int i=0;i<gridsize; i++){
            for(int l=0; l<gridsize; l++){
                if(i%2 == 0){
                    prq[k] = i * gridsize + l;
                    k++;
                }
                else{
                    prq[k] = i * gridsize + (gridsize - l - 1);
                    k++;
                }
            }
        }
        for(int i = 0; i < components.size(); i++){
            ArrayList<Integer> temp = components.get(i);
            int cur_index = 0;
            boolean prev = false;

            for(int x = 1; x < components.get(i).size(); x++){
                int curr_node = components.get(i).get(x);
                cur_index = indexOf(prq, curr_node);
                for(int y = 0; y < x; y++){
                    if(cur_index < indexOf(prq, temp.get(y))){
                        ArrayList<Integer> temp1 = new ArrayList<>();
                        for(int z = y; z<x; z++){
                            temp1.add(temp.get(z));
                        }
                        temp.set(y,curr_node);
                        for(int z = 0; z<temp1.size(); z++){
                            temp.set(y+1, temp1.get(z));
                            y++;
                        }
                        prev = true;
                        break;
                    }
                }
                if(prev == false){
                    temp.set(x,components.get(i).get(x));
                }
            }
            components.set(i, temp);
        }

        System.out.println("Sorted Components:");
        for(int i = 0; i < components.size(); i++){
            System.out.println(components.get(i));
            if(components.get(i).size() > count){
                count = components.get(i).size();
            }
        }
        int round = 0, j = 0;
        while(round < count) {
            for (int i = 0; i < components.size(); i++) {
                if (components.get(i).size() > round) {
                    priority_queue[j] = components.get(i).get(round);
                    j++;
                }
            }
            round++;
        }
    }

    /*
     * Generate a priority queue for transaction execution in Grid graph-online.
     */
    public static void generatePriorityQueueOnline(ArrayList<ArrayList<Integer>> components, int priorityq[]){
        priority_queue = new int[total_nodes];

        int count = 0;

        //find snake-path in grid
//        int gridsize = (int) Math.sqrt(total_nodes), k = 0;
        int prq[] = new int[total_nodes];
/*        for(int i=0;i<gridsize; i++){
            for(int l=0; l<gridsize; l++){
                if(i%2 == 0){
                    prq[k] = i * gridsize + l;
                    k++;
                }
                else{
                    prq[k] = i * gridsize + (gridsize - l - 1);
                    k++;
                }
            }
        }*/

        prq = priorityq;

        for(int i = 0; i < components.size(); i++){
            ArrayList<Integer> temp = components.get(i);
            int cur_index = 0;
            boolean prev = false;

            for(int x = 1; x < components.get(i).size(); x++){
                int curr_node = components.get(i).get(x);
                cur_index = indexOf(prq, curr_node);
                for(int y = 0; y < x; y++){
                    if(cur_index < indexOf(prq, temp.get(y))){
                        ArrayList<Integer> temp1 = new ArrayList<>();
                        for(int z = y; z<x; z++){
                            temp1.add(temp.get(z));
                        }
                        temp.set(y,curr_node);
                        for(int z = 0; z<temp1.size(); z++){
                            temp.set(y+1, temp1.get(z));
                            y++;
                        }
                        prev = true;
                        break;
                    }
                }
                if(prev == false){
                    temp.set(x,components.get(i).get(x));
                }
            }
            components.set(i, temp);
        }

//        System.out.println("Sorted Components:");
        for(int i = 0; i < components.size(); i++){
//            System.out.println(components.get(i));
            if(components.get(i).size() > count){
                count = components.get(i).size();
            }
        }
        int round = 0, j = 0;
        while(round < count) {
            for (int i = 0; i < components.size(); i++) {
                if (components.get(i).size() > round) {
                    priority_queue[j] = components.get(i).get(round);
                    j++;
                }
            }
            round++;
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
    public static void generatePriorityQueueLine(int linesize, ArrayList<Transaction> txs){
        priority_queue = new int[total_nodes];
        int l = calculateL(txs);
        int j=0,k = 0,total = l;
        if(l<(linesize/2)){
            while(((k+1)*l) <= total_nodes) {
                for (int i = 0; i < l; i++) {
                    priority_queue[j] = k*l + i;
                    j++;
                }
                k = k + 2;
            }
            if(k*l < total_nodes){
                for(int i=0;i<(total_nodes - k*l);i++){
                    priority_queue[j] = k*l + i;
                    j++;
                }
            }
            k=1;
            while(((k+1)*l) <= total_nodes) {
                for (int i = 0; i < l; i++) {
                    priority_queue[j] = k*l + i;
                    j++;
                }
                k = k + 2;
            }
            if(k*l < total_nodes){
                for(int i=0;i<(total_nodes - k*l);i++){
                    priority_queue[j] = k*l + i;
                    j++;
                }
            }
        }
        else {
            for (int i = 0; i < total_nodes; i++) {
                priority_queue[i] = i;
            }
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
        /*for(int i=0;i<totalclusters;i++){
            System.out.print(lst.get(i)+",");
        }
        System.out.println("");*/
        int k=0;
        for(int i=0;i<totalclusters;i++){
            ArrayList<Integer> rnd = getRandList(clustersize-1,1,clustersize-1);
            /*for(int j=0;j<clustersize-1;j++){
                System.out.print(rnd.get(j)+1+",");
            }
            System.out.println("");*/
            priority_queue[k] = lst.get(i)*clustersize;
            k++;
            for(int j=0;j<clustersize-1;j++) {
                priority_queue[k] = lst.get(i)*clustersize + rnd.get(j) + 1;
                k++;
            }
        }
    }

    /*
     * Generate Read-Write set for a transaction with random size for offline case.
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
     * Generate Read-Write set for a transaction with random size for online case.
     */
    public static ArrayList<Transaction> generateTxsOnline(ArrayList<ArrayList<Integer>> objList, int tot_tx, int updt_rate){
        ArrayList<Transaction> txs = new ArrayList<Transaction>(tot_tx);

//        ArrayList<ArrayList<Integer>> objList = getRWList(total_objs, total_nodes);
        for(int i=0;i<tot_tx;i++) {
            List<Objects> ws = new ArrayList<Objects>();
            List<Objects> rs = new ArrayList<Objects>();

            List<Objects> rwset = setRWSetOnline(objList);

            Random rand = new Random();
            int rws_size = rwset.size();
            int ws_size = rws_size * updt_rate / 100;
            int rs_size = rws_size - ws_size;
            int n = 0, sum = 0;

            ArrayList<Integer> randList = getRandList(rws_size, 0, rwset.size()-1);
//            System.out.println(randList);

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
            Transaction tx = new Transaction(i, rws_size, updt_rate, rset, wset,"IDLE",0,0,0,0);

            txs.add(tx);
        }
        return txs;
    }

    /*
     * Calculate length of a subgraph in Line graph based on longest shortest walk of any object
     */
    public static int calculateL(ArrayList<Transaction> txs){
        int l=0,curr_node=0,obj_node=0;
        for (Transaction tx : txs) {
            List<Objects> rset = tx.getRset();
            List<Objects> wset = tx.getWset();

            for (Objects objects : rset) {
                obj_node = objects.getNode();
                if(abs(obj_node - curr_node) > l){
                    l = abs(obj_node - curr_node);
//                    System.out.println(l);
                }
            }
            for (Objects objects : wset) {
                obj_node = objects.getNode();
                if(abs(obj_node - curr_node) > l){
                    l = abs(obj_node - curr_node);
//                    System.out.println(l);
                }
            }
            curr_node++;
        }
        return l;
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
     * Generate Read-Write set for a transaction with fixed size - online.
     */
    public static ArrayList<Transaction> generateTxsOnline(ArrayList<ArrayList<Integer>> objList, int tot_tx, int updt_rate, int rws_size){
        ArrayList<Transaction> txs = new ArrayList<Transaction>(tot_tx);

//        ArrayList<ArrayList<Integer>> objList = getRWList(total_objs, total_nodes);
        for(int i=0;i<tot_tx;i++) {
            List<Objects> ws = new ArrayList<Objects>();
            List<Objects> rs = new ArrayList<Objects>();

            int ws_size = rws_size * updt_rate / 100;
            int rs_size = rws_size - ws_size;
            int n = 0, sum = 0;

            List<Objects> rwset = setRWSetOnline(objList);
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
     * Generate a list of unique random numbers.
     */
    public static int [] updateReadyList(int [] list){
        Random rand = new Random();
        for(int i = 0; i < list.length; i++){
            if(list[i] == 0){
                list[i] = rand.nextInt(2);
            }
        }
        return list;
    }

    /*
     * Generate a list of union of read-write sets to have different components.
     */
    public static ArrayList<ArrayList<Integer>> getRWList(int total_objs, int total_nodes){

        ArrayList<ArrayList<Integer>> objList = new ArrayList<>();
        int tot_comps = (int) sqrt(total_nodes);
//        System.out.println(total_nodes);
        Random rand = new Random();
        int range = rand.nextInt(tot_comps) + 1;
        for(int i=0;i<range;i++){
            objList.add(new ArrayList<Integer>());
        }

        for(int i = 1; i < total_objs + 1; i++){
            int j = rand.nextInt(range);
//            System.out.println(j);
            objList.get(j).add(new Integer(i));
        }
        System.out.println(objList);
        return objList;
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
                rw.add(objs.get(x));
                sum = sum + objs.get(x).getObj_size();
            }
        }
        return rw;
    }
	
	/*
     * Generate a random Read-Write set - online.
     */
    private static List<Objects> setRWSetOnline(ArrayList<ArrayList<Integer>> objList){
        List<Objects> rw = new ArrayList<>();
        int sum = 0;
        Random rand = new Random();
        ArrayList<Integer> randList = new ArrayList<>();

        //ArrayList<ArrayList<Integer>> objList = getRWList(total_objs, total_nodes);

        int tot_comps = objList.size();
        int j = rand.nextInt(tot_comps);

        int max = objList.get(j).size();
        int rw_size = rand.nextInt(max)+1;
        while (sum<rw_size) {
            int x = rand.nextInt(max);
//            System.out.println("sum = "+sum+" rw_size = "+rw_size+" max = "+max+" x = "+x);
            if (!randList.contains(x)) {
                randList.add(x);
                rw.add(objs.get((objList.get(j).get(x))-1));
                sum = sum + objs.get((objList.get(j).get(x))-1).getObj_size();
            }
//            System.out.println("sum = "+sum+" rw_size = "+rw_size+" max = "+max+" x = "+x);
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
     * Generate a transaction dependency graph based on objects positioned on the node -online.
     */
    private static ArrayList<ArrayList<Integer>> generateDependencyGraphOnline(ArrayList<ArrayList<Transaction>> txs, int total_nodes, int tx_num){
        ArrayList<ArrayList<Integer>> adjMatrix = new ArrayList<>();

        for(int i = 0;i<total_nodes;i++) {
            ArrayList<Integer> dependent = new ArrayList<>(total_nodes);
            for (int j = 0; j < total_nodes; j++) {
                dependent.add(0);
            }
            adjMatrix.add(dependent);
        }
        /*System.out.println("adjacency matrix before:\n");
        for (int i = 0; i < total_nodes; i++) {
            for (int j = 0; j < total_nodes; j++) {
                System.out.print(adjMatrix.get(i).get(j) + " ");
            }
            System.out.println("\n");
        }*/

        for(int i = 0;i<total_nodes;i++){
            List<Objects> rs = txs.get(i).get(tx_num).getRset();
            List<Objects> ws = txs.get(i).get(tx_num).getWset();

            ArrayList<Integer> dependent1 = new ArrayList<>(total_nodes);
            for(int j=0;j<total_nodes;j++){
                dependent1.add(0);
            }
            for(int j=0;j<total_nodes;j++){
                if(j != i) {
                    boolean depends = false;
                    List<Objects> rs1 = txs.get(j).get(tx_num).getRset();
                    List<Objects> ws1 = txs.get(j).get(tx_num).getWset();
                    for (int k = 0; k < ws.size(); k++) {
                        Objects obj = ws.get(k);
                        int obj_id = obj.getObj_id();
                        for (int l = 0; l < ws1.size(); l++) {
                            Objects obj1 = ws1.get(l);
                            int obj_id1 = obj1.getObj_id();
                            if (obj_id == obj_id1) {
                                depends = true;
                                break;
                            }
                        }
                        if (depends == true) {
                            break;
                        }
                        for (int l = 0; l < rs1.size(); l++) {
                            Objects obj1 = rs1.get(l);
                            int obj_id1 = obj1.getObj_id();
                            if (obj_id == obj_id1) {
                                depends = true;
                                break;
                            }
                        }
                        if (depends == true) {
                            break;
                        }
                    }
                    for (int k = 0; k < rs.size(); k++) {
                        Objects obj = rs.get(k);
                        int obj_id = obj.getObj_id();
                        for (int l = 0; l < ws1.size(); l++) {
                            Objects obj1 = ws1.get(l);
                            int obj_id1 = obj1.getObj_id();
                            if (obj_id == obj_id1) {
                                depends = true;
                                break;
                            }
                        }
                        if (depends == true) {
                            break;
                        }
                    }
                    if (depends == true) {
                        dependent1.set(j, 1);
                    }
                }
            }
            /*for (int j = 0; j < total_nodes; j++) {
                System.out.print(dependent1.get(j) + " ");
            }
            System.out.println("\n");*/
            adjMatrix.set(i,dependent1);
        }
        /*System.out.println("Transaction Conflict Graph (adjacency matrix):\n");
        for (int i = 0; i < total_nodes; i++) {
            for (int j = 0; j < total_nodes; j++) {
                System.out.print(adjMatrix.get(i).get(j) + " ");
            }
            System.out.println("\n");
        }*/
        return adjMatrix;
    }


    /*
     * Generate a transaction dependency graph based on objects positioned on the node -online.
     */
    private static ArrayList<ArrayList<Integer>> generateAdjMatrixOnline(ArrayList<Transaction> txs, int total_nodes){
        ArrayList<ArrayList<Integer>> adjMatrix = new ArrayList<>();

        for(int i = 0;i<total_nodes;i++) {
            ArrayList<Integer> dependent = new ArrayList<>(total_nodes);
            for (int j = 0; j < total_nodes; j++) {
                dependent.add(0);
            }
            adjMatrix.add(dependent);
        }
        /*System.out.println("adjacency matrix before:\n");
        for (int i = 0; i < total_nodes; i++) {
            for (int j = 0; j < total_nodes; j++) {
                System.out.print(adjMatrix.get(i).get(j) + " ");
            }
            System.out.println("\n");
        }*/

        for(int i = 0;i<total_nodes;i++){
            List<Objects> rs = txs.get(i).getRset();
            List<Objects> ws = txs.get(i).getWset();

            ArrayList<Integer> dependent1 = new ArrayList<>(total_nodes);
            for(int j=0;j<total_nodes;j++){
                dependent1.add(0);
            }
            for(int j=0;j<total_nodes;j++){
                if(j != i) {
                    boolean depends = false;
                    List<Objects> rs1 = txs.get(j).getRset();
                    List<Objects> ws1 = txs.get(j).getWset();
                    for (int k = 0; k < ws.size(); k++) {
                        Objects obj = ws.get(k);
                        int obj_id = obj.getObj_id();
                        for (int l = 0; l < ws1.size(); l++) {
                            Objects obj1 = ws1.get(l);
                            int obj_id1 = obj1.getObj_id();
                            if (obj_id == obj_id1) {
                                depends = true;
                                break;
                            }
                        }
                        if (depends == true) {
                            break;
                        }
                        for (int l = 0; l < rs1.size(); l++) {
                            Objects obj1 = rs1.get(l);
                            int obj_id1 = obj1.getObj_id();
                            if (obj_id == obj_id1) {
                                depends = true;
                                break;
                            }
                        }
                        if (depends == true) {
                            break;
                        }
                    }
                    for (int k = 0; k < rs.size(); k++) {
                        Objects obj = rs.get(k);
                        int obj_id = obj.getObj_id();
                        for (int l = 0; l < ws1.size(); l++) {
                            Objects obj1 = ws1.get(l);
                            int obj_id1 = obj1.getObj_id();
                            if (obj_id == obj_id1) {
                                depends = true;
                                break;
                            }
                        }
                        if (depends == true) {
                            break;
                        }
                    }
                    if (depends == true) {
                        dependent1.set(j, 1);
                    }
                }
            }
            /*for (int j = 0; j < total_nodes; j++) {
                System.out.print(dependent1.get(j) + " ");
            }
            System.out.println("\n");*/
            adjMatrix.set(i,dependent1);
        }
        /*System.out.println("Transaction Conflict Graph (adjacency matrix):\n");
        for (int i = 0; i < total_nodes; i++) {
            for (int j = 0; j < total_nodes; j++) {
                System.out.print(adjMatrix.get(i).get(j) + " ");
            }
            System.out.println("\n");
        }*/
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
     * Find components from transaction dependency graph based on Read set and Write set.
     */
    private static ArrayList<ArrayList<Integer>> generateComponents(ArrayList<ArrayList<Transaction>> txs, int total_nodes, int tx_num){
        ArrayList<ArrayList<Integer>> compArray = new ArrayList<>();
        ArrayList<ArrayList<Integer>> adjMatrix = generateDependencyGraphOnline(txs,total_nodes,tx_num);
        ArrayList<Integer> nodes = new ArrayList<>(total_nodes);
        for(int i = 0;i<total_nodes;i++) {
            nodes.add(i);
        }



        while(nodes.size() > 0) {
            ArrayList<Integer> comp = new ArrayList<>();
            int i = 0;
            comp.add(new Integer(nodes.get(0)));
            nodes.remove(new Integer(nodes.get(0)));
            int rounds = comp.size();
            while (i < rounds) {
                for (int j = 0; j < total_nodes; j++) {
                    int conflict = adjMatrix.get(comp.get(i)).get(j);
                    if (conflict == 1 && !comp.contains(new Integer(j))) {
                        comp.add(new Integer(j));
                        nodes.remove(new Integer(j));
                        rounds++;
                    }
                }
                i++;
            }
            compArray.add(comp);
        }

        return compArray;
    }

    /*
     * Find components from transaction dependency graph based on Read set and Write set - online.
     */
    private static ArrayList<ArrayList<Integer>> generateComponentsOnline(ArrayList<Transaction> txs, int total_nodes){
        ArrayList<ArrayList<Integer>> compArray = new ArrayList<>();
        ArrayList<ArrayList<Integer>> adjMatrix = generateAdjMatrixOnline(txs,total_nodes);
        ArrayList<Integer> nodes = new ArrayList<>();
        for(int i = 0;i<total_nodes;i++) {
            nodes.add(i);
        }

        while(nodes.size() > 0) {
            ArrayList<Integer> comp = new ArrayList<>();
            ArrayList<Integer> comp1 = new ArrayList<>();
            int i = 0;
            comp.add(new Integer(nodes.get(0)));
            comp1.add(new Integer(txs.get(nodes.get(0)).getTx_id()));
            nodes.remove(new Integer(nodes.get(0)));
            int rounds = comp.size();
            while (i < rounds) {
                for (int j = 0; j < total_nodes; j++) {
                    int conflict = adjMatrix.get(comp.get(i)).get(j);
                    if (conflict == 1 && !comp.contains(new Integer(j))) {
                        comp.add(new Integer(j));
                        nodes.remove(new Integer(j));
                        comp1.add(txs.get(j).getTx_id());
                        rounds++;
                    }
                }
                i++;
            }
            compArray.add(comp1);
        }
//        System.out.println("Components:");
//        System.out.println(compArray);
        return compArray;
    }

    /*
     * Find transaction for given set - online.
     */
    private static Transaction getTx(ArrayList<Transaction> txs_list, int tx_id){
        Transaction t = new Transaction();
        for(int i = 0; i < txs_list.size();i++){
            if(txs_list.get(i).getTx_id() == tx_id){
                t = txs_list.get(i);
                break;
            }
        }
        return t;
    }

    /*
     * Remove transaction from a given set - online.
     */
    private static ArrayList<Transaction> removeTx(ArrayList<Transaction> txs_list, int tx_id){
        for(int i = 0; i < txs_list.size();i++){
            if(txs_list.get(i).getTx_id() == tx_id){
                txs_list.remove(i);
                break;
            }
        }
        return txs_list;
    }


    /*
     * Find independent sets from components based on Read set and Write set - online.
     */
    private static ArrayList<ArrayList<Integer>> generateIndependentSetOnline(ArrayList<ArrayList<Integer>> components, ArrayList<Transaction> txs_list){
        ArrayList<ArrayList<Integer>> i_setArray = new ArrayList<>();
        for(int i = 0;i < components.size(); i++) {
            ArrayList<Integer> comp = components.get(i);
            while (comp.size() > 0) {
                ArrayList<Integer> i_set = new ArrayList<>();
//                System.out.println("comp size before = "+comp.size());
                ArrayList<Integer> comp1 = new ArrayList<>();
                for (Integer x : comp) {
                    comp1.add(new Integer(x));
                }
                i_set.add(comp1.get(0));
                Transaction t = getTx(txs_list, comp1.get(0));
                comp.remove(0);
                ArrayList<Integer> rws = new ArrayList<>();
                for (int j = 0; j < t.getRset().size(); j++) {
                    rws.add(t.getRset().get(j).getObj_id());
                }
                for (int j = 0; j < t.getWset().size(); j++) {
                    rws.add(t.getWset().get(j).getObj_id());
                }
//            System.out.println(rws);
                for (int j = 1; j < comp1.size(); j++) {
                    boolean conflict = false;
                    Transaction t1 = getTx(txs_list, comp1.get(j));
                    for (int k = 0; k < rws.size(); k++) {
                        int obj = rws.get(k);
//                    for (int k = 1; k < comp.size(); k++) {
                        for (int l = 0; l < t1.getWset().size(); l++) {
                            int obj1 = t1.getWset().get(l).getObj_id();
                            if (obj == obj1) {
                                conflict = true;
                                if (!i_set.contains(comp1.get(j))) {
                                    i_set.add(new Integer(comp1.get(j)));
//                                    System.out.println(comp1.get(j));
//                                    comp.remove(new Integer(comp1.get(j)));
//                                    System.out.println(comp);
                                }
                                break;
                            }
                        }
                        if (conflict == true) {
                            break;
                        }
                        for (int l = 0; l < t1.getRset().size(); l++) {
                            int obj1 = t1.getRset().get(l).getObj_id();
                            if (obj == obj1) {
//                                System.out.println(obj);
                                conflict = true;
                                if (!i_set.contains(comp1.get(j))) {
                                    i_set.add(new Integer(comp1.get(j)));
//                                    System.out.println(comp1.get(j));
//                                    comp.remove(new Integer(comp1.get(j)));
//                                    System.out.println(comp);
                                }
                                break;
                            }
                        }
                        if (conflict == true) {
                            break;
                        }
                    }
                /*if (conflict == false) {
                    if (!i_set.contains(components.get(i).get(j))) {
                        i_set.add(new Integer(components.get(i).get(j)));
                        comp.remove(new Integer(components.get(i).get(j)));
                    }
                }*/
                }
//            System.out.println(i_set);
                i_setArray.add(i_set);
                for(int j = 0; j < i_set.size(); j++){
                    comp.remove(new Integer(i_set.get(j)));
                }
//                System.out.println("comp size after = "+comp.size());
            }
        }
//        System.out.println("Independent sets:");
//        System.out.println(i_setArray);
        return i_setArray;
    }

    /*
     * Retrieve node of a grid with node_id.
     */
    private static Node getNode(int n_id, Graphs g){
        int j=0;
        Node nd = g.getNodes().get(j);
        while(nd.getNode_id() != n_id ) //TODO
        {
            j++;
            nd = g.getNodes().get(j);
        }
        return nd;
    }

    /*
     * Find Graph type online.
     */
    private static String getGraphType(int total_nodes, Graphs g){
        ArrayList<Integer> neighbors = new ArrayList<>();
        for(int i = 0; i<total_nodes; i++){
            Node nd = g.getNodes().get(i);
            int tot_neighbors = nd.getNeighbors().size();
            if(!neighbors.contains(tot_neighbors)){
                neighbors.add(tot_neighbors);
            }
        }
        int count = neighbors.size();
        if(neighbors.contains(2) && neighbors.contains(3) && neighbors.contains(4) && count == 3){
            return "GRID";
        }
        else if(count == 1){
            return "CLIQUE";
        }
        else if(neighbors.contains(1) && neighbors.contains(2) && count == 2){
            return "LINE";
        }
        else if(!neighbors.contains(1) && count == 2){
            return "CLUSTER";
        }
        else if(neighbors.contains(1) && neighbors.contains(2) && count == 3){
            return "STAR";
        }
        else{
            return "UNKNOWN";
        }
    }

    /*
     * Find Graph type offline.
     */
    private static String getGraphType(Graphs g){
        String gtype = g.getGraph_id();
        if(gtype.contains("grid")){
            return "GRID";
        }
        else if(gtype.contains("clique")){
            return "CLIQUE";
        }
        else if(gtype.contains("line")){
            return "LINE";
        }
        else if(gtype.contains("cluster")){
            return "CLUSTER";
        }
        else if(gtype.contains("star")){
            return "STAR";
        }
        else{
            return "UNKNOWN";
        }
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
            Node nd = getNode(node_id,g);
            int access_cost = getCommCostGrid(n, nd);
            if(total_time < access_cost){
                total_time = access_cost;
            }
            commcost += access_cost;
            Objects obj = ws.get(i);
            obj.setNode(n.getNode_id());
            objs.set(obj.getObj_id(),obj);
        }
        int[] exec = {total_time,commcost};
        return exec;
    }


    /*
     * Execute transaction and return execution time based on the objects in read set and write set and its position on grid - online.
     */
    private static int[] executeTxGridOnline(Transaction t, Node n, Graphs g){
        int total_time = 0,commcost =0;
        List<Objects> rs = t.getRset();
        List<Objects> ws = t.getWset();
        for(int i = 0;i<rs.size();i++){
            int objid = rs.get(i).getObj_id();
            int node_id = 0;
            for (Objects obj : objs) {
                if (obj.getObj_id() == objid) {
                    node_id = obj.getNode();
                    break;
                }
            }
            Node nd = getNode(node_id,g);
            int access_cost = getCommCostGrid(n, nd);
            if(total_time < access_cost){
                total_time = access_cost;
            }
            commcost += access_cost;
        }
        for(int i = 0;i<ws.size();i++){
            int objid = ws.get(i).getObj_id();
            int node_id = 0;
            for (Objects obj : objs) {
                if (obj.getObj_id() == objid) {
                    node_id = obj.getNode();
                    break;
                }
            }
            Node nd = getNode(node_id,g);
            int access_cost = getCommCostGrid(n, nd);
            if(total_time < access_cost){
                total_time = access_cost;
            }
            commcost += access_cost;
//            Objects obj = ws.get(i);
//            obj.setNode(n.getNode_id());
//            objs.set(obj.getObj_id()-1,obj);
        }
        int[] exec = {total_time,commcost};
        return exec;
    }


    /*
     * Check if transaction is running
     */
    private static boolean checkRunning(ArrayList<Integer> is, ArrayList<Transaction> ready_txs, ArrayList<Transaction> running_txs){
        boolean status = false;
        for (Integer i : is) {
            Transaction tx = getTx(ready_txs, i);
            for (Transaction running_tx : running_txs) {
                if(tx.getTx_id() == running_tx.getTx_id()){
                    status = true;
                    break;
                }
            }
            if(status){
                break;
            }
        }
        return status;
    }

    /*
     * Update transaction list
     */
    private static void updateTxsList(ArrayList<Transaction> txs, Transaction t){
        int i = 0;
        for (Transaction tx : txs) {
            if(tx.getTx_id() == t.getTx_id()){
                txs.set(i,t);
                break;
            }
            i++;
        }
    }

    /*
     * Create schedule inside each independent set based on priority queue.
     */
    private static ArrayList<Integer> scheduleTxsOffline(ArrayList<Integer> tx_ids, ArrayList<Transaction> txs){
        ArrayList<Integer> sorted_txs = new ArrayList<>();
        Transaction t = getTx(txs, tx_ids.get(0));
        int h_node = t.getHome_node();
        int pos = 0, pos1 = 0;
        for(int i = 0; i < priority_queue.length; i++){
            if(priority_queue[i] == h_node){
                pos = i;
                break;
            }
        }
        sorted_txs.add(tx_ids.get(0));
        int siz = 1;
        while(siz < tx_ids.size()-1) {
            t = getTx(txs, tx_ids.get(siz));
            h_node = t.getHome_node();
            for(int i = 0; i < priority_queue.length; i++){
                if(priority_queue[i] == h_node){
                    pos1 = i;
                    break;
                }
            }
            if(pos1 < pos){
                sorted_txs.clear();
                sorted_txs.add(tx_ids.get(siz));
            }
            siz++;
        }
        tx_ids.remove(new Integer(sorted_txs.get(0)));
        for(int i = 0; i < tx_ids.size(); i++){
            sorted_txs.add(tx_ids.get(i));
        }
        return sorted_txs;
    }


    /*
     * Create schedule inside each independent set based on priority queue.
     */
    private static ArrayList<Integer> scheduleTxsOffline(ArrayList<Integer> tx_ids, ArrayList<Transaction> txs, int[] ready_count){
        ArrayList<Integer> sorted_txs = new ArrayList<>();
        int h_node = 0, x = 0;
        int round = ready_count[getTx(txs, tx_ids.get(0)).getHome_node()];
        for(int i = 1; i < tx_ids.size(); i++){
            if(ready_count[getTx(txs, tx_ids.get(i)).getHome_node()] < round){
                round = ready_count[i];
            }
        }
//        System.out.println("round = "+round);

        for(int i = 0; i < tx_ids.size(); i++){
            if(ready_count[getTx(txs, tx_ids.get(i)).getHome_node()] < round+1){
                h_node = getTx(txs, tx_ids.get(i)).getHome_node();
                x = i;
//                System.out.println("found = "+ready_count[getTx(txs, tx_ids.get(i)).getHome_node()]);
                break;
            }
        }


        int pos = 0, pos1 = 0;

        for(int i = 0; i < priority_queue.length; i++){
            if(priority_queue[i] == h_node){
                pos = i;
                break;
            }
        }
        sorted_txs.add(tx_ids.get(0));
        int siz = 0;
        while(siz < tx_ids.size()-1) {
            if(siz != x) {
                Transaction t = getTx(txs, tx_ids.get(siz));
                h_node = t.getHome_node();
                if(ready_count[t.getHome_node()] <= round) {
                    for (int i = 0; i < priority_queue.length; i++) {
                        if (priority_queue[i] == h_node) {
                            pos1 = i;
                            break;
                        }
                    }
                    if (pos1 < pos) {
                        sorted_txs.clear();
                        sorted_txs.add(tx_ids.get(siz));
                    }
                }
            }
            siz++;
        }
        tx_ids.remove(new Integer(sorted_txs.get(0)));
        for(int i = 0; i < tx_ids.size(); i++){
            sorted_txs.add(tx_ids.get(i));
        }
        return sorted_txs;
    }



    /*
     * Create schedule inside each independent set based on lower communication cost on grid - online.
     */
    private static ArrayList<Integer> scheduleTxs(ArrayList<Integer> tx_ids, ArrayList<Transaction> txs, Graphs g, int size){
        ArrayList<Integer> sorted_txs = new ArrayList<>();
        String g_type = getGraphType(g);
        while(tx_ids.size()>0) {
            Transaction t = getTx(txs, tx_ids.get(0));
            int comcost = 0;
            if(g_type.equals("LINE")) {
                comcost = getExecuteTxLine(t, getNode(t.getHome_node(), g), g)[1];
            }
            else if(g_type.equals("CLIQUE")) {
                comcost = getExecuteTxClique(t, getNode(t.getHome_node(), g), g)[1];
            }
            else if(g_type.equals("GRID")) {
                comcost = executeTxGridOnline(t, getNode(t.getHome_node(), g), g)[1];
            }
            else if(g_type.equals("CLUSTER")) {
                comcost = getExecuteTxCluster(t, getNode(t.getHome_node(), g), g, size)[1];
            }
            else if(g_type.equals("STAR")) {
                comcost = getExecuteTxStar(t, getNode(t.getHome_node(), g), g, size)[1];
            }
            int lowcosttx = tx_ids.get(0);
            for (int i = 1; i < tx_ids.size(); i++) {
                t = getTx(txs, tx_ids.get(i));
                int comcost1 = 0;
                if(g_type.equals("LINE")){
                    comcost1 = getExecuteTxLine(t, getNode(t.getHome_node(), g), g)[1];
                }
                else if(g_type.equals("CLIQUE")) {
                    comcost1 = getExecuteTxClique(t, getNode(t.getHome_node(), g), g)[1];
                }
                else if(g_type.equals("GRID")) {
                    comcost1 = executeTxGridOnline(t, getNode(t.getHome_node(), g), g)[1];
                }
                else if(g_type.equals("CLUSTER")) {
                    comcost1 = getExecuteTxCluster(t, getNode(t.getHome_node(), g), g, size)[1];
                }
                else if(g_type.equals("STAR")) {
                    comcost1 = getExecuteTxStar(t, getNode(t.getHome_node(), g), g, size)[1];
                }
                if (comcost1 < comcost) {
                    comcost = comcost1;
                    lowcosttx = tx_ids.get(i);
                }
            }
            sorted_txs.add(lowcosttx);
            tx_ids.remove(new Integer(lowcosttx));
        }
        return sorted_txs;
    }

    /*
     * Create schedule inside each independent set based on lower communication cost on grid - online.
     */
    private static ArrayList<Integer> scheduleTxsExec(ArrayList<Integer> tx_ids, ArrayList<Transaction> txs, Graphs g, int size){
        ArrayList<Integer> sorted_txs = new ArrayList<>();
        String g_type = getGraphType(g);
        while(tx_ids.size()>0) {
            Transaction t = getTx(txs, tx_ids.get(0));
            int execcost = 0;
            if(g_type.equals("LINE")) {
                execcost = getExecuteTxLine(t, getNode(t.getHome_node(), g), g)[0];
            }
            else if(g_type.equals("CLIQUE")) {
                execcost = getExecuteTxClique(t, getNode(t.getHome_node(), g), g)[0];
            }
            else if(g_type.equals("GRID")) {
                execcost = executeTxGridOnline(t, getNode(t.getHome_node(), g), g)[0];
            }
            else if(g_type.equals("CLUSTER")) {
                execcost = getExecuteTxCluster(t, getNode(t.getHome_node(), g), g, size)[0];
            }
            else if(g_type.equals("STAR")) {
                execcost = getExecuteTxStar(t, getNode(t.getHome_node(), g), g, size)[0];
            }
            int lowcosttx = tx_ids.get(0);
            for (int i = 1; i < tx_ids.size(); i++) {
                t = getTx(txs, tx_ids.get(i));
                int execcost1 = 0;
                if(g_type.equals("LINE")) {
                    execcost1 = getExecuteTxLine(t, getNode(t.getHome_node(), g), g)[0];
                }
                else if(g_type.equals("CLIQUE")) {
                    execcost1 = getExecuteTxClique(t, getNode(t.getHome_node(), g), g)[0];
                }
                else if(g_type.equals("GRID")) {
                    execcost1 = executeTxGridOnline(t, getNode(t.getHome_node(), g), g)[0];
                }
                else if(g_type.equals("CLUSTER")) {
                    execcost1 = getExecuteTxCluster(t, getNode(t.getHome_node(), g), g, size)[0];
                }
                else if(g_type.equals("STAR")) {
                    execcost1 = getExecuteTxStar(t, getNode(t.getHome_node(), g), g, size)[0];
                }
                if (execcost1 < execcost) {
                    execcost = execcost1;
                    lowcosttx = tx_ids.get(i);
                }
            }
            sorted_txs.add(lowcosttx);
            tx_ids.remove(new Integer(lowcosttx));
        }
        return sorted_txs;
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
            objs.set(obj.getObj_id(),obj);
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
            objs.set(obj.getObj_id(),obj);
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
            objs.set(obj.getObj_id(),obj);
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
            objs.set(obj.getObj_id(),obj);
        }
        int[] exec = {total_time,commcost};
        return exec;
    }

    /*
     * Find execution time of a transaction in grid graph.
     */
    private static int[] getExecutionTimeGrid(Transaction t, Node n, Graphs g){
        int total_time = 0,commcost =0;
        List<Objects> rs = t.getRset();
        List<Objects> ws = t.getWset();
        for(int i = 0;i<rs.size();i++){
            int node_id = rs.get(i).getNode();
            Node nd = getNode(node_id,g);
            int access_cost = getCommCostGrid(n,nd);
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
        }
        int[] exec = {total_time,commcost};
        return exec;
    }

    /*
     * Get execution parameters for line graph.
     */
    private static int[] getExecuteTxLine(Transaction t, Node n, Graphs g){
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
        }
        int[] exec = {total_time,commcost};
        return exec;
    }

    /*
     * Get execution parameters for clique graph.
     */
    private static int[] getExecuteTxClique(Transaction t, Node n, Graphs g){
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
        }
        int[] exec = {total_time,commcost};
        return exec;
    }

    /*
     * Get execution parameters for cluster graph.
     */
    private static int[] getExecuteTxCluster(Transaction t, Node n, Graphs g, int cluster_size){
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
        }
        int[] exec = {total_time,commcost};
        return exec;
    }

    /*
     * Get execution parameters for star graph.
     */
    private static int[] getExecuteTxStar(Transaction t, Node n, Graphs g, int ray_size){
        int total_time = 0,commcost =0;
        List<Objects> rs = t.getRset();
        List<Objects> ws = t.getWset();
        for(int i = 0;i<rs.size();i++){
            int node_id = rs.get(i).getNode();
            Node nd = getNode(node_id,g);
            int access_cost = getCommCostStar(n, nd,ray_size);
//            System.out.println("comcost = "+access_cost + " nodes = "+n.getX()+","+nd.getX());
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
        }
        int[] exec = {total_time,commcost};
        return exec;
    }

    /*
     * Find optimal costs for transctions execution in different graphs.
     */
    private static int[] getOptimalCosts(ArrayList<ArrayList<Transaction>> txs_arr, int tot_nodes, Graphs g){
        int opt_com = 0, opt_rt = 0, graph_type = 0;
        String g_type = getGraphType(g);
        for(int i = 0; i < tot_nodes; i++){
            int exectime = 0;
            for(int j =0; j < txs_arr.get(i).size(); j++){
                int result[] = new int[] {0, 0};
                if(g_type.equals("LINE")){
                    result = getExecuteTxLine(txs_arr.get(i).get(j),getNode(i, g), g);
                }
                else if(g_type.equals("CLIQUE")){
                    result = getExecuteTxClique(txs_arr.get(i).get(j),getNode(i, g), g);
                }
                else if(g_type.equals("GRID")) {
                    result = getExecutionTimeGrid(txs_arr.get(i).get(j), getNode(i, g), g);
                }
                exectime += result[0];
                opt_com += result[1];
            }
            if(opt_rt < exectime) {
                opt_rt = exectime;
            }
        }
        int[] exec = {opt_rt,opt_com};
        return exec;
    }


    /*
     * Find optimal costs for transctions execution in different graphs.
     */
    private static int[] getOptimalCosts(ArrayList<ArrayList<Transaction>> txs_arr, int tot_nodes, Graphs g, int size){
        int opt_com = 0, opt_rt = 0, graph_type = 0;
        String g_type = getGraphType(g);
        for(int i = 0; i < tot_nodes; i++){
            int exectime = 0;
            for(int j =0; j < txs_arr.get(i).size(); j++){
                int result[] = new int[] {0, 0};
                if(g_type.equals("CLUSTER")) {
                    result = getExecuteTxCluster(txs_arr.get(i).get(j), getNode(i, g), g, size);
                }
                else if(g_type.equals("STAR")) {
                    result = getExecuteTxStar(txs_arr.get(i).get(j), getNode(i, g), g, size);
                }
                exectime += result[0];
                opt_com += result[1];
            }
            if(opt_rt < exectime) {
                opt_rt = exectime;
            }
        }
        int[] exec = {opt_rt,opt_com};
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
                    t1.setArrived_at(0);
                    t1.setWaiting_time(cumulative_rt);
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
            reversePriorityQueue();
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
            ArrayList<Transaction> txs = new ArrayList<>();
            for (ArrayList<Transaction> all_tx : all_txs) {
                txs.add(all_tx.get(round));
            }
            generatePriorityQueueLine(total_nodes,txs);

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

    /*
     * retrieve object from the object list
     */
    public static Objects getObj(ArrayList<Objects> objs, int obj_id){
        int i = 0;
        Objects obj = new Objects();
        while (i < objs.size()){
            int id = objs.get(i).getObj_id();
            if(id == obj_id){
                obj = objs.get(i);
                break;
            }
            i++;
        }
        return obj;
    }

    /*
     * Find total conflicts for a transaction
     */
    public static int getTotalConflicts(ArrayList<Transaction> txs, Transaction t){
        List<Objects> rs = t.getRset();
        List<Objects> ws = t.getWset();
        ArrayList<Integer> conflict_list = new ArrayList<>();
        for(int i = 0; i < rs.size(); i++){
            int objid = rs.get(i).getObj_id();
            for(int j = 0; j < txs.size(); j++){
                if(txs.get(j).getTx_id() != t.getTx_id()) {
                    List<Objects> rs1 = txs.get(j).getRset();
                    List<Objects> ws1 = txs.get(j).getWset();
                    for (int k = 0; k < rs1.size(); k++) {
                        int objid1 = rs1.get(k).getObj_id();
                        if (objid == objid1 && (!conflict_list.contains(txs.get(j).getTx_id()))) {
                            conflict_list.add(txs.get(j).getTx_id());
                        }
                    }
                    for (int k = 0; k < ws1.size(); k++) {
                        int objid1 = ws1.get(k).getObj_id();
                        if (objid == objid1 && (!conflict_list.contains(txs.get(j).getTx_id()))) {
                            conflict_list.add(txs.get(j).getTx_id());
                        }
                    }
                }
            }
        }
        for(int i = 0; i < ws.size(); i++){
            int objid = ws.get(i).getObj_id();
            for(int j = 0; j < txs.size(); j++){
                if(txs.get(j).getTx_id() != t.getTx_id()) {
                    List<Objects> rs1 = txs.get(j).getRset();
                    List<Objects> ws1 = txs.get(j).getWset();
                    for (int k = 0; k < rs1.size(); k++) {
                        int objid1 = rs1.get(k).getObj_id();
                        if (objid == objid1 && (!conflict_list.contains(txs.get(j).getTx_id()))) {
                            conflict_list.add(txs.get(j).getTx_id());
                        }
                    }
                    for (int k = 0; k < ws1.size(); k++) {
                        int objid1 = ws1.get(k).getObj_id();
                        if (objid == objid1 && (!conflict_list.contains(txs.get(j).getTx_id()))) {
                            conflict_list.add(txs.get(j).getTx_id());
                        }
                    }
                }
            }
        }
        return conflict_list.size();
    }


    public static void main(String[] args) throws IOException{
//        System.out.println("Hello World!");
        Scanner reader = new Scanner(System.in);
        int graph_type = 0, subgraph_line=0, subgraph_cluster=0, cluster_size=0, subgraph_star=0, ray_nodes=0, thread_count = 8;
        int optimal_rt = 0, optimal_comcost = 0, optimal_waittime = 0, optimal_conflicts = 0;
        String grph_type = "grid", bench = "bank";

        Graphs grid = new Graphs();
        Graphs clique = new Graphs();
        Graphs line = new Graphs();
        Graphs star = new Graphs();
        Graphs cluster = new Graphs();

        System.out.println("\n--------------------------------------------------------------------------------------------------\nOptions:");

        System.out.println("\t-g\t graph type, default grid [line, clique, grid, cluster, star]");
        System.out.println("\t-n\t total number of nodes, default 100");
        System.out.println("\t-k\t total subgrids in grid graph, default 1 (optional for line, clique, cluster, star)");
        System.out.println("\t-c\t total number of clusters, default 10 (optional for line, clique, grid, star)");
        System.out.println("\t-d\t size of each cluster in cluster graph, default 10 (optional for line, clique, grid, star)");
        System.out.println("\t-r\t total rays in star graph, default 14 (optional for line, clique, grid, cluster)");
        System.out.println("\t-s\t size of each ray in star graph, default 7 (optional for line, clique, grid, cluster)");
        System.out.println("\t-b\t benchmark type, default bank \n\t\t [bank, ll, hs, rb, sl, bayes, genome, intruder, kmeans, labyrinth, ssca2, vacation, yada]");
        System.out.println("\t-t\t total number of threads in benchmark, default 8");

        System.out.println("\nExample: \tLINE \t--> -g line -n100 -b bank -t8\n\t\t\tCLIQUE \t--> -g clique -n100 -b rb -t8\n\t\t\tGRID \t--> -g grid -n100 -k2 -b bayes -t8\n\t\t\tCLUSTER\t--> -g cluster -c10 -d10 -b genome -t8\n\t\t\tSTAR \t--> -g star -r14 -s7 -b intruder -t8");
        System.out.println("--------------------------------------------------------------------------------------------------\n");

        System.out.print("Execution Parameters: ");
        String param = reader.nextLine();
        String[] params = param.split("-");
        for(int i = 0; i < params.length;i++){
            if(params[i].startsWith("gg")){
                grph_type = params[i].replaceFirst("gg", "g").trim();
            }
            else if(params[i].startsWith("g")){
                grph_type = params[i].replaceFirst("g", "").trim();
            }
            else if(params[i].startsWith("n")){
                total_nodes = Integer.parseInt(params[i].replaceAll("n", "").trim());
            }
            else if(params[i].startsWith("k")){
                sub_grid = Integer.parseInt(params[i].replaceAll("k", "").trim());
            }
            else if(params[i].startsWith("c")){
                subgraph_cluster = Integer.parseInt(params[i].replaceAll("c", "").trim());
            }
            else if(params[i].startsWith("d")){
                cluster_size = Integer.parseInt(params[i].replaceAll("d", "").trim());
            }
            else if(params[i].startsWith("r")){
                subgraph_star = Integer.parseInt(params[i].replaceAll("r", "").trim());
            }
            else if(params[i].startsWith("s")){
                ray_nodes = Integer.parseInt(params[i].replaceAll("s", "").trim());
            }
            else if(params[i].startsWith("bb")){
                bench = params[i].replaceFirst("bb", "b").trim();
            }
            else if(params[i].startsWith("b")){
                bench = params[i].replaceFirst("b", "").trim();
            }
            else if(params[i].startsWith("-t")){
                thread_count = Integer.parseInt(params[i].replaceAll("-t", "").trim());
            }
        }

        System.out.println("Graph type: \t\t\t"+grph_type);
        System.out.println("Benchmarks: \t\t\t"+bench);
        System.out.println("Threads:    \t\t\t"+thread_count);
        if(grph_type.equals("grid")){
            grid_size = (int) Math.sqrt(total_nodes);
            System.out.println("Grid size:   \t\t\t"+grid_size+"*"+grid_size);
            System.out.println("Total subgrids (k*k):\t"+(sub_grid * sub_grid));
        }
        if(grph_type.equals("cluster")){
            System.out.println("Total clusters:\t\t\t"+subgraph_cluster);
            System.out.println("Cluster size:  \t\t\t"+cluster_size);
            total_nodes = subgraph_cluster * cluster_size;
        }
        if(grph_type.equals("star")){
            System.out.println("Total rays:\t\t\t\t"+subgraph_star);
            System.out.println("Ray size:  \t\t\t\t"+ray_nodes);
            total_nodes = subgraph_star * ray_nodes + 1;
        }
        System.out.println("Total nodes:\t\t\t"+total_nodes);
        /*System.out.print("Continue? (Y/N)");
        if(reader.next().equals("n") || reader.nextLine().equals("N")){
            System.exit(0);
        }*/
        System.out.println("Running ...");

        /*System.out.print("Graph Type [1->Line, 2->Clique, 3->Grid, 4->CLuster, 5->Star]: ");
        graph_type = reader.nextInt();
        if (graph_type == 1) {
//            System.out.print("\nProvide the sub-graph length (l): ");
//            subgraph_line = reader.nextInt();
            System.out.print("Total number of nodes (N): ");
            total_nodes = reader.nextInt();
        } else if (graph_type == 2) {
            System.out.print("Total number of nodes (N): ");
            total_nodes = reader.nextInt();
        } else if (graph_type == 3) {
            System.out.print("Total number of nodes (N): ");
            total_nodes = reader.nextInt();
            System.out.print("(OFFLINE) Sub-grid size (n*n; n = N/k), k = ");
            sub_grid = reader.nextInt();
            grid_size = (int) Math.sqrt(total_nodes);
        } else if (graph_type == 4) {
            System.out.print("Total number of clusters: ");
            subgraph_cluster = reader.nextInt();
            System.out.print("Size of each cluster (complete graph): ");
            cluster_size = reader.nextInt();
            total_nodes = subgraph_cluster * cluster_size;
        } else if (graph_type == 5) {
            System.out.print("Total number of rays: ");
            subgraph_star = reader.nextInt();
            System.out.print("Number of nodes on each ray: ");
            ray_nodes = reader.nextInt();
            total_nodes = subgraph_star * ray_nodes + 1;
        } else {
            System.out.println("Invalid option.");
            System.exit(1);
        }*/
//        Runtime rt = Runtime.getRuntime();
//        Process p = rt.exec("./ref/tinystm/test/bank/bank");
//        System.out.println(p);

        if(grph_type.contains("line")){
            graph_type = 1;
        }
        else if(grph_type.contains("clique")){
            graph_type = 2;
        }
        else if(grph_type.contains("grid")){
            graph_type = 3;
        }
        else if(grph_type.contains("cluster")){
            graph_type = 4;
        }
        else if(grph_type.contains("star")){
            graph_type = 5;
        }
        else{
            System.out.println("Error. Invalid graph type "+grph_type);
            System.exit(0);
        }
        int threads = 8;
        String exe_cmd = "./ref/tinySTM/test/bank/bank";
        String exe_arg1 = "-n8";
        String exe_arg2 = "-d20";
        Process process = new Process() {
            @Override
            public OutputStream getOutputStream() {
                return null;
            }

            @Override
            public InputStream getInputStream() {
                return null;
            }

            @Override
            public InputStream getErrorStream() {
                return null;
            }

            @Override
            public int waitFor() throws InterruptedException {
                return 0;
            }

            @Override
            public int exitValue() {
                return 0;
            }

            @Override
            public void destroy() {

            }
        };

        if(bench.equals("bank")) {
            process = new ProcessBuilder("./ref/tinySTM/test/bank/bank", "-n"+thread_count, "-d10").start();
        }
        else if(bench.equals("hs")) {
            process = new ProcessBuilder("./ref/tinySTM/test/intset/intset-hs", "-n"+thread_count, "-d20").start();
        }
        else if(bench.equals("ll")) {
            process = new ProcessBuilder("./ref/tinySTM/test/intset/intset-ll", "-n"+thread_count, "-d20").start();
        }
        else if(bench.equals("rb")) {
            process = new ProcessBuilder("./ref/tinySTM/test/intset/intset-rb", "-n"+thread_count, "-d20").start();
        }
        else if(bench.equals("sl")) {
            process = new ProcessBuilder("./ref/tinySTM/test/intset/intset-sl", "-n"+thread_count, "-d20").start();
        }
        else if(bench.equals("bayes")) {
            process = new ProcessBuilder("./ref/stamp/bayes/bayes", "-v32", "-r1024", "-n2", "-p20", "-s0", "-i2", "-e2", "-t"+thread_count).start();
        }
        else if(bench.equals("genome")) {
            process = new ProcessBuilder("./ref/stamp/genome/genome", "-g256", "-s16", "-n16384", "-t"+thread_count).start();
        }
        else if(bench.equals("intruder")) {
            process = new ProcessBuilder("./ref/stamp/intruder/intruder", "-a10", "-l4", "-n2038", "-s1", "-t"+thread_count).start();
        }
        else if(bench.equals("kmeans")) {
            process = new ProcessBuilder("./ref/stamp/kmeans/kmeans", "-m40", "-n40", "-t0.05", "-i","ref/stamp/kmeans/inputs/random-n2048-d16-c16.txt", "-p"+thread_count).start();
        }
        else if(bench.equals("labyrinth")) {
            process = new ProcessBuilder("./ref/stamp/labyrinth/labyrinth", "-iref/stamp/labyrinth/inputs/random-x32-y32-z3-n96.txt", "-t"+thread_count).start();
        }
        else if(bench.equals("ssca2")) {
            process = new ProcessBuilder("./ref/stamp/ssca2/ssca2", "-s13", "-i1.0", "-u1.0", "-l3", "-p3", "-t"+thread_count).start();
        }
        else if(bench.equals("vacation")) {
            process = new ProcessBuilder("./ref/stamp/vacation/vacation", "-n2", "-q90", "-u98", "-r16384", "-t4096", "-c"+thread_count).start();
        }
        else if(bench.equals("yada")) {
            process = new ProcessBuilder("./ref/stamp/yada/yada", "-a20", "-iref/stamp/yada/inputs/633.2", "-t"+thread_count).start();
        }
        else{
            System.out.println("Error. Invalid benchmark "+bench);
            System.exit(0);
        }
        /*InputStream is = process.getInputStream();
        OutputStream os = process.getOutputStream();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        BufferedReader readder = new BufferedReader(new InputStreamReader(is));*/

        String op, op1="";
        BufferedReader input = new BufferedReader (new InputStreamReader(process.getInputStream()));
        while ((op = input.readLine()) != null) {
            if(op.contains("<<>>")) {
                op1 = op.replaceAll("<<>>","");
//                System.out.println(op1);
            }
//            System.out.println(op);
        }
        input.close();
        /*if(!(new BufferedReader(new InputStreamReader(process.getErrorStream()))).readLine().equals(null)) {
            System.out.println((new BufferedReader(new InputStreamReader(process.getErrorStream()))).readLine());
        }*/

        final String[] split = op1.split(",");
        String txl = split[split.length-1].split("-")[1];
        int rw_size = threads*threads;
        int length = Integer.parseInt(txl) + rw_size;
//        System.out.println(length);
        String [][] Trs = new String[length][rw_size], Tws = new String[length][rw_size];
        int[] rset_size = new int[length], wset_size = new int[length];
        for(int i = 0; i < length; i++){
            rset_size[i] = 0;
            wset_size[i] = 0;
            for(int j = 0; j < rw_size; j++){
                Trs[i][j] = "";
                Tws[i][j] = "";
            }
        }
        for(int i = 0; i < split.length; i++){
            String[] tx_sp = split[i].split("-");
            int pos = Integer.parseInt(tx_sp[1]);
            if(tx_sp[0].equals("rs")){
                Trs[pos][rset_size[pos]] = tx_sp[2];
//                System.out.println(Trs[pos][rset_size[pos]]);
                rset_size[pos]++;
            }
            else if(tx_sp[0].equals("ws")){
                Tws[pos][wset_size[pos]] = tx_sp[2];
                wset_size[pos]++;
            }
        }

        ArrayList<ArrayList<String>> TXRS = new ArrayList<>();
        ArrayList<ArrayList<String>> TXWS = new ArrayList<>();
        int tottxs = 0;

        for(int i = 0; i < length; i++){
            ArrayList<String> a = new ArrayList(), b = new ArrayList();
            boolean rws = false;
            for(int j = 0; j < rw_size; j ++){
                if(!Trs[i][j].equals("")){
                    if(!a.contains(Trs[i][j])){
                        a.add(Trs[i][j]);
//                        System.out.println(Trs[i][j]);
                    }
                    rws = true;
                }
                if(!Tws[i][j].equals("")){
                    if(!b.contains(Tws[i][j])){
                        b.add(Tws[i][j]);
//                        System.out.println(Tws[i][j]);
                    }
                    rws = true;
                }
            }
            if(rws){
                TXRS.add(a);
                TXWS.add(b);
                tottxs++;
            }
        }

//        System.out.println("Transactions:\n");
        /*for(int i = 0; i < tottxs; i++){
            System.out.print("T"+i+":  \tRS => (");
            for(int j = 0; j < TXRS.get(i).size(); j++){
                System.out.print(TXRS.get(i).get(j)+" ");
            }
            System.out.print(")\n\t\tWS => (");
            for(int j = 0; j < TXWS.get(i).size(); j++){
                System.out.print(TXWS.get(i).get(j)+" ");
            }
            System.out.print(")\n");
        }*/

        Hashtable<String, Integer> txs_ht = new Hashtable<String, Integer>();
        ArrayList<Transaction> transactions = new ArrayList<>();
        ArrayList<Integer> obj_lst = new ArrayList<>();
        int txs_count=0;
        for(int i = 0; i < tottxs; i++){
            for(int j = 0; j < TXRS.get(i).size(); j++){
                if(!TXRS.get(i).get(j).isEmpty()){
                    if(!txs_ht.containsKey(TXRS.get(i).get(j))){
                        txs_ht.put(TXRS.get(i).get(j), new Integer(txs_count));
                        txs_count++;
                    }
                }
            }
            for(int j = 0; j < TXWS.get(i).size(); j++){
                if(!TXWS.get(i).get(j).isEmpty()){
                    if(!txs_ht.containsKey(TXWS.get(i).get(j))){
                        txs_ht.put(TXWS.get(i).get(j), new Integer(txs_count));
                        txs_count++;
                    }
                }
            }
        }

        for(int i = 0; i < tottxs; i++){
            List<Objects> rs_obj = new ArrayList<>(), ws_obj = new ArrayList<>();
            int rwset_size = 0;
            Random rand = new Random();
            for(int j = 0; j < TXRS.get(i).size(); j++){
                if(!TXRS.get(i).get(j).isEmpty()){
                    int obj_id = txs_ht.get(TXRS.get(i).get(j));
                    if(!obj_lst.contains(obj_id)) {
                        obj_lst.add(obj_id);
                        int obj_node = rand.nextInt(total_nodes);
                        Objects obj = new Objects(obj_id, 1, obj_node);
                        objs.add(obj);
                        rs_obj.add(obj);
                        rwset_size++;
                    }
                    else{
                        Objects obj = getObj(objs, obj_id);
                        objs.add(obj);
                        rs_obj.add(obj);
                        rwset_size++;
                    }
                }
            }
            for(int j = 0; j < TXWS.get(i).size(); j++){
                if(!TXWS.get(i).get(j).isEmpty()){
                    int obj_id = txs_ht.get(TXWS.get(i).get(j));
                    if(!obj_lst.contains(obj_id)) {
                        obj_lst.add(obj_id);
                        int obj_node = rand.nextInt(total_nodes);
                        Objects obj = new Objects(obj_id, 1, obj_node);
                        objs.add(obj);
                        ws_obj.add(obj);
                        rwset_size++;
                    }
                    else{
                        Objects obj = getObj(objs, obj_id);
                        objs.add(obj);
                        ws_obj.add(obj);
                        rwset_size++;
                    }
                }
            }
            Transaction t = new Transaction(i,rwset_size,update_rate,rs_obj,ws_obj,"IDLE",0,0,0,0);
            transactions.add(t);
        }
/*

        System.out.println("Tx\trw-set-size\tupdate-rate");
        System.out.println("---------------------------------");
        for (int i = 0; i < tottxs; i++) {
            System.out.print("T" + transactions.get(i).getTx_id() + "   \t" + transactions.get(i).getRw_set_size() + "\t\t" + transactions.get(i).getUpdate_rate() + "\t\tRead Set(Objects) ==> (");
            for (int j = 0; j < transactions.get(i).getRset().size(); j++) {
//                System.out.print("o"+txs.get(i).getRset().get(j).getObj_id()+":"+txs.get(i).getRset().get(j).getObj_size()+" ");
                System.out.print(transactions.get(i).getRset().get(j).getObj_id());
                if (j < transactions.get(i).getRset().size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.print(")\n\t\t\t\t\t\tWrite Set(Objects) ==> (");
            for (int j = 0; j < transactions.get(i).getWset().size(); j++) {
                System.out.print(transactions.get(i).getWset().get(j).getObj_id());
                if (j < transactions.get(i).getWset().size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.print(")\n");
        }

*/

        txs = transactions;



        /*System.out.print("Algorithm Type [1->OFFLINE, 2->ONLINE]: ");
        int alg = reader.nextInt();
        if(alg == 1) {*/

            /*System.out.print("Graph Type [1->Line, 2->Clique, 3->Grid, 4->CLuster, 5->Star]: ");
            int graph_type = reader.nextInt();
            if (graph_type == 1) {
//            System.out.print("\nProvide the sub-graph length (l): ");
//            subgraph_line = reader.nextInt();
                System.out.print("Total number of nodes: ");
                total_nodes = reader.nextInt();
            } else if (graph_type == 2) {
                System.out.print("Total number of nodes: ");
                total_nodes = reader.nextInt();
            } else if (graph_type == 3) {
                System.out.print("Total number of nodes: ");
                total_nodes = reader.nextInt();
                System.out.print("Sub-grid size (n*n; n = N/k), k = ");
                sub_grid = reader.nextInt();
                grid_size = (int) Math.sqrt(total_nodes);
            } else if (graph_type == 4) {
                System.out.print("Total number of clusters: ");
                subgraph_cluster = reader.nextInt();
                System.out.print("Size of each cluster (complete graph): ");
                cluster_size = reader.nextInt();
                total_nodes = subgraph_cluster * cluster_size;
            } else if (graph_type == 5) {
                System.out.print("Total number of rays: ");
                subgraph_star = reader.nextInt();
                System.out.print("Number of nodes on each ray: ");
                ray_nodes = reader.nextInt();
                total_nodes = subgraph_star * ray_nodes + 1;
            } else {
                System.out.println("Invalid option.");
                System.exit(1);
            }*/
//            System.out.print("Total number of objects: ");
//            total_objs = reader.nextInt();

            total_objs = txs_ht.size();
//            System.out.print("Total transactions per node: ");
//            total_txs = reader.nextInt();

            total_txs = tottxs/total_nodes;
            int remaining_txs = tottxs % total_nodes;

            for(int i = 0; i < total_nodes; i++){
                ArrayList<Transaction> txss = new ArrayList<>();
                if(i < remaining_txs) {
                    for (int j = 0; j < total_txs + 1; j++) {
                        Transaction t = transactions.get(j * total_nodes + i);
                        txss.add(t);
                    }
                    nodal_txs.add(txss);
                }
                else{
                    for (int j = 0; j < total_txs; j++) {
                        Transaction t = transactions.get(j * total_nodes + i);
                        txss.add(t);
                    }
                    nodal_txs.add(txss);
                }
            }
//        System.out.println("\n\tCase 1: Read-Write set size for a TX is fixed.");
//        System.out.println("\tCase 2: Read-Write set size for a TX is random.");
//            System.out.print("Read-Write Set size [1->Fixed Size, 2->Random Size]: ");
//            int option = reader.nextInt();
            /*int option = 2;


            ArrayList<Integer> obj_home = getRandList(total_objs, 1, total_nodes);
            for (int i = 0; i < total_objs; i++) {
                Objects obj = new Objects(i + 1, 1, obj_home.get(i));
                objs.add(obj);
            }


            if (option == 1) {
                if (alg == 1) {
                    for (int x = 0; x < total_nodes; x++) {
                        txs = generateTransactions(total_objs, total_txs, update_rate, rwset_size);
                        nodal_txs.add(txs);
                    }
                } else {
                    ArrayList<ArrayList<Integer>> objList = getRWList(total_objs, total_nodes);
                    for (int x = 0; x < total_nodes; x++) {
                        txs = generateTxsOnline(objList, total_txs, update_rate);
                        nodal_txs.add(txs);
                    }

                }
            } else if (option == 2) {
                if (alg == 1) {
                    for (int x = 0; x < total_nodes; x++) {
                        txs = generateTransactions(total_objs, total_txs, update_rate);
                        nodal_txs.add(txs);
                    }
                } else {
                    ArrayList<ArrayList<Integer>> objList = getRWList(total_objs, total_nodes);
                    for (int x = 0; x < total_nodes; x++) {
                        txs = generateTxsOnline(objList, total_txs, update_rate);
                        nodal_txs.add(txs);
                    }
                }
            } else {
                System.out.println("Invalid choice!");
                System.exit(0);
            }*/


        /*ArrayList<ArrayList<Integer>> objList = getRWList(total_objs, total_nodes);
        for(int i=0;i<total_txs;i++){
            List<Objects> ws = new ArrayList<Objects>();
            List<Objects> rs = new ArrayList<Objects>();
            List<Objects> rwset = setRWSet(objList);
            int ws_size = (rwset.size()) * update_rate/100;
            int rs_size = (rwset.size()) - ws_size;
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
        }*/

            /*System.out.println("---------------------------------");
            System.out.println("Tx\trw-set-size\tupdate-rate");
            System.out.println("---------------------------------");
            for (int i = 0; i < total_txs; i++) {
                System.out.print("T" + txs.get(i).getTx_id() + "   \t" + txs.get(i).getRw_set_size() + "\t\t" + txs.get(i).getUpdate_rate() + "\t\tRead Set(Objects) ==> (");
                for (int j = 0; j < txs.get(i).getRset().size(); j++) {
//                System.out.print("o"+txs.get(i).getRset().get(j).getObj_id()+":"+txs.get(i).getRset().get(j).getObj_size()+" ");
                    System.out.print(txs.get(i).getRset().get(j).getObj_id());
                    if (j < txs.get(i).getRset().size() - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.print(")\n\t\t\t\t\t\tWrite Set(Objects) ==> (");
                for (int j = 0; j < txs.get(i).getWset().size(); j++) {
                    System.out.print(txs.get(i).getWset().get(j).getObj_id());
                    if (j < txs.get(i).getWset().size() - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.print(")\n");
            }*/

/*
            System.out.println("\n---------------------------------\nNodes vs. Transactions\n---------------------------------");
            for (int i = 0; i < total_nodes; i++) {
                System.out.print("N" + (i + 1) + "  \tT" + nodal_txs.get(i).get(0).getTx_id() + "   \t" + nodal_txs.get(i).get(0).getRw_set_size() + "\t\t" + nodal_txs.get(i).get(0).getUpdate_rate() + "\t\tRead Set(Objects) ==> (");
                for (int j = 0; j < nodal_txs.get(i).get(0).getRset().size(); j++) {
//                System.out.print("o"+txs.get(i).getRset().get(j).getObj_id()+":"+txs.get(i).getRset().get(j).getObj_size()+" ");
                    System.out.print(nodal_txs.get(i).get(0).getRset().get(j).getObj_id());
                    if (j < nodal_txs.get(i).get(0).getRset().size() - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.print(")\n\t\t\t\t\t\t\t\tWrite Set(Objects) ==> (");
                for (int j = 0; j < nodal_txs.get(i).get(0).getWset().size(); j++) {
                    System.out.print(nodal_txs.get(i).get(0).getWset().get(j).getObj_id());
                    if (j < nodal_txs.get(i).get(0).getWset().size() - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.print(")\n");
            }
*/

            if (graph_type == 1) {
                line = Graphs.generateLineGraph(total_nodes);
                ArrayList<Transaction> txs = new ArrayList<>();
                for (ArrayList<Transaction> nodal_tx : nodal_txs) {
                    txs.add(nodal_tx.get(0));
                }
                generatePriorityQueueLine(total_nodes,txs);

                int opt[] = getOptimalCosts(nodal_txs, total_nodes, line);
                optimal_rt = opt[0];
                optimal_comcost = opt[1];

//                executeLine(line);
            } else if (graph_type == 2) {
                clique = Graphs.generateCliqueGraph(total_nodes);
                generatePriorityQueueClique(total_nodes);
                int opt[] = getOptimalCosts(nodal_txs, total_nodes, clique);
                optimal_rt = opt[0];
                optimal_comcost = opt[1];
//                executeClique(clique);
            } else if (graph_type == 3) {
                grid = Graphs.generateGridGraph(grid_size);

/*
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
*/

                generatePriorityQueueGrid(grid_size, sub_grid);
/*
                System.out.println("Priority queue:");
                for (int i = 0; i < total_nodes; i++) {
                    System.out.print(priority_queue[i] + " ");
                }
*/

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

/*                System.out.println("\n-----------------------------------------------\nTransaction Dependency Graph\n-----------------------------------------------");
                ArrayList<ArrayList<Integer>> dependtx = new ArrayList<>();
                for (int i = 0; i < total_nodes; i++) {
                    dependtx = generateConflictGraph(nodal_txs, total_nodes, 0);
                }
/*
                for (int i = 0; i < total_nodes; i++) {
                    for (int j = 0; j < total_nodes; j++) {
                        System.out.print(dependtx.get(i).get(j) + " ");
                    }
                    System.out.println("\n");
                }

                System.out.println("\n-----------------------------------------------\nComponents of Transaction Conflict Graph\n-----------------------------------------------");
*/
//                ArrayList<ArrayList<Integer>> components = generateComponents(nodal_txs, total_nodes, 0);

 /*               System.out.println("Components of the conflict graph:\n");
                for (int i = 0; i < components.size(); i++) {
                    System.out.print("C" + (i + 1) + ":\t(");
                    for (int j = 0; j < components.get(i).size(); j++) {
                        System.out.print(components.get(i).get(j) + " ");
                    }
                    System.out.print(")\n");
                }
*/
                int opt[] = getOptimalCosts(nodal_txs, total_nodes, grid);
                optimal_rt = opt[0];
                optimal_comcost = opt[1];

                /*for(int i = 0; i < total_nodes; i++){
                    int exectime = 0, opt_wait = 0;
                    for(int j =0; j < nodal_txs.get(i).size(); j++){
                        int[] result = getExecutionTimeGrid(nodal_txs.get(i).get(j),getNode(j, grid), grid);
                        exectime += result[0];
                        optimal_comcost += result[1];
                        optimal_waittime += opt_wait;
                        opt_wait = exectime;
                    }
                    if(optimal_rt < exectime) {
                        optimal_rt = exectime;
                    }
                }*/

//                System.out.println("\n-----------------------------------------------\nTransaction execution\n-----------------------------------------------");
//                executeGrid(grid);
            } else if (graph_type == 4) {
                cluster = Graphs.generateClusterGraph(total_nodes, subgraph_cluster, cluster_size);
                generatePriorityQueueCluster(subgraph_cluster, cluster_size);
/*
                System.out.println("Priority queue:");
                for (int i = 0; i < total_nodes; i++) {
                    System.out.print(priority_queue[i] + " ");
                }

                System.out.println("\n-----------------------------------------------\nTransaction Conflict Graph\n-----------------------------------------------");
*/
                /*ArrayList<ArrayList<Integer>> dependtx = new ArrayList<>();
                for (int i = 0; i < total_nodes; i++) {
                    dependtx = generateConflictGraph(nodal_txs, total_nodes, 0);
                }*/
 /*               for (int i = 0; i < total_nodes; i++) {
                    for (int j = 0; j < total_nodes; j++) {
                        System.out.print(dependtx.get(i).get(j) + " ");
                    }
                    System.out.println("\n");
                }
*/
                int opt[] = getOptimalCosts(nodal_txs, total_nodes, cluster, cluster_size);
                optimal_rt = opt[0];
                optimal_comcost = opt[1];

//                System.out.println("\n-----------------------------------------------\nTransaction execution\n-----------------------------------------------");
//                executeCluster(cluster, cluster_size);
            } else if (graph_type == 5) {
                star = Graphs.generateStarGraph(total_nodes, subgraph_star, ray_nodes);
                generatePriorityQueueStar(subgraph_star, ray_nodes);
                int opt[] = getOptimalCosts(nodal_txs, total_nodes, star, ray_nodes);
                optimal_rt = opt[0];
                optimal_comcost = opt[1];
//                executeStar(star, ray_nodes);
            }


            /*int totexectime = 0, totcommcost = 0, tot_waittime = 0, tot_conflicts = 0;
            System.out.println("Total execution time for each node\nNode\tRW Set\tRSET\tWSET\tCONFLICTS\tWaiting time\tExec time\tComm Cost");
            for (int i = 0; i < total_nodes; i++) {
                int exec_time = 0, rwsetsize = 0, rset = 0, wset = 0, conflict = 0, commcost = 0, wait_time = 0, execution_time;
                for (int j = 0; j < total_txs; j++) {
                    exec_time += nodal_txs.get(i).get(j).getExecution_time();
                    execution_time = nodal_txs.get(i).get(j).getExecution_time();
                    wait_time = nodal_txs.get(i).get(j).getWaiting_time();
                    rwsetsize += nodal_txs.get(i).get(j).getRw_set_size();
                    rset += nodal_txs.get(i).get(j).getRset().size();
                    wset += nodal_txs.get(i).get(j).getWset().size();
                    conflict += nodal_txs.get(i).get(j).getConflicts();
//                if(nodal_txs.get(i).get(j).getComm_cost() > commcost) {
                    commcost += nodal_txs.get(i).get(j).getComm_cost();
//                }
                    if(totexectime < execution_time + wait_time){
                        totexectime = execution_time + wait_time;
                    }
                    totcommcost += nodal_txs.get(i).get(j).getComm_cost();
                    tot_waittime += wait_time;
                }
                tot_conflicts += conflict;
                System.out.println("N" + i + "\t: \t  " + rwsetsize + "\t" + rset + "\t\t" + wset + "\t\t\t" + conflict + "\t\t  " + wait_time + "\t\t  " + exec_time + "\t\t\t " + commcost);
            }

            System.out.println("Based on priority:");
            for (int i = 0; i < total_nodes; i++) {
                int exec_time = 0, rwsetsize = 0, rset = 0, wset = 0, conflict = 0, commcost = 0;
                for (int j = 0; j < total_txs; j++) {
                    exec_time += nodal_txs.get(priority_queue[i]).get(j).getExecution_time();
                    rwsetsize += nodal_txs.get(priority_queue[i]).get(j).getRw_set_size();
                    rset += nodal_txs.get(priority_queue[i]).get(j).getRset().size();
                    wset += nodal_txs.get(priority_queue[i]).get(j).getWset().size();
                    conflict += nodal_txs.get(priority_queue[i]).get(j).getConflicts();
//                if(nodal_txs.get(priority_queue[i]).get(j).getComm_cost() > commcost) {
                    commcost += nodal_txs.get(priority_queue[i]).get(j).getComm_cost();
//                }
                }
                System.out.println("N" + priority_queue[i] + "\t" + rwsetsize + "\t" + rset + "\t" + wset + "\t" + conflict + "\t" + exec_time + "\t" + commcost);
            }

            System.out.println("\n\nTotal Execution Time: "+totexectime);
            System.out.println("Total Communication Cost: "+totcommcost);*/
//        }
        /*else if (alg == 2)*/{
            /*System.out.print("Graph Type [1->Line, 2->Clique, 3->Grid, 4->CLuster, 5->Star]: ");
            int graph_type = reader.nextInt();
            if (graph_type == 1) {
                System.out.print("Total number of nodes: ");
                total_nodes = reader.nextInt();
            } else if (graph_type == 2) {
                System.out.print("Total number of nodes: ");
                total_nodes = reader.nextInt();
            } else if (graph_type == 3) {
                System.out.print("Total number of nodes: ");
                total_nodes = reader.nextInt();
                sub_grid = 1;
                grid_size = (int) Math.sqrt(total_nodes);
            } else if (graph_type == 4) {
                System.out.print("Total number of clusters: ");
                subgraph_cluster = reader.nextInt();
                System.out.print("Size of each cluster (complete graph): ");
                cluster_size = reader.nextInt();
                total_nodes = subgraph_cluster * cluster_size;
            } else if (graph_type == 5) {
                System.out.print("Total number of rays: ");
                subgraph_star = reader.nextInt();
                System.out.print("Number of nodes on each ray: ");
                ray_nodes = reader.nextInt();
                total_nodes = subgraph_star * ray_nodes + 1;
            } else {
                System.out.println("Invalid option.");
                System.exit(1);
            }*/
            /*Random ran = new Random();
            total_objs = ran.nextInt(total_nodes/2 + 1) + total_nodes/2;
            ArrayList<Integer> obj_home = getRandList(total_objs, 1, total_nodes);
            for (int i = 0; i < total_objs; i++) {
                Objects obj = new Objects(i + 1, 1, obj_home.get(i));
                objs.add(obj);
            }*/

            //define total number of transactions

            /*if(total_nodes<100)
                total_txs = 100;
            else
                total_txs = total_nodes;

            ArrayList<ArrayList<Integer>> objList = getRWList(total_objs, total_nodes);
            txs = generateTxsOnline(objList, total_txs, update_rate);*/

/*
            System.out.println("\n---------------------------------\nNodes vs. Transactions\n---------------------------------");
            for (int i = 0; i < total_nodes; i++) {
                System.out.print("N" + (i + 1) + "  \tT" + txs.get(i).getTx_id() + "   \t" + txs.get(i).getRw_set_size() + "\t\t" + txs.get(i).getUpdate_rate() + "\t\tRead Set(Objects) ==> (");
                for (int j = 0; j < txs.get(i).getRset().size(); j++) {
//                System.out.print("o"+txs.get(i).getRset().get(j).getObj_id()+":"+txs.get(i).getRset().get(j).getObj_size()+" ");
                    System.out.print(txs.get(i).getRset().get(j).getObj_id());
                    if (j < txs.get(i).getRset().size() - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.print(")\n\t\t\t\t\t\t\t\tWrite Set(Objects) ==> (");
                for (int j = 0; j < txs.get(i).getWset().size(); j++) {
                    System.out.print(txs.get(i).getWset().get(j).getObj_id());
                    if (j < txs.get(i).getWset().size() - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.print(")\n");
            }
*/

            /*grid = Graphs.generateGridGraph(grid_size);
            ArrayList<Node> nds = grid.getNodes();
            for(int i = 0; i < nds.size();i++){
                System.out.println(nds.get(i).getNode_id());
            }*/

            ArrayList<Objects> objts = new ArrayList<>(objs.size());
            /*objts =  (ArrayList<Objects>) objs.clone();
            Iterator<Objects> iter = objs.iterator();
            while (iter.hasNext()){
                objts.add((Objects) iter.next().clone());
            }
            for (Objects obj : objs) {
                objts.add(obj.clone());
            }*/
            for(int i = 0; i < objs.size(); i++){
                int objid = objs.get(i).getObj_id();
                int objsize = objs.get(i).getObj_size();
                int objnode = objs.get(i).getNode();
                Objects o = new Objects(objid, objsize, objnode);
                objts.add(o);
            }
            ArrayList<ArrayList<Integer>> costsArray = new ArrayList<>();
            ArrayList<Transaction> txs_pool1 = new ArrayList<>();
            for(int i = 0; i < txs.size(); i++){
                List<Objects> ws = new ArrayList<>();
                List<Objects> rs = new ArrayList<>();
                for(int j = 0; j < txs.get(i).getRset().size(); j++){
                    int objid = txs.get(i).getRset().get(j).getObj_id();
                    int objsize = txs.get(i).getRset().get(j).getObj_size();
                    int objnode = txs.get(i).getRset().get(j).getNode();
                    Objects o = new Objects(objid, objsize, objnode);
                    rs.add(o);
                }
                for(int j = 0; j < txs.get(i).getWset().size(); j++){
                    int objid = txs.get(i).getWset().get(j).getObj_id();
                    int objsize = txs.get(i).getWset().get(j).getObj_size();
                    int objnode = txs.get(i).getWset().get(j).getNode();
                    Objects o = new Objects(objid, objsize, objnode);
                    ws.add(o);
                }
                Transaction t = new Transaction(txs.get(i).getTx_id(),txs.get(i).getRw_set_size(), txs.get(i).getUpdate_rate(),
                        rs, ws, txs.get(i).getStatus(), txs.get(i).getExecution_time(), txs.get(i).getWaited_for(),
                        txs.get(i).getConflicts(), txs.get(i).getComm_cost());
                txs_pool1.add(t);
            }
            ArrayList<ArrayList<Transaction>> readylst = new ArrayList<>();

            for(int z = 0; z < 2; z++) {

                System.out.println("Tx\trw-set-size\tupdate-rate");
                System.out.println("---------------------------------");
                for (int i = 0; i < txs.size(); i++) {
                    System.out.print("T" + txs.get(i).getTx_id() + "   \tRead Set(Objects) ==> (");
                    for (int j = 0; j < txs.get(i).getRset().size(); j++) {
//                System.out.print("o"+txs.get(i).getRset().get(j).getObj_id()+":"+txs.get(i).getRset().get(j).getObj_size()+" ");
                        System.out.print(txs.get(i).getRset().get(j).getObj_id() + "-"+txs.get(i).getRset().get(j).getNode());
                        if (j < txs.get(i).getRset().size() - 1) {
                            System.out.print(", ");
                        }
                    }
                    System.out.print(")\n\t\tWrite Set(Objects) ==> (");
                    for (int j = 0; j < txs.get(i).getWset().size(); j++) {
                        System.out.print(txs.get(i).getWset().get(j).getObj_id() + "-"+txs.get(i).getWset().get(j).getNode());
                        if (j < txs.get(i).getWset().size() - 1) {
                            System.out.print(", ");
                        }
                    }
                    System.out.print(")\n");
                }




                System.out.println(txs.get(0).getTx_id()+" - "+txs.get(0).getWset().get(0).getNode()+" - "+txs_pool1.get(0).getWset().get(0).getNode());

                int tot_wait_online = 0, tot_conflicts_online = 0;

                ArrayList<Transaction> txs_pool = new ArrayList<>();

                for(int i = 0; i < txs.size(); i++){
                    List<Objects> ws = new ArrayList<>();
                    List<Objects> rs = new ArrayList<>();
                    for(int j = 0; j < txs.get(i).getRset().size(); j++){
                        int objid = txs.get(i).getRset().get(j).getObj_id();
                        int objsize = txs.get(i).getRset().get(j).getObj_size();
                        int objnode = txs.get(i).getRset().get(j).getNode();
                        Objects o = new Objects(objid, objsize, objnode);
                        rs.add(o);
                    }
                    for(int j = 0; j < txs.get(i).getWset().size(); j++){
                        int objid = txs.get(i).getWset().get(j).getObj_id();
                        int objsize = txs.get(i).getWset().get(j).getObj_size();
                        int objnode = txs.get(i).getWset().get(j).getNode();
                        Objects o = new Objects(objid, objsize, objnode);
                        ws.add(o);
                    }
                    Transaction t = new Transaction(txs.get(i).getTx_id(),txs.get(i).getRw_set_size(), txs.get(i).getUpdate_rate(),
                            rs, ws, txs.get(i).getStatus(), txs.get(i).getExecution_time(), txs.get(i).getWaited_for(),
                            txs.get(i).getConflicts(), txs.get(i).getComm_cost());
                    txs_pool.add(t);
                }
                ArrayList<Transaction> ready_txs = new ArrayList<>();
                ArrayList<Transaction> waiting_txs = new ArrayList<>();
                ArrayList<Transaction> running_txs = new ArrayList<>();
                ArrayList<Transaction> committed_txs = new ArrayList<>();
                int[] ready_list = new int[total_nodes];
                int[] ready_count = new int[total_nodes];
                for (int i = 0; i < total_nodes; i++) {
                    ready_list[i] = 0;
                    ready_count[i] = 0;
                }
                ArrayList<Integer> commit_list = new ArrayList<>();
                ArrayList<Integer> prev_run_list = new ArrayList<>();
                int prev_commit_list_size = 0, curr_commit_list_size = 0;

                int total_txs = txs_pool.size();
                int timestep = 0, tot_comm_cost = 0;
//                System.out.println(txs_pool1.size() + " "+committed_txs.size());
                while (committed_txs.size() < total_txs) {
                    timestep++;
//                while (txs_pool.size() > 0) {
                    if(z == 0) {
                        if (txs_pool.size() > 0) {
                            //assign new transaction to the empty node dynamically
//                int update_list [] = updateReadyList(ready_list);
                            for (int i = 0; i < ready_list.length; i++) {
//                    if(ready_list[i] == 0 && update_list[i] == 1){
                                if (ready_list[i] == 0) {
//                                    if(ready_count[i] < nodal_txs.get(i).size()) {
                                        Transaction t = getTx(txs, txs_pool.get(0).getTx_id());
//                                        Transaction t = getTx(txs, nodal_txs.get(i).get(ready_count[i]).getTx_id());
                                        t.setArrived_at(timestep - 1);
                                        t.setHome_node(i);
                                        t.setConflicts(0);
                                        t.setStatus("WAITING");
                                        ready_txs.add(t);
                                        txs_pool.remove(0);
                                        ready_list[i] = 1;
                                        ready_count[i] += 1;
                                        updateTxsList(txs, t);
//                                    }
                                }
                                if (txs_pool.size() == 0) {
                                    break;
                                }
                            }
                        }
                    }
                    else {
                        if (txs_pool.size() > 0) {
                            //assign new transaction to the empty node dynamically
//                int update_list [] = updateReadyList(ready_list);
                            for (int i = 0; i < ready_list.length; i++) {
//                    if(ready_list[i] == 0 && update_list[i] == 1){
                                if (ready_list[i] == 0) {
                                    Random rand = new Random();
                                    int update = rand.nextInt(1000) % 200;
                                    if (update == 13) {
                                        Transaction t = getTx(txs, txs_pool.get(0).getTx_id());
                                        t.setArrived_at(timestep - 1);
                                        t.setHome_node(i);
                                        t.setConflicts(0);
                                        t.setStatus("WAITING");
                                        ready_txs.add(t);
                                        txs_pool.remove(0);
                                        ready_list[i] = 1;
                                        updateTxsList(txs, t);
                                    }
                                }
                                if (txs_pool.size() == 0) {
                                    break;
                                }
                            }
                        }
                    }

                    /*if(z == 0){
                        ArrayList<Transaction> ttt = new ArrayList<>();
                        for(int i = 0; i < ready_txs.size(); i++){
                            ttt.add(ready_txs.get(i));
                        }
                        readylst.add(ttt);
                    }
                    if(z == 1){
                        ready_txs = readylst.get(timestep-1);
                    }*/
//                    System.out.println("Ready txs size = "+ready_txs.size());
                    for(int i = 0; i < ready_txs.size(); i++){
                        Transaction t = ready_txs.get(i);
                        int conflicts = getTotalConflicts(ready_txs, t);
                        if(t.getConflicts() < conflicts){
                            t.setConflicts(conflicts);
                            ready_txs.set(i, t);
                        }
                    }

//                    System.out.println(ready_txs.size());
//                    System.out.println(committed_txs.size() + " " + total_txs);
                    ArrayList<ArrayList<Integer>> components = generateComponentsOnline(ready_txs, ready_txs.size());
                    ArrayList<ArrayList<Integer>> ind_sets = generateIndependentSetOnline(components, ready_txs);

                    if(z == 0) {
                        for (int i = 0; i < ind_sets.size(); i++) {
                            ArrayList<Integer> sortedIS = new ArrayList<>();  //based on priority queue
                            if(graph_type == 1){
                                generatePriorityQueueLine(total_nodes,ready_txs);
                                sortedIS = scheduleTxsOffline(ind_sets.get(i), ready_txs, ready_count); //uniform commits
//                                sortedIS = scheduleTxsOffline(ind_sets.get(i), ready_txs); // non-uniform commits
                            }
                            else{
                                sortedIS = scheduleTxsOffline(ind_sets.get(i), ready_txs, ready_count);
//                                sortedIS = scheduleTxsOffline(ind_sets.get(i), ready_txs); // non-uniform commits
                            }
                            ind_sets.set(i, sortedIS);
                        }
//                        System.out.println("Scheduled Independent Set:");
//                        System.out.println(ind_sets);
                    }
                    else if(z == 2) {
                        for (int i = 0; i < ind_sets.size(); i++) {
                            ArrayList<Integer> sortedIS = new ArrayList<>();//based on comm cost
                            if(graph_type == 1){
                                sortedIS = scheduleTxs(ind_sets.get(i), ready_txs, line, subgraph_line);  //based on comm cost
//                                sortedIS = scheduleTxsExec(ind_sets.get(i), ready_txs, line, subgraph_line); //based on exec time
                            }
                            else if(graph_type == 2){
                                sortedIS = scheduleTxs(ind_sets.get(i), ready_txs, clique, clique.getNumNodes());  //based on comm cost
//                                sortedIS = scheduleTxsExec(ind_sets.get(i), ready_txs, clique, clique.getNumNodes()); //based on exec time
                            }
                            else if(graph_type == 3){
                                sortedIS = scheduleTxs(ind_sets.get(i), ready_txs, grid, grid_size);  //based on comm cost
//                                sortedIS = scheduleTxsExec(ind_sets.get(i), ready_txs, grid, grid_size); //based on exec time
                            }
                            else if(graph_type == 4){
                                sortedIS = scheduleTxs(ind_sets.get(i), ready_txs, cluster, cluster_size);  //based on comm cost
//                                sortedIS = scheduleTxsExec(ind_sets.get(i), ready_txs, cluster, cluster_size); //based on exec time
                            }
                            else if(graph_type == 5){
                                sortedIS = scheduleTxs(ind_sets.get(i), ready_txs, star, ray_nodes);  //based on comm cost
//                                sortedIS = scheduleTxsExec(ind_sets.get(i), ready_txs, star, ray_nodes); //based on exec time
                            }
                            ind_sets.set(i, sortedIS);
                        }
//                        System.out.println("Scheduled Independent Set:");
//                        System.out.println(ind_sets);
                    }

                    for (int i = 0; i < ind_sets.size(); i++) {
//                        System.out.println(ready_txs.get(ind_sets.get(i).get(0)).getTx_id());
                        if(ind_sets.get(i).size() > 0) {
                            boolean status = checkRunning(ind_sets.get(i),ready_txs, running_txs);
                            if(!status) {
                                Transaction tx = getTx(ready_txs, ind_sets.get(i).get(0));
                                if (!prev_run_list.contains(tx.getTx_id())) {
                                    prev_run_list.add(tx.getTx_id());
//                            System.out.println(tx.getHome_node());
                                    int[] costs = {0, 0};
                                    if (graph_type == 1) {
                                        costs = getExecuteTxLine(tx, getNode(tx.getHome_node(), line), line);
                                    } else if (graph_type == 2) {
                                        costs = getExecuteTxClique(tx, getNode(tx.getHome_node(), clique), clique);
                                    } else if (graph_type == 3) {
                                        costs = executeTxGridOnline(tx, getNode(tx.getHome_node(), grid), grid);
                                    } else if (graph_type == 4) {
                                        costs = getExecuteTxCluster(tx, getNode(tx.getHome_node(), cluster), cluster, cluster_size);
                                    } else if (graph_type == 5) {
                                        costs = getExecuteTxStar(tx, getNode(tx.getHome_node(), star), star, ray_nodes);
                                    }
                                    tx.setExecution_time(costs[0]);
                                    tx.setComm_cost(costs[1]);
                                    tx.setWaited_for(0);
                                    tx.setWaiting_time(timestep - 1 - tx.getArrived_at());
                                    tx.setStatus("RUNNING");
                                    running_txs.add(tx);
//                                    updateTxsList(txs, tx);
                                }
                            }
                        }
                        else{
                            System.out.println("Error. IS size 0.");
                        }
                    }
                    for (Transaction tx : ready_txs) {
                        if(!running_txs.contains(tx)){
                            if(!waiting_txs.contains(tx)){
                                waiting_txs.add(tx);
                            }
                        }
                    }
                    System.out.print(prev_run_list.size()+", ");

//                    System.out.println("running size: " + running_txs.size());
                    for (int i = 0; i < running_txs.size(); i++) {
                        Transaction tx = running_txs.get(i);
                        int exec_until_now = tx.getWaited_for();
                        tx.setWaited_for(exec_until_now + 1);
                        if (tx.getExecution_time() == exec_until_now + 1) {
//                            System.out.println("T" + tx.getTx_id() + " commits: exec cost => " + timestep + "  comm cost => " + tx.getComm_cost());
                            curr_commit_list_size++;
                            tot_comm_cost += tx.getComm_cost();
                            ready_list[tx.getHome_node()] = 0;
//                            prev_run_list.remove(new Integer(tx.getTx_id()));

                            List<Objects> ws = new ArrayList<>();
                            for (int j = 0; j < tx.getWset().size(); j++) {
                                Objects obj = tx.getWset().get(j);
//                                obj.setNode(getNode(tx.getTx_id(), grid).getNode_id());
                                obj.setNode(tx.getHome_node());
                                objs.set(obj.getObj_id(), obj);
                                ws.add(obj);
                            }
                            tx.setWset(ws);
                            tx.setStatus("COMMITTED"+ready_count[tx.getHome_node()]);
                            committed_txs.add(tx);
                            commit_list.add(tx.getTx_id());
                            updateTxsList(txs,tx);
                        } else if (tx.getExecution_time() == 0) {
//                            System.out.println("T" + tx.getTx_id() + " commits: exec cost => " + timestep + "  comm cost => " + tx.getComm_cost());
                            curr_commit_list_size++;
                            tot_comm_cost += tx.getComm_cost();
                            ready_list[tx.getHome_node()] = 0;
//                            prev_run_list.remove(new Integer(tx.getTx_id()));

                            List<Objects> ws = new ArrayList<>();
                            for (int j = 0; j < tx.getWset().size(); j++) {
                                Objects obj = tx.getWset().get(j);
//                                obj.setNode(getNode(tx.getTx_id(), grid).getNode_id());
                                obj.setNode(tx.getHome_node());
                                objs.set(obj.getObj_id(), obj);
                                ws.add(obj);
                            }
                            tx.setWset(ws);
                            tx.setStatus("COMMITTED"+ready_count[tx.getHome_node()]);
                            committed_txs.add(tx);
                            commit_list.add(tx.getTx_id());
                            updateTxsList(txs,tx);
                        }
                    }
                    if (curr_commit_list_size > prev_commit_list_size) {
                        for (int i = 0; i < curr_commit_list_size - prev_commit_list_size; i++) {
                            running_txs = removeTx(running_txs, commit_list.get(commit_list.size() - i - 1));
                            ready_txs = removeTx(ready_txs, commit_list.get(commit_list.size() - i - 1));
                            prev_run_list.remove(new Integer(commit_list.get(commit_list.size() - i - 1)));
//                            running_txs.remove(committed_txs.get(committed_txs.size() - i - 1));
//                            ready_txs.remove(committed_txs.get(committed_txs.size() - i - 1));
                        }
                    }
                    prev_commit_list_size = curr_commit_list_size;
                /*System.out.println("Total Commits:");
                    for(int i = 0; i < commit_list.size(); i++){
                        System.out.print(commit_list.get(i)+ " ");
                    }
                System.out.println("Time:"+timestep);*/
//                }
                }
                ArrayList<Integer> cstlst = new ArrayList<>();
                cstlst.add(timestep);
                cstlst.add(tot_comm_cost);

                if(z == 0){
                    System.out.println("OFFLINE.");
                }
                else if (z == 1){
                    System.out.println("ONLINE (NO SCHEDULE).");
                }
                else{
                    System.out.println("ONLINE (INNER SCHEDULE).");
                }
                System.out.println("Total Execution cost = " + timestep);
                System.out.println("Total Communication cost = " + tot_comm_cost);
//                System.out.println("Total Objects = "+objts.size());

                System.out.println("\n\nNode \tCommits \tTransaction \t Arrived at \t Wating Time \t Execution Time \t Communication Cost");
                for (int i = 0; i < committed_txs.size(); i++) {
                    System.out.println("N"+committed_txs.get(i).getHome_node()+" \t\t"+committed_txs.get(i).getStatus().replace("COMMITTED", "")+"\t   T" + committed_txs.get(i).getTx_id() + "\t\t\t\t  " + committed_txs.get(i).getArrived_at() + " \t\t\t  " + committed_txs.get(i).getWaiting_time() + "  \t\t\t\t  " + committed_txs.get(i).getExecution_time() + "  \t\t\t\t  " + committed_txs.get(i).getComm_cost());
                    tot_wait_online += committed_txs.get(i).getWaiting_time();
                    tot_conflicts_online += committed_txs.get(i).getConflicts();
                }
                System.out.println("Total committed txs = " + committed_txs.size());
                int test = new Scanner(System.in).nextInt();
                cstlst.add(tot_wait_online);
                cstlst.add(tot_conflicts_online);
                costsArray.add(cstlst);
                System.out.println(txs.get(0).getTx_id()+" - "+txs.get(0).getWset().get(0).getNode()+" - "+txs_pool1.get(0).getWset().get(0).getNode());
                objs.clear();
                txs.clear();
//                objs = new ArrayList<>(objts);
//                objs =  (ArrayList<Objects>) objts.clone();
                int ind = 0;
                while(ind < objts.size()){
                    int objid = objts.get(ind).getObj_id();
                    int objsize = objts.get(ind).getObj_size();
                    int objnode = objts.get(ind).getNode();
                    Objects o = new Objects(objid, objsize, objnode);
                    objs.add(o);
                    ind++;
                }
                ind = 0;
                while(ind < txs_pool1.size()){
                    List<Objects> ws = new ArrayList<>();
                    List<Objects> rs = new ArrayList<>();
                    for(int j = 0; j < txs_pool1.get(ind).getRset().size(); j++){
                        int objid = txs_pool1.get(ind).getRset().get(j).getObj_id();
                        int objsize = txs_pool1.get(ind).getRset().get(j).getObj_size();
                        int objnode = txs_pool1.get(ind).getRset().get(j).getNode();
                        Objects o = new Objects(objid, objsize, objnode);
                        rs.add(o);
                    }
                    for(int j = 0; j < txs_pool1.get(ind).getWset().size(); j++){
                        int objid = txs_pool1.get(ind).getWset().get(j).getObj_id();
                        int objsize = txs_pool1.get(ind).getWset().get(j).getObj_size();
                        int objnode = txs_pool1.get(ind).getWset().get(j).getNode();
                        Objects o = new Objects(objid, objsize, objnode);
                        ws.add(o);
                    }
                    Transaction t = new Transaction(txs_pool1.get(ind).getTx_id(),txs_pool1.get(ind).getRw_set_size(), txs_pool1.get(ind).getUpdate_rate(),
                            rs, ws, txs_pool1.get(ind).getStatus(), txs_pool1.get(ind).getExecution_time(), txs_pool1.get(ind).getWaited_for(),
                            txs_pool1.get(ind).getConflicts(), txs_pool1.get(ind).getComm_cost());
                    txs.add(t);
                    ind++;
                }
//                objs = objts;
//                txs = txs_pool1;
                ready_txs.clear();
                committed_txs.clear();
                running_txs.clear();
                prev_run_list.clear();
            }
            System.out.println("Done.");
            System.out.println("\nTotal Transactions: \t"+tottxs+"\n\n-------------------------------------------------------------------------------------------");
            System.out.println("Cases\t\t\t\tExecution Time\tCommunication Cost \t  Waiting Time \tTotal Conflicts\n------------------------------------------------------------------------------------------");
            System.out.println("OPTIMAL: \t\t\t\t "+optimal_rt +"  \t\t\t " + optimal_comcost + " \t\t\t\t   0   \t\t      " + optimal_conflicts);
//            System.out.println("OFFLINE: \t\t\t\t "+totexectime +" \t\t\t  " + totcommcost + " \t\t\t  " + tot_waittime + " \t\t  " + tot_conflicts);
            for(int i = 0; i < costsArray.size(); i++){
                if(i == 0)
                    System.out.println("OFFLINE: \t\t\t\t "+costsArray.get(i).get(0)+"\t\t\t "+costsArray.get(i).get(1)+"  \t\t\t  "+costsArray.get(i).get(2) +"  \t\t  "+costsArray.get(i).get(3));
                else if(i == 1)
                    System.out.println("ONLINE (No Schedule): \t "+costsArray.get(i).get(0)+"\t\t\t "+costsArray.get(i).get(1)+"  \t\t\t  "+costsArray.get(i).get(2) +"  \t\t  "+costsArray.get(i).get(3));
                else
                    System.out.println("ONLINE (Inner Schedule): "+costsArray.get(i).get(0)+"\t\t\t "+costsArray.get(i).get(1)+"  \t\t\t  "+costsArray.get(i).get(2) +"  \t\t  "+costsArray.get(i).get(3));
            }

            /*
            for (int x = 0; x < total_nodes; x++) {
                txs = generateTxsOnline(objList, total_txs, update_rate);
                nodal_txs.add(txs);
            }*/

            /*System.out.println("\n---------------------------------\nNodes vs. Transactions\n---------------------------------");
            for (int i = 0; i < total_nodes; i++) {
                System.out.print("N" + (i + 1) + "  \tT" + nodal_txs.get(i).get(0).getTx_id() + "   \t" + nodal_txs.get(i).get(0).getRw_set_size() + "\t\t" + nodal_txs.get(i).get(0).getUpdate_rate() + "\t\tRead Set(Objects) ==> (");
                for (int j = 0; j < nodal_txs.get(i).get(0).getRset().size(); j++) {
//                System.out.print("o"+txs.get(i).getRset().get(j).getObj_id()+":"+txs.get(i).getRset().get(j).getObj_size()+" ");
                    System.out.print(nodal_txs.get(i).get(0).getRset().get(j).getObj_id());
                    if (j < nodal_txs.get(i).get(0).getRset().size() - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.print(")\n\t\t\t\t\t\t\t\tWrite Set(Objects) ==> (");
                for (int j = 0; j < nodal_txs.get(i).get(0).getWset().size(); j++) {
                    System.out.print(nodal_txs.get(i).get(0).getWset().get(j).getObj_id());
                    if (j < nodal_txs.get(i).get(0).getWset().size() - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.print(")\n");
            }*/


            /*
            Graphs grid = new Graphs();
            Graphs clique = new Graphs();
            Graphs line = new Graphs();
            Graphs star = new Graphs();
            Graphs cluster = new Graphs();
            if (graph_type == 1) {
                line = Graphs.generateLineGraph(total_nodes);
                ArrayList<ArrayList<Integer>> components = generateComponents(nodal_txs, total_nodes, 0);

                System.out.println("Components of the conflict graph:\n");
                for (int i = 0; i < components.size(); i++) {
                    System.out.print("C" + (i + 1) + ":\t(");
                    for (int j = 0; j < components.get(i).size(); j++) {
                        System.out.print(components.get(i).get(j) + " ");
                    }
                    System.out.print(")\n");
                }

                generatePriorityQueueLine(total_nodes, total_txs-1);
                generatePriorityQueueOnline(components, priority_queue);
                System.out.println("Priority queue:");
                for (int i = 0; i < total_nodes; i++) {
                    System.out.print(priority_queue[i] + " ");
                }
                executeLine(line);
            } else if (graph_type == 2) {
                clique = Graphs.generateCliqueGraph(total_nodes);
                ArrayList<ArrayList<Integer>> components = generateComponents(nodal_txs, total_nodes, 0);

                System.out.println("Components of the conflict graph:\n");
                for (int i = 0; i < components.size(); i++) {
                    System.out.print("C" + (i + 1) + ":\t(");
                    for (int j = 0; j < components.get(i).size(); j++) {
                        System.out.print(components.get(i).get(j) + " ");
                    }
                    System.out.print(")\n");
                }

                generatePriorityQueueClique(total_nodes);
                generatePriorityQueueOnline(components,priority_queue);
                System.out.println("Priority queue:");
                for (int i = 0; i < total_nodes; i++) {
                    System.out.print(priority_queue[i] + " ");
                }
                executeClique(clique);
            } else if (graph_type == 3) {
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

//                System.out.println("\n-----------------------------------------------\nComponents of Transaction Conflict Graph\n-----------------------------------------------");
                ArrayList<ArrayList<Integer>> components = generateComponents(nodal_txs, total_nodes, 0);

                System.out.println("Components of the conflict graph:\n");
                for (int i = 0; i < components.size(); i++) {
                    System.out.print("C" + (i + 1) + ":\t(");
                    for (int j = 0; j < components.get(i).size(); j++) {
                        System.out.print(components.get(i).get(j) + " ");
                    }
                    System.out.print(")\n");
                }

                generatePriorityQueueOnlineGrid(components);
                System.out.println("Priority queue:");
                for (int i = 0; i < total_nodes; i++) {
                    System.out.print(priority_queue[i] + " ");
                }

                System.out.println("\n-----------------------------------------------\nTransaction Dependency Graph\n-----------------------------------------------");
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
            else if (graph_type == 4) {
                cluster = Graphs.generateClusterGraph(total_nodes, subgraph_cluster, cluster_size);
//                System.out.println("\n-----------------------------------------------\nComponents of Transaction Conflict Graph\n-----------------------------------------------");
                ArrayList<ArrayList<Integer>> components = generateComponents(nodal_txs, total_nodes, 0);

                System.out.println("Components of the conflict graph:\n");
                for (int i = 0; i < components.size(); i++) {
                    System.out.print("C" + (i + 1) + ":\t(");
                    for (int j = 0; j < components.get(i).size(); j++) {
                        System.out.print(components.get(i).get(j) + " ");
                    }
                    System.out.print(")\n");
                }

                generatePriorityQueueCluster(subgraph_cluster, cluster_size);
                generatePriorityQueueOnline(components, priority_queue);
                System.out.println("Priority queue:");
                for (int i = 0; i < total_nodes; i++) {
                    System.out.print(priority_queue[i] + " ");
                }

                System.out.println("\n-----------------------------------------------\nTransaction Dependency Graph\n-----------------------------------------------");
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
                executeCluster(cluster, cluster_size);
            }
            else if (graph_type == 5) {
                star = Graphs.generateStarGraph(total_nodes, subgraph_star, ray_nodes);
                ArrayList<ArrayList<Integer>> components = generateComponents(nodal_txs, total_nodes, 0);

                System.out.println("Components of the conflict graph:\n");
                for (int i = 0; i < components.size(); i++) {
                    System.out.print("C" + (i + 1) + ":\t(");
                    for (int j = 0; j < components.get(i).size(); j++) {
                        System.out.print(components.get(i).get(j) + " ");
                    }
                    System.out.print(")\n");
                }

                generatePriorityQueueStar(subgraph_star, ray_nodes);
                generatePriorityQueueOnline(components, priority_queue);
                System.out.println("Priority queue:");
                for (int i = 0; i < total_nodes; i++) {
                    System.out.print(priority_queue[i] + " ");
                }
                executeStar(star, ray_nodes);
            }

            System.out.println("Total execution time for each node\nNode\tRW Set\tRSET\tWSET\tCONFLICTS\tExec time\tComm Cost");
            for (int i = 0; i < total_nodes; i++) {
                int exec_time = 0, rwsetsize = 0, rset = 0, wset = 0, conflict = 0, commcost = 0;
                for (int j = 0; j < total_txs; j++) {
                    exec_time += nodal_txs.get(i).get(j).getExecution_time();
                    rwsetsize += nodal_txs.get(i).get(j).getRw_set_size();
                    rset += nodal_txs.get(i).get(j).getRset().size();
                    wset += nodal_txs.get(i).get(j).getWset().size();
                    conflict += nodal_txs.get(i).get(j).getConflicts();
//                if(nodal_txs.get(i).get(j).getComm_cost() > commcost) {
                    commcost += nodal_txs.get(i).get(j).getComm_cost();
//                }
                }
                System.out.println("N" + i + "\t: \t  " + rwsetsize + "\t" + rset + "\t\t" + wset + "\t\t\t" + conflict + "\t\t  " + exec_time + "\t\t\t " + commcost);
            }

            System.out.println("Based on priority:");
            for (int i = 0; i < total_nodes; i++) {
                int exec_time = 0, rwsetsize = 0, rset = 0, wset = 0, conflict = 0, commcost = 0;
                for (int j = 0; j < total_txs; j++) {
                    exec_time += nodal_txs.get(priority_queue[i]).get(j).getExecution_time();
                    rwsetsize += nodal_txs.get(priority_queue[i]).get(j).getRw_set_size();
                    rset += nodal_txs.get(priority_queue[i]).get(j).getRset().size();
                    wset += nodal_txs.get(priority_queue[i]).get(j).getWset().size();
                    conflict += nodal_txs.get(priority_queue[i]).get(j).getConflicts();
//                if(nodal_txs.get(priority_queue[i]).get(j).getComm_cost() > commcost) {
                    commcost += nodal_txs.get(priority_queue[i]).get(j).getComm_cost();
//                }
                }
                System.out.println("N" + priority_queue[i] + "\t" + rwsetsize + "\t" + rset + "\t" + wset + "\t" + conflict + "\t" + exec_time + "\t" + commcost);
            }

*/
            /*System.out.print("Total Nodes (approx.): ");
            total_nodes = reader.nextInt();
            Random rand = new Random();
            *//* Select graph type randomly *//*
            int g_type = rand.nextInt(5) + 1;

            Graphs line = new Graphs();
            Graphs clique = new Graphs();
            Graphs grid = new Graphs();
            Graphs cluster = new Graphs();
            Graphs star = new Graphs();

            if(g_type == 1){
                line = Graphs.generateLineGraph(total_nodes);
                executeLine(line);
            }
            else if(g_type == 2){
                clique = Graphs.generateCliqueGraph(total_nodes);

            }
            else if(g_type == 3){

                int gridsize = (int) Math.sqrt(total_nodes) * (int) Math.sqrt(total_nodes);
                grid = Graphs.generateGridGraph(gridsize);
            }*/
        }
        /*else{
            System.out.println("Invalid option.");
            System.exit(1);
        }*/
    }
}