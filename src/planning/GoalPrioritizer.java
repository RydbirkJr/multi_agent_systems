package planning;


import map.Node;
import map.Edge;
import map.Element;
import actions.*;
import core.Box;
import core.Logger;
import core.State;
import enums.Color;
import map.GoalField;
import map.Level;
import map.MinDistance;
import map.Square;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Anders on 20/03/16.
 */
public class GoalPrioritizer {
    public HashMap<Color, List<Goal>> orderedGoals;
    private List<Goal> goals;
    private HashSet<Goal> goalsBeingProcessed;
    private Logger logger;
    private HashMap<Edge,EdgeGoal> edgeMap = new HashMap<>();
    private HashMap<Node,NodeGoal> nodeMap = new HashMap<>();
    private OccurrenceDetector occurrenceDetector;
    private int allowedOccurrences;
    private Level level;
    private int resetCounter;
    
    public GoalPrioritizer(
            Level level,
            List<Box> boxes
            ) {
        this.level = level;
        this.logger = new Logger("Goal");
        List<MoveGoal> goals = mapBoxesToGoals(level, boxes);
        sortGoals(goals);

        this.allowedOccurrences = 4;
        this.occurrenceDetector = new OccurrenceDetector();

        goalsBeingProcessed = new HashSet<>(goals.size());
    }

    public void resetTopSort(State state, boolean increaseCounter){
        if(increaseCounter){
            resetCounter++;
        }

        List<MoveGoal> temp = goals.stream()
                .filter(g -> g instanceof MoveGoal)
                .map(g -> (MoveGoal) g)
                .collect(Collectors.toList());

        temp.stream()
                .forEach(g -> g.updateBox(state));

        sortGoals(temp);
        restartOccurrences();
    }

    /**
     * Retrieves the goal with the lowest estimate that is still viable.
     * This also flags the selected goal as being processed by some agent.
     * @param agent
     * @param state
     * @return
     */
    public TopLevelAction getNextPlan(State state){

        Goal goal = getBestGoal(state);
//        CreatePlan topLevelAction

        //If null is returned, no boxGoals can be accomplished right now
        if(goal == null) return null;

        occurrenceDetector.putOccurrence(goal, state);

        logger.info(goal.toString());

        //Flag this goal as being processed
        goalsBeingProcessed.add(goal);

        //Expected state when the goal is completed:
        State expected = goal.getExpectedState(state);

        //Get the topLevelAction that it should topLevelAction to continue with
        //logger.error("Before ContinueTowards : " + goal.toString());
        Goal continueTowards = getBestGoal(expected);

        //logger.error("After ContinueTowards");
        TopLevelAction plan = null;

        if(goal instanceof MoveGoal){
            plan = new CreatePlan(state, (MoveGoal) goal, continueTowards);
        } else if(goal instanceof ClearPathGoal){
            plan = new PlanClearPath(state, (ClearPathGoal) goal, continueTowards);
        } else if(goal instanceof AgentGoal){
            logger.error("New agent goal to be processed.");
            plan = new PlanMoveAgent(state, (AgentGoal) goal, continueTowards);
        } else if(goal instanceof EdgeGoal){
        	plan = new PlanSolvedEdge(state,(EdgeGoal)goal,continueTowards);
        }	else {
            logger.error("Ikke implementeret move agent goal endnu.");
            System.exit(0);
        }

        //Return topLevelAction
        return plan;
    }

    public boolean allCompletedForAgent(State state){
        List<Goal> agentGoals = orderedGoals.get(state.getAgent().getColor());

        long count = agentGoals
                .stream()
                .filter(g -> g.canReach(state))
                .filter(g -> g.isCompleted(state))
                .count();

        if(count == agentGoals.size()) {
            //All completed
            return true;
        }

        return false;
    }

