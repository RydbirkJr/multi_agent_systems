package core;

import merging.AgentWrapper;

/**
 * Created by Anders on 14/05/16.
 */
public class CouldNotMoveAgentException extends Exception{

    public final AgentWrapper wrapper;

    public CouldNotMoveAgentException(AgentWrapper wrapper){
        this.wrapper = wrapper;
    }
}
