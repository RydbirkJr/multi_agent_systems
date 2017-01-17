package core;

import actions.TopLevelAction;
import planning.AgentGoal;
import planning.ClearPathGoal;

import java.util.List;

/**
 * Created by Anders on 12/05/16.
 */
public class WrongColorPlan extends Result {

    public final List<ClearPathGoal> boxGoals;
    public final List<AgentGoal> agentGoals;

    public WrongColorPlan(TopLevelAction plan, List<ClearPathGoal> boxGoals, List<AgentGoal> agentGoals) {
        super(plan);
        this.boxGoals = boxGoals;
        this.agentGoals = agentGoals;
    }
}
