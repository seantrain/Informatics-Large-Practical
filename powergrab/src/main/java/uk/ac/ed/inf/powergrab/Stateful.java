package uk.ac.ed.inf.powergrab;

import java.util.*;

public class Stateful {

	public Map currentmap;
	public int seed;
	public double startLat;
	public double startLong;
	public int moves = 250;
	public double coins = 0;
	public double power = 250.0;
	public double maxCoins = 0;
	public double maxPower = 0;
	public int stuckcounter = 0;
	public int start;
	public ArrayList<String> visitedStations = new ArrayList<String>();
	public ArrayList<Powerstation> skippedStations = new ArrayList<Powerstation>();
	public ArrayList<Powerstation> locations = new ArrayList<Powerstation>();
	public ArrayList<ArrayList<Powerstation>> sepLocations = new ArrayList<ArrayList<Powerstation>>();
	public ArrayList<Powerstation> posLocations = new ArrayList<Powerstation>();
	public ArrayList<Powerstation> negLocations = new ArrayList<Powerstation>();
	
	public Results results = new Results();
	
	public Stateful (Map currentmap, int seed, double startLat, double startLong, int start) {
		
		this.currentmap = currentmap;
		this.seed = seed;
		this.startLat = startLat;
		this.startLong = startLong;
		this.start = start;

		locations = currentmap.psarray;
		sepLocations = Map.seperateLocations(locations);
		posLocations = sepLocations.get(0);
		negLocations = sepLocations.get(1);
		
		this.statefulsim(start);
		
	}
	
	//must go to closest and charge, even if 0
	public int Closest(Position currentPos, int mode) {
		
		double smallestDistance = 1000;
		int index = 0;
		
		//returns closest positive powerstation thats not been visited or skipped
		if (mode == 0) {
			for (int i = 0; i < posLocations.size(); i++) {
				double newdistance = currentPos.distanceBetween(posLocations.get(i).pos);
				if (smallestDistance > newdistance && !visitedStations.contains(posLocations.get(i).id) && !skippedStations.contains(posLocations.get(i))) {
					smallestDistance = newdistance;
					index = i;
				}
			}
			
			return index;
		//returns closest powerstation that has been skipped
		} else {
			for (int i = 0; i < skippedStations.size(); i++) {
				double newdistance = currentPos.distanceBetween(skippedStations.get(i).pos);
				if (smallestDistance > newdistance) {
					smallestDistance = newdistance;
					index = i;
				}
			}
			return index;
		}
		
	}
	
	public DirectionWithPos setofdirections(Position startpos, int closest) {
		
		ArrayList<Integer> set = new ArrayList<Integer>();
		DirectionWithPos returnvalue = new DirectionWithPos();
		ArrayList<Position> poset = new ArrayList<Position>();
		ArrayList<Integer> currentMoves = new ArrayList<Integer>();
		boolean skip = false;
		Position pos = startpos;
		Position destination = posLocations.get(closest).pos;
		
		poset.add(pos);
		
		//System.out.println("Initial distance between: " + pos.distanceBetween(destination));
		//System.out.println("Initially within distance: " + pos.withinDistance(destination));
		
		int tempMoves = 0;
		int repeatdetected = 0;
		
		do {
			
			int nextdirection = pos.direction(destination);
			int newdirection = nextdirection;
			
			if (repeatdetected == 0) {
				
				if (pos.nextPosition(nextdirection).withinDistanceAny(negLocations) || !pos.nextPosition(nextdirection).inPlayArea()) {
					//if drone will go negative, will return non-negative direction. 
					//If drone is going out of bounds, will return non-out-of-bounds direction
					newdirection = avoidNegative(pos, destination, nextdirection);						
				}
				
				pos = pos.nextPosition(newdirection);
				poset.add(pos);
				currentMoves.add(newdirection);
				//System.out.println("Distance between: " + pos.distanceBetween(destination));
				set.add(nextdirection);
				tempMoves++;
			//unnreachable code, testing ///////////////////////////////////////////
			} else if (repeatdetected == 100) {
				//get all the negative locations within disance to the new position
				//if you traveled in the new direction
				ArrayList<Integer> negativeInRange = new ArrayList<Integer>();
				for(int i = 0; i < negLocations.size(); i++) {
					if (pos.nextPosition(newdirection).withinDistance(negLocations.get(i).pos)) {
						negativeInRange.add(i);
					}
				}
				double tempcoins = posLocations.get(closest).coins;
				int chargefrom = closest;
				if (pos.nextPosition(newdirection).withinDistance(destination) && negativeInRange.size() != 0) {
					double distanceholder = pos.distanceBetween(destination);
					for (int i = 0; i < negativeInRange.size(); i++) {
						if (distanceholder > pos.distanceBetween(negLocations.get(negativeInRange.get(i)).pos)) {
							distanceholder = pos.distanceBetween(negLocations.get(negativeInRange.get(i)).pos);
							tempcoins += negLocations.get(negativeInRange.get(i)).coins;
							chargefrom = i;
						}
					}
				}
				if (tempcoins < 0) {
					//System.out.println("Skip envoked due to negative overall coins value");
					skip = true;
					returnvalue.directionset = set;
					returnvalue.updatedpos = poset.get(poset.size()-1);
					returnvalue.positionset = poset;
					returnvalue.movesused = tempMoves;
					returnvalue.skip = skip;
					skippedStations.add(posLocations.get(closest));
					return returnvalue;
				} else {
					pos = pos.nextPosition(newdirection);
					poset.add(pos);
					currentMoves.add(newdirection);
					//System.out.println("Distance between: " + pos.distanceBetween(destination));
					set.add(nextdirection);
					tempMoves++;
					if (chargefrom != closest) {
						System.out.println(closest + "   " + chargefrom);
						System.out.println("\n\n\nNegative!\n\n\n");
						coins+= negLocations.get(chargefrom).coins;
						negLocations.get(chargefrom).coins = 0;
						power+= negLocations.get(chargefrom).power;
						negLocations.get(chargefrom).power = 0;
						System.out.println("Coins after going negative: " + coins);
					}
					
				}
			} else if (repeatdetected == 2) {
				skip = true;
				skippedStations.add(posLocations.get(closest));
				returnvalue.directionset = set;
				returnvalue.updatedpos = poset.get(poset.size()-1);
				returnvalue.positionset = poset;
				returnvalue.movesused = tempMoves;
				returnvalue.skip = skip;
				if (start == 14) {
					System.out.println("Station: " + closest + " added to skippedStations, size: " + skippedStations.size());
				}
				return returnvalue;
			}
			//if the drone gets stuck, skip that power station till later
			if (checkStuck(currentMoves)) {
				repeatdetected = 2;
				stuckcounter++;
			}
			
		} while (!pos.withinDistance(destination) && moves-tempMoves > 0);
		
		//System.out.println("Within Distance NOW!\n");

		returnvalue.directionset = set;
		returnvalue.updatedpos = poset.get(poset.size()-1);
		returnvalue.positionset = poset;
		returnvalue.movesused = tempMoves;
		returnvalue.skip = skip;

		return returnvalue;
	}
	
