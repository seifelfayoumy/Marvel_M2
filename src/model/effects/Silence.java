package model.effects;

import model.world.Champion;

public class Silence extends Effect {

	public Silence( int duration) {
		super("Silence", duration, EffectType.DEBUFF);
		
	}

	public void apply(Champion c) {
		c.setMaxActionPointsPerTurn(c.getMaxActionPointsPerTurn() +2);
		c.setCurrentActionPoints(c.getCurrentActionPoints() +2);
//		c.getAppliedEffects().add(this);
		
	}
	public void remove(Champion c) {
		c.setMaxActionPointsPerTurn(c.getMaxActionPointsPerTurn() -2);
		c.setCurrentActionPoints(c.getCurrentActionPoints() -2);
		c.getAppliedEffects().remove(this);
	}
}
