package uk.ac.ed.inf.powergrab;

class Powerstation {

	Position pos;
	double coins;
	double power;
	String id;
	
	Powerstation(Position pos, double coins, double power, String id) {
		this.pos = pos;
		this.coins = coins;
		this.power = power;
		this.id = id;
	}
}
