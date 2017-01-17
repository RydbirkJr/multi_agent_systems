package map;

/**
 * Created by Anders on 15/03/16.
 */
public class MinDistance {
    public final int row, col, d;
    public final MinDistance prev;

    public MinDistance(int row, int col, int d, MinDistance prev){
        this.row = row;
        this.col = col;
        this.d = d;
        this.prev = prev;
    }

    public String toString(){
        return "Min(" + row + "," + col + ")";
    }
}
