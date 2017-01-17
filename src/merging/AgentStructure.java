package merging;

import core.Agent;
import map.Level;
import map.Square;

import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;

/**
 * Created by Anders on 15/05/16.
 */
public class AgentStructure {

    private final HashMap<Integer, HashSet<Square>> resources;
    private final HashSet<Square> canAllocate;
    private final boolean useCanAllocate;

    public AgentStructure(Agent curAgent, AgentWrapper[] agents, Level level, boolean useCanAllocateOnly){
        this.resources = new HashMap<>(agents.length);
        this.canAllocate = new HashSet<>(agents.length);
        this.useCanAllocate = useCanAllocateOnly;

        Stream.of(agents)
                //Remove this agent
                .filter(a -> a.getAgent().getNumber() != curAgent.getNumber())
                .forEach(a -> {
                    Square agentSquare = level.getSquare(a.getAgent().getRow(), a.getAgent().getCol());

                    if(useCanAllocateOnly){
                        canAllocate.add(agentSquare);
                    } else {
                        canAllocate.add(agentSquare);
                        updateAgentResource(a.getTime(), agentSquare);
                        updateAgentResource(a.getTime() + 1, agentSquare);
                    }


                });
    }

    private void updateAgentResource(int time, Square square){
        HashSet<Square> squares = this.resources.get(time);
        if(squares == null){
            squares = new HashSet<>();
            this.resources.put(time, squares);
        }
        squares.add(square);
    }

    public boolean contains(int time, Square square){
        HashSet<Square> squares =  this.resources.get(time);
        if(squares == null){
            return false;
        }

        return squares.contains(square);
    }

    public boolean canAllocate(Square square){
        //only use this if enabled by settings for state
        if(!useCanAllocate) return true;

        return !this.canAllocate.contains(square);
    }

}
