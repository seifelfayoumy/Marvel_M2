package model.effects;

import model.world.Champion;

public class Shield extends Effect {

	public Shield( int duration) {
		super("Shield", duration, EffectType.BUFF);
		
	}
	
	public void apply(Champion c) {
		c.setSpeed((int)(c.getSpeed()*1.02));
		c.getAppliedEffects().add(this);
	}
	public void remove(Champion c) {
		c.setSpeed((int)(c.getSpeed()*0.98));
		c.getAbilities().remove(this);
	}

}
