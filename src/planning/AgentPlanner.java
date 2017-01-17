package planning;

import java.util.*;
import java.util.stream.Collectors;

import actions.*;
import search.Node;
import search.SearchClient;
import core.*;
import enums.Direction;
import search.Heuristic;
import map.*;
import search.Strategy;


public class AgentPlanner {

	private Logger logger;

	public AgentPlanner(int number){
		this.logger = new Logger("Agent(" + number + ")");
	}

	public Result createPlan(CreatePlan cp){
		Square boxSquare = cp.getPreState().getLevel().getSquare(cp.getBox().row, cp.getBox().col);
		Square agentSquare = cp.getPreState().getLevel().getSquare(cp.getPreState().getAgent().getRow(), cp.getPreState().getAgent().getCol());
		Square goalSquare = ((MoveGoal)cp.getGoal()).getGoalField();

		try{
			//Get all actions to clear the way to the box
			ValidatePath vp1 = new ValidatePath(cp, agentSquare, boxSquare, cp.getPreState().getLevel().getResources(boxSquare, goalSquare));
			validatePath(vp1, cp.getBox());

			//This is literaly the same as if the goal is already completed. Shouldn't be tested here.
			if(boxSquare.getDistance(goalSquare.row, goalSquare.col).d > 0)
			{

				//New: add the validate path call AFTER
				ValidatePath vp2 = new ValidatePath(cp, boxSquare, goalSquare, vp1.getResources());
				validatePath(vp2,  cp.getBox());

				//If validating a path for a box to goal, add this together
				Goal continueTowardsGoal = cp.getContinueTowards();
				Square continueSquare = continueTowardsGoal == null ? null : continueTowardsGoal.getContinueTowardsSquare(cp.getPreState());

				moveBox(new MoveBox(vp2, cp.getBox(),  vp2.getToSquare()), continueSquare);
			}

		} catch(IncompletePlanException e) {
			logger.error("Incomplete topLevelAction received.");
			return new IncompletePlan(cp, e.state);
		} catch(WrongAgentColorException e){
			logger.error("The topLevelAction " + cp + " requires boxes to be removed by other agents.");

			//            e.printStackTrace();
			return new WrongColorPlan(cp, e.boxGoals, e.agentGoals);

		} catch(Exception e){
			logger.error("An error occurred.");
			e.printStackTrace();
			System.exit(0);
		}


		return new CompletedPlan(cp);
	}

	public Result clearPath(PlanClearPath pcp){
		State state = pcp.getPreState();
		Level level = state.getLevel();
		Square agentSquare = level.getSquare(state.getAgent().getRow(), state.getAgent().getCol());
		List<Box> boxes = pcp.getBoxes();
		Box first = boxes.get(0);
		Box last = boxes.get(boxes.size() - 1);
		Square boxSquare = level.getSquare(first.row, first.col);

		if(agentSquare.getDistance(first.row, first.col).d > agentSquare.getDistance(last.row, last.col).d){
			Collections.reverse(boxes);
		}

		try{
			validatePath(new ValidatePath(pcp, agentSquare, boxSquare), last);

			Set<Square> resources = level.getResources(agentSquare, boxSquare);

			if(!first.equals(last)){
				resources.addAll(level.getResources(boxSquare, last.row, last.col));
			}
			Square continueTowards = pcp.getContinueTowards() == null ? null : pcp.getContinueTowards().getContinueTowardsSquare(state);
			removeBoxes(new RemoveBoxes(pcp, boxes, agentSquare, resources), continueTowards);

		} catch(WrongAgentColorException e){
			return new WrongColorPlan(pcp, e.boxGoals, e.agentGoals);

		} catch (IncompletePlanException e){
			return new IncompletePlan(pcp, e.state);
		} catch( Exception e){
			e.printStackTrace();
			System.exit(0);
		}

		return new CompletedPlan(pcp);
	}

