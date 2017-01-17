package messages;

import core.State;

/**
 * Created by Anders on 13/05/16.
 */
public class NoNewStatesMessage {

    public final int agentNumber;
    public final State prevState;

    public NoNewStatesMessage(int agentNumber, State prevState){
        this.agentNumber = agentNumber;
        this.prevState = prevState;
    }
}
