package actions;

import core.Box;
import core.State;
import planning.ClearPathGoal;
import planning.Goal;

import java.util.List;

/**
 * Created by Anders on 12/05/16.
 */
public class PlanClearPath extends TopLevelAction {

    private final ClearPathGoal goal;
    private final Goal continueTowards;

    public PlanClearPath(State state, ClearPathGoal goal, Goal continueTowards) {
        super(state);
        this.goal = goal;
        this.continueTowards = continueTowards;
    }

    public Goal getContinueTowards(){
        return continueTowards;
    }

    public List<Box> getBoxes(){
        return goal.getBoxes();
    }

    @Override
    public String toString() {
        return "PlanClearPath(" +
                 goal +
                ')';
    }

    @Override
    public Goal getGoal() {
        return this.goal;
    }
}
