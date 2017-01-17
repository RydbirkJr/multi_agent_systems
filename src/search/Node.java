package search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

import core.*;
import enums.Direction;
import enums.SearchType;
import enums.Type;
import map.MinDistance;
import map.Square;
import planning.Goal;
import planning.MoveGoal;


public class Node {

	private static Random rnd = new Random( 1 );
	private final Logger logger;
	public final State state;
	public final Square goal;
	public final Box box;
	public final Square expectedAgentPlacement;
	public final int noUpCount;
	public final SearchType searchType;

	// Arrays are indexed from the top-left of the level, with first index being row and second being column.
	// Row 0: (0,0) (0,1) (0,2) (0,3) ...
	// Row 1: (1,0) (1,1) (1,2) (1,3) ...
	// Row 2: (2,0) (2,1) (2,2) (2,3) ...
	// ...
	// (Start in the top left corner, first go down, then go right)
	// E.g. walls[2] is an array of booleans having size MAX_GRID
	// walls[row][col] is true if there's a wall at (row, col)

	public Node parent;
	public Command action;

	private int g;

	/**
	 * This is the initializing constructor. Sets the goal square and initial state.
	 * @param state The initial map state.
	 * @param box The box to get to the goal. If it is null, the agent is moved towards the goal.
	 * @param goal The square where the box is heading.
     * @param continueTowards The square that the agent should be placed towards after completion
	 */
	public Node(State state, Logger logger, Box box, Square goal, Square continueTowards, SearchType searchType){
		this.logger = logger;
		this.state = state;
		this.box = box;
		this.goal = goal;
		this.searchType = searchType;
		this.expectedAgentPlacement = calculateAgentPlacement(continueTowards);
		this.g = 0;
		this.noUpCount = 0;
	}

	/**
	 * Swap box and agent position. If possible, the agent can turn with the current box.
	 * @param state
	 * @param logger
	 * @param box
	 */
	public Node(State state, Logger logger, Box box){
		this.logger = logger;
		this.state = state;
		this.box = box;
		this.goal = state.getLevel().getSquare(state.getAgent().getRow(), state.getAgent().getCol());
		this.searchType = SearchType.BoxToSquare;
		this.expectedAgentPlacement = state.getLevel().getSquare(box.row, box.col);
		this.g = 0;
		this.noUpCount = 0;
	}

	/**
	 * This constructor is used AFTER the initialisation. Thus, this is
	 * called always except for the initial node.
	 * @param parent The previous node in the search
	 * @param state The current state - placement of boxes etc.
	 * @param box The box we're trying to get to the goal.
	 */
	public Node( Node parent, Command cmd, State state, Box box) {
		this.parent = parent;
		this.searchType = parent.searchType;
		this.logger = parent.logger;
		this.box = box;
		this.goal = parent.goal;
        this.expectedAgentPlacement = parent.expectedAgentPlacement;
		this.state = state;
		this.action = cmd;

		if(cmd.actType == Type.NoOp){
			this.noUpCount = parent.noUpCount + 1;
		} else {
			this.noUpCount = parent.noUpCount;
		}

		if ( parent != null ) {
			this.g = parent.g() + 1;
		}
	}

    /**
     * Finds the square of the next box to move and places the agent on that square.
     * @param continueTowards
     * @return
     */
    private Square calculateAgentPlacement(Square nextSquare){

        if(nextSquare == null) return null;



		logger.info("Continue with... Looking for shortest distance to next square: " + nextSquare + " from square: " + this.goal);

        Square square = null;
        int dist = Integer.MAX_VALUE;
        for(Direction d : Direction.values()){
            Square temp = state.getLevel().getSquare(goal, d);
            if(temp != null && temp.isPassable()){
                //continue if a box is placed on the field
                if(state.getBox(temp) != null) continue;

                //Else find distance and assign field if better placement than before.
                MinDistance min = temp.getDistance(nextSquare.row, nextSquare.col);

                if(min != null && min.d < dist){
					dist = min.d;
                    square = temp;
                }
            }
        }

        logger.info("Continue with agent at: " + square);

        return square;
    }

	public int g() {
		return g;
	}

	public boolean isInitialState() {
		return this.parent == null;
	}

	public boolean isGoalState() {


		switch (searchType){
			case BoxToSquare:
			case AgentToSquare:
				boolean agentPlacedCorrect = true;
				Square square;
				Agent agent = state.getAgent();
				if(box == null){

					square = state.getLevel().getSquare(agent.getRow(), agent.getCol());
				} else {
					if(expectedAgentPlacement != null) {
//                Logger.global("Agent placement: " + agent + " goal square: " + expectedAgentPlacement + " distance: " + expectedAgentPlacement.getDistance(agent.getRow(), agent.getCol()).d);
						agentPlacedCorrect = expectedAgentPlacement.equals(state.getLevel().getSquare(agent.getRow(), agent.getCol()));
					}

					square = state.getLevel().getSquare(box.row, box.col);
				}

				return square.row == goal.row && square.col == goal.col && agentPlacedCorrect;

			case AgentOutOfTheWay:
				return state.canAllocateSaveSpot(state.getAgent().getRow(), state.getAgent().getCol());
			default:
				logger.error("Unknown search type");
				System.exit(0);
				return false;
		}
	}

