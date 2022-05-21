package engine;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import exceptions.AbilityUseException;
import exceptions.ChampionDisarmedException;
import exceptions.InvalidTargetException;
import exceptions.LeaderAbilityAlreadyUsedException;
import exceptions.LeaderNotCurrentException;
import exceptions.NotEnoughResourcesException;
import exceptions.UnallowedMovementException;
import model.abilities.Ability;
import model.abilities.AreaOfEffect;
import model.abilities.CrowdControlAbility;
import model.abilities.DamagingAbility;
import model.abilities.HealingAbility;
import model.effects.Disarm;
import model.effects.Dodge;
import model.effects.Effect;
import model.effects.EffectType;
import model.effects.Embrace;
import model.effects.PowerUp;
import model.effects.Root;
import model.effects.Shield;
import model.effects.Shock;
import model.effects.Silence;
import model.effects.SpeedUp;
import model.effects.Stun;
import model.world.AntiHero;
import model.world.Champion;
import model.world.Condition;
import model.world.Cover;
import model.world.Damageable;
import model.world.Direction;
import model.world.Hero;
import model.world.Villain;

public class Game {
	private static ArrayList<Champion> availableChampions;
	private static ArrayList<Ability> availableAbilities;
	private Player firstPlayer;
	private Player secondPlayer;
	private Object[][] board;
	private PriorityQueue turnOrder;
	private boolean firstLeaderAbilityUsed;
	private boolean secondLeaderAbilityUsed;
	private final static int BOARDWIDTH = 5;
	private final static int BOARDHEIGHT = 5;

	public Game(Player first, Player second) {
		firstPlayer = first;

		secondPlayer = second;
		availableChampions = new ArrayList<Champion>();
		availableAbilities = new ArrayList<Ability>();
		board = new Object[BOARDWIDTH][BOARDHEIGHT];
		turnOrder = new PriorityQueue(6);
		placeChampions();
		placeCovers();
		this.prepareChampionTurns();
	}

	public static void loadAbilities(String filePath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String line = br.readLine();
		while (line != null) {
			String[] content = line.split(",");
			Ability a = null;
			AreaOfEffect ar = null;
			switch (content[5]) {
			case "SINGLETARGET":
				ar = AreaOfEffect.SINGLETARGET;
				break;
			case "TEAMTARGET":
				ar = AreaOfEffect.TEAMTARGET;
				break;
			case "SURROUND":
				ar = AreaOfEffect.SURROUND;
				break;
			case "DIRECTIONAL":
				ar = AreaOfEffect.DIRECTIONAL;
				break;
			case "SELFTARGET":
				ar = AreaOfEffect.SELFTARGET;
				break;

			}
			Effect e = null;
			if (content[0].equals("CC")) {
				switch (content[7]) {
				case "Disarm":
					e = new Disarm(Integer.parseInt(content[8]));
					break;
				case "Dodge":
					e = new Dodge(Integer.parseInt(content[8]));
					break;
				case "Embrace":
					e = new Embrace(Integer.parseInt(content[8]));
					break;
				case "PowerUp":
					e = new PowerUp(Integer.parseInt(content[8]));
					break;
				case "Root":
					e = new Root(Integer.parseInt(content[8]));
					break;
				case "Shield":
					e = new Shield(Integer.parseInt(content[8]));
					break;
				case "Shock":
					e = new Shock(Integer.parseInt(content[8]));
					break;
				case "Silence":
					e = new Silence(Integer.parseInt(content[8]));
					break;
				case "SpeedUp":
					e = new SpeedUp(Integer.parseInt(content[8]));
					break;
				case "Stun":
					e = new Stun(Integer.parseInt(content[8]));
					break;
				}
			}
			switch (content[0]) {
			case "CC":
				a = new CrowdControlAbility(content[1], Integer.parseInt(content[2]), Integer.parseInt(content[4]),
						Integer.parseInt(content[3]), ar, Integer.parseInt(content[6]), e);
				break;
			case "DMG":
				a = new DamagingAbility(content[1], Integer.parseInt(content[2]), Integer.parseInt(content[4]),
						Integer.parseInt(content[3]), ar, Integer.parseInt(content[6]), Integer.parseInt(content[7]));
				break;
			case "HEL":
				a = new HealingAbility(content[1], Integer.parseInt(content[2]), Integer.parseInt(content[4]),
						Integer.parseInt(content[3]), ar, Integer.parseInt(content[6]), Integer.parseInt(content[7]));
				break;
			}
			availableAbilities.add(a);
			line = br.readLine();
		}
		br.close();
	}

