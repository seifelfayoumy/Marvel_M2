package model.effects;
import java.lang.Cloneable;

import model.world.Champion;



public class Effect implements Cloneable{
	private String name;
	private EffectType type;
	private int duration;

	public Effect(String name, int duration, EffectType type) {
		this.name = name;
		this.type = type;
		this.duration = duration;
	}

	public String getName() {
		return name;
	}

	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}

	public EffectType getType() {
		return type;
	}
	
	public Object clone() throws CloneNotSupportedException{
		return super.clone();
	}
	
	public void apply(Champion c) {
		
	}
	public void remove(Champion c) {
		
	}

}
