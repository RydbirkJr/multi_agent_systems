package core;

import java.util.LinkedList;

import enums.Direction;
import enums.Type;

public class Command {
	public final Type actType;
	public final Direction dir1; //Agent
	public final Direction dir2; //Box

	public final static Command[] every;

	/**
	 * Static initializer. Is run the first time the class is called.
	 */
	static {
		LinkedList< Command > cmds = new LinkedList< Command >();

		for ( Direction d1 : Direction.values() ) {
			for ( Direction d2 : Direction.values() ) {
				if ( !Command.isOpposite( d1, d2 ) ) {
					cmds.add( new Command( Type.Push, d1, d2 ) );
				}
			}
		}
		for ( Direction d1 : Direction.values() ) {
			for ( Direction d2 : Direction.values() ) {
				if ( d1 != d2 ) {
					cmds.add( new Command( Type.Pull, d1, d2 ) );
				}
			}
		}

		for ( Direction d : Direction.values() ) {
			cmds.add( new Command( d ) );
		}

		cmds.add(new Command());

		every = cmds.toArray( new Command[0] );
	}

	private static boolean isOpposite( Direction d1, Direction d2 ) {
		return d1.ordinal() + d2.ordinal() == 3;
	}

	/**
	 * Constructor
	 * @param d
	 */
	public Command( Direction d ) {
		actType = Type.Move;
		dir1 = d;
		dir2 = null;
	}

	public Command(){
		actType = Type.NoOp;
		dir1 = null;
		dir2 = null;
	}

	public Command( Type t, Direction d1, Direction d2 ) {
		actType = t;
		dir1 = d1;
		dir2 = d2;
	}

	public String toString() {
		String res = "";
		switch (actType){
			case Move:
				res = actType.toString() + "(" + dir1 + ")";
				break;
			case Pull:
				//Fall through
			case Push:
				res = actType.toString() + "(" + dir1 + "," + dir2 + ")";
				break;
			case NoOp:
				res = actType.toString();
				break;
		}
		return res;
	}
}
