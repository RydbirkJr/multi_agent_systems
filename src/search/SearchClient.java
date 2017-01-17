package search;

import core.*;
import enums.SearchType;
import map.Square;

public class SearchClient {

	public static int max_depth = 300;
	public static int max_explored = 3000;

	// Auxiliary static classes
	public static void error( String msg ) throws Exception {
		throw new Exception( "GSCError: " + msg );
	}

	public Node initialState = null;
	private Logger logger;

	public SearchClient( State state, Box box, Square to, Square continueTowards) {
		this.logger = new Logger("Agent(" + state.getAgent().getNumber() + ")");
		this.initialState = new Node(state, logger, box, to, continueTowards, SearchType.BoxToSquare);
	}

	public SearchClient(State state, Square from, Square to){
        this.logger = new Logger("Agent(" + state.getAgent().getNumber() + ")");
		Agent agent = state.getAgent();
		state = new State(state, new Agent( from.row, from.col,agent.getNumber(),agent.getColor() ), state.time);
		this.initialState = new Node(state, logger, null, to, null, SearchType.AgentToSquare);
	}

	public SearchClient(State state, Logger logger){
		this.logger = logger;
		this.initialState = new Node(state, logger, null, null, null, SearchType.AgentOutOfTheWay);

	}

    public SearchClient(State state, Box box){
        this.logger = new Logger("Agent(" + state.getAgent().getNumber() + ")");
        this.initialState = new Node(state, logger, box);
    }

	public Node Search(Strategy strategy){
		return Search(strategy, max_depth);
	}

	public Node Search( Strategy strategy, int maxDepth ) {

		logger.info(String.format("Search starting with strategy %s", strategy));
		String type = this.initialState.box != null ? this.initialState.box.toString() : "Agent";
		logger.info("Searching from (" + type+ "): " + this.initialState.getFromSquare() + " to: " + this.initialState.goal);
		strategy.addToFrontier(this.initialState);

        Node leafNode = null;

		int iterations = 0;
		while ( true ) {
			if ( iterations % 1000 == 0 ) {
				logger.info(strategy.searchStatus());
			}
			if ( Memory.shouldEnd() ) {
				logger.error(String.format("Memory limit almost reached, terminating search %s\n", Memory.stringRep()));
				return null;
			}
			if ( strategy.timeSpent() > 300 ) { // Minutes timeout
				logger.error(String.format("Time limit reached, terminating search %s\n", Memory.stringRep()));
				return null;
			}

			if ( strategy.frontierIsEmpty() ) {
				logger.error( "Frontier is empty. Returns null." );
				logger.error( strategy.searchStatus());
				return null;
			}

			leafNode = strategy.getAndRemoveLeaf();

			if ( leafNode.isGoalState() ) {
				logger.info("Summary for " + strategy);
				logger.info("Found solution of length " + leafNode.g());
				logger.info(strategy.searchStatus());
				return leafNode;
			}

			if(maxDepth != 0){

				if(leafNode.g() > maxDepth) {
					logger.error( "Terminated search because of max depth." );
					return null;
				}
			}

			if(strategy.explored.size() > max_explored){
				logger.error( "Terminated search because of explored size." );
				return null;
			}

			strategy.addToExplored( leafNode );
			for ( Node n : leafNode.getExpandedNodes() ) { // The list of expanded nodes is shuffled randomly; see Node.java
				if ( !strategy.isExplored( n ) && !strategy.inFrontier( n ) ) {
					strategy.addToFrontier( n );
				}
			}
			iterations++;
		}
	}

//	public static void main( String[] args ) throws Exception {


//		Strategy strategy = null;
//		strategy = new StrategyBFS();
		// Ex 1:
//		strategy = new StrategyDFS();

		// Ex 3:
		//strategy = new BestFirst( new AStar( client.initialState ) );
//		strategy = new Strategy.StrategyBestFirst( new Heuristic.WeightedAStar( client.initialState ) );
//		strategy = new Strategy.StrategyBestFirst( new Heuristic.Greedy( client.initialState ) );
//		strategy = new Strategy.StrategyIDA( new Heuristic.AStar( client.initialState ) );

//		LinkedList< Node > solution = client.Search( strategy );

//		if ( solution == null ) {
//			System.err.println( "Unable to solve level" );
//			System.exit( 0 );
//		} else {

			//This is for sending to server - responsibility moved to ServerClient
//			for ( Node n : solution ) {
//				String act = n.action.toActionString();
//				System.out.println( act );
//				String response = serverMessages.readLine();
//				if ( response.contains( "false" ) ) {
//					System.err.format( "Server responsed with %s to the inapplicable action: %s\n", response, act );
//					System.err.format( "%s was attempted in \n%s\n", act, n );
//					break;
//				}
//			}
//		}

//	}
}
