package planning;

import actions.NotValid;
import actions.TopLevelAction;
import akka.actor.*;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;
import core.*;
import client.ServerClient;
import enums.AgentStatus;
import enums.Color;
import map.Level;
import merging.MergeActor;
import messages.*;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;
import search.SearchClient;

import java.nio.file.FileSystemAlreadyExistsException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Anders on 29/03/16.
 */
public class PlannerActor extends AbstractActor {
    private final HashMap<Integer, ActorRef> agentActors;
    private final HashMap<Color, List<Agent>> colorMapping;
    private GoalPrioritizer goalPrioritizer;
    private final Logger logger;
    private final ActorRef merger;
    private final Level level;
    private final AgentStatus[] agentStatus;
    private int restartCounter = 0;

    public static Props props(List<Agent> agents, Level level, List<Box> boxes, ServerClient client) {
        return Props.create(PlannerActor.class, new Creator<PlannerActor>() {

            @Override
            public PlannerActor create() throws Exception {
                return new PlannerActor(agents, level, boxes, client);
            }
        });
    }

    public PlannerActor(List<Agent> agents, Level level, List<Box> boxes, ServerClient client) {
        this.logger = new Logger("Planner");
        this.colorMapping = new HashMap<>(agents.size());
        this.agentActors = new HashMap<>();
        this.merger = getContext().actorOf(MergeActor.props(agents, boxes, level, client));
        this.level = level;
        this.agentStatus = new AgentStatus[agents.size()];


        try{
            this.goalPrioritizer = new GoalPrioritizer(level, boxes);
            goalPrioritizer.print();
        } catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }




        agents.forEach(a -> {
            List<Agent> cur = this.colorMapping.get(a.getColor());
            if(cur == null){
                cur = new ArrayList<>();
                this.colorMapping.put(a.getColor(), cur);
            }

            cur.add(a);
        });

        logger.info("Planner online");

        agents.forEach(i -> {
            logger.info("Creating agent actors: " + i.toString());
            this.agentActors.put(i.getNumber(), getContext().actorOf(AgentActor.props(i.getNumber()), "agent:" + i.getNumber()));
            this.agentStatus[i.getNumber()] = AgentStatus.Idle;
        });

