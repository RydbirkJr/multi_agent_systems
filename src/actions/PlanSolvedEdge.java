package actions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.Box;
import core.State;
import map.Edge;
import map.Square;
import planning.ClearPathGoal;
import planning.EdgeGoal;
import planning.Goal;

public class PlanSolvedEdge extends TopLevelAction {
	private final EdgeGoal edgeGoal;
    private final Goal continueTowards;

	public PlanSolvedEdge(State state, EdgeGoal edgeGoal, Goal continueTowards) {
		super(state);
		this.edgeGoal = edgeGoal;
		this.continueTowards = continueTowards;
	}
	
	public Goal getContinueTowards(){
        return continueTowards;
    }
	
	public Goal getGoal() {
        return this.edgeGoal;
    }
	
	@Override
	public String toString() {
		return "PlanSolvedEdgePlan(" +
                edgeGoal +
               ')';
	}
	
	public Edge getEdge()
	{
		return edgeGoal.edge;
	}
	
	public Set<Square> getResources()
	{
		return edgeGoal.edge.getResources();
	}

}