	public Result moveAgent(PlanMoveAgent pma){
		State state = pma.getPreState();
		Level level = state.getLevel();
		Square agentSquare = level.getSquare(state.getAgent().getRow(), state.getAgent().getCol());

		Square spot = findSaveSpot(state, agentSquare, pma.getResources(), 1, 0, null);

		if(spot == null){
			logger.error("SPOT ER ALTSÅ NULL");
			System.exit(0);
		}

		moveAgent(new MoveAgent(pma, agentSquare, spot));

        return new CompletedPlan(pma);
    }
    
    public Result solvedEdge(PlanSolvedEdge pse)
    {
    	State state = pse.getPreState();
    	Level level = state.getLevel();
    	Square agentSquare = level.getSquare(state.getAgent().getRow(), state.getAgent().getCol());
    	List<Box> boxes = ((EdgeGoal) pse.getGoal()).getBoxes(state);    	
    	Box first = boxes.get(0);
        Box last = boxes.get(boxes.size() - 1);
        Square boxSquare = level.getSquare(first.row, first.col);
    	
    	try
    	{    	
    		//pse.getGoal().toString();//there is a logger in the tostring
    		logger.error("Before validate in solvedEdge");
    		//validatePath(new ValidatePath(pse, agentSquare, boxSquare), last);
    		
    		logger.error("After validate in solvedEdge");
    		Set<Square> resources = level.getResources(agentSquare, boxSquare);
            resources.addAll(pse.getResources());
            	    
	    	Square continueTowards = pse.getContinueTowards() == null ? 
	    				null : pse.getContinueTowards().getContinueTowardsSquare(state);
	    	
	    	logger.error("Before removeBoxes solvedEdge");	    	
	    	removeBoxes(new RemoveBoxes(pse, boxes, agentSquare, resources), continueTowards);
	    	logger.error("After removeBoxes solvedEdge");
	    	
    		
    	//} //catch(WrongAgentColorException e){
    	//	logger.error("PlanSolvedEdge WrongAgentColorException");
        //    return new WrongColorPlan(pse, e.boxGoals, e.agentGoals);
        } catch (IncompletePlanException e){
            return new IncompletePlan(pse, e.state);
        } catch( Exception e){
            e.printStackTrace();
            System.exit(0);
        }
    	
    	return new CompletedPlan(pse);    
	}


	public Action validatePath(ValidatePath vp, Box goalBox) throws IncompletePlanException, WrongAgentColorException {

		State state = vp.getPreState();
		Level level = state.getLevel();
		Square goalSquare = level.getSquare(goalBox.row, goalBox.col);

		if(level == null) logger.error("Level is null");

		// NOTICE: This is turned around since the resources is returned in the order they are inserted from the to-square
		// We want it in the backward order to find the first closest box first
		Set<Square> resources = level.getResources(vp.getToSquare(), vp.getFromSquare());

		if(vp.getToSquare() != null){
			resources.add(vp.getToSquare());
		}

		vp.setResources(resources);

		BlockingElements blocking = investigateResources(state, resources, goalBox);

		RemoveBoxes rb = null;

		if(!blocking.boxes.isEmpty()){
			List<ClearPathGoal> goals = new ArrayList<>(blocking.boxes.size());
//			List<AgentGoal> agentGoals = new ArrayList<>(blocking.agents.size());

			List<Box> curBoxes = new ArrayList<>();
			Box prevBox = null;

			for(Box box : blocking.boxes){
				if(prevBox == null){
					curBoxes.add(box);
				} else if(prevBox.getColor() == box.getColor()){
					curBoxes.add(box);
				} else {
					//THere will always be a box in cur boxes - at least the previous box
					goals.add(new ClearPathGoal(curBoxes, goalBox.getColor()));
					curBoxes = new ArrayList<>();

					curBoxes.add(box);
				}
				prevBox = box;
			}

//			for(Agent agent : blocking.agents){
//				agentGoals.add(new AgentGoal(new HashSet<>(1),agent, resources));
//			}

			if(!curBoxes.isEmpty()){
				goals.add(new ClearPathGoal(curBoxes, goalBox.getColor()));
			}

			if( ! ( goals.size() == 1 && prevBox.getColor() == state.getAgent().getColor() ) ){
				logger.error("prevBox color " + prevBox.getColor() + ", agentColor: " + state.getAgent().getColor());
				throw new WrongAgentColorException(goals, new ArrayList<>());
			}

			//Please notice this is not really a "wrong agent color" issue.
			Agent agent = state.getAgent();
			Node node = searchCanMove(state,level.getSquare(agent.getRow(), agent.getCol()),blocking.boxes.get(0));

			if(node == null){
				blocking.boxes.add(0, goalBox);
			}

			//Compile all the boxes to be removed
			rb = new RemoveBoxes(vp, blocking.boxes, vp.getToSquare(), resources);
			removeBoxes(rb, goalSquare, vp.getPrevResources());
		} else {
			//Nothing is changed - set the changes of VP to the initial
			vp.setEffect(state);
		}

		return vp;
	}
	public RemoveBoxes removeBoxes(RemoveBoxes rb, Square goalSquare) throws IncompletePlanException{
		return removeBoxes(rb, goalSquare, null);
	}

