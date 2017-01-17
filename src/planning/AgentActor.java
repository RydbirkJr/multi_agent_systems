package planning;

import actions.*;
import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

/**
 * Created by Anders on 01/04/16.
 */
public class AgentActor extends AbstractActor {

    private final int number;
    private final AgentPlanner planner;

    public static Props props(int number){
        return Props.create(AgentActor.class, new Creator<AgentActor>() {

            @Override
            public AgentActor create() throws Exception {

                return new AgentActor(number);
            }

        });
    }

    public AgentActor(int number){
        this.planner = new AgentPlanner(number);
        this.number = number;
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive(){
        return ReceiveBuilder
                .match(CreatePlan.class, a -> {
                    sender().tell(planner.createPlan(a), self());
                })
                .match(PlanClearPath.class, plan -> {
                    sender().tell(planner.clearPath(plan), self());
                })
                .match(PlanMoveAgent.class, plan -> {
                    sender().tell(planner.moveAgent(plan), self());
                })
                .match(PlanSolvedEdge.class, plan -> {
                    sender().tell(planner.solvedEdge(plan), self());
                })   
                .build();
    }
}
