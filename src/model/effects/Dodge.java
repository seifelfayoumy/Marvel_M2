package model.effects;

import model.world.Champion;
import model.world.Condition;

public class Dodge extends Effect {

	public Dodge(int duration) {
		super("Dodge", duration, EffectType.BUFF);
		
	}
	
	public void apply(Champion c) {
		c.setSpeed((int)(c.getSpeed()*1.05));
		c.setCondition(Condition.ROOTED);
		c.getAppliedEffects().add(this);
	}
	public void remove(Champion c) {
		c.setSpeed((int)(c.getSpeed()*0.95));
		c.setCondition(Condition.ACTIVE);
		c.getAbilities().remove(this);
	}

}
