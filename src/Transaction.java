import java.util.List;

/**
 * @author Pavan Poudel
 * Date - 2018/08/03
 */
public class Transaction {
    private int tx_id;
    private int rw_set_size;
    private int update_rate;
    private List<Objects> rset;
    private List<Objects> wset;

    public Transaction(int tx_id, int rw_set_size, int update_rate, List<Objects> rset, List<Objects> wset) {
        this.tx_id = tx_id;
        this.rw_set_size = rw_set_size;
        this.update_rate = update_rate;
        this.rset = rset;
        this.wset = wset;
    }

    public int getTx_id() {
        return tx_id;
    }

    public void setTx_id(int tx_id) {
        this.tx_id = tx_id;
    }

    public int getRw_set_size() {
        return rw_set_size;
    }

    public void setRw_set_size(int rw_set_size) {
        this.rw_set_size = rw_set_size;
    }

    public int getUpdate_rate() {
        return update_rate;
    }

    public void setUpdate_rate(int rw_ratio) {
        this.update_rate = rw_ratio;
    }

    public List<Objects> getRset() {
        return rset;
    }

    public void setRset(List<Objects> rset) {
        this.rset = rset;
    }

    public List<Objects> getWset() {
        return wset;
    }

    public void setWset(List<Objects> wset) {
        this.wset = wset;
    }
}