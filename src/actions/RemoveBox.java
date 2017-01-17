package actions;

import core.Agent;
import core.Box;

public class RemoveBox extends Action{
	
	Agent agent;
	Box box;
	
	public RemoveBox(Action parent, Agent agent, Box box){
		super(parent);
		this.agent = agent;
		this.box = box;
	}

	public Agent getAgent() {
		return agent;
	}

	public Box getBox() {
		return box;
	}

	public String toString(){
		return "RemoveBox";
	}

}