    private Goal getBestGoal(State state){
        List<Goal> agentGoals = orderedGoals.get(state.getAgent().getColor());

        Goal goal = agentGoals.stream()
                .filter(g -> this.allowedOccurrences > occurrenceDetector.occurrences(g, state))
                .filter(g -> 10 > occurrenceDetector.totalOccurrences(g))
                .filter(g -> g.canReach(state))
                .filter(g -> !g.isCompleted(state))
                .filter(g -> g.preconditions(state))
                .filter(g -> !goalsBeingProcessed.contains(g))

//                .sorted((p1, p2) -> Integer.compare(p1.getGoal().estimate(state), p2.getGoal().estimate(state)))
//                .limit(2)
//                .collect(Collectors.toList());
//                .findFirst()
                .min((g1, g2) -> Integer.compare(g1.estimate(state, 2), g2.estimate(state, 2)))
                .orElseGet(() -> null);

        logger.error("Goal found in getBestGoal(): " + goal);
        
        /*if(goal == null)
        {
        	for(Goal g : agentGoals)
        	{
        		if(g.isCompleted(state) || !(g instanceof EdgeGoal || g instanceof NodeGoal)) continue;
        		
        		if(g.preconditions(state))
				{
        			goal = g;
				}        		
        	}
        }*/
        
        return goal;
    }

    public void releaseGoal(Goal goal){
        goalsBeingProcessed.remove(goal);
    }

    public boolean allCompleted(State state){
        //Counts the number of NOT completed boxGoals
        long count = goals
                .stream()
                .filter(g -> !g.isCompleted(state))
                .count();

//        Logger.goalCount("Total goals to be completed: " + count);

        return count == 0;
    }

    public void print(){
        for(Goal goal : this.goals){
            logger.info("Goal: " + goal);

            for(Goal dep : goal.getDependencyList()){
                logger.info("\t -> " + dep);
            }
        }
    }

    public boolean haveGoalsButAttemptedTooOften(State state){
        List<Goal> agentGoals = orderedGoals.get(state.getAgent().getColor());

        return agentGoals.stream()
                .anyMatch(g -> this.allowedOccurrences <= occurrenceDetector.occurrences(g, state));
    }

    public void restartOccurrences(){
        this.occurrenceDetector = new OccurrenceDetector();

    }

    public void printCompletedGoals(State state){
        goals
                .stream()
                .forEach(g -> logger.info("Goal: " + g + " is completed: " + g.isCompleted(state)));
    }

    private void sortGoals(List<MoveGoal> goals){
        ArrayDeque<MoveGoal> goalQueue = new ArrayDeque<>(goals);
        List<Goal> processedGoals = new ArrayList<>();

        while (!goalQueue.isEmpty()){
            MoveGoal goal = goalQueue.poll();
            processGoal(goal, processedGoals);
        }

        this.goals = processedGoals;

        if(resetCounter % 2 == 0){
            prioritizeNode(level);
        }

        this.orderedGoals = colorSort(topSort(this.goals));
    }

    private void processGoal(MoveGoal curGoal, List<Goal> processedGoals){

        //Validate resources of completed boxGoals    	
        for (Goal completedGoal : processedGoals) {
            MoveGoal tempGoal = (MoveGoal) completedGoal;

            if (curGoal.checkResources(tempGoal.getGoalField())) {
                //The current goal uses the field of the completed goal
                //The current goal comes before the completed goal
            	/*if(curGoal.getLetter() == tempGoal.getLetter())
            	{
            		swapIdenticalBox(curGoal, tempGoal);
            	}
            	else
            	{*/
            		tempGoal.addDependencyTo(curGoal);
            	//}
            }

            if (tempGoal.checkResources(curGoal.getGoalField())) {
                //The current goal uses the field of the completed goal
                //The completed goal comes before the current goal
            	/*if(curGoal.getLetter() == tempGoal.getLetter())
            	{
            		swapIdenticalBox(tempGoal,curGoal);
            	}
            	else
            	{*/
            		curGoal.addDependencyTo(tempGoal);
            	//}
            }
        }
        processedGoals.add(curGoal);
    }

    private List<MoveGoal> mapBoxesToGoals(Level level, List<Box> boxes){
        //init goal queue
        List<MoveGoal> queue = new ArrayList<>();

        logger.info("# of boxGoals in map: " + level.goals.size());

        //Set all boxes - assumes single box pr. goal
        HashMap<Character, List<Box>> boxSet = new HashMap<>(boxes.size());
        for(Box box : boxes){

            List<Box> list = boxSet.get(box.getLetter());

            //Nothing found
            if(list == null){
                list = new ArrayList<>();
                boxSet.put(box.getLetter(), list);
            }

            list.add(box);
        }

        //Maps resources for all boxGoals
        for(GoalField goal: level.goals){
            //Find box
            Box box = getAndRemoveBox(goal, boxSet.get(goal.getLetter()));

            Set<Square> resources = level.getResources(goal, box.row, box.col);
            queue.add(new MoveGoal(goal, resources, box));
        }

        return queue;
    }