	public static void loadChampions(String filePath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String line = br.readLine();
		while (line != null) {
			String[] content = line.split(",");
			Champion c = null;
			switch (content[0]) {
			case "A":
				c = new AntiHero(content[1], Integer.parseInt(content[2]), Integer.parseInt(content[3]),
						Integer.parseInt(content[4]), Integer.parseInt(content[5]), Integer.parseInt(content[6]),
						Integer.parseInt(content[7]));
				break;

			case "H":
				c = new Hero(content[1], Integer.parseInt(content[2]), Integer.parseInt(content[3]),
						Integer.parseInt(content[4]), Integer.parseInt(content[5]), Integer.parseInt(content[6]),
						Integer.parseInt(content[7]));
				break;
			case "V":
				c = new Villain(content[1], Integer.parseInt(content[2]), Integer.parseInt(content[3]),
						Integer.parseInt(content[4]), Integer.parseInt(content[5]), Integer.parseInt(content[6]),
						Integer.parseInt(content[7]));
				break;
			}

			c.getAbilities().add(findAbilityByName(content[8]));
			c.getAbilities().add(findAbilityByName(content[9]));
			c.getAbilities().add(findAbilityByName(content[10]));
			availableChampions.add(c);
			line = br.readLine();
		}
		br.close();
	}

	private static Ability findAbilityByName(String name) {
		for (Ability a : availableAbilities) {
			if (a.getName().equals(name))
				return a;
		}
		return null;
	}

	public void placeCovers() {
		int i = 0;
		while (i < 5) {
			int x = ((int) (Math.random() * (BOARDWIDTH - 2))) + 1;
			int y = (int) (Math.random() * BOARDHEIGHT);

			if (board[x][y] == null) {
				board[x][y] = new Cover(x, y);
				i++;
			}
		}

	}

	public void placeChampions() {
		int i = 1;
		for (Champion c : firstPlayer.getTeam()) {
			board[0][i] = c;
			c.setLocation(new Point(0, i));
			i++;
		}
		i = 1;
		for (Champion c : secondPlayer.getTeam()) {
			board[BOARDHEIGHT - 1][i] = c;
			c.setLocation(new Point(BOARDHEIGHT - 1, i));
			i++;
		}
	
	}
	
	public Champion getCurrentChampion() {
		if(!this.getTurnOrder().isEmpty()) {
			Champion c =  (Champion) turnOrder.peekMin();
			return c;
		}
		return null;
		
	}

	public Player checkGameOver() {
		if(this.getAvailableChampions().size() == 1) {
			for(int i = 0;i<this.getFirstPlayer().getTeam().size();i++) {
				if(this.getFirstPlayer().getTeam().get(i).getName().equals(this.getAvailableChampions().get(0).getName())) {
					return this.getFirstPlayer();
				}
			}
			return this.getSecondPlayer();

		}
		return null;
	}
	
	public void move(Direction d) throws UnallowedMovementException, NotEnoughResourcesException {
		Champion c = this.getCurrentChampion();
		for(int i = 0; i<c.getAppliedEffects().size();i++) {
			if(c.getAppliedEffects().get(i).getName().equals("Root")) {
				throw new UnallowedMovementException();
			}
		}
		
		if(this.getCurrentChampion().getCurrentActionPoints() > 0) {
			this.getCurrentChampion().setCurrentActionPoints(this.getCurrentChampion().getCurrentActionPoints() - 1);
			
			if(d.equals(Direction.UP)) {
				if(c.getLocation().getX() < 4 && this.getBoard()[(int) (c.getLocation().getX()+1)][(int) c.getLocation().getY()] == null) {
					this.board[(int) (c.getLocation().getX() + 1)][(int) c.getLocation().getY()] = c;
					this.board[(int) c.getLocation().getX()][(int) c.getLocation().getY()] = null;
					c.setLocation(new Point((int)(c.getLocation().getX()+1),(int)((c.getLocation().getY()))));
				}else {
					throw new UnallowedMovementException();
				}
			}
			if(d.equals(Direction.DOWN)) {
				if(c.getLocation().getX() > 0 && this.getBoard()[(int) (c.getLocation().getX()-1)][(int) c.getLocation().getY()] == null) {
					this.board[(int) (c.getLocation().getX() -1)][(int) c.getLocation().getY()] = c;
					this.board[(int) c.getLocation().getX()][(int) c.getLocation().getY()] = null;
					c.setLocation(new Point((int)(c.getLocation().getX()-1),(int)((c.getLocation().getY()))));
				}else {
					throw new UnallowedMovementException();
				}
			}
			if(d.equals(Direction.RIGHT)) {
				if(c.getLocation().getY() < 4 && this.getBoard()[(int) c.getLocation().getX()][(int) (c.getLocation().getY()+1)] == null) {
					this.board[(int) c.getLocation().getX()][(int) (c.getLocation().getY()+1)] = c;
					this.board[(int) c.getLocation().getX()][(int) c.getLocation().getY()] = null;
					c.setLocation(new Point((int)(c.getLocation().getX()),(int)((c.getLocation().getY()+1))));
				}else {
					throw new UnallowedMovementException();
				}
			}
			if(d.equals(Direction.LEFT)) {
				if(c.getLocation().getY() > 0 && this.getBoard()[(int) c.getLocation().getX()][(int) (c.getLocation().getY()-1)] == null) {
					this.board[(int) c.getLocation().getX()][(int) (c.getLocation().getY() -1)] = c;
					this.board[(int) c.getLocation().getX()][(int) c.getLocation().getY()] = null;
					c.setLocation(new Point((int)(c.getLocation().getX()),(int)((c.getLocation().getY()-1))));
				}else {
					throw new UnallowedMovementException();
				}
			}
		}else {
			throw new NotEnoughResourcesException();
		}
	}
	
