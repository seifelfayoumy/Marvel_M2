package model.effects;

import model.abilities.AreaOfEffect;
import model.abilities.DamagingAbility;
import model.world.Champion;

public class Root extends Effect {

	public Root( int duration) {
		super("Root", duration, EffectType.DEBUFF);
		
	}
	
	public void apply(Champion c) {
		
		c.getAppliedEffects().add(this);
		
	}
	public void remove(Champion c) {
	
		c.getAppliedEffects().remove(this);
		
	}

}