	public RemoveBoxes removeBoxes(RemoveBoxes rb, Square goalSquare, Set<Square> prevResources) throws IncompletePlanException{
		// Find a save spot for each box - treat it as a compiled task
		// Find X number of save spots
		// Move boxes there, first come, first served
		// When a savespot has been assigned to each, assign move actions



		int depth =  rb.boxes.size();

		State state = rb.getPreState();
		Agent agent = state.getAgent();

		if(rb.boxes.isEmpty()) return rb;
		Box first = rb.boxes.get(0);
		Square agentSquare = state.getLevel().getSquare(agent.getRow(), agent.getCol());
		MinDistance dist = agentSquare.getDistance(first.row, first.col);

		Set<Square> resources = rb.resources;
		if(prevResources != null){
			prevResources.addAll(rb.resources);
			resources = prevResources;
		}

		//This is optimum. Period.
		resources.remove(agentSquare);
		
		ArrayDeque<Box> boxQueue = new ArrayDeque<>(rb.boxes);
		
		while(!boxQueue.isEmpty()) {
			Box box = boxQueue.pop();
			logger.info("Looking for a savespot for..." + box.toString() + ", depth: " + depth);
			
			Square spot = findSaveSpot(new State(state, agent, dist.d), state.getLevel().getSquare(box.row, box.col), resources, depth, 0, boxQueue.peek());

			if(spot == null){
//				if( (boxQueue.size() + 1) == depth){
//					rb.boxes.remove(depth - 1);
//					removeBoxes(rb, goalSquare, prevResources);
//					throw new IncompletePlanException(state);
//
//				}
				logger.error("SPOT ER ALTSÅ NULL");
				throw new IncompletePlanException(state);
			}

			logger.error("Savespot is: " + spot);
			
			MoveBox currentAction;
			Square sq = state.getLevel().getSquare(box.row, box.col);
			if(sq instanceof GoalField && ((GoalField) sq).getLetter() == box.getLetter()){
				//This is a box that has to be moved back again.
				currentAction = new MoveBox(rb, box, spot, sq);
			} else {
				currentAction = new MoveBox(rb, box, spot);
			}

			int distance = state.getDistance(agent, box, spot);

			//Get effect of MoveBox
			agent = new Agent(spot.row, spot.col, agent.getNumber(), agent.getColor());
			state = new State(state, agent, box, spot, distance);

			currentAction.setEffect(state);
			moveBox(currentAction, goalSquare);

			depth--;
			//            resources = rb.resources;
		}

		//Test if not enough save spots was found. What to do?
		//        if(saveSpots.size() < rb.boxes.size()){
		//            System.err.println("Didn't find enough save spots. Req: " + rb.boxes.size() + " Spots found: " + saveSpots.size() + " from: " + rb.fromSquare);
		//        }

		return rb;
	}

