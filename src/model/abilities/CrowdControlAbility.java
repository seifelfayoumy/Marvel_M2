package model.abilities;

import java.util.ArrayList;

import model.effects.Effect;
import model.world.Champion;
import model.world.Damageable;

public class CrowdControlAbility extends Ability {
	private Effect effect;

	public CrowdControlAbility(String name, int cost, int baseCoolDown, int castRadius, AreaOfEffect area, int required,
			Effect effect) {
		super(name, cost, baseCoolDown, castRadius, area, required);
		this.effect = effect;

	}

	public Effect getEffect() {
		return effect;
	}

	public void execute(ArrayList<Damageable> targets) throws CloneNotSupportedException{
		Effect e = effect;
		for(int i = 0;i <targets.size();i++) {
			e = (Effect) effect.clone();
		
				e.apply((Champion)targets.get(i));
				Champion c1 = ((Champion) targets.get(i));
				c1.getAppliedEffects().add((Effect) e.clone());

//			if(targets.get(i) instanceof Champion) {
//				e.apply((Champion) targets.get(i));
//				Champion c1 = ((Champion) targets.get(i));
//				c1.getAppliedEffects().add((Effect) e.clone());
//			}
			
		}
	}
}
