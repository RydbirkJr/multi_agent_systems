package actions;


public class NotValid extends Action{
	
	Action lastAction;
	
	public NotValid(Action lastAction)
	{

		super(lastAction);
		this.lastAction = lastAction;
	}
	
	public Action getLastAction() {
		return lastAction;
	}

	public String toString(){
		return "NotValid: " + lastAction.toString();
	}
}
