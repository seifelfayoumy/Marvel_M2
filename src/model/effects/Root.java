package model.effects;

import model.abilities.AreaOfEffect;
import model.abilities.DamagingAbility;
import model.world.Champion;
import model.world.Condition;

public class Root extends Effect {

	public Root( int duration) {
		super("Root", duration, EffectType.DEBUFF);
		
	}
	
	public void apply(Champion c) {
		if(!c.getCondition().equals(Condition.INACTIVE)) {
			c.setCondition(Condition.ROOTED);
		}
	
		c.getAppliedEffects().add(this);
		
	}
	public void remove(Champion c) {
		if(!c.getCondition().equals(Condition.INACTIVE)) {
			c.setCondition(Condition.ACTIVE);
		}
		c.getAppliedEffects().remove(this);
		
	}

}
