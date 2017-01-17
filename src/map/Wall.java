package map;

public class Wall extends Square {

	public Wall(int row, int col){
		super(row,col);
	}

	@Override
	public Boolean isPassable() {
		return false;
	}

}
