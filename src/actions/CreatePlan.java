package actions;

import core.Box;
import core.State;
import planning.Goal;
import planning.MoveGoal;

public class CreatePlan extends TopLevelAction {
	
	private MoveGoal goal;
	private Goal continueTowards;

	public CreatePlan(State state, MoveGoal goal, Goal continueTowards){
		super(state);
		this.goal = goal;
		this.continueTowards = continueTowards;
	}

	public Goal getContinueTowards(){
		return continueTowards;
	}

	public Goal getGoal() {
		return goal;
	}

	public Box getBox() {
		return getPreState().getBox(goal.getBox().getID());
	}

	public String toString(){
		String cont = continueTowards != null ? ", continue: " + continueTowards.toString() : "";
		return "CreatePlan('" + goal.getGoalField() + "', box: " + getBox().toString()  + ", agent: " + preState.getAgent().toString() + cont + ")";
	}

}
