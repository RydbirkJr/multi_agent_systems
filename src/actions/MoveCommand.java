package actions;

import java.util.List;

import core.Agent;
import core.Command;

public class MoveCommand extends Action{
	public final List<Command> commands;	

	public MoveCommand(Action parent, List<Command> commands)
	{
		super(parent);
		this.commands = commands;
	}

	public String toString(){
		return "MoveCommand(length:" + commands.size() + ")";
	}
}
