package messages;

import core.Agent;

import java.util.List;

/**
 * Created by Anders on 08/05/16.
 */
public class GetInitMessage {
    public final List<Agent> agents;

    public GetInitMessage(List<Agent> agents){
        this.agents = agents;
    }
}