    private Box getAndRemoveBox(GoalField goal, List<Box> boxes){
        int minDistance = Integer.MAX_VALUE;
        Box minBox = null;

        for(Box box : boxes){
            MinDistance dist = goal.getDistance(box.row, box.col);

            //It cannot reach - continue
            if(dist == null) continue;

            if(dist.d < minDistance){
                minBox = box;
                minDistance = dist.d;
            }
        }

        //Remove box from total list of boxes
        boxes.remove(minBox);

        return minBox;
    }
    
    private HashMap<Color, List<Goal>> colorSort(List<Goal> goals){
    	HashMap<Color, List<Goal>> colorGoals = new HashMap<>();
    	colorGoals.put(Color.blue, new ArrayList<>());
    	colorGoals.put(Color.red, new ArrayList<>());
    	colorGoals.put(Color.cyan, new ArrayList<>());
    	colorGoals.put(Color.magenta, new ArrayList<>());
    	colorGoals.put(Color.orange, new ArrayList<>());
    	colorGoals.put(Color.pink, new ArrayList<>());
    	colorGoals.put(Color.yellow, new ArrayList<>());
    	colorGoals.put(Color.green, new ArrayList<>());
    	for(Goal goal : goals){
            Color color = goal.getColor();
            
            if(color == null){ 
            	color = Color.blue;
            }

            logger.info("Added Goal " + goal.toString() + " to color " + color);

			colorGoals.get(color).add(goal);
		}    	
    	return colorGoals;	
    }

    private List<Goal> topSort(List<Goal> goals){
        List<Goal> visitedGoals = new ArrayList<>();
        List<Goal> sortedGoals = new ArrayList<>();
    	
    	for(Goal goal : goals){
    		visit(goal, null, visitedGoals, sortedGoals);
    	}
    	
		return sortedGoals; 	
    }

    public void addClearPathDependency(Goal prev, ClearPathGoal goal){
        //do not set backward dependency if it is the same agent color
        if(prev.getColor() == goal.getColor()) return;


        List<MoveGoal> colorGoals = orderedGoals.get(goal.color)
                .stream()
                .filter(g -> g instanceof MoveGoal)
                .map(g -> (MoveGoal) g)
                .collect(Collectors.toList());

        for(Box box : goal.getBoxes()){
            Optional<MoveGoal> boxGoal = colorGoals.stream()
                    .filter(g -> g.getBox().equalsID(box))
                    .findFirst();

            boxGoal.ifPresent(bg -> bg.addDependencyTo(prev));
        }

    }
    
    private void visit(Goal goal, Goal prev, List<Goal> visitedGoals, List<Goal> sortedGoals){
    	if(!visitedGoals.contains(goal)){
    		visitedGoals.add(goal);

			for(Goal dependency : new ArrayList<>(goal.getDependencyList())){
				//-JHL
				//MoveGoal mg = (MoveGoal) dependency;
				Goal mg = dependency;
				//+JHL
				visit(mg, goal, visitedGoals, sortedGoals);
			}
			sortedGoals.add(goal);
			logger.info("TopSort: " + goal);
		}else{
			if(!sortedGoals.contains(goal)){
                logger.error("(Warning) Cycle detected in topSort");
                if(prev != null){
                    logger.error("Relaxing dependency for goal " + goal + " to " + prev);
                    prev.relaxDependency(goal);
                }
			}
		}
    }
    
