package merging;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;
import client.ServerClient;
import core.*;
import map.Level;
import messages.*;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Anders on 03/05/16.
 */
public class MergeActor extends AbstractActor {

    private final PlanMerger planner;

    public static Props props(List<Agent> agents, List<Box> boxes, Level level, ServerClient client){
        return Props.create(MergeActor.class, new Creator<MergeActor>() {

            @Override
            public MergeActor create() throws Exception {

                return new MergeActor(agents, boxes, level, client);
            }
        });
    }

    public MergeActor(List<Agent> agents, List<Box> boxes, Level level, ServerClient client){
        planner = new PlanMerger(agents, boxes, level, client);
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive(){
        return ReceiveBuilder
                .match(Result.class, a -> {
                    try{
                        MergerResponse resp = planner.merge(a);

                        planner.printAgentStatus();

                        sender().tell(resp, self());
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                })
                .match(GetInitMessage.class, msg -> {
                    //Retrieve the structures used for initialize the search
                    sender().tell(new InitMessage(planner.boxes, planner.resources, msg.agents, planner.agents), self());
                })
                .match(AllCompletedMessage.class, m -> {

                    try {
                        boolean completed = planner.commitRest();
                        planner.printAgentStatus();

                        if (!completed) {
                            sender().tell(new IncompleteSolutionMessage(planner.getNewestState()), self());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                })
                .match(StillNotWorkingMessage.class, msg -> {
                    try {
                        State state = planner.getState(msg.agentNumber, true, msg.tooManyTriesOnSameGoal);
                        if (state != null) {
                            sender().tell(state, self());
                        } else {
                            Logger.global("Received the empty state from merger for time " + msg.state.time + " for agent " + msg.state.getAgent());
                            sender().tell(new NoNewStatesMessage(msg.agentNumber, msg.state), self());
                        }

                        planner.printAgentStatus();
                    } catch(Exception e){
                        e.printStackTrace();
                    }


                })
                .match(RequestNewStatesMessage.class, msg -> {
                    try {
                        msg.agentIDs.stream()
                                .forEach(id -> sender().tell(planner.getState(id, false, false), self()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .build();
    }
}
