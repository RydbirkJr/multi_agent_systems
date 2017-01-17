package map;

public class Field extends Square {

    public Field(int row, int col){
        super(row,col);
    }

	@Override
	public Boolean isPassable() {
		return true;
	}
}
