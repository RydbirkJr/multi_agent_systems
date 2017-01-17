package merging;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import actions.*;
import client.ServerClient;
import core.*;
import enums.Color;
import enums.Type;
import map.Level;
import map.Square;
import messages.MergerResponse;
import search.Heuristic;
import search.Node;
import search.SearchClient;
import search.Strategy;

public class PlanMerger {
    //Variable to keep track of resources somehow -- Maybe Level instead -- something 3rd?
    private final Level level;
    private final Logger logger;
    protected AgentWrapper[] agents;
    //Cannot make an array with generic elements
    private CommandStructure commands;
    protected ResourceStructure resources;
    protected  BoxStructure boxes;
//    protected final AgentStructure agentStructure;
    private final ServerClient client;
    private final TreeSet<Integer> timestamps;


    public PlanMerger(List<Agent> agents, List<Box> boxes, Level level, ServerClient client) {
        this.logger = new Logger("Merger");
        this.timestamps = new TreeSet<>();
        this.level = level;
        this.client = client;
        this.commands = new CommandStructure(agents.size());
        this.resources = new ResourceStructure(level.MAX_ROW, level.MAX_COLUMN);
        this.boxes = new BoxStructure(boxes, level, 0);
//        this.agentStructure = new AgentStructure(agents, level, 0);
        //Load agents into array ordered by their given number.
        this.agents = new AgentWrapper[agents.size()];
        for (Agent a : agents) {
            this.agents[a.getNumber()] = new AgentWrapper(a, 0);
        }
    }

    /**
     * This method has the responsibility to:
     * - Unwrap the topLevelAction
     * - Catch eventually errors
     * - Create a new state that the agent are gonna search from
     *
     * @param plan
     * @return
     */
    public MergerResponse merge(Result plan) {

        Action cp = plan.topLevelAction;

        if(cp instanceof PlanMoveAgent){
            logger.error("cp is a PlanMoveAgent");
            System.exit(0);
        }

        State state = cp.getPreState();
        MergeState mergeState = new MergeState(boxes, resources, agents, commands, new ArrayList<>());
        AgentWrapper agentWrapper = mergeState.agents[state.getAgent().getNumber()];

        logger.plan("Initializing new merge session for agent " + agentWrapper.getAgent() + " in time " + agentWrapper.getTime() +".");

        //This will always be set
        MergeState res = merge(plan, agentWrapper, mergeState, 0);

        //res is applied
        this.agents = res.agents;
        this.boxes = res.boxes;
        this.commands = res.commands;
        this.resources = res.resources;

        agentWrapper = agents[state.getAgent().getNumber()];
        timestamps.add(agentWrapper.getTime());

        logger.plan("Merge completed. Agent now at: " + agentWrapper.getAgent() + " in time " + agentWrapper.getTime());

        List<Command[]> result = this.commands.getCommittableCommands();
        commitCommands(result);

        //Retrieve agent again. It might have been updated at this point, thus we need a new pointer.
        State nextState = new State(agentWrapper.getAgent(), level, boxes, resources, agentWrapper.getTime(), agents, false);

        if(nextState == null){
            logger.error("Next state is null.");
        }

        return new MergerResponse(nextState, plan.topLevelAction.getGoal(), state.time != agentWrapper.getTime());

//        boolean accepted = false;
//        boolean acceptPlan = true;
//        try {
//            //Uses the local assigned resources now
//            applyPlan(cp, agentWrapper, mergeState);
//
//
//            if(!mergeState.toBeRemoved.isEmpty()){
//                List<AgentWrapper> temp = new ArrayList<>(mergeState.toBeRemoved.values());
//                //We found one agent - or more!
//                for(AgentWrapper a : temp){
//                    //If it is the same agent, ignore
//                    if(a.getAgent().getNumber() == agentWrapper.getAgent().getNumber()) continue;
//
//                    logger.topLevelAction("Found agent on the path: " + a.getAgent());
//                    PlanMoveAgent pma = moveAgentOutOfTheWay(a, mergeState);
//
//                    if(pma == null){
//                        throw new CouldNotMoveAgentException(a);
//                    }
//
//                    try{
//                        logger.topLevelAction("Moving agent out of the way: " + a);
//                        moveAgent( (MoveAgent) pma.getChildren().get(0), a, mergeState);
//                        logger.topLevelAction("Moved agent: " + a);
//                    } catch (IllegalPlanException e){
//                        e.printStackTrace();
//                        System.exit(0);
//                    }
//                }
//            }
//
//            accepted = true;
//        }catch (IllegalPlanException e) {
//            logger.error("Illegal topLevelAction thrown.");
//
//        } catch (CouldNotMoveAgentException e){
//            logger.error("Could not move agent: " + e.wrapper.getAgent());
//            acceptPlan = false;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.exit(0);
//        }
//
//        if(acceptPlan){
//            //State is set to accepted state
//            this.agents = mergeState.agents;
//            this.boxes = mergeState.boxes;
//            this.commands = mergeState.commands;
//            this.resources = mergeState.resources;
//        }


        //Extract the topLevelAction
        //Check that the given box to be moved is actually movable
        // - This means that it has not been moved by another agent later in the accepted topLevelAction
        //TODO: Flag when a box was moved last
        //moveBox on by one
        //Allocate resources
        // - Detect conflict
        // - remember to allocate resources for previous state and next
        //Update agent and box position in the end
        //return new state with allocated resources in the future
        //New resources should be newly allocated so we do not have concurrent issues

        //At this point I should add in the new states, the new boxesMap etc.

        //Let's check if we should commit some of the topLevelAction
    }

