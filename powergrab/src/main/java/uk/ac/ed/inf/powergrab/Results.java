package uk.ac.ed.inf.powergrab;

import java.util.*;

class Results {
	//holds the total coins and power of that simulations
	double totalcoins;
	double totalpower;
	//numbers of moves used in the simulation
	int numberofmoves;
	//number of times the drone for stuck 
	int stuckcounter;
	//the moves list of the drone (set of moves it did)
	ArrayList<Position> moveslist = new ArrayList<Position>();
	//the number of positive powerstations on the map
	int numberofposstations;
}
