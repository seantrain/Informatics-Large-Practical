package uk.ac.ed.inf.powergrab;

//custom data structure for storing the values of a single powerstation
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