    private MergeState merge(Result result, AgentWrapper wrapper, MergeState before, int depth){

        MergeState mergeState = new MergeState(before);

        try {
            //Uses the local assigned resources now
            applyPlan(result.topLevelAction, wrapper.getAgent().getNumber(), mergeState);

            //At this point, if there is any errors, they are thrown.
            //There MIGHT be agents to be removed.

            //No agents to remove and no errors thrown. return mergeState.
            if(mergeState.toBeRemoved.isEmpty()){
                logger.plan("No agents to remove. Returning state.");
                return mergeState;
            }

            //There is at least one agent to remove
            //Find one agent without dependencies and apply its topLevelAction
            //rerun merge with the new merge state

            for(AgentWrapper a : mergeState.toBeRemoved){
                logger.plan("Trying to make topLevelAction to remove agent: " + a);

                //The topLevelAction found here does not necessarily concern agent "a"
                PlanMoveAgent plan = findPlanForSubAgent(a, mergeState);

                if(plan == null) throw new CouldNotMoveAgentException(a);
                AgentWrapper receivedAgent = mergeState.agents[plan.getPreState().getAgent().getNumber()];
                logger.plan("Received agent: " + receivedAgent + "from findPlanForSubAgent for: " + a);

                logger.plan("Trying to merge " + plan + " in before merging " + result.topLevelAction + ".");
                MergeState appliedAgentMoveState = merge(new CompletedPlan(plan),
                        receivedAgent,
                        new MergeState(this.boxes, this.resources, this.agents, this.commands, new ArrayList<>()), depth + 1);
                logger.plan("Merged " + plan + " in. Now, merge " + result.topLevelAction + ".");

                if(depth > 50) return appliedAgentMoveState;

//                appliedAgentMoveState.addDependency(receivedAgent);
                return merge(result, wrapper, appliedAgentMoveState, depth + 1);
            }

            logger.error("Reached end of merge try-catch but should had returned earlier. Plan: " + result.topLevelAction);
        }catch (IllegalPlanException e) {
            logger.error("Illegal topLevelAction thrown. Returning completed merge so far.");
            if(!mergeState.toBeRemoved.isEmpty()) return before;

        } catch (CouldNotMoveAgentException e){
            logger.error("Could not move agent: " + e.wrapper.getAgent() + ". Returning state before merge.");
            return before;

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        return mergeState;

    }


    public State getState(int agentNumber, boolean getNextState, boolean tooManyRetries){
        AgentWrapper curAgent = agents[agentNumber];
        Square agentSquare = level.getSquare(curAgent.getAgent().getRow(), curAgent.getAgent().getCol());

        Integer time;

        if(tooManyRetries){
            if(!timestamps.isEmpty() && curAgent.getTime() == timestamps.last()) return null;
            time = curAgent.getTime() + 10;
            //The agent is not allowed to go over the latest timestamp
            if(!timestamps.isEmpty() && time > timestamps.last()) time = timestamps.last();
        } else {
            try{
                time = getNextState ? timestamps.higher(curAgent.getTime()) : curAgent.getTime();
            } catch(NullPointerException e){
                //Didn't find higher timestamp. Return empty state.
                return null;
            }
        }

        if(time == null) return null;

        for (int curTime = curAgent.getTime(); curTime < time; curTime++) {
            commands.addCommand(curTime, curAgent.getAgent().getNumber(), new Command());
            resources.put(curTime, agentSquare);
            curAgent.incrementTime();
        }

        return new State(curAgent.getAgent(), level, boxes, resources, curAgent.getTime(), agents, false);
    }

    public boolean commitRest(){

        return commitCommands(this.commands.getRestOfCommands());
    }

    /**
     * returns the newest state for any agents,
     * thus the highest completion achieved so far.
     * @return
     */
    public State getNewestState(){
        AgentWrapper wrapper = Stream.of(agents)
                .max((a1, a2) -> a1.getTime() - a2.getTime())
                .get();

        return new State(wrapper.getAgent(), level, boxes, resources, wrapper.getTime(), agents, false);
    }

    private boolean commitCommands(List<Command[]> commands){
        if (commands != null) {
            int time = client.getCount();
            //Execute commands here
            int size = commands.size();
            boolean result = false;
            for (int i = 0; i < size; i++) {
//                logger.info("Conducting moveBox i: " + (time + i));

                try{
                    result = client.move(commands.get(i));
                } catch(InvalidMoveException e){
                    for(int j = 0; j < client.getCount() + 1; j++){
                        logger.error("Resources in time " + j + ":");

                        for(Square res : resources.getResources(j)){
                            logger.error("\t\t" + res);
                        }
                    }

                    System.exit(0);
                }

//                HashSet<Square> res = resources.getResources(time + i);

//                logger.info("Resources size: " + res.size());
//
//                for (Square square : res) {
//                    logger.topLevelAction(square.toString());
//                }
//
//                System.err.println("");
            }

            logger.plan("# of moves: " + client.getCount());
            return result;
        }
        return false;
    }

    /**
     * We are treating moves as groups of MoveBox actions. In case of error, roll
     * back the entire MoveBox action
     *
     * @param mb
     * @param agentWrapper
     */
    private void moveBox(MoveBox mb, AgentWrapper agentWrapper, MergeState data) throws IllegalPlanException {
        BoxWrapper box = data.boxes.getBoxByID(mb.getBox().getID());

        Agent preAgent = agentWrapper.getAgent();
        int preTime = agentWrapper.getTime();

        try {

            //Identify that the box is possible to moveBox at this point
            if(box.getBox().getColor() != preAgent.getColor()){
                logger.error("Agent: " + preAgent + " does not match box color for box: " + box.getBox());
                throw new IllegalPlanException();
            }

            //If the lastMove is larger than the agent time, drop the topLevelAction
            //The current box and the box from the moveBox should be the same
            if (box.getLastMove() > preTime || !box.getBox().equals(mb.getBox())) {
                //TODO: Throw unable to merge plans
                throw new IllegalPlanException();
            }

            //This will happen on incomplete plans
            if (mb.getChildren().size() == 0) {
                logger.plan("No children.");
                throw new IllegalPlanException();
            }

            //Assumes only one child
            if (mb.getChildren().size() > 1) {
                //TODO: Do something if too many children exist. early return?
                logger.error("Unexpected behavior. Too many children to a MoveBox action.");
                throw new IllegalPlanException();
            }

            if(!preAgent.equals(mb.getPreState().getAgent())){
                logger.error("The agent is not positioned on the same spot in the prestate compared to the saved position. Expected: " + preAgent + ", actual: " + mb.getPreState().getAgent() );
                throw new IllegalPlanException();
            }

            //Assign local resources. Are not allocated before the action is approved
            List<HashSet<Square>> localResources = new ArrayList<>();

            //Assumes only one child - checked above
            MoveCommand mc = (MoveCommand) mb.getChildren().get(0);

            int boxMoveTime = 0;
            for (Command cmd : mc.commands) {

                //If the command is altering a box position and the boxMoveTime is not set,
                // assign the mox moveBox round
                if (cmd.actType == Type.Pull || cmd.actType == Type.Push && boxMoveTime == 0) {
                    boxMoveTime = agentWrapper.getTime();
                }

                //assign the used resources to the local resources pr. round
                localResources.add(move(cmd, agentWrapper, box.getBox(), data));
            }

            //Last validation check
            if(!data.resources.canAllocate(mb.getSquare().row, mb.getSquare().col, agentWrapper.getTime() ) ){
                logger.error("The box cannot be placed on a spot allocated in the future. " + box.getBox() + " for square " + mb.getSquare() + "." );
                throw new UsedAllocatedResourceException(mb.getSquare());
            }

            if(!data.resources.canAllocate(agentWrapper.getAgent().getRow(), agentWrapper.getAgent().getCol(), agentWrapper.getTime() ) ){
                logger.error("The agent "+ agentWrapper.getAgent() + " is now placed on a field that is allocated in the future.");
                throw new UsedAllocatedResourceException(level.getSquare(agentWrapper.getAgent().getRow(), agentWrapper.getAgent().getCol()));
            }

            //We assume here that all the commands was legal
            //Update commands for agent by adding it to the command structure
            int curTime = preTime;
            int agentNumber = agentWrapper.getAgent().getNumber();
            for (Command cmd : mc.commands) {
                data.commands.addCommand(curTime, agentNumber, cmd);
                curTime++;
            }

            //The total action is accepted
            logger.plan("Commands for: " + mb.toString() + " is accepted.");

            //resources before box was moved should also be applied
            //Up until the box is moved, the given field are locked

            int boxFreezeMinimum = Integer.max(box.getLastMove(), data.commands.getLowestAgentCount());
            Square preBoxSquare = level.getSquare(mb.getBox().row, mb.getBox().col);

            //TODO: Should also consider not locking the resource "in the past", meaning that if all agents have moved up until x...
            // - this is included in the boxFreezeMinimum

            //TODO: We can do it even better: If all agents of a specific color has moved up until a point x, it cannot be moved by others before x
            // - Possible improvement, not necessary now.
            data.resources.freezeField(boxFreezeMinimum, boxMoveTime - 1, preBoxSquare);

            //Box is updated here
            //Notice that the box positions are not stored for the fields when it is moved
            data.boxes.remove(preBoxSquare, boxMoveTime);
            data.boxes.put(mb.getSquare(), new Box(box.getBox(), mb.getSquare().row, mb.getSquare().col), agentWrapper.getTime());
            box.updateLastMove(boxMoveTime);
            box.updateBox(mb.getSquare().row, mb.getSquare().col);

            //Resources are applied
            data.resources.addResources(preTime, localResources);

        } catch (UsedAllocatedResourceException e) {
            //Roll back agent position
            data.agents[agentWrapper.getAgent().getNumber()] = new AgentWrapper(preAgent, preTime);
            logger.error("Spot already in use: " + e.square);
            throw new IllegalPlanException();
        }
    }

    private HashSet<Square> move(Command cmd, AgentWrapper agentWrapper, Box curBox, MergeState data) throws UsedAllocatedResourceException {

        HashMap<Square, AgentWrapper> notMovingAgents = getNotMovingAgents(agentWrapper, data);

        int agentRow = agentWrapper.getAgent().getRow();
        int agentCol = agentWrapper.getAgent().getCol();

        Square cur = level.getSquare(agentRow, agentCol);

        //Verify the agent's current position
        if (data.resources.contains(agentWrapper.getTime(), cur)) {
            logger.error("The field where the agent is placed is suddenly used: " + agentWrapper.getAgent());
            //Is this even possible? This is the spot that the agent was positioned on and was suddenly used.
            throw new UsedAllocatedResourceException(cur);
        }
        if(notMovingAgents.containsKey(cur)){
            data.addToBeRemoved(notMovingAgents.get(cur));
        }

        HashSet<Square> localResources = new HashSet<>(3);
        localResources.add(cur);

        Square next, box;

        switch (cmd.actType) {
            case Move:
                next = level.getSquare(cur, cmd.dir1);
                agentRow = next.row;
                agentCol = next.col;

                //Verify resource
                if (data.resources.contains(agentWrapper.getTime(), next) || data.boxes.containsBoxIgnore(next, agentWrapper.getTime(), curBox)) {
                    throw new UsedAllocatedResourceException(next);
                }

                if(notMovingAgents.containsKey(next)){
                    data.addToBeRemoved(notMovingAgents.get(next));
                }

                localResources.add(next);

                break;
            case Push:
                //If there is NOT a box on the next
                //Thus, if we know that the box is placed on the expected spot, and assume that the actions are legal,
                //We only need to update the agent position to actually allocate resources

                box = level.getSquare(cur, cmd.dir1);
                next = level.getSquare(box, cmd.dir2);

                agentRow = box.row;
                agentCol = box.col;

                if(notMovingAgents.containsKey(box)){
                    data.addToBeRemoved(notMovingAgents.get(box));
                }

                if (data.resources.contains(agentWrapper.getTime(), box) || data.boxes.containsBoxIgnore(box, agentWrapper.getTime(), curBox)) {
                    throw new UsedAllocatedResourceException(box);
                }

                if(notMovingAgents.containsKey(next)){
                    data.addToBeRemoved(notMovingAgents.get(next));
                }

                if (data.resources.contains(agentWrapper.getTime(), next) || data.boxes.containsBoxIgnore(next, agentWrapper.getTime(), curBox)) {
                    throw new UsedAllocatedResourceException(next);
                }

                localResources.add(box);
                localResources.add(next);
                break;
            case Pull:

                next = level.getSquare(cur, cmd.dir1);
                box = level.getSquare(cur, cmd.dir2);

                agentRow = next.row;
                agentCol = next.col;

                if(notMovingAgents.containsKey(box)){
                    data.addToBeRemoved(notMovingAgents.get(box));
                }

                //Verify the resources
                if (data.resources.contains(agentWrapper.getTime(), box) || data.boxes.containsBoxIgnore(box, agentWrapper.getTime(), curBox)) {
                    throw new UsedAllocatedResourceException(box);
                }

                if(notMovingAgents.containsKey(next)){
                    data.addToBeRemoved(notMovingAgents.get(next));
                }

                if (data.resources.contains(agentWrapper.getTime(), next) || data.boxes.containsBoxIgnore(next, agentWrapper.getTime(), curBox)) {
                    throw new UsedAllocatedResourceException(next);
                }

                localResources.add(box);
                localResources.add(next);
                break;
            case NoOp:

                //Resources for this field already allocated
                break;
        }

        agentWrapper.moveAgent(agentRow, agentCol);

        //Increment time after use
        agentWrapper.incrementTime();

        return localResources;
    }

    private void applyPlan(Action action, int agentNumber, MergeState mergeState) throws IllegalPlanException {

        AgentWrapper agent = mergeState.agents[agentNumber];
        logger.plan(action.toString());

        if (action instanceof MoveBox) {
            moveBox((MoveBox) action, agent, mergeState);
            return;
        }

        if(action instanceof MoveAgent){
            moveAgent((MoveAgent) action, agent, mergeState);
            return;
        }

        List<Action> children = action.getChildren();

        if (children.isEmpty()) {
            logger.plan("No children.");
            return;
        }

        for (Action child : children) {
            applyPlan(child, agentNumber, mergeState);
        }
    }

    private void moveAgent(MoveAgent action, AgentWrapper agentWrapper, MergeState data) throws  IllegalPlanException {
        Agent preAgent = agentWrapper.getAgent();
        int preTime = agentWrapper.getTime();

        try {

            //This will happen on incomplete plans
            if (action.getChildren().size() == 0) {
                logger.plan("No children.");
                throw new IllegalPlanException();
            }

            //Assumes only one child
            if (action.getChildren().size() > 1) {
                //TODO: Do something if too many children exist. early return?
                logger.error("Unexpected behavior. Too many children to a MoveBox action.");
                throw new IllegalPlanException();
            }

            if(!preAgent.equals(action.getPreState().getAgent())){
                logger.error("The agent is not positioned on the same spot in the prestate compared to the saved position. Expected: " + preAgent + ", actual: " + action.getPreState().getAgent());
                throw new IllegalPlanException();
            }

            //Assign local resources. Are not allocated before the action is approved
            List<HashSet<Square>> localResources = new ArrayList<>();

            //Assumes only one child - checked above
            MoveCommand mc = (MoveCommand) action.getChildren().get(0);

            for (Command cmd : mc.commands) {

                //assign the used resources to the local resources pr. round
                localResources.add(move(cmd, agentWrapper, null, data));
            }

            //We assume here that all the commands was legal
            //Update commands for agent by adding it to the command structure
            int curTime = preTime;
            int agentNumber = agentWrapper.getAgent().getNumber();
            for (Command cmd : mc.commands) {
                data.commands.addCommand(curTime, agentNumber, cmd);
                curTime++;
            }

            //The total action is accepted
            logger.plan("Commands for: " + action.toString() + " is accepted.");

            //Resources are applied
            data.resources.addResources(preTime, localResources);

        } catch (UsedAllocatedResourceException e) {
            //Roll back agent position
            data.agents[agentWrapper.getAgent().getNumber()] = new AgentWrapper(preAgent, preTime);

            logger.error("Spot already in use: " + e.square);
            throw new IllegalPlanException();
        }
    }

    private HashMap<Square, AgentWrapper> getNotMovingAgents(AgentWrapper wrapper, MergeState data){
        HashMap<Square, AgentWrapper> set = new HashMap<>(agents.length);

        Stream.of(data.agents)
                .filter(a -> a.getTime() <= wrapper.getTime())
                .filter(a -> !a.getAgent().equals(wrapper.getAgent()))
                .forEach(a -> set.put(level.getSquare(a.getAgent().getRow(), a.getAgent().getCol()), a));

        return set;
    }

    private PlanMoveAgent findPlanForSubAgent(AgentWrapper agentWrapper, MergeState mergeState) throws CouldNotMoveAgentException{

        //This is only to hold a new toBeRemoved list
        //and to search for resources for the agent
        //Block field for current agent if we need to search further
        MergeState subState = new MergeState(mergeState);

        PlanMoveAgent pma = moveAgentOutOfTheWay(agentWrapper, subState);
        try{
            if(pma == null) throw new CouldNotMoveAgentException(agentWrapper);
            applyPlan(pma, agentWrapper.getAgent().getNumber(), subState);

        } catch(IllegalPlanException e){
            logger.error("Illegal topLevelAction at findPlanForSubAgent. Not expected");
            System.exit(0);
        }

        if(subState.toBeRemoved.isEmpty()){
            logger.plan("Substate empty. returning leaf: " + pma);
            return pma;
        } else {
            //Create state where we block the field
            //There is at least one child so we need to search that in stead
            MergeState blockedState = new MergeState(mergeState);
            Square thisAgentSquare = level.getSquare(agentWrapper.getAgent().getRow(), agentWrapper.getAgent().getCol());
            blockedState.boxes.put(thisAgentSquare, new Box(thisAgentSquare.row, thisAgentSquare.col,'z', Color.blue), agentWrapper.getTime());

            for(AgentWrapper a : subState.toBeRemoved){
                logger.plan("Trying to find next agent to remove: " + a);
                return findPlanForSubAgent(a, blockedState);
            }
        }

        logger.error("Reached the end of findPlanForSubAgent somehow. Investigate. Plan: " + pma);
        System.exit(0);
        return pma;
    }

    private PlanMoveAgent moveAgentOutOfTheWay(AgentWrapper agentWrapper, MergeState mergeState){

//        Stream.of(mergeState.agents)
//                .filter(a -> !a.getAgent().equals(agentWrapper.getAgent()))
//                .filter(a -> a.getTime() <= agentWrapper.getTime())
//                .forEach(a -> {
//                    Square temp = level.getSquare(a.getAgent().getRow(), a.getAgent().getCol());
//                    logger.topLevelAction("Adding resources for agent " + a.getAgent() + " on square " + temp + " for time " + agentWrapper.getTime());
//                    mergeState.resources.put(agentWrapper.getTime(), temp);
//                });

        State state = new State(agentWrapper.getAgent(), level, mergeState.boxes, mergeState.resources, agentWrapper.getTime(), mergeState.agents, true);
        Square agentSquare = level.getSquare(agentWrapper.getAgent().getRow(), agentWrapper.getAgent().getCol());

        SearchClient client = new SearchClient(state, logger);
        Node node = client.Search(new Strategy.StrategyBestFirst(new Heuristic.WeightedAStar(client.initialState)));

        if(node != null){
            logger.plan("AgentWrapper: " + agentWrapper + ", size of topLevelAction: " + node.extractPlan().size());

            Square toSquare = level.getSquare(node.state.getAgent().getRow(), node.state.getAgent().getCol());

            List<Command> commands = node.extractPlan()
                    .stream()
                    .map(n -> n.action)
                    .collect(Collectors.toList());

            PlanMoveAgent pma = new PlanMoveAgent(state, null, null);
            MoveAgent ma = new MoveAgent(pma, agentSquare, toSquare);

            MoveCommand mc = new MoveCommand(ma, commands);
            mc.setEffect(node.state);
            return pma;
        }

        return null;

    }

    protected void printAgentStatus(){
        StringBuilder builder = new StringBuilder();

        builder.append("Agent status:\n");

        float max = (float) getNewestState().time;

        for(AgentWrapper wrapper : agents){

            int percentage = Math.round(wrapper.getTime() / max * 50F);

            String temp = wrapper.getAgent().toString();
            if(temp.length() < 8){
                temp += "\t";
            }

            builder.append("\t\t" + temp + "\t:");
            for (int i = 0; i < percentage; i++){
                builder.append("|");
            }
            builder.append(" "+ wrapper.getTime() + "\n");
        }

        logger.plan(builder.toString());

    }
}
