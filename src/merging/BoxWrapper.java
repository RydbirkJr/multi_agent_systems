package merging;

import core.Box;

/**
 * Created by Anders on 05/05/16.
 */
public class BoxWrapper {

    private Box box;
    private int lastMove;

    public BoxWrapper(Box box, int lastMove){
        this.box = box;
        this.lastMove = lastMove;
    }

    @Override
    public int hashCode() {
        return box.getID();
    }

    public int getLastMove(){
        return lastMove;
    }

    public Box getBox(){
        return box;
    }

    public void updateLastMove(int time){
        this.lastMove = time;
    }

    public void updateBox(int row, int col){
        this.box = new Box(box, row, col);
    }
}