	public void attack(Direction d) throws NotEnoughResourcesException, ChampionDisarmedException{
			
		
		Champion c = this.getCurrentChampion();
		for(int i = 0; i<c.getAppliedEffects().size();i++) {
			if(c.getAppliedEffects().get(i).getName().equals("Disarm")) {
				throw new ChampionDisarmedException();
			}
			
		}

		boolean isShock = false;
		boolean isFirstPlayer = false;
		boolean isSecondPlayer = false;
		Player player = null;
		

		for(int i = 0; i < this.getFirstPlayer().getTeam().size();i++) {
			if(this.getFirstPlayer().getTeam().get(i) == c) {
				isFirstPlayer = true;
				player = this.getFirstPlayer();
			}
		}
		for(int i = 0; i < this.getSecondPlayer().getTeam().size();i++) {
			if(this.getSecondPlayer().getTeam().get(i) == c) {
				isSecondPlayer = true;
				player = this.getSecondPlayer();
			}
		}
		

		if(c.getCurrentActionPoints() >= 2) {
			c.setCurrentActionPoints(c.getCurrentActionPoints() -2);
			
			int nearest = 99999;
			Damageable nearestChampion = null;
			int range = c.getAttackRange();
			int damage = c.getAttackDamage();
			ArrayList<Champion> targets = new ArrayList<Champion>();
			boolean myTeam = false;
			for(int i = 0;i<this.getFirstPlayer().getTeam().size();i++) {
				targets.add(this.getFirstPlayer().getTeam().get(i));
			}
			for(int i = 0;i<this.getSecondPlayer().getTeam().size();i++) {
				targets.add(this.getSecondPlayer().getTeam().get(i));
			}
			for (int i = 0; i<targets.size();i++) {
				myTeam = false;
				Damageable temp = targets.get(i);
				
				for(int j = 0;j<player.getTeam().size();j++) {
					Champion ch = player.getTeam().get(j);
					if(ch == temp) {
						myTeam = true;
					}
				}
				if(temp != c && myTeam == false) {
					if(d.equals(Direction.UP)) {
						if(c.getLocation().getY() == temp.getLocation().getY() && temp.getLocation().getX()> c.getLocation().getX()) {
							int distance = (int) (temp.getLocation().getX() - c.getLocation().getX());
							if (distance <= nearest) {
								nearest = distance;
								nearestChampion = temp;
								
							}
						}
					}
					if(d.equals(Direction.DOWN)) {
						if(c.getLocation().getY() == temp.getLocation().getY() && temp.getLocation().getX() < c.getLocation().getX()) {
							int distance = (int) (c.getLocation().getX() - temp.getLocation().getX());
							if (distance <= nearest) {
								nearest = distance;
								nearestChampion = temp;
								
							}
						}
					}
					if(d.equals(Direction.RIGHT)) {
						if(c.getLocation().getY() < temp.getLocation().getY() && temp.getLocation().getX() == c.getLocation().getX()) {
							int distance = (int) (temp.getLocation().getY() - c.getLocation().getY());
							if (distance <= nearest) {
								nearest = distance;
								nearestChampion = temp;
								
							}
						}
					}
					if(d.equals(Direction.LEFT)) {
						if(c.getLocation().getY() > temp.getLocation().getY() && temp.getLocation().getX() == c.getLocation().getX()) {
							int distance = (int) (c.getLocation().getY() - temp.getLocation().getY());
							if (distance <= nearest) {
								nearest = distance;
								nearestChampion = temp;
								
							}
							
						}
					}
				}

			}

				
			if( nearest < range && nearestChampion != null ) {
				Champion nearestCh = (Champion) nearestChampion;
				if(nearestChampion instanceof Champion) {
					
					for(int i = 0; i<nearestCh.getAppliedEffects().size();i++) {
						if(nearestCh.getAppliedEffects().get(i).getName().equals("Disarm")) {
							throw new ChampionDisarmedException();
						}
						if(nearestCh.getAppliedEffects().get(i).getName().equals("Dodge")){
							if(Math.random() < 0.5) {
								return;
							}
						}
						if(nearestCh.getAppliedEffects().get(i).getName().equals("Shield")){
							nearestCh.getAppliedEffects().remove(i);
							return;
							
						}
						if(nearestCh.getAppliedEffects().get(i).getName().equals("Shock")){
							isShock = true;
						}
						
					}
				}
				int damageTmp =c.getAttackDamage();
				if(isShock) {
					damageTmp = (int) ((int) damageTmp *0.9);
				}
				

				if((nearestChampion instanceof Hero && c instanceof AntiHero || nearestChampion instanceof Cover )
						|| (nearestChampion instanceof Hero && c instanceof Villain)
						|| (nearestChampion instanceof Villain && c instanceof Hero)
						|| (nearestChampion instanceof Villain && c instanceof AntiHero)
						|| (nearestChampion instanceof AntiHero && c instanceof Hero)
						|| (nearestChampion instanceof AntiHero && c instanceof Villain)) {
					nearestChampion.setCurrentHP((nearestChampion.getCurrentHP() - ((int)(damageTmp * 1.5))));
					
				}else {
					nearestChampion.setCurrentHP((nearestChampion.getCurrentHP() - damageTmp));
				}
				if(nearestChampion.getCurrentHP() <=0 && nearestChampion instanceof Champion) {
					board[(int)(nearestChampion.getLocation().getX())][(int)(nearestChampion.getLocation().getY())] = null;
					nearestCh.setCondition(Condition.KNOCKEDOUT);
				}
			}
			
		}else {
			throw new NotEnoughResourcesException();
		}

	}
	
