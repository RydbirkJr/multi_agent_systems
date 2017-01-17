package search;

import core.Agent;
import core.Logger;
import map.Square;

import java.util.Comparator;

public abstract class Heuristic implements Comparator<Node> {

    public Node initialState;

    public Heuristic(Node initialState) {
        this.initialState = initialState;
    }

//    private void setGoals(Node initialState){
//        this.boxGoals = new ArrayList<GoalOld>();
//
//        for(int row = 0; row < initialState.MAX_ROW; row++){
//            for(int col = 0; col < initialState.MAX_COLUMN; col++){
//                char goal = initialState.boxGoals[row][col];
//                if(goal > 0){
//                    boxGoals.add(new GoalOld(goal, row, col,initialState.walls));
//                }
//            }
//        }
//    }

//    private boolean isFinished(GoalOld goal, Node n){
//        char box = n.boxes[goal.row][goal.col];
//        return Character.toUpperCase(goal.letter) == box;
//    }

//    private int getEuclidDistance(int startRow, int startCol, int endRow, int endCol){
//        int a = (int) Math.pow(startRow - endRow, 2);
//        int b = (int) Math.pow(startCol - endCol, 2);
//        return (int) Math.sqrt(a + b);
//    }

//    private int secondHeuristic(Node n){
//        int temp = 0;
//        for(GoalOld goal : boxGoals){
//            if(isFinished(goal, n)) continue;
//            temp += getCombinedGoalDistance(goal, n);
//        }
//        return temp;
//    }

//    private int getCombinedGoalDistance(GoalOld goal, Node n){
//        char letter = Character.toUpperCase(goal.letter);
//        int distance = Integer.MAX_VALUE;
//        for(int i = 0; i < n.MAX_ROW; i++){
//            for(int k = 0; k < n.MAX_COLUMN; k++){
//                if(n.boxes[i][k] == letter) {
//                    distance = Math.min(getDistance(i,k,n,goal), distance);
//                }
//            }
//        }
//        return distance;
//    }

//    private int getDistance(int row, int col, Node n, GoalOld goal){
//        return goal.minimumDistance[row][col] + getEuclidDistance(n.agentRow, n.agentCol, row, col);
//    }

//    private int firstHeuristic(Node state){
//        GoalOld g = null;
//        for(GoalOld goal : boxGoals){
//        char box = state.boxes[goal.row][goal.col];
//            if(Character.toUpperCase(goal.letter) != box) {
//                g = goal;
//                break;
//            };
//        }
//
//        if(g == null) return 0;
//
//        return getEuclidDistance(state.agentRow, state.agentCol, g.row, g.col);
//    }

    /**
     * Compare method
     * @param n1
     * @param n2
     * @return
     */
    public int compare( Node n1, Node n2 ) {
        return f( n1 ) - f( n2 );
    }

    /**
     * Heuristic function
     * Finds the distance from goal to the bo and from the agent to the box and combines them.
     * @param n
     * @return
     */
    public int h( Node n ) {

        switch (n.searchType){
            case BoxToSquare:
            case AgentToSquare:
                Square from = n.getFromSquare();
                Square goal = n.goal;
                Agent agent = n.state.getAgent();

                int agentDist = 0;
                if(n.expectedAgentPlacement != null){
                    agentDist = n.expectedAgentPlacement.getDistance(agent.getRow(), agent.getCol()).d;
                }

                return goal.getDistance(from.row, from.col).d
                        + n.state.getLevel()
                        .getSquare(agent.getRow(), agent.getCol())
                        .getDistance(from.row, from.col).d - 1
                        + agentDist;

            case AgentOutOfTheWay: return n.g();
            default:
                Logger.global("Unknown search type in heuristic");
                System.exit(0);
                return 0;
        }
    }

    /**
     * Abstract function for f() to be implemented in extensions
     * @param n
     * @return
     */
    public abstract int f( Node n );

    /**
     * A* implementation
     */
    public static class AStar extends Heuristic {
        public AStar(Node initialState) {
            super( initialState );
        }

        public int f( Node n ) {
            return n.g() + h( n );
        }

        public String toString() {
            return "A* evaluation";
        }
    }

    /**
     * Weighted A* implementation
     */
    public static class WeightedAStar extends Heuristic {
        private int W;

        public WeightedAStar(Node initialState) {
            super( initialState );
            W = 5; // You're welcome to test this out with different values, but for the reporting part you must at least indicate benchmarks for W = 5
        }

        public int f( Node n ) {
            return n.g() + W * h( n );
        }

        public String toString() {
            return String.format( "WA*(%d) evaluation", W );
        }
    }

    /**
     * Greedy implementation
     */
    public static class Greedy extends Heuristic {

        public Greedy(Node initialState) {
            super( initialState );
        }

        public int f( Node n ) {
            return h( n );
        }

        public String toString() {
            return "Greedy evaluation";
        }
    }
}
