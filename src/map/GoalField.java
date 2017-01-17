package map;

import enums.Color;

public class GoalField extends Square {

	private char letter;
	private Color color;
	
	public GoalField(int row, int col, char letter, Color color){
		super(row,col);
		this.letter = letter;
		this.color = color;
	}
	
	@Override
	public Boolean isPassable() {
		return true;
	}

	public char getLetter(){
		return this.letter;
	}
	
	public Color getColor(){
		return this.color;
	}
}