	public void castAbility(Ability a) throws AbilityUseException,NotEnoughResourcesException,CloneNotSupportedException {
		
		Champion c = this.getCurrentChampion();
		ArrayList<Damageable> targets = new ArrayList<Damageable>();
		
		if(a.getRequiredActionPoints() > c.getCurrentActionPoints()) {
			throw new  NotEnoughResourcesException();
		}else {
			c.setCurrentActionPoints(c.getCurrentActionPoints() - a.getRequiredActionPoints());
		}
		if(a.getManaCost() > c.getMana()) {
			throw new  NotEnoughResourcesException();
		}else {
			c.setMana(c.getMana() - a.getManaCost());
		}
		if(a.getCurrentCooldown() != 0) {
			throw new  AbilityUseException();
		}
		else if(a.getCurrentCooldown() == 0) {
			a.setCurrentCooldown(a.getBaseCooldown());
		}else {
			a.setCurrentCooldown(a.getCurrentCooldown() - 1);
		}
		boolean isFirstPlayer = false;
		boolean isSecondPlayer = false;
		Player player = null;
		

		for(int i = 0; i < this.getFirstPlayer().getTeam().size();i++) {
			if(this.getFirstPlayer().getTeam().get(i) == c) {
				isFirstPlayer = true;
				player = this.getFirstPlayer();
			}
		}
		for(int i = 0; i < this.getSecondPlayer().getTeam().size();i++) {
			if(this.getSecondPlayer().getTeam().get(i) == c) {
				isSecondPlayer = true;
				player = this.getSecondPlayer();
			}
		}
		
		
		if(a.getCastArea().equals(AreaOfEffect.TEAMTARGET)) {
			if(a instanceof HealingAbility) {
				for(int i = 0; i < player.getTeam().size();i++) {
					targets.add((Damageable) player.getTeam().get(i));
				}
			}else if(a instanceof DamagingAbility) {
				if(isFirstPlayer) {
					for(int i = 0; i < this.getSecondPlayer().getTeam().size();i++) {
						targets.add((Damageable) this.getSecondPlayer().getTeam().get(i));
					}
				}else {
					for(int i = 0; i < this.getFirstPlayer().getTeam().size();i++) {
						targets.add((Damageable) this.getFirstPlayer().getTeam().get(i));
					}
				}
				
			}else {
				CrowdControlAbility cc = (CrowdControlAbility) a;
				if(cc.getEffect().getType().equals(EffectType.BUFF)) {
					for(int i = 0; i < player.getTeam().size();i++) {
						targets.add((Damageable) player.getTeam().get(i));
					}
				}else {
					for(int j = 0; j<c.getAppliedEffects().size();j++) {
						if(c.getAppliedEffects().get(j).getName().equals("Disarm")){
							c.getAppliedEffects().remove(j);
						    targets.remove(c);
							
						}
					}
					if(isFirstPlayer) {
						for(int i = 0; i < this.getFirstPlayer().getTeam().size();i++) {
							targets.add((Damageable) this.getFirstPlayer().getTeam().get(i));
						}
					}else {
						for(int i = 0; i < this.getSecondPlayer().getTeam().size();i++) {
							targets.add((Damageable) this.getSecondPlayer().getTeam().get(i));
						}
					}
				}
			}
			if(!a.getCastArea().equals(AreaOfEffect.SURROUND)) {
				int range = a.getCastRange();
				for (int i =0;i<targets.size();i++) {
					Champion temp = (Champion) targets.get(i);
					int distance = (int) (Math.abs(temp.getLocation().getX()-c.getLocation().getX()) + Math.abs(temp.getLocation().getY()-c.getLocation().getY()));
					if(distance > range) {
						targets.remove(i);
					}
				}
			}
		}else if(a.getCastArea().equals(AreaOfEffect.SELFTARGET)){
			if(a instanceof HealingAbility) {
					targets.add((Damageable) c);
				
			}else if(a instanceof DamagingAbility) {
				throw new AbilityUseException();
				
			}else {
				CrowdControlAbility cc = (CrowdControlAbility) a;
				if(cc.getEffect().getType().equals(EffectType.BUFF)) {
					targets.add((Damageable) c);
				}else {
					throw new AbilityUseException();
				}
			}
			if(!a.getCastArea().equals(AreaOfEffect.SURROUND)) {
				int range = a.getCastRange();
				for (int i =0;i<targets.size();i++) {
					Champion temp = (Champion) targets.get(i);
					int distance = (int) (Math.abs(temp.getLocation().getX()-c.getLocation().getX()) + Math.abs(temp.getLocation().getY()-c.getLocation().getY()));
					if(distance > range) {
						targets.remove(i);
					}
				}
			}
		}else if(a.getCastArea().equals(AreaOfEffect.SURROUND)) {
			if(a instanceof HealingAbility) {
				for(int i = 0; i < player.getTeam().size();i++) {
					targets.add((Damageable) player.getTeam().get(i));
					for (int j =0;j<targets.size();j++) {
						Damageable temp = (Damageable) targets.get(j);
						int distanceX = (int) (Math.abs(c.getLocation().getX() - temp.getLocation().getX()));
						int distanceY= (int) (Math.abs(c.getLocation().getY()-temp.getLocation().getY()));
						if((distanceX >1 || distanceY>1) || temp == c){
							targets.remove(j);
						}
					}
				}
			}else if(a instanceof DamagingAbility) {
				if(isFirstPlayer) {
					for(int i = 0; i < this.getSecondPlayer().getTeam().size();i++) {
						targets.add((Damageable) this.getSecondPlayer().getTeam().get(i));
						for (int j =0;j<targets.size();j++) {
							Damageable temp = (Damageable) targets.get(j);
							int distanceX = (int) (Math.abs(c.getLocation().getX() - temp.getLocation().getX()));
							int distanceY= (int) (Math.abs(c.getLocation().getY()-temp.getLocation().getY()));
							if((distanceX >1 || distanceY>1) || temp == c){
								targets.remove(j);
							}
						}
					}
				}else {
					for(int i = 0; i < this.getFirstPlayer().getTeam().size();i++) {
						targets.add((Damageable) this.getFirstPlayer().getTeam().get(i));
						for (int j =0;j<targets.size();j++) {
							Damageable temp = (Damageable) targets.get(j);
							int distanceX = (int) (Math.abs(c.getLocation().getX() - temp.getLocation().getX()));
							int distanceY= (int) (Math.abs(c.getLocation().getY()-temp.getLocation().getY()));
							if((distanceX >1 || distanceY>1) || temp == c){
								targets.remove(j);
							}
						}
					}
				}
				
			}else {
				CrowdControlAbility cc = (CrowdControlAbility) a;
				if(cc.getEffect().getType().equals(EffectType.BUFF)) {
					for(int i = 0; i < player.getTeam().size();i++) {
						targets.add((Damageable) player.getTeam().get(i));
						
						for (int j =0;j<targets.size();j++) {
							Damageable temp = (Damageable) targets.get(j);
							int distanceX = (int) (Math.abs(c.getLocation().getX() - temp.getLocation().getX()));
							int distanceY= (int) (Math.abs(c.getLocation().getY()-temp.getLocation().getY()));
							if((distanceX >1 || distanceY>1) || temp == c){
								targets.remove(j);
							}
						}
					}
					
				}else {
					if(isFirstPlayer) {
						for(int i = 0; i < this.getFirstPlayer().getTeam().size();i++) {
							targets.add((Damageable) this.getFirstPlayer().getTeam().get(i));
							player.getTeam().get(i).getAppliedEffects().add(cc.getEffect());
							for (int j =0;j<targets.size();j++) {
								Damageable temp = (Damageable) targets.get(j);
								int distanceX = (int) (Math.abs(c.getLocation().getX() - temp.getLocation().getX()));
								int distanceY= (int) (Math.abs(c.getLocation().getY()-temp.getLocation().getY()));
								if((distanceX >1 || distanceY>1) || temp == c){
									targets.remove(j);
								}
							}
						}
					}else {
						for(int i = 0; i < this.getSecondPlayer().getTeam().size();i++) {
							targets.add((Damageable) this.getSecondPlayer().getTeam().get(i));
							player.getTeam().get(i).getAppliedEffects().add(cc.getEffect());
							for (int j =0;j<targets.size();j++) {
								Damageable temp = (Damageable) targets.get(j);
								int distanceX = (int) (Math.abs(c.getLocation().getX() - temp.getLocation().getX()));
								int distanceY= (int) (Math.abs(c.getLocation().getY()-temp.getLocation().getY()));
								if((distanceX >1 || distanceY>1) || temp == c){
									targets.remove(j);
								}
							}
						}
					}
				}

			}
			
			for (int i =0;i<targets.size();i++) {
				Damageable temp = (Damageable) targets.get(i);
				int distanceX = (int) (Math.abs(c.getLocation().getX() - temp.getLocation().getX()));
				int distanceY= (int) (Math.abs(c.getLocation().getY()-temp.getLocation().getY()));
				if((distanceX >1 || distanceY>1) || temp == c){
					targets.remove(i);
				}
			}
		}
		


	
		if(!a.getCastArea().equals(AreaOfEffect.SURROUND)) {
			int range = a.getCastRange();
			for (int i =0;i<targets.size();i++) {
				Champion temp = (Champion) targets.get(i);
				int distance = (int) (Math.abs(temp.getLocation().getX()-c.getLocation().getX()) + Math.abs(temp.getLocation().getY()-c.getLocation().getY()));
				if(distance > range) {
					targets.remove(i);
				}
			}
		}
		for(int i = 0; i< targets.size();i++) {
			Champion c1 = (Champion)targets.get(i);
			if(a instanceof DamagingAbility) {
				for(int j = 0; j<c1.getAppliedEffects().size();j++) {
					if(c1.getAppliedEffects().get(j).getName().equals("Shield")){
						c1.getAppliedEffects().remove(j);
						return;
						
					}
				}
			}
			for(int j = 0; j<c1.getAppliedEffects().size();j++) {
				if(c1.getAppliedEffects().get(j).getName().equals("Silence")){
					return;
					
				}
			}
			for(int j = 0; i<c1.getAppliedEffects().size();j++) {
				if(c1.getAppliedEffects().get(j).getName().equals("Stun")){
					return;
					
				}
			}
		}
		try {
			a.execute(targets);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			throw new CloneNotSupportedException();
		}
	}
	
public void castAbility(Ability a, Direction d) throws AbilityUseException,NotEnoughResourcesException,CloneNotSupportedException {
		
		Champion c = this.getCurrentChampion();
		ArrayList<Damageable> targets = new ArrayList<Damageable>();
		
		if(a.getRequiredActionPoints() > c.getCurrentActionPoints()) {
			throw new  NotEnoughResourcesException();
		}else {
			c.setCurrentActionPoints(c.getCurrentActionPoints() - a.getRequiredActionPoints());
		}
		if(a.getManaCost() > c.getMana()) {
			throw new  NotEnoughResourcesException();
		}else {
			c.setMana(c.getMana() - a.getManaCost());
		}
		if(a.getCurrentCooldown() != 0) {
			throw new  AbilityUseException();
		}
		else if(a.getCurrentCooldown() == 0) {
			a.setCurrentCooldown(a.getBaseCooldown());
		}else {
			a.setCurrentCooldown(a.getCurrentCooldown() - 1);
		}
		boolean isFirstPlayer = false;
		boolean isSecondPlayer = false;
		Player player = null;
		

		for(int i = 0; i < this.getFirstPlayer().getTeam().size();i++) {
			if(this.getFirstPlayer().getTeam().get(i) == c) {
				isFirstPlayer = true;
				player = this.getFirstPlayer();
			}
		}
		for(int i = 0; i < this.getSecondPlayer().getTeam().size();i++) {
			if(this.getSecondPlayer().getTeam().get(i) == c) {
				isSecondPlayer = true;
				player = this.getSecondPlayer();
			}
		}
		
		
		if(a.getCastArea().equals(AreaOfEffect.DIRECTIONAL)) {
			if(a instanceof HealingAbility) {
				for(int i = 0; i < player.getTeam().size();i++) {
					targets.add((Damageable) player.getTeam().get(i));
				}
			}else if(a instanceof DamagingAbility) {
				if(isFirstPlayer) {
					for(int i = 0; i < this.getSecondPlayer().getTeam().size();i++) {
						targets.add((Damageable) this.getSecondPlayer().getTeam().get(i));
					}
				}else {
					for(int i = 0; i < this.getFirstPlayer().getTeam().size();i++) {
						targets.add((Damageable) this.getFirstPlayer().getTeam().get(i));
					}
				}
				
			}else {
				CrowdControlAbility cc = (CrowdControlAbility) a;
				if(cc.getEffect().getType().equals(EffectType.BUFF)) {
					for(int i = 0; i < player.getTeam().size();i++) {
						targets.add((Damageable) player.getTeam().get(i));
					}
				}else {
					if(isFirstPlayer) {
						for(int i = 0; i < this.getSecondPlayer().getTeam().size();i++) {
							targets.add((Damageable) this.getSecondPlayer().getTeam().get(i));
						}
					}else {
						for(int i = 0; i < this.getFirstPlayer().getTeam().size();i++) {
							targets.add((Damageable) this.getFirstPlayer().getTeam().get(i));
						}
					}
				}
			}
		
			int range = a.getCastRange();
			for (int i = 0; i<targets.size();i++) {
				Damageable temp = (Damageable)targets.get(i);
	
				if(d.equals(Direction.UP)) {
					if(!(c.getLocation().getY() == temp.getLocation().getY() && temp.getLocation().getX() > c.getLocation().getX())) {
						int distance = (int) (temp.getLocation().getX() - c.getLocation().getX());
						if (distance > range) {
							targets.remove(temp);
						}
					}
				}
				if(d.equals(Direction.DOWN)) {
					if(!(c.getLocation().getY() == temp.getLocation().getY() && temp.getLocation().getX() < c.getLocation().getX())) {
						int distance = (int) (c.getLocation().getX() - temp.getLocation().getX());
						if (distance > range) {
							targets.remove(temp);
						}
					}
				}
				if(d.equals(Direction.RIGHT)) {
					if(!(c.getLocation().getY() < temp.getLocation().getY() && temp.getLocation().getX() == c.getLocation().getX())) {
						int distance = (int) (temp.getLocation().getY() - c.getLocation().getY());
						if (distance > range) {
							targets.remove(temp);
						}
					}
				}
				if(d.equals(Direction.LEFT)) {
					if(!(c.getLocation().getY() > temp.getLocation().getY() && temp.getLocation().getX() == c.getLocation().getX())) {
						int distance = (int) (c.getLocation().getY() - temp.getLocation().getY());
						if (distance > range) {
							targets.remove(temp);
						}
					}
				}
			}
		}
		
		for(int i = 0; i< targets.size();i++) {
			Champion c1 = (Champion)targets.get(i);
			if(a instanceof DamagingAbility) {
				for(int j = 0; j<c1.getAppliedEffects().size();j++) {
					if(c1.getAppliedEffects().get(j).getName().equals("Shield")){
						c1.getAppliedEffects().remove(j);
						return;
						
					}
				}
			}
			for(int j = 0; j<c1.getAppliedEffects().size();j++) {
				if(c1.getAppliedEffects().get(j).getName().equals("Silence")){
					return;
					
				}
			}
			for(int j = 0; i<c1.getAppliedEffects().size();j++) {
				if(c1.getAppliedEffects().get(j).getName().equals("Stun")){
					return;
					
				}
			}
		}
		try {
			a.execute(targets);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			throw new CloneNotSupportedException();
		}
	}

public void castAbility(Ability a,int x,int y) throws  InvalidTargetException,AbilityUseException,NotEnoughResourcesException,CloneNotSupportedException {
	
	Champion c = this.getCurrentChampion();
	ArrayList<Damageable> targets = new ArrayList<Damageable>();
	
	if(board[x][y] instanceof Champion ) {
	
		if(a.getRequiredActionPoints() > c.getCurrentActionPoints()) {
			throw new  NotEnoughResourcesException();
		}else {
			c.setCurrentActionPoints(c.getCurrentActionPoints() - a.getRequiredActionPoints());
		}
		if(a.getManaCost() > c.getMana()) {
			throw new  NotEnoughResourcesException();
		}else {
			c.setMana(c.getMana() - a.getManaCost());
		}
		if(a.getCurrentCooldown() != 0) {
			throw new  AbilityUseException();
		}
		else if(a.getCurrentCooldown() == 0) {
			a.setCurrentCooldown(a.getBaseCooldown());
		}else {
			a.setCurrentCooldown(a.getCurrentCooldown() - 1);
		}
		boolean isFirstPlayer = false;
		boolean isSecondPlayer = false;
		Player player = null;
		
	
		for(int i = 0; i < this.getFirstPlayer().getTeam().size();i++) {
			if(this.getFirstPlayer().getTeam().get(i) == c) {
				isFirstPlayer = true;
				player = this.getFirstPlayer();
			}
		}
		for(int i = 0; i < this.getSecondPlayer().getTeam().size();i++) {
			if(this.getSecondPlayer().getTeam().get(i) == c) {
				isSecondPlayer = true;
				player = this.getSecondPlayer();
			}
		}
		
		
		if(a.getCastArea().equals(AreaOfEffect.SINGLETARGET)) {
			if(a instanceof HealingAbility) {
				for(int i = 0; i < player.getTeam().size();i++) {
					targets.add((Damageable) player.getTeam().get(i));
				}
			}else if(a instanceof DamagingAbility) {
				if(isFirstPlayer) {
					for(int i = 0; i < this.getSecondPlayer().getTeam().size();i++) {
						targets.add((Damageable) this.getSecondPlayer().getTeam().get(i));
					}
				}else {
					for(int i = 0; i < this.getFirstPlayer().getTeam().size();i++) {
						targets.add((Damageable) this.getFirstPlayer().getTeam().get(i));
					}
				}
				
			}else {
				CrowdControlAbility cc = (CrowdControlAbility) a;
				if(cc.getEffect().getType().equals(EffectType.BUFF)) {
//					throw new InvalidTargetException();
					for(int i = 0; i < player.getTeam().size();i++) {
						targets.add((Damageable) player.getTeam().get(i));
					}
				}else {
					if(isFirstPlayer) {
						for(int i = 0; i < this.getFirstPlayer().getTeam().size();i++) {
							targets.add((Damageable) this.getFirstPlayer().getTeam().get(i));
						}
					}else {
						for(int i = 0; i < this.getSecondPlayer().getTeam().size();i++) {
							targets.add((Damageable) this.getSecondPlayer().getTeam().get(i));
						}
					}
				}
			}
		}
		
		for(int i = 0; i< targets.size();i++) {
			Champion c1 = (Champion)targets.get(i);
			if(a instanceof DamagingAbility) {
				for(int j = 0; j<c1.getAppliedEffects().size();j++) {
					if(c1.getAppliedEffects().get(j).getName().equals("Shield")){
						c1.getAppliedEffects().remove(j);
						return;
						
					}
				}
			}
			for(int j = 0; j<c1.getAppliedEffects().size();j++) {
				if(c1.getAppliedEffects().get(j).getName().equals("Silence")){
					return;
					
				}
			}
			for(int j = 0; i<c1.getAppliedEffects().size();j++) {
				if(c1.getAppliedEffects().get(j).getName().equals("Stun")){
				return;
					
				}
			}
		}
		int range = a.getCastRange();
	
		if(!a.getCastArea().equals(AreaOfEffect.SURROUND)) {
			for (int i =0;i<targets.size();i++) {
				Damageable temp = (Damageable) targets.get(i);
				int distance = (int) (Math.abs(temp.getLocation().getX()-c.getLocation().getX()) + Math.abs(temp.getLocation().getY()-c.getLocation().getY()));
				if(distance > range) {
					targets.remove(i);
				}
			}
		}
		for (int i =0;i<targets.size();i++) {
			Damageable temp = (Damageable) targets.get(i);
			if(board[x][y] != temp) {
				targets.remove(i);
			}
		}
		
		try {
			a.execute(targets);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			throw new CloneNotSupportedException();
		}
	}else {
		throw new InvalidTargetException();
	}
}

	
	public void useLeaderAbility() throws LeaderAbilityAlreadyUsedException, LeaderNotCurrentException{
		ArrayList<Champion> targets = new ArrayList<Champion>();
		Champion c = this.getCurrentChampion();
		boolean isFirstPlayer = false;
		boolean isSecondPlayer = false;
		Player player = null;
		if(this.getFirstPlayer().getLeader() == c) {
			isFirstPlayer = true;
			player = this.getFirstPlayer();
			
		}else if(this.getSecondPlayer().getLeader() == c) {
			isSecondPlayer = true;
			player = this.getSecondPlayer();
		}
		if(isFirstPlayer || isSecondPlayer) {
			if(isFirstPlayer) {
				if(this.firstLeaderAbilityUsed == false) {
					this.firstLeaderAbilityUsed = true;
				}else {
					throw new LeaderAbilityAlreadyUsedException();
				}
			}else {
				if(this.secondLeaderAbilityUsed == false) {
					this.secondLeaderAbilityUsed = true;
				}else {
					throw new LeaderAbilityAlreadyUsedException();
				}
			}
			if(c instanceof Hero) {
				targets = player.getTeam();
			}else if(c instanceof Villain) {
				if(isFirstPlayer) {
					targets = this.getSecondPlayer().getTeam();
				}else {
					targets = this.getFirstPlayer().getTeam();
				}
			}else {
				for(int i = 0;i<this.getFirstPlayer().getTeam().size();i++) {
					Champion tmp = this.getFirstPlayer().getTeam().get(i);
					if(this.getFirstPlayer().getLeader() != tmp) {
						targets.add(tmp);
					}
				}
				for(int i = 0;i<this.getSecondPlayer().getTeam().size();i++) {
					Champion tmp = this.getSecondPlayer().getTeam().get(i);
					if(this.getSecondPlayer().getLeader() != tmp) {
						targets.add(tmp);
					}
				}
			}
			c.useLeaderAbility(targets);
		}else {
			throw new LeaderNotCurrentException();
		}
	}
	