	public MoveBox moveBox(MoveBox mb, Square continueTowards) throws IncompletePlanException{
		State state = mb.getPreState();

		logger.error("Box to be moved: " + mb.getBox() + ", state box: " + state.getBox(mb.getBox().getID()));

		//Apply search
		SearchClient client = new SearchClient(state, state.getBox(mb.getBox().getID()) , mb.getSquare(), continueTowards);
		Strategy strategy = new Strategy.StrategyBestFirst(new Heuristic.WeightedAStar(client.initialState));
		Node solution = client.Search(strategy);

		if(solution == null){
			//Didn't found a solution for.
			logger.error("Didn't find a solution for " + mb.toString());
			logger.error("State: " + mb.getPreState().toString());
			throw new IncompletePlanException(mb.getPreState());
			//Error: what to do here?
		}

		//Get commands
		List<Command> commands = solution.extractPlan()
				.stream()
				.map(n -> n.action)
				.collect(Collectors.toList());

		MoveCommand mc = new MoveCommand(mb, commands);
		mc.setEffect(solution.state);


        return mb;
    }

    public MoveAgent moveAgent(MoveAgent ma){
        Node solution = searchCanMove(ma.getPreState(), ma.getFromSquare(), ma.getToSquare(), 2);

        List<Command> commands = solution.extractPlan()
                .stream()
                .map(n -> n.action)
                .collect(Collectors.toList());

        MoveCommand mc = new MoveCommand(ma, commands);
        mc.setEffect(solution.state);

        return ma;
    }

    private BlockingElements investigateResources(State state, Set<Square> resources, Box goalBox){
        //We need to go through the resources and:
        // - If we find a box of same color: Add it to our current list of MoveToSaveSpot boxes
        // - If we find a box of another color: Communicate to other color where to move it
        // -- We could assume in the beginning that this is not a case we consider
        // - If we found another box color previously, we need to treat the rest of the boxes as if they are a new set of MoveBox boxes

//        logger.info("Investigating resources...");

        //This assumes that all the found boxes are of the same color
        List<Box> boxes = new ArrayList<>();
        List<Agent> agents = new ArrayList<>();
        List<Box> boxesInARow = new ArrayList<>();
        List<Agent> agentsInARow = new ArrayList<>();
        boolean lastWasFree = true;
        Square lastFreeSquare = null;
        int time = 0;
        for(Square sq : resources){

            Box box = state.getBox(sq, time);
//            Agent agent = state.getAgent(sq, time);

            try{
                if(box != null) {
                    logger.info("Box found: " + box + " and goalbox is: " + goalBox);
                    if(!box.equals(goalBox)){

                        boxesInARow.add(box);
                        lastWasFree = false;
                    } else if(!lastWasFree) {

                        //It is the goalbox and there were a box before that
                        //Need to test if we can move from the last free square to the goal field
                        Node node = searchCanMove(state, lastFreeSquare, box);

                        if (node != null) {
                            boxesInARow.forEach(b -> logger.info("Don't need to remove: " + b));
                            agentsInARow.forEach(b -> logger.info("Don't need to remove: " + b));
                            boxesInARow.clear();
                            agentsInARow.clear();
                        } else {
                            boxesInARow.forEach(b -> logger.info("Need to remove: " + b));
                            agentsInARow.forEach(a -> logger.info("Need to remove: " + a));
                            boxes.addAll(boxesInARow);
                            agents.addAll(agentsInARow);
                            agentsInARow.clear();
                            boxesInARow.clear();
                        }
                    }
//                } else if(agent != null && !state.getAgent().equals(agent)){
//                    agentsInARow.add(agent);
//                    lastWasFree = false;
                } else {
                    if(lastWasFree){
                        lastFreeSquare = sq;
                    }else{
                        Node node;
                        if(lastFreeSquare == null){
                            //If goalbox is null, it yields the same result
                            node = searchCanMove(state, sq, goalBox);
                        } else {
                            node = searchCanMove(state, lastFreeSquare, sq, 1);
                        }

                        if(node != null) {
                            boxesInARow.forEach(b -> logger.info("Don't need to remove: " + b));
                            agentsInARow.forEach(b -> logger.info("Don't need to remove: " + b));
                            boxesInARow.clear();
                            agentsInARow.clear();
                        } else {
                            boxesInARow.forEach(b -> logger.info("Need to remove: " + b));
                            agentsInARow.forEach(a -> logger.info("Need to remove: " + a));
                            boxes.addAll(boxesInARow);
                            agents.addAll(agentsInARow);
                            agentsInARow.clear();
                            boxesInARow.clear();
                        }

                        lastFreeSquare = sq;
                        lastWasFree = true;
                    }
                }

                time++;

            }catch(Exception e){
                e.printStackTrace();
                System.exit(0);
            }
        }

        if(!boxesInARow.isEmpty()){
            boxes.addAll(boxesInARow);
        }

        if(!agentsInARow.isEmpty()){
            agents.addAll(agentsInARow);
        }

        return new BlockingElements(boxes, agents);
    }



