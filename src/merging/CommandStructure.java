package merging;

import core.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Anders on 06/05/16.
 */
public class CommandStructure {

    private List<Command[]> commands;
    private final int noOfAgents;
    private final int[] actionCounts;
    private int committedCommands = 0;

    public CommandStructure(int noOfAgents){
        this.commands = new ArrayList<>();
        this.noOfAgents = noOfAgents;
        this.actionCounts = new int[noOfAgents];
    }

    public CommandStructure(CommandStructure prev){
        this.noOfAgents = prev.noOfAgents;
        this.committedCommands = prev.committedCommands;
        this.actionCounts = Arrays.copyOf(prev.actionCounts, prev.actionCounts.length);
        this.commands = new ArrayList<>(prev.commands.size());
        prev.commands.forEach(cmdArray -> this.commands.add(Arrays.copyOf(cmdArray, cmdArray.length)));
    }

    public void addCommand(int time, int agent, Command cmd){

        Command[] entry;

        if(this.commands.size() == time){
            this.commands.add(new Command[noOfAgents]);
        }

        entry = this.commands.get(time);

        //Add command
        entry[agent] = cmd;

        //Update agent count
        actionCounts[agent] = time;
    }

    /**
     * @return Null if it should not commit any. Or else a list of commands.
     */
    public List<Command[]> getCommittableCommands(){
        int lowestAgentCount = getLowestAgentCount();

        if(lowestAgentCount > committedCommands ){
            List<Command[]> result = commands.subList(committedCommands, lowestAgentCount+1);
            committedCommands = lowestAgentCount+1;
            return result;
        }

        return null;
    }

    /**
     * Get the lowest agent move so far. Thus, if one agent haven't moved, zero is returned.
     * @return
     */
    public int getLowestAgentCount(){
        int temp = Integer.MAX_VALUE;
        for(int count : actionCounts){
            temp = Integer.min(temp, count);
        }

        return temp;
    }

    public List<Command[]> getRestOfCommands(){
        return commands.subList(committedCommands, commands.size());
    }
}
