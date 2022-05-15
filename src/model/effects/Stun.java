package model.effects;

import model.world.Champion;
import model.world.Condition;

public class Stun extends Effect {

	public Stun(int duration) {
		super("Stun", duration, EffectType.DEBUFF);
	}
	
	public void apply(Champion c) {
		c.setCondition(Condition.INACTIVE);
		c.getAppliedEffects().add(this);
		
	}
	public void remove(Champion c) {
		c.setCondition(Condition.ACTIVE);
		c.getAbilities().remove(this);
	}


}
