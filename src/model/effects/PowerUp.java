package model.effects;

import model.abilities.DamagingAbility;
import model.abilities.HealingAbility;
import model.world.Champion;

public class PowerUp extends Effect {
	

	public PowerUp(int duration) {
		super("PowerUp", duration, EffectType.BUFF);
		
	}
	
	public void apply(Champion c) {
//		c.setAttackDamage((int)(c.getAttackDamage()*1.2));
//		c.setCurrentHP((int)(c.getCurrentHP()*1.2));
		for(int i = 0;i<c.getAbilities().size();i++) {
			if(c.getAbilities().get(i) instanceof DamagingAbility) {
				DamagingAbility c1 = (DamagingAbility) c.getAbilities().get(i);
				c1.setDamageAmount((int)(c1.getDamageAmount()*1.2));
			}
			
			if(c.getAbilities().get(i) instanceof HealingAbility) {
				HealingAbility c1 = (HealingAbility) c.getAbilities().get(i);
				c1.setHealAmount((int)(c1.getHealAmount()*1.2));
			}
		}
		c.getAppliedEffects().add(this);
		
	}
	public void remove(Champion c) {
		for(int i = 0;i<c.getAbilities().size();i++) {
			if(c.getAbilities().get(i) instanceof DamagingAbility) {
				DamagingAbility c1 = (DamagingAbility) c.getAbilities().get(i);
				c1.setDamageAmount((int)(c1.getDamageAmount()/1.2));
			}
			
			if(c.getAbilities().get(i) instanceof HealingAbility) {
				HealingAbility c1 = (HealingAbility) c.getAbilities().get(i);
				c1.setHealAmount((int)(c1.getHealAmount()/1.2));
			}
		}
		c.getAppliedEffects().remove(this);
	}
	
}
