package model.world;

import java.util.ArrayList;

import model.effects.Embrace;

public class Villain extends Champion {

	public Villain(String name, int maxHP, int maxMana, int actions, int speed, int attackRange, int attackDamage) {
		super(name, maxHP, maxMana, actions, speed, attackRange, attackDamage);

	}

	public void useLeaderAbility(ArrayList<Champion> targets) {
		for(int i = 0;i<targets.size();i++) {
			if(targets.get(i).getCurrentHP() <= 30) {
				targets.remove(i);
			}
		}
	}

	
}
