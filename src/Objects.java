/**
 * @author Pavan Poudel
 * Date - 2018/08/03
 */
import java.util.ArrayList;
public class Objects implements Cloneable{
    private int obj_id;
    private int obj_size;
    public int node;

    public Objects(int obj_id, int obj_size, int node) {
        this.obj_id = obj_id;
        this.obj_size = obj_size;
        this.node = node;
    }

    public int getObj_id() {
        return obj_id;
    }

    public Objects() {
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

    public int getNode() {
        return node;
    }

    public void setNode(int node) {
        this.node = node;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Objects clone = null;
        try
        {
            clone = (Objects) super.clone();

            //Copy new date object to cloned method
            clone.setObj_id((int) this.getObj_id());
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
        return clone;
    }
}
