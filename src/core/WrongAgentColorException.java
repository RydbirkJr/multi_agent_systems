package core;

import planning.AgentGoal;
import planning.ClearPathGoal;

import java.util.List;

/**
 * Created by Anders on 11/05/16.
 */
public class WrongAgentColorException extends  Exception{

    public final List<ClearPathGoal> boxGoals;
    public final List<AgentGoal> agentGoals;

    public WrongAgentColorException(List<ClearPathGoal> boxGoals, List<AgentGoal> agentGoals){
        this.boxGoals = boxGoals;
        this.agentGoals = agentGoals;
    }
}
