package merging;


import com.sun.scenario.effect.Merge;
import map.Square;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Anders on 13/05/16.
 */
public class MergeState {

    public final BoxStructure boxes;
    public final ResourceStructure resources;
    public final AgentWrapper[] agents;
    public final CommandStructure commands;
    public final List<AgentWrapper> toBeRemoved;
    public final List<Integer> agentIDs;

    public MergeState(BoxStructure boxes, ResourceStructure resources, AgentWrapper[] agents, CommandStructure commands, List<Integer> agentIDs) {
        this.boxes = new BoxStructure(boxes);
        this.resources = new ResourceStructure(resources);
        this.commands = new CommandStructure(commands);
        this.agents = new AgentWrapper[agents.length];
        this.toBeRemoved = new ArrayList<>();
        this.agentIDs = new ArrayList<>(agentIDs);

        for (int i = 0; i < agents.length; i++) {
            this.agents[i] = new AgentWrapper(agents[i]);
        }
    }

    public MergeState(MergeState prev) {
        this(prev.boxes, prev.resources, prev.agents, prev.commands, prev.agentIDs);
    }

    public void addToBeRemoved(AgentWrapper agent) {
        if(!agentIDs.contains(agent.getAgent().getNumber())){
            if (!toBeRemoved.contains(agent)) {
                toBeRemoved.add(agent);
            }

        }


    }

    public void addDependency(AgentWrapper agent){
        if(!agentIDs.contains(agent.getAgent().getNumber())){
            agentIDs.add(agent.getAgent().getNumber());
        }
    }
}
