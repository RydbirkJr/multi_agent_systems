package messages;

import core.Agent;
import merging.AgentWrapper;
import merging.BoxStructure;
import merging.ResourceStructure;

import java.util.List;

/**
 * Created by Anders on 08/05/16.
 */
public class InitMessage {

    public final BoxStructure boxes;
    public final ResourceStructure resources;
    public final List<Agent> agents;
    public final AgentWrapper[] agentsWrappers;

    public InitMessage(BoxStructure boxes, ResourceStructure resources, List<Agent> agents, AgentWrapper[] agentsWrappers){
        this.boxes = boxes;
        this.resources = resources;
        this.agents = agents;
        this.agentsWrappers = agentsWrappers;
    }
}
