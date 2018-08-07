import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Pavan Poudel
 * Date - 2018/08/03
 */
public class Main {

    private static int total_objs = 100;
    private static int total_txs = 100;
    private static int update_rate = 20;
    private static int rwset_size = 256;
    private static ArrayList<objects> objs = new ArrayList<objects>(total_objs);
    private static ArrayList<transaction> txs = new ArrayList<transaction>(total_txs);

    public static void main(String[] args) {
//        System.out.println("Hello World!");

        for(int i=0;i<total_objs;i++){
            Random rand = new Random();
            int objsize = rand.nextInt(4)+1;
            if(objsize > 2){
                objsize = 4;
            }
            objects obj = new objects(i+1, objsize);

            objs.add(obj);
        }

        for(int i=0;i<total_txs;i++){
            List<objects> ws = new ArrayList<objects>();
            List<objects> rs = new ArrayList<objects>();
            int ws_size = rwset_size * update_rate/800;
            int rs_size = (rwset_size/8) - ws_size;
//            System.out.println("ws = "+ws_size+" rs = "+rs_size);
            List<objects> rwset = setRWSet(rwset_size,total_objs);
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

            List<objects> rset = rs;
            List<objects> wset = ws;
            transaction tx = new transaction(i+1, rwset_size, update_rate,rset,wset);

            txs.add(tx);
        }

        System.out.println("\n*** ----------------------------- ***\n");
        System.out.println("Tx \tRW Set \tUpdate Rate");
        System.out.println("---------------------------------");
        for(int i=0;i<total_txs;i++){
            System.out.print("T"+txs.get(i).getTx_id()+"\t"+txs.get(i).getRw_set_size()+"\t\t"+txs.get(i).getUpdate_rate()+"\t\tRead Set(Obj:len) ==> ");
            for(int j=0;j<txs.get(i).getRset().size();j++){
                System.out.print("o"+txs.get(i).getRset().get(j).getObj_id()+":"+txs.get(i).getRset().get(j).getObj_size()+" ");
            }
            System.out.print("\n\t\t\t\t\tWrite Set(Obj:len) ==> ");
            for(int j=0;j<txs.get(i).getWset().size();j++){
                System.out.print("o"+txs.get(i).getWset().get(j).getObj_id()+":"+txs.get(i).getWset().get(j).getObj_size()+" ");
            }
            System.out.println("");
        }
    }

    private static List<objects> setRWSet(int rw_set_size, int total_objs){
        List<objects> rw = new ArrayList<objects>();
        int rw_size = rw_set_size/8;
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

    /*private static List<objects> getRWSet(int update_rate, int rw_set_size, List<objects> rwset, int rw){
        List<objects> ws = new ArrayList<objects>();
        List<objects> rs = new ArrayList<objects>();
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

    private static List<objects> getWriteSet(int write_rate, int rw_set_size, int total_objs){
        List<objects> ws = new ArrayList<objects>();
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