    private Node searchCanMove(State state, Square from, Square to, int weight){
        if(from == null || to == null) {
            logger.error("From or to is null. Terminated searchCanMove (to square).");
            return null;
        }
        SearchClient client = new SearchClient(state, from, to);
        return client.Search(new Strategy.StrategyBestFirst(new Heuristic.WeightedAStar(client.initialState)), weight * 15);
    }

    /**
     * Should find out if it is possible to come from square to the position of the given box.
     * @param state
     * @param from
     * @param to This box is removed from a copy of the state and then searched as the goal square.
     * @return
     */
    private Node searchCanMove(State state, Square from, Box to){
        if(from == null || to == null) {
            logger.error("From or to is null. Terminated searchCanMove (to box).");
            return null;
        }
        State newState = new State(state, to);
        Square next = newState.getLevel().getSquare(to.row, to.col);

        SearchClient client = new SearchClient(newState, from, next);
        return client.Search(new Strategy.StrategyBestFirst(new Heuristic.WeightedAStar(client.initialState)), 3 * from.getDistance(next.row, next.col).d);

	}

	/*public MoveAgent moveAgent(MoveAgent ma){
		Node solution = searchCanMove(ma.getPreState(), ma.getFromSquare(), ma.getToSquare(), 2);

		List<Command> commands = solution.extractPlan()
				.stream()
				.map(n -> n.action)
				.collect(Collectors.toList());

		MoveCommand mc = new MoveCommand(ma, commands);
		mc.setEffect(solution.state);

		return ma;
	}*/

