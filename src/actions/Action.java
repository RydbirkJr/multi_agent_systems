package actions;

//import core.Agent;

import java.util.ArrayList;
import java.util.List;

import core.Logger;
import core.State;

public abstract class Action {

	boolean isFirstChild = false;
	Action prev;
	List<Action> children;
	State preState = null;
	State postState = null;

	/**
	 * Creates an action and stores the hierarchy of all the actions in the topLevelAction.
	 * @param parent
	 */
	public Action(Action parent){
		this.children = new ArrayList<>();
		if(parent != null){
			parent.addChild(this);
		}
	}

	public Action(State state)
	{
		this.children = new ArrayList<>();
		this.preState = state;
	}

	public void addChild(Action child)
	{
		if(children.isEmpty())
		{
			child.prev = this;
			child.isFirstChild = true;
		}
		else
		{
			child.prev = children.get(children.size() - 1);
		}
		children.add(child);
	}
	
	public State getPreState()
	{
		//When Top Level is reached
		if(preState != null)
		{
			return preState;
		}
		//When this action is first child: get prestate for parent
		else if(this.isFirstChild)
		{
			return prev.getPreState();
		}
		//If this is NOT first child, get previous effect
		else if(!this.isFirstChild) {
			return prev.getEffect();
		}

		new Logger("Action").error("Found error in getPreState");
		return null;
	}
	public State getEffect()
	{
		if(!children.isEmpty()){
			return children.get(children.size() - 1).getEffect();
		}

		return postState;
	}
	
	public void setEffect(State state)
	{
		this.postState = state;
	}

	public List<Action> getChildren(){
		return this.children;
	}
	
//	public abstract boolean preconditions();

	public abstract String toString();
}