	public ArrayList< Node > getExpandedNodes() {
		ArrayList< Node > expandedNodes = new ArrayList< Node >( Command.every.length );
		Agent oldAgent = state.getAgent();

		if(!isLegalState()){
//			logger.error("Not legal state: " + oldAgent + " in time: " + state.time);
			return new ArrayList<>();
		}

		for ( Command c : Command.every ) {
			// Determine applicability of action

			int newAgentRow = oldAgent.getRow() + dirToRowChange( c.dir1 );
			int newAgentCol = oldAgent.getCol() + dirToColChange( c.dir1 );

			Agent newAgent = new Agent(
					newAgentRow,
					newAgentCol,
					oldAgent.getNumber(),
					oldAgent.getColor());

            if(c.actType == Type.NoOp){
                Node n = this.ChildNode(c, oldAgent);
                expandedNodes.add(n);
            }else if ( c.actType == Type.Move ) {
				// Check if there's a wall or box on the cell to which the agent is moving
				if ( state.cellIsFree( newAgentRow, newAgentCol ) ) {
					Node n = this.ChildNode(c, newAgent);
					expandedNodes.add( n );
				}
			} else if ( c.actType == Type.Push ) {
				Box box = state.getBox(newAgentRow, newAgentCol);

				// Make sure that there's actually a box to move
				if ( box != null && canMoveBox(box)) {

					int newBoxRow = newAgentRow + dirToRowChange(c.dir2);
					int newBoxCol = newAgentCol + dirToColChange( c.dir2 );

					// .. and that new cell of box is free
					if ( state.cellIsFree( newBoxRow, newBoxCol ) ) {
						Node n = this.ChildNode(c, newAgent, box , state.getLevel().getSquare(newBoxRow, newBoxCol));
						expandedNodes.add( n );
					}
				}
			} else if ( c.actType == Type.Pull ) {
				// Cell is free where agent is going
				if ( state.cellIsFree( newAgentRow, newAgentCol ) ) {
					int boxRow = this.state.getAgent().getRow()+ dirToRowChange( c.dir2 );
					int boxCol = this.state.getAgent().getCol() + dirToColChange( c.dir2 );

					Box curBox = state.getBox(boxRow, boxCol);
					// .. and there's a box in "dir2" of the agent
					if ( curBox != null && canMoveBox(curBox)) {
						Node n = this.ChildNode(c, newAgent, curBox, this.state.getLevel().getSquare(this.state.getAgent().getRow(), this.state.getAgent().getCol()));
						expandedNodes.add( n );
					}
				}
			}
		}
		Collections.shuffle( expandedNodes, rnd );
		return expandedNodes;
	}

	private boolean isLegalState(){
		Square boxSquare = getFromSquare();
		Agent agent = state.getAgent();

		if(! state.cellIsAllocated( agent.getRow(),agent.getCol() ) && !state.cellIsAllocated( boxSquare.row,boxSquare.col ) ){
			//Not resource allocated
			return true;
		}
		//This state cannot be used anymore
		return false;
	}


	private boolean canMoveBox(Box box){
		//either move all boxes or only the targeted box
		return box.equalsID(this.box);
	}

	private int dirToRowChange( Direction d ) {
		return ( d == Direction.S ? 1 : ( d == Direction.N ? -1 : 0 ) ); // South is down one row (1), north is up one row (-1)
	}

	private int dirToColChange( Direction d ) {
		return ( d == Direction.E ? 1 : ( d == Direction.W ? -1 : 0 ) ); // East is left one column (1), west is right one column (-1)
	}

	/**
	 * Returns a new node for when only the agent has been moved.
	 * @param agent The new agent position.
	 * @return
	 */
	private Node ChildNode(Command cmd, Agent agent) {
		Node copy = new Node(this, cmd, new State(this.state, agent, 1), this.box);
		return copy;
	}

	/**
	 * Returns a new node where a box has been moved.
	 * @param agent The updated agent
	 * @param box The box to be moved. If it matches the goal box, the goal box is updated.
	 * @param to The new spot for the box
	 * @return
	 */
	private Node ChildNode(Command cmd, Agent agent, Box box, Square to) {

        //Create new state
		State newState = new State(this.state, agent, box, to, 1);

		//In case the moved box is the goal box, update the box
		Box tempBox = box.equalsID(this.box) ? newState.getBox(to) : this.box;

		Node copy = new Node(this, cmd, newState, tempBox);
		return copy;
	}

	public LinkedList< Node > extractPlan() {
		LinkedList< Node > plan = new LinkedList< Node >();
		Node n = this;
		while( !n.isInitialState() ) {
			plan.addFirst( n );
			n = n.parent;
		}
		return plan;
	}

	public Square getFromSquare(){
		if(box == null){
			Agent agent = state.getAgent();
			return state.getLevel().getSquare(agent.getRow(), agent.getCol());
		} else {
			return state.getLevel().getSquare(box.row, box.col);
		}
	}

	@Override
	public int hashCode() {
		return state.hashCode();
	}

	@Override
	public boolean equals( Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		Node other = (Node) obj;

		Agent curAgent = state.getAgent();
		if ( !curAgent.equals(other.state.getAgent()) )
			return false;
		if ( !state.getBoxes().equals(other.state.getBoxes()) ) {
			return false;
		}

		//TODO: We do not compare on goal and box to move at the moment!
		return true;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
//		for ( int row = 0; row < MAX_ROW; row++ ) {
//			if ( !this.walls[row][0] ) {
//				break;
//			}
//			for ( int col = 0; col < MAX_COLUMN; col++ ) {
//				if ( this.boxes[row][col] > 0 ) {
//					s.append( this.boxes[row][col] );
//				} else if ( this.boxGoals[row][col] > 0 ) {
//					s.append( this.boxGoals[row][col] );
//				} else if ( this.walls[row][col] ) {
//					s.append( "+" );
//				} else if ( row == this.agentRow && col == this.agentCol ) {
//					s.append( "0" );
//				} else {
//					s.append( " " );
//				}
//			}
//
//			s.append( "\n" );
//		}
		return s.toString();
	}

}