	/*private BlockingElements investigateResources(State state, Set<Square> resources, Box goalBox){
		//We need to go through the resources and:
		// - If we find a box of same color: Add it to our current list of MoveToSaveSpot boxes
		// - If we find a box of another color: Communicate to other color where to move it
		// -- We could assume in the beginning that this is not a case we consider
		// - If we found another box color previously, we need to treat the rest of the boxes as if they are a new set of MoveBox boxes

		//        logger.info("Investigating resources...");

		//This assumes that all the found boxes are of the same color
		List<Box> boxes = new ArrayList<>();
		List<Agent> agents = new ArrayList<>();
		List<Box> boxesInARow = new ArrayList<>();
		List<Agent> agentsInARow = new ArrayList<>();
		boolean lastWasFree = true;
		Square lastFreeSquare = null;
		int time = 0;
		for(Square sq : resources){

			Box box = state.getBox(sq, time);
			//            Agent agent = state.getAgent(sq, time);

			try{
				if(box != null) {
					logger.info("Box found: " + box + " and goalbox is: " + goalBox);
					if(!box.equals(goalBox)){

						boxesInARow.add(box);
						lastWasFree = false;
					} else if(!lastWasFree) {

						//It is the goalbox and there were a box before that
						//Need to test if we can move from the last free square to the goal field
						Node node = searchCanMove(state, lastFreeSquare, box);

						if (node != null) {
							boxesInARow.forEach(b -> logger.info("Don't need to remove: " + b));
							agentsInARow.forEach(b -> logger.info("Don't need to remove: " + b));
							boxesInARow.clear();
							agentsInARow.clear();
						} else {
							boxesInARow.forEach(b -> logger.info("Need to remove: " + b));
							agentsInARow.forEach(a -> logger.info("Need to remove: " + a));
							boxes.addAll(boxesInARow);
							agents.addAll(agentsInARow);
							agentsInARow.clear();
							boxesInARow.clear();
						}
					}
					//                } else if(agent != null && !state.getAgent().equals(agent)){
					//                    agentsInARow.add(agent);
					//                    lastWasFree = false;
				} else {
					if(lastWasFree){
						lastFreeSquare = sq;
					}else{
						Node node;
						if(lastFreeSquare == null){
							//If goalbox is null, it yields the same result
							node = searchCanMove(state, sq, goalBox);
						} else {
							node = searchCanMove(state, lastFreeSquare, sq, 1);
						}

						if(node != null) {
							boxesInARow.forEach(b -> logger.info("Don't need to remove: " + b));
							agentsInARow.forEach(b -> logger.info("Don't need to remove: " + b));
							boxesInARow.clear();
							agentsInARow.clear();
						} else {
							boxesInARow.forEach(b -> logger.info("Need to remove: " + b));
							agentsInARow.forEach(a -> logger.info("Need to remove: " + a));
							boxes.addAll(boxesInARow);
							agents.addAll(agentsInARow);
							agentsInARow.clear();
							boxesInARow.clear();
						}

						lastFreeSquare = sq;
						lastWasFree = true;
					}
				}

				time++;

			}catch(Exception e){
				e.printStackTrace();
				System.exit(0);
			}
		}

		if(!boxesInARow.isEmpty()){
			boxes.addAll(boxesInARow);
		}

		if(!agentsInARow.isEmpty()){
			agents.addAll(agentsInARow);
		}

		return new BlockingElements(boxes, agents);
	}*/


	/*private Node searchCanMove(State state, Square from, Square to, int weight){
		if(from == null || to == null) {
			logger.error("From or to is null. Terminated searchCanMove (to square).");
			return null;
		}
		SearchClient client = new SearchClient(state, from, to);
		return client.Search(new Strategy.StrategyBestFirst(new Heuristic.WeightedAStar(client.initialState)), weight * 15);
	}*/

	/**
	 * Should find out if it is possible to come from square to the position of the given box.
	 * @param state
	 * @param from
	 * @param to This box is removed from a copy of the state and then searched as the goal square.
	 * @return
	 */
	/*private Node searchCanMove(State state, Square from, Box to){
		if(from == null || to == null) {
			logger.error("From or to is null. Terminated searchCanMove (to box).");
			return null;
		}
		State newState = new State(state, to);
		Square next = newState.getLevel().getSquare(to.row, to.col);

		SearchClient client = new SearchClient(newState, from, next);
		return client.Search(new Strategy.StrategyBestFirst(new Heuristic.WeightedAStar(client.initialState)), 2 * from.getDistance(next.row, next.col).d);

	}*/


	private boolean searchCanSwapPosition(State state, Square from, Box to){
		if(from == null || to == null) {
			logger.error("From or to is null. Terminated seachCanSwapPosition (to box).");
			return true;
		}
		SearchClient client = new SearchClient(state, to);
		Node node = client.Search(new Strategy.StrategyBestFirst(new Heuristic.WeightedAStar(client.initialState)), 3 * from.getDistance(from.row, from.col).d);

		if(node != null) return true;

		return false;
	}