    public void prioritizeNode(Level lvl)
    {
    	fillOutNode(lvl);

    	Node startNode = lvl.graph.getLargestNode();
        if(startNode == null){
            return;
        }
    	ArrayDeque<Goal> goalQueue = new ArrayDeque<>();
    	
    	for(Edge edge : lvl.graph.edgeList)
    	{    		
    		edgeMap.put(edge, new EdgeGoal(null, edge, new ArrayList<>()));
    	}
    	
    	for(Node node : lvl.graph.nodeList)
    	{
    		nodeMap.put(node, new NodeGoal(null, node));
    		
    		for(Goal goal : node.getGoals()){
    			nodeMap.get(node).addDependencyTo(goal);
    		}
    	}
    	
    	visitNode(nodeMap.get(startNode), lvl, null, new ArrayList<>());
    	
    	for(EdgeGoal eg : edgeMap.values())
    	{
            if(eg.edge.next == null){
                logger.error("next null");
                continue;
            }
    		List<Box> tempBoxes = new ArrayList(eg.edge.next.getBoxes());    		
    		if(tempBoxes == null) continue;
    		
    		for(Box box : tempBoxes)
    		{
				edgeMap.get(eg.edge).addBox(box);			
    		}
    		
    		for(Square square : eg.edge.edges.values())
    		{    			
    			for(Goal goal : goals)
    			{
    				GoalField goalField = ((MoveGoal)goal).getGoalField();
    				
    				if(goalField.equals(square))
    				{
    					goal.addDependencyTo(eg);
    				}
    			}
    		}
    	}
    	
    	for(EdgeGoal ed : edgeMap.values())
    	{
    		logger.error(ed.toString() + ed.getNumberOfboxes());
    	}
    	
    	this.goals.addAll(nodeMap.values());
    	this.goals.addAll(edgeMap.values());
    }
    
    public void visitNode(NodeGoal nodeGoal, Level lvl, Edge from, List<NodeGoal> visitNodeGoal)
    {
    	List<Goal> goalSquares = nodeGoal.node.getGoals();
    	List<Element> passingElement = new ArrayList<>();
    	
    	Element boxPlaceElement;
    	Box tempBox;
    	Square tempSquare;
    	
    	if(visitNodeGoal.contains(nodeGoal))
    	{
    		return;
    	}

    	if(from != null){
    		edgeMap.get(from).addDependencyTo(nodeGoal);
    		from.next = nodeGoal.node;
    	}
    	visitNodeGoal.add(nodeGoal);
    	
    	/*for(Goal goal : goalSquares)
    	{
    		tempBox = ((MoveGoal)goal).getBox();
    		tempSquare = lvl.getSquare(tempBox.row, tempBox.col);    		
    		boxPlaceElement = lvl.graph.getElement(tempSquare);
    		logger.error("boxPlaceElemnt " + boxPlaceElement.getNumber() + " temBox " + tempBox.toString());
    		passingElement = lvl.graph.getPath(boxPlaceElement, (Element) nodeGoal.node);    		
    	}*/
    	
    	for(Edge edge : nodeGoal.node.getConnectedEdges())
    	{
    		if(from == null || !from.equals(edge))
    		{    			
    			nodeGoal.addDependencyTo(edgeMap.get(edge));
    			
    			for(Node node : edge.getConnectedNode())
    			{
    				edge.prev = nodeGoal.node;
    				//edge.next = node;    				
    				visitNode(nodeMap.get(node),lvl,edge,visitNodeGoal);
    			}
    		}    		
    	}
    }   
    
    
    
    public void fillOutNode(Level lvl)
    {
    	List<Node> nodesSF = lvl.graph.getNodeListSmallestFirst(); 
    	//List<Node> nodes = lvl.graph.getCriticalNode();
    	
    	for(int i = 0; i < goals.size(); i++)
    	{
    		if(goals.get(i) instanceof MoveGoal)
    		{
	    		for(Node node : nodesSF)
	    		{
	    			Square tempSquare = ((MoveGoal)goals.get(i)).getGoalField();
	    			
	    			if(node.isPartOf(tempSquare))
	    			{
	    				node.addGoal(goals.get(i));
	    			}
	    			
	    			Box tempBox = ((MoveGoal)goals.get(i)).getBox();
	    			tempSquare = lvl.getSquare(tempBox.row, tempBox.col);
	    			if(node.isPartOf(tempSquare))
	    			{
	    				node.addBox(tempBox);
	    			}
	    		}
    		}
    	}
    }
}