	public int avoidNegative(Position currentPos, Position destination, int currentDirection) {
		
		ArrayList<Integer> cantgo = new ArrayList<Integer>();
		//if the drone cannot go in any direction without going negative, go in the original direction
		int newDirection = currentDirection;
		//will always be a distance smaller than 1000 since map length is < 1000
		double smallestDistance = 1000;
		
		//find all directions that will result in charging from a negative station or going out of the play area
		for (int i = 0; i < 16; i++) {
			if (currentPos.nextPosition(i).withinDistanceAny(negLocations) || !currentPos.nextPosition(i).inPlayArea()) {
				cantgo.add(i);
			}
		}
		
		//System.out.println("Cannot go to:" + cantgo.toString());
		
		for (int i = 0; i < 16; i++) {
			double distancebetween = currentPos.nextPosition(i).distanceBetween(destination);
			if (!cantgo.contains(i)) {
				if (smallestDistance > distancebetween) {
					smallestDistance = distancebetween;
					newDirection = i;
				}
			}
		}
		
		//travel through negative
		if (cantgo.size() == 16) {
			return newDirection;
		}
		
		
		return newDirection;
	}
	
	public int opDirection(int a) {
		
		if (a >= 9 && a <= 16) {
			return (a - 8);
		} else if (a >= 0 && a <= 8) {
			return (8 + a);
		}
		
		return 0;
	}
	
	
	public boolean checkStuck(ArrayList<Integer> currentMoves) {
		
		int countermove = 0;
		
		for (int i = 0; i < currentMoves.size()-1; i++) {
			if (currentMoves.get(i+1) == opDirection(currentMoves.get(i))) {
				countermove++;
			}
		}
		
		if (countermove > 5) {
			//System.out.println("DRONE GETS STUCK!\n\n");
			return true;
		}
		
		
		return false;
	}

	
	public void statefulsim(int start) {
		
		Position currentPos = new Position(startLat, startLong);
		
		ArrayList<Position> positionset = new ArrayList<Position>();
		ArrayList<Position> moveslist = new ArrayList<Position>();
		
		int skipcount = 0;
		//stopped for exiting the loop when all stations have been visited/skipped
		int stop = 0;
		int totalcoins = 0;
		//this computes the moves to the first powerstation specified by start
		//this is done since it is possible to get a better result by choosing a different powerstation to initially visit.
		//the game is simulated for all the number of powerstations, choosing a different start station each time
		//the optimal start powerstation will be determined by the one with the most coins, most power and least number of moves
		
		DirectionWithPos temp = setofdirections(currentPos, start);
		
		if (start == 14) {
			System.out.println("\n Station 14: \n");
		}
		if (!temp.skip) {
			coins+= posLocations.get(start).coins;
			power+= posLocations.get(start).power;
			for (int j = 1; j < temp.positionset.size(); j++) {
				positionset.add(temp.positionset.get(j));
			}
			visitedStations.add(posLocations.get(start).id);
			currentPos = temp.updatedpos;
			moves -= temp.movesused;
		} else {
			skipcount++;
			skippedStations.add(posLocations.get(start));
		}
		
		
		do {
			//mode 0 to find closest positive powerstation
			int closest = Closest(currentPos, 0);
			//System.out.println("Closest station id : " + posLocations.get(closest).id);
			temp = setofdirections(currentPos, closest);
			
			//if the station is reached (not skipped due to being stuck) values are updated
			if (!temp.skip && !skippedStations.contains(posLocations.get(closest))) {
				coins+= posLocations.get(closest).coins;
				power+= posLocations.get(closest).power;
				currentPos = temp.updatedpos;
				moves-= temp.movesused;
				for (int j = 1; j < temp.positionset.size(); j++) {
					positionset.add(temp.positionset.get(j));
				}
				visitedStations.add(posLocations.get(closest).id);
				if (start == 14) {
					System.out.println("Visited station: " + closest + ", in " + temp.movesused + " moves. Stations visited: " + visitedStations.size() + ". Stations left (not skipped): " + (posLocations.size()-visitedStations.size()));
				}
			//else station is skipped and added to skippedStation arraylist for further checking 
			} else if (temp.skip && !skippedStations.contains(posLocations.get(closest))){
				skipcount++;
				skippedStations.add(posLocations.get(closest));
				System.out.println("Station skipped: " + closest);
				
			}
			
			if (skippedStations.size() + visitedStations.size() == posLocations.size()) {
				if (skippedStations.size() == 0) {
					stop = 1;
				} else {
					//mode 1 (anything but 0) for finding closest for skipped powerstations
					do {
						//mode 1 (anything but 0) for finding closest for skipped powerstations
						closest = Closest(currentPos, 1);
						temp = setofdirections(currentPos, closest);
						if (!temp.skip) {
							coins+= skippedStations.get(closest).coins;
							power+= skippedStations.get(closest).power;
							currentPos = temp.updatedpos;
							moves-= temp.movesused;
							for (int j = 1; j < temp.positionset.size(); j++) {
								positionset.add(temp.positionset.get(j));
							}
							visitedStations.add(posLocations.get(closest).id);
							skippedStations.remove(closest);
							skipcount--;
							System.out.println("Coins gained from skipped station: " + closest);
						} else if(temp.skip) {
							System.out.println("Cannot access skipped stations: " + skippedStations.get(closest).id);
							skippedStations.remove(closest);
						}
					} while (skippedStations.size() != 0);
				stop = 1;
				}
			}
			if (start == 14 && !temp.skip) {
				//System.out.println("\nMoves used " + temp.movesused + ", Moves left: " + moves + ", Coins added: " + posLocations.get(closest).coins + "  Power added: " + posLocations.get(closest).power);
				totalcoins+= posLocations.get(closest).coins;
				
			}
		} while (stop == 0 && moves > 0 && power > 1.25);
		
		System.out.println("Coins actually collected:" + totalcoins);
		System.out.println("Stations left to visit: " + (posLocations.size()-visitedStations.size()));
		int[] last2moves = new int[2];
		
		last2moves[0] = currentPos.direction(positionset.get(positionset.size()-2));
		last2moves[1] = currentPos.nextPosition(last2moves[0]).direction(currentPos);
		int movesholder = moves;
		//go back and forth to use up all 250 moves
		do {
			currentPos = currentPos.nextPosition(last2moves[0]);
			positionset.add(currentPos);
			movesholder--;
			currentPos = currentPos.nextPosition(last2moves[1]);
			positionset.add(currentPos);
			movesholder--;

		} while (movesholder > 0);
		
		
		moveslist = positionset;
		
		results.numberofmoves = 250-moves;
		results.stuckcounter = stuckcounter;
		results.totalcoins = coins;
		results.totalpower = power;
		results.moveslist = moveslist;
		results.numberofposstations = posLocations.size();
		//for testing purposes: accuracy of the drone
		
		for (int i = 0; i < posLocations.size(); i++) {
			maxCoins += posLocations.get(i).coins;
			maxPower += posLocations.get(i).power;
		}
		
		//System.out.println("--------------------DRONE " + start + " RESULTS------------------");
		//System.out.println("Skips envoked: " + skipcount);
		//System.out.println("Stuck drone: " + stuckcounter);
		//System.out.println("\nCoins collected: " + coins + "\nPower collected: " + (power-250));
		//System.out.println("\nMax Coins: " + maxCoins + "\nMax Power: " + maxPower);
		//if ((coins/maxCoins)*100 > 100 || (coins/maxCoins)*100 > 99.999 && (coins/maxCoins)*100 < 100) {
		//	System.out.println("\nACCURACY: " + 100.0 + "%");
		//} else {
		//	System.out.println("\nACCURACY: " + (coins/maxCoins)*100 + "%");
		//}
		//System.out.println("-------------------------END-------------------------");
		if (start == 14) {
			System.out.println("\n\n\n");
		}
	}

}