	public void endTurn() {
		if(!this.getTurnOrder().isEmpty()) {
			this.getTurnOrder().remove();
			if(this.getTurnOrder().isEmpty()) {
				this.prepareChampionTurns();
			}else {
				if(this.getCurrentChampion().getCondition().equals(Condition.INACTIVE)) {
					this.endTurn();
				}else {
					this.getCurrentChampion().setCurrentActionPoints(this.getCurrentChampion().getMaxActionPointsPerTurn());
					for(int i = 0; i< this.getCurrentChampion().getAbilities().size();i++) {
						Ability a = this.getCurrentChampion().getAbilities().get(i);
						if(a.getCurrentCooldown() != 0) {
							a.setCurrentCooldown(a.getCurrentCooldown() - 1);
						}
					}
					for(int i = 0; i< this.getCurrentChampion().getAppliedEffects().size();i++) {
						Effect e = this.getCurrentChampion().getAppliedEffects().get(i);
						
					}
				}
			}

		}

	
	}
	
	public void prepareChampionTurns() {
		if(this.getTurnOrder().isEmpty()) {
			for(int i = 0;i<this.getFirstPlayer().getTeam().size();i++) {
				Champion c = this.getFirstPlayer().getTeam().get(i);
				if(c.getCondition().equals(Condition.ACTIVE) || c.getCondition().equals(Condition.ROOTED)) {
					this.getTurnOrder().insert((Comparable)c);
				}
			}
			for(int i = 0;i<this.getSecondPlayer().getTeam().size();i++) {
				Champion c = this.getSecondPlayer().getTeam().get(i);
				if(c.getCondition().equals(Condition.ACTIVE) || c.getCondition().equals(Condition.ROOTED)) {
					this.getTurnOrder().insert((Comparable)c);
				}
			}
		}
	}
	
	public static ArrayList<Champion> getAvailableChampions() {
		return availableChampions;
	}

	public static ArrayList<Ability> getAvailableAbilities() {
		return availableAbilities;
	}

	public Player getFirstPlayer() {
		return firstPlayer;
	}

	public Player getSecondPlayer() {
		return secondPlayer;
	}

	public Object[][] getBoard() {
		return board;
	}

	public PriorityQueue getTurnOrder() {
		return turnOrder;
	}

	public boolean isFirstLeaderAbilityUsed() {
		return firstLeaderAbilityUsed;
	}

	public boolean isSecondLeaderAbilityUsed() {
		return secondLeaderAbilityUsed;
	}

	public static int getBoardwidth() {
		return BOARDWIDTH;
	}

	public static int getBoardheight() {
		return BOARDHEIGHT;
	}
}
