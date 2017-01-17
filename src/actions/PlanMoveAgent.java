package actions;

import core.State;
import map.Square;
import planning.AgentGoal;
import planning.Goal;

import java.util.Set;

/**
 * Created by Anders on 13/05/16.
 */
public class PlanMoveAgent extends TopLevelAction {

    private final AgentGoal goal;
    private final Goal continueTowards;

    public PlanMoveAgent(State state, AgentGoal goal, Goal continueTowards) {
        super(state);
        this.goal = goal;
        this.continueTowards = continueTowards;
    }

    @Override
    public Goal getGoal() {
        return this.goal;
    }

    @Override
    public String toString() {
        return "PlanMoveAgent(" + preState.getAgent() + ")";
    }

    public Goal getContinueTowards(){
        return continueTowards;
    }

    public Set<Square> getResources(){
        return goal.blockedResources;
    }
}