        //Tell the merger to return the initial structures for states
        this.merger.tell(new GetInitMessage(agents), self());
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(NotValid.class, a -> {

                    logger.error("Something went wrong. NotValid received.");
                })
                .match(MergerResponse.class, resp -> {
                    logger.plan("Received new merger response from merger for " + resp.state.getAgent() + ". Release goal: " + resp.goal);
                    logger.info(Memory.stringRep());
                    goalPrioritizer.releaseGoal(resp.goal);
//
                    if (resp.goal instanceof EdgeGoal) {


                        try {
                            if (resp.goal.isCompleted(resp.state)) {
                                logger.error("Size of boxes: " + resp.state.getBoxes());
                                this.goalPrioritizer.resetTopSort(resp.state, false);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.exit(0);
                        }

                    }

                    assignNextGoal(resp.state);

                    if (resp.accepted) {
                        if (resp.goal instanceof ClearPathGoal) {
                            ClearPathGoal g = (ClearPathGoal) resp.goal;
                            g.setCompleted();
                        }
                        logger.plan("Requesting new states for non-working agents after completion of " + resp.state.getAgent() + " in time " + resp.state.time + ".");
                        requestStatesForNonWorkingAgents(resp.state.time);
                    }
                })
                .match(State.class, state -> {
                    logger.info(Memory.stringRep());
                    int agentNumber = state.getAgent().getNumber();

                    switch (agentStatus[agentNumber]){
                        case Working:
                            logger.error("Received a new state for agent " + state.getAgent()+ " but the agent is already working.");
                            break;
                        case Completed:
                            logger.error("Received a new state for agent " + state.getAgent()+ " but the agent is completed.");
                            break;
                        case Replanning:
                        case Idle:
                            logger.plan("Received new state from merger for " + state.getAgent() + " in time " + state.time);
                            agentStatus[agentNumber] = AgentStatus.Replanning;
                            assignNextGoal(state);
                            break;
                        default:
                            break;
                    }
                })

                .match(NewestStateMessage.class, msg -> {
                    goalPrioritizer.resetTopSort(msg.state, true);
                    self().tell(msg.state, self());
                })

                .match(NoNewStatesMessage.class, msg -> {
                    agentStatus[msg.agentNumber] = AgentStatus.Idle;

                    if(!checkIfAllCompleted()){
                        checkIfNoOneWorks(msg.prevState);
                    }

                })

                .match(IncompletePlan.class, result -> {
                    logger.plan("Received incomplete topLevelAction from " + result.state.getAgent());
                    processPlan(result);

                })
                .match(CompletedPlan.class, result -> {
                    try {
                        logger.plan("Received completed topLevelAction from " + result.topLevelAction.getPreState().getAgent());
                        processPlan(result);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .match(InitMessage.class, msg -> {
                    try {
                        msg.agents.forEach(a -> {
                            State state = new State(a, level, msg.boxes, msg.resources, 0, msg.agentsWrappers, false);
                            assignNextGoal(state);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(0);
                    }
                })
                .match(IncompleteSolutionMessage.class, msg -> {
                    if (!goalPrioritizer.allCompleted(msg.state)) {
                        logger.error("Could not solve level. Received IncompleteSolution from PlanMerger.");
                        goalPrioritizer.print();

                    }

                })
                .match(WrongColorPlan.class, plan -> {
                    State state = plan.topLevelAction.getPreState();
                    logger.plan("received wrong color topLevelAction from " + state.getAgent());
                    goalPrioritizer.releaseGoal(plan.topLevelAction.getGoal());
                    assignNextGoal(plan.topLevelAction.getPreState());

                    ClearPathGoal prev = null;
                    for (ClearPathGoal cpGoal : plan.boxGoals) {
                        if (cpGoal.getBoxes().isEmpty()) {
                            continue;
                        }

                        Goal prevGoal = plan.topLevelAction.getGoal();

                        prevGoal.addDependencyTo(cpGoal);
                        goalPrioritizer.orderedGoals.get(cpGoal.color).add(0, cpGoal);
                        goalPrioritizer.addClearPathDependency(prevGoal, cpGoal);
                        restartColorIfCompleted(cpGoal.color);
                    }

                    for (AgentGoal goal : plan.agentGoals) {
                        plan.topLevelAction.getGoal().addDependencyTo(goal);
                        goalPrioritizer.orderedGoals.get(goal.getColor()).add(0, goal);
                    }


                    requestStatesForNonWorkingAgents(state.time);
                })
                .match(String.class, i -> {
                    logger.info(i);
                })
                .match(Object.class, o -> unhandled(o))
                .build();
    }

    private void processPlan(Result res) {

        //Release current goal tell topLevelAction to merger
        goalPrioritizer.releaseGoal(res.topLevelAction.getGoal());
        merger.tell(res, self());
    }

    private void assignNextGoal(State state) {
        try {
            int agentNumber = state.getAgent().getNumber();

            if (goalPrioritizer.allCompleted(state)) {
                for(int i = 0; i < agentStatus.length; i++){
                    agentStatus[i] = AgentStatus.Completed;
                }

                logger.plan("All goals completed in time: " + state.time);

                checkIfAllCompleted();
                return;
            }

            if(goalPrioritizer.allCompletedForAgent(state)){
                //Update the latest the agent was working in
                agentStatus[agentNumber] = AgentStatus.Completed;
//                agentCompleted[agentNumber] = true;
//                workingAgents[agentNumber] = false;
//                replanningAgents[agentNumber] = false;
                logger.plan("Agent " + state.getAgent() + " completed all actions in time " + state.time + ".");
                checkIfAllCompleted();
            } else {
                TopLevelAction next = goalPrioritizer.getNextPlan(state);

                if (next != null) {
                    agentStatus[agentNumber] = AgentStatus.Working;
                    logger.plan("Agent:" + agentNumber + ", get to work on " + next.toString());
                    this.agentActors.get(agentNumber).tell(next, self());

//                    this.workingAgents[agentNumber] = true;
//                    this.replanningAgents[agentNumber] = false;
//                    this.agentCompleted[agentNumber] = false;

                }else if(agentStatus[agentNumber] == AgentStatus.Replanning){
                    //If the agent wasn't working in the first place, ask for a new state

                    //If this is true, the agent shouldn't wait for other goals to complete but just try again in x time
                    boolean tooManyTriesOnSameGoal = goalPrioritizer.haveGoalsButAttemptedTooOften(state);
                    this.merger.tell(new StillNotWorkingMessage(agentNumber, state, tooManyTriesOnSameGoal), self());

                } else {
                    boolean tooManyTriesOnSameGoal = goalPrioritizer.haveGoalsButAttemptedTooOften(state);

                    if(tooManyTriesOnSameGoal){
                        agentStatus[agentNumber] = AgentStatus.Replanning;
                        this.merger.tell(new StillNotWorkingMessage(agentNumber, state, tooManyTriesOnSameGoal), self());
                    } else {
                        agentStatus[agentNumber] = AgentStatus.Idle;
                        checkIfNoOneWorks(state);
                    }

                    //Store that the agent is free somehow
//                    this.workingAgents[agentNumber] = false;
//                    this.replanningAgents[agentNumber] = false;
//                    this.agentCompleted[agentNumber] = false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * One agent can have a longer topLevelAction than others, but if the short topLevelAction completes before the long topLevelAction,
     * and all boxGoals are completed AND no agents are working, commit the plans.
     */
    private void checkIfNoOneWorks(State state) {
        int working = 0;
        for(int i = 0; i < this.agentStatus.length; i++){
            working += this.agentStatus[i] == AgentStatus.Working ? 1 : 0;
        }

        if(working == 0){
            logger.plan("Noone is working. Restart goal occurrences and requesting newest state from merger.");
            SearchClient.max_depth += 50;
            SearchClient.max_explored += 200;
            restartCounter++;;
            merger.tell(new GetNewestStateMessage(), self());
            goalPrioritizer.resetTopSort(state, true);

            if(SearchClient.max_depth == 3000){
                logger.plan("Telling merger to commit all anyway.");
                this.merger.tell(new AllCompletedMessage(), self());
                System.exit(0);
            } else {
                requestStatesForNonWorkingAgents(0);
            }
        }
    }

    private void requestStatesForNonWorkingAgents(int time){
        List<Integer> agentIDs = new ArrayList<>(agentStatus.length);

        for(int i = 0; i < agentStatus.length; i++){
            //If the agent is not working and not replanning and not completed
            if(agentStatus[i] == AgentStatus.Idle) {
                agentIDs.add(i);
                this.agentStatus[i] = AgentStatus.Replanning;
            }
        }

        if(!agentIDs.isEmpty()){
            logger.plan("Requesting new states for: {" + String.join(", ", agentIDs.stream().map(a -> a + "").collect(Collectors.toList())) + "}");
            this.merger.tell(new RequestNewStatesMessage(agentIDs, time), self());
        } else {
            logger.plan("Didn't find any agents to request new states for.");
            checkIfAllCompleted();
        }
    }

    private void restartColorIfCompleted(Color color){
        List<Agent> agents = this.colorMapping.get(color);
        logger.plan("Validating if " + color.name() + " is completed");
        agents.stream()
                .filter(a -> agentStatus[a.getNumber()] == AgentStatus.Completed)
                .forEach(a -> {
                    logger.plan("Setting agent " + a + " to idle instead of completed.");
                    agentStatus[a.getNumber()] = AgentStatus.Idle;
                });
    }

    private boolean checkIfAllCompleted(){
        int completed = 0;
        for(int i = 0; i < this.agentStatus.length; i++){
//            completed += this.agentCompleted[i] ? 1 : 0;
            completed += this.agentStatus[i] == AgentStatus.Completed ? 1 : 0;
        }

        logger.plan("Total completed agents: " + completed);

        if(completed == this.agentStatus.length){
            //All agents are not working
            logger.goalCount("All have completed. Commit all commands.");
//            logger.goalCount("Restart counter: " + restartCounter);
            this.merger.tell(new AllCompletedMessage(), self());
            return true;
        }

        for(int i = 0; i < agentStatus.length; i++){
            logger.plan("Current agent status: " + i + " : " + agentStatus[i].name());
        }

        return false;
    }
}