	private boolean isLegalSafeSpot(State state, Box box, Square square, Direction boxDir){
		//TODO: Y this? Why null box?
		if(box == null) return false;
		if(boxDir != null){
			logger.info("Investigating savespot: " + square.toString() + "And testing direction: " + boxDir);

			List<Square> squares = new ArrayList<>(2);

			for(Direction d : Direction.values()){
				if(d != boxDir && d.ordinal() + boxDir.ordinal() != 3){
					squares.add(state.getLevel().getSquare(square, d));
				}
			}

			State temp = new State(state, state.getAgent(), box, square, 0);

			Node node = searchCanMove(temp, squares.get(0), squares.get(1), 1);

			if(node == null){
				return false;
			}
		}

		return true;
	}

	public Square findSaveSpot(State state, Square fromSquare, Set<Square> resources, int depth, int greedLevel, Box nextBox){
		ArrayDeque<Square> queue = new ArrayDeque<>();
		boolean[][] checkedSquares = new boolean[state.getLevel().MAX_ROW][state.getLevel().MAX_COLUMN];
		int found = 0;
		Square spot = null;
		
		if(resources == null){
			resources = new HashSet<Square>();
		}

		queue.add(fromSquare);
		while (!queue.isEmpty()){
			Square square = queue.poll();
			int row = square.row;
			int col = square.col;


			//Enough is found
			if(found == depth) break;

			//Check if already processed
			if(checkedSquares[row][col]) continue;

			//Set is seen
			checkedSquares[row][col] = true;

			//if true, then it's a savespot
			
				Direction boxDir = state.blockingSaveSpot(square);

				if(checkSaveSpot(greedLevel, resources, state, square, fromSquare, nextBox)){
					found++;
					if(found == depth){
						spot = square;
						logger.error("Found: " + spot.toString() + " - count: " + found);
						break;
					}
				}

			//Add adjacent fields
			for(Direction d : Direction.values()){
				Square next = state.getLevel().getSquare(square, d);
				//Can return null if outside bounds
				if(next == null) continue;


				//Test if it is passable and there's not a box on that field
				boolean canMove = next.isPassable() && state.getBox(next) == null;
				if(canMove) queue.add(next);
			}
		}
		if(spot == null) {
			if(greedLevel != 2){

				greedLevel += 1;
//				Logger.goalCount("GREED LEVEL "+ greedLevel +" ACTIVATED");
				spot = findSaveSpot(state, fromSquare, resources, depth+1+(greedLevel%2), greedLevel, nextBox);
			} else if(depth >= 0) {
				spot = findSaveSpot(state, fromSquare, resources, depth-1, greedLevel, nextBox);
			}
		}
//		Logger.goalCount("Spot: " + spot);
		return spot;
	}

	private boolean checkSaveSpot(int greedLevel, Set<Square> resources, State state, Square square, Square fromSquare, Box nextBox){
		int row = square.row;
		int col = square.col;

		switch (greedLevel) {
		case 0:
			if(square.getIsSaveSpot() && !resources.contains(square))
			{
				if(nextBox != null){

					Agent tempAgent = new Agent(fromSquare.row, fromSquare.col, state.getAgent().getNumber(), state.getAgent().getColor());
					Box curBox = state.getBox(fromSquare.row, fromSquare.col);

					if(curBox == null){
						return false;
					}

					State tempState = new State(state, tempAgent, curBox, square, 1);
					if(!searchCanSwapPosition(tempState, state.getLevel().getSquare(fromSquare.row, fromSquare.col), nextBox)){
						return false;
					}
				}

				Direction boxDir = state.blockingSaveSpot(square);
				if(isLegalSafeSpot(state, state.getBox(fromSquare), square, boxDir) && !state.blockingDiagonals(square) && state.canAllocateSaveSpot(row, col)){
					return true;
				}
			}
			break;
		case 1:
			if(!resources.contains(square))
			{
				if(state.canAllocateSaveSpot(row, col) && !(square instanceof GoalField)){
					return true;
				}
			}
			break;
		case 2:
			if(!resources.contains(square))
			{
				if(state.canAllocateSaveSpot(row, col)){
					return true;
				}
			}
			break;
		}

		return false;
	}


}
