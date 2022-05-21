package model.effects;

import java.util.ArrayList;

import model.abilities.Ability;
import model.abilities.AreaOfEffect;
import model.abilities.DamagingAbility;
import model.world.Champion;
import model.world.Condition;

public class Disarm extends Effect {
	private ArrayList indexes;

	public Disarm( int duration) {
		super("Disarm", duration, EffectType.DEBUFF);
		this.indexes = new ArrayList();
	}
	
	public void apply(Champion c) {
		
		c.getAbilities().add(new DamagingAbility("Punch",0,1,1,AreaOfEffect.SINGLETARGET,1,50));
		c.getAppliedEffects().add(this);
		
	}
	public void remove(Champion c) {
		c.getAbilities().remove(this);
		c.getAppliedEffects().remove(this);
		
	}
}
