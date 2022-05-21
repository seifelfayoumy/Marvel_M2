package model.effects;

import model.world.Champion;

public class PowerUp extends Effect {
	

	public PowerUp(int duration) {
		super("PowerUp", duration, EffectType.BUFF);
		
	}
	
	public void apply(Champion c) {
		c.setAttackDamage((int)(c.getAttackDamage()*1.2));
		c.getAppliedEffects().add(this);
		
	}
	public void remove(Champion c) {
		c.setAttackDamage((int)(c.getAttackDamage()/1.2));
		c.getAppliedEffects().remove(this);
	}
	
}
