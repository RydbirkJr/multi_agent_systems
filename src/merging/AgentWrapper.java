package merging;

import core.Agent;
import enums.Direction;

/**
 * Created by Anders on 05/05/16.
 */
public class AgentWrapper {

    private Agent agent;
    private int time;

    public AgentWrapper(Agent agent, int time){
        this.agent = agent;
        this.time = time;
    }

    public AgentWrapper(AgentWrapper oldAgent){
        this.agent = oldAgent.getAgent();
        this.time = oldAgent.getTime();
    }

    public void moveAgent(int row, int col){
        agent = new Agent(row, col, agent.getNumber(), agent.getColor());
    }

    public void incrementTime(){
        time++;
    }

    public int getTime(){
        return time;
    }

    public Agent getAgent(){
        return agent;
    }

    public String toString(){
        return "Wrapper(agent: " + agent + ", time: " + time + ")";
    }

}
