package model.world;

import java.util.ArrayList;

import model.effects.Effect;
import model.effects.Embrace;

public class Hero extends Champion {

	public Hero(String name, int maxHP, int maxMana, int actions, int speed, int attackRange, int attackDamage) {
		super(name, maxHP, maxMana, actions, speed, attackRange, attackDamage);

	}


	public void useLeaderAbility(ArrayList<Champion> targets) {
		for(int i = 0;i<targets.size();i++) {
			for(int j =0;i<targets.get(i).getAppliedEffects().size();j++) {
				String name = targets.get(i).getAppliedEffects().get(j).getName();
				if(name.equals("Disarm")||name.equals("Root")||name.equals("Shock")||name.equals("Silence")||name.equals("Stun")) {
					targets.get(i).getAppliedEffects().remove(j);
				}
			}
			targets.get(i).getAppliedEffects().add(new Embrace(2));
		}
	}
}
