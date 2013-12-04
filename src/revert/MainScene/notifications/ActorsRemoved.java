package revert.MainScene.notifications;

import java.util.Set;

import revert.Entities.Actor;

public class ActorsRemoved 
{
	public Set<Actor> actors;
	
	public ActorsRemoved(Set<Actor> a)
	{
		actors = a;
	}
}