package messages;

import core.State;

/**
 * Created by Anders on 13/05/16.
 */
public class StillNotWorkingMessage {

    public final int agentNumber;
    public final State state;
    public final boolean tooManyTriesOnSameGoal;

    public StillNotWorkingMessage(int agentNumber, State state, boolean tooManyTriesOnSameGoal){
        this.agentNumber = agentNumber;
        this.state = state;
        this.tooManyTriesOnSameGoal = tooManyTriesOnSameGoal;
    }
}
