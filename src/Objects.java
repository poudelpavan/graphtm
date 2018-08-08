/**
 * @author Pavan Poudel
 * Date - 2018/08/03
 */
public class Objects {
    private int obj_id;
    private int obj_size;
    public Node node;

    public Objects(int obj_id, int obj_size) {
        this.obj_id = obj_id;
        this.obj_size = obj_size;
    }

    public int getObj_id() {
        return obj_id;
    }

    public void setObj_id(int obj_id) {
        this.obj_id = obj_id;
    }

    public int getObj_size() {
        return obj_size;
    }

    public void setObj_size(int obj_size) {
        this.obj_size = obj_size;
    }
}
