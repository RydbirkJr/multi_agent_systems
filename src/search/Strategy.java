package search;

import core.Memory;
import enums.Type;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.PriorityQueue;

public abstract class Strategy {

	public HashSet<Node> explored;
	public long startTime = System.currentTimeMillis();
	private static final int NO_OP_MAX = 5;

	public Strategy() {
		explored = new HashSet< Node >();
	}

	public void addToExplored( Node n ) {
		explored.add( n );
	}

	public boolean isExplored( Node n ) {
		//if it is not initial state, and the type is noop and the noOpCount is below 10, do not flag as explored;
		//it is false if it is initial state, if it is not a noop or if it is above noop max
		boolean isNoOp = !n.isInitialState() && n.action.actType == Type.NoOp && n.noUpCount < NO_OP_MAX;
		return explored.contains( n ) && !isNoOp;
	}

	public int countExplored() {
		return explored.size();
	}

	public String searchStatus() {
		return String.format( "#Explored: %4d, #Frontier: %3d, Time: %3.2f s \t%s", countExplored(), countFrontier(), timeSpent(), Memory.stringRep() );
	}

	public float timeSpent() {
		return ( System.currentTimeMillis() - startTime ) / 1000f;
	}

	public abstract Node getAndRemoveLeaf();

	public abstract void addToFrontier( Node n );

	public abstract boolean inFrontier( Node n );

	public abstract int countFrontier();

	public abstract boolean frontierIsEmpty();

	public abstract String toString();

	public static class StrategyBFS extends Strategy {

		private ArrayDeque< Node > frontier;

		public StrategyBFS() {
			super();
			frontier = new ArrayDeque< Node >();
		}

		public Node getAndRemoveLeaf() {
			return frontier.pollFirst();
		}

		public void addToFrontier( Node n ) {
			frontier.addLast( n );
		}

		public int countFrontier() {
			return frontier.size();
		}

		public boolean frontierIsEmpty() {
			return frontier.isEmpty();
		}

		public boolean inFrontier( Node n ) {
			return frontier.contains( n );
		}

		public String toString() {
			return "Breadth-first Search";
		}
	}

	public static class StrategyDFS extends Strategy {
		private ArrayDeque<Node> frontier;

		public StrategyDFS() {
			super();
			frontier = new ArrayDeque< Node >();
		}

		public Node getAndRemoveLeaf() {
			return frontier.pollLast();
		}

		public void addToFrontier( Node n ) {
			frontier.add(n);
		}

		public int countFrontier() {
			return frontier.size();
		}

		public boolean frontierIsEmpty() {
			return frontier.isEmpty();
		}

		public boolean inFrontier( Node n ) {
			return frontier.contains(n);
		}

		public String toString() {
			return "Depth-first Search";
		}
	}

	// Ex 3: Best-first Search uses a priority queue (Java contains no implementation of a Heap data structure)
	public static class StrategyBestFirst extends Strategy {
		private Heuristic heuristic;
		private PriorityQueue<Node> heap;


		public StrategyBestFirst( Heuristic h ) {
			super();
			heuristic = h;
			heap = new PriorityQueue<Node>(h);
		}
		public Node getAndRemoveLeaf() {
			return heap.poll();
		}

		public void addToFrontier( Node n ) {
			heap.add(n);
		}

		public int countFrontier() {

			return heap.size();
		}

		public boolean frontierIsEmpty() {

			return heap.isEmpty();
		}

		public boolean inFrontier( Node n ) {
			return heap.contains(n);
		}

		public String toString() {
			return "Best-first Search (PriorityQueue) using " + heuristic.toString();
		}
	}

	public static class StrategyIDA extends Strategy {
		private Heuristic heuristic;
		private ArrayDeque< Node > frontier;
		private int bound;
        private int min;
		private Node root;
        private int count;


		public StrategyIDA( Heuristic h ) {
			super();
			frontier = new ArrayDeque< Node >();
			heuristic = h;
            min = Integer.MAX_VALUE;
            count = 0;
		}

		public Node getAndRemoveLeaf() {
			return frontier.pollLast();
		}

		public void addToFrontier( Node n ) {
		    if(root == null){
                root = n;
                //set initial bound
                bound = heuristic.h(root);
                frontier.add(root);

            } else {
                int tempBound = heuristic.f(n);

                if(bound >= tempBound){
                    frontier.add(n);
                } else {
                    this.min = (int) Math.min(this.min, tempBound);
                }
            }
		}

		public int countFrontier() {

			return frontier.size();
		}

		public boolean frontierIsEmpty() {
            if(frontier.isEmpty()){
                //if(min == bound) return true;
                bound = min; //== bound ? min +1 : min;
                min = Integer.MAX_VALUE;
                frontier.add(root);
                explored.clear();
            }
            return false;
		}

        @Override
        public void addToExplored( Node n ) {
            count++;
        }

        @Override
        public int countExplored(){
            return count;
        }

		public boolean inFrontier( Node n ) {
			return frontier.contains(n);
		}

		public String toString() {
			return "Iterative-Depening " + heuristic.toString() + " Search";
		}
	}
}
