import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * @author Pavan Poudel
 * Date - 2018/08/03
 */
public class Main {

    private static int total_objs = 128;
    private static int total_txs = 100;
    private static int update_rate = 20;
    private static int rwset_size = 16;
    private static ArrayList<Objects> objs = new ArrayList<Objects>(total_objs);
    private static ArrayList<Transaction> txs = new ArrayList<Transaction>(total_txs);

    /* Read-Write set for a transaction is randomly calculated */
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
            /*while (randList.size() < rwset.size()) {
                int a = rand.nextInt(rwset.size());
                if (!randList.contains(a)) {
                    randList.add(a);
                }
            }*/

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

    /* Read-Write set for a transaction is fixed */
    public static ArrayList<Transaction> generateTransactions(int tot_obj, int tot_tx, int updt_rate, int rws_size){
        ArrayList<Transaction> txs = new ArrayList<Transaction>(tot_tx);

        for(int i=0;i<tot_tx;i++) {
            List<Objects> ws = new ArrayList<Objects>();
            List<Objects> rs = new ArrayList<Objects>();

            Random rand = new Random();
//            int rws_size = rand.nextInt(tot_obj);
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
            txs = generateTransactions(total_objs, total_txs, update_rate,rwset_size);
        }
        else if(option == 2){
            txs = generateTransactions(total_objs,total_txs,update_rate);
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
//            System.out.println("ws = "+ws_size+" rs = "+rs_size);
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
                System.out.print(txs.get(i).getRset().get(j).getObj_id()+", ");
            }
            System.out.print(")\n\t\t\t\t\t\tWrite Set(Objects) ==> (");
            for(int j=0;j<txs.get(i).getWset().size();j++){
                System.out.print(txs.get(i).getWset().get(j).getObj_id()+", ");
            }
            System.out.print(")\n");
        }
    }

    private static List<Objects> setRWSet(int rw_size, int total_objs){
        List<Objects> rw = new ArrayList<Objects>();
        //int rw_size = rw_set_size/8;
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

    /*private static List<Objects> getRWSet(int update_rate, int rw_set_size, List<Objects> rwset, int rw){
        List<Objects> ws = new ArrayList<Objects>();
        List<Objects> rs = new ArrayList<Objects>();
        int ws_size = rw_set_size * update_rate/800;
        int rs_size = (rw_set_size/8) - ws_size;
        int n = 0, sum =0;
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
                sum = sum + objs.get(randList.get(n)).getObj_size();
            }
            else{
                rs.add(rwset.get(randList.get(n)));
            }
            n++;
        }
        if(rw == 0) {
            return rs;
        }
        else{
            return  ws;
        }
    }

    private static List<Objects> getWriteSet(int write_rate, int rw_set_size, int total_objs){
        List<Objects> ws = new ArrayList<Objects>();
        int ws_size = rw_set_size * update_rate/800;
        int sum = 0;
        //Random rand = new Random();
        while (sum<ws_size) {
            Random rand = new Random();
            int x = rand.nextInt(total_objs);
            //if((sum + objs.get(x).getObj_size())<ws_size){
                ws.add(objs.get(x));
                sum = sum + objs.get(x).getObj_size();
            //}
        }
        return ws;
    }*/
}
