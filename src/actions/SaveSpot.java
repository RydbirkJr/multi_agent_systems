package actions;

import core.Agent;
import core.Box;
import map.Field;
import map.Level;
import map.Square;

public class SaveSpot extends Action{
	
	Agent agent;
	Box box;
	Square square;
	
	public SaveSpot(Action parent, Agent agent, Box box, Square saveSquare){
		super(parent);
		this.agent = agent;
		this.box = box;
		this.square = saveSquare;
	}

	public Agent getAgent() {
		return agent;
	}

	public Box getBox() {
		return box;
	}

	public Square getSquare() {
		return square;
	}

	public String toString(){
		return "SaveSpot";
	}
}
