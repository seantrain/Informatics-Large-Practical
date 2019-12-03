package uk.ac.ed.inf.powergrab;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Stateful {

	public Map currentmap;
	public double startLat;
	public double startLong;
	public int moves = 250;
	public double coins = 0;
	public double power = 250.0;
	public double maxCoins = 0;
	public double maxPower = 0;
	public int movesToComplete;
	public ArrayList<String> visitedStations = new ArrayList<String>();
	public ArrayList<Powerstation> skippedStations = new ArrayList<Powerstation>();
	public ArrayList<String> avoidChecking = new ArrayList<String>();
	public ArrayList<Position> moveslist = new ArrayList<Position>();
	public ArrayList<Double> coinholder = new ArrayList<Double>();
	public ArrayList<Double> powerholder = new ArrayList<Double>();
	public ArrayList<Powerstation> locations = new ArrayList<Powerstation>();
	public SeparateStations sepLocations = new SeparateStations();
	public ArrayList<Powerstation> posLocations = new ArrayList<Powerstation>();
	public ArrayList<Powerstation> negLocations = new ArrayList<Powerstation>();
	
	public Stateful (Map currentmap, double startLat, double startLong, boolean actuallycharge) throws IOException {
		
		this.currentmap = currentmap;
		this.startLat = startLat;
		this.startLong = startLong;

		locations = currentmap.psarray;
		//separate locations into positive and negative locations
		sepLocations = Map.sepLocations(locations);
		posLocations = sepLocations.positive;
		negLocations = sepLocations.negative;
		this.statefulsim(actuallycharge);
		
	}
	
	//method for finding closest powerstation in different criteria
	public int Closest(Position currentPos, int mode) {
		
		double smallestDistance = 1000;
		int index = -1;
		
		//returns closest positive powerstation thats not been visited or skipped (mode 0)
		if (mode == 0) {
			for (int i = 0; i < posLocations.size(); i++) {
				double newdistance = currentPos.distanceBetween(posLocations.get(i).pos);
				if (smallestDistance > newdistance && !visitedStations.contains(posLocations.get(i).id) && !skippedStations.contains(posLocations.get(i))) {
					smallestDistance = newdistance;
					index = i;
				}
			}
			return index;
			
		//returns closest powerstation overall (mode 1)
		} else if (mode == 1) {
			for (int i = 0; i < locations.size(); i++) {
				double newdistance = currentPos.distanceBetween(locations.get(i).pos);
				if (smallestDistance > newdistance && !visitedStations.contains(locations.get(i).id) && !skippedStations.contains(locations.get(i))) {
					smallestDistance = newdistance;
					index = i;
				}
			}
			return index;
		
		//returns closest powerstation that has been skipped (mode 2)
		} else if (mode == 2){
			for (int i = 0; i < skippedStations.size(); i++) {
				double newdistance = currentPos.distanceBetween(skippedStations.get(i).pos);
				if (smallestDistance > newdistance && !avoidChecking.contains(skippedStations.get(i).id)) {
					smallestDistance = newdistance;
					index = i;
				}
			}
			return index;
		}
		
		return index;
		
	}
	
	//simulates going to the destination position from the start position
	public DronesMoves setofdirections(Position startpos, Position destination) {
		
		ArrayList<Integer> set = new ArrayList<Integer>();
		//the values that will be returned depending on the outcome of the simulation
		//from going to the destination
		DronesMoves returnvalue = new DronesMoves();
		ArrayList<Position> poset = new ArrayList<Position>();
		boolean skip = false;
		Position pos = startpos;
		final double powertemp = power;
		final int movestemp = moves;
		ArrayList<Double> temppowerholder = new ArrayList<Double>();
		ArrayList<Double> tempcoinholder = new ArrayList<Double>();
		
		//loop for the simulation, ends if the destination is visited or the drone gets stuck
		do {
			
			//the direction to travel to the given destination
			int nextdirection = pos.direction(destination);
			
			//if the direction to the destination goes into a negative station or out the play area
			//used avoidNegative function that returns a valid new direction to go in
			if (pos.nextPosition(nextdirection).withinDistanceAny(negLocations) || !pos.nextPosition(nextdirection).inPlayArea()) {
				nextdirection = avoidNegative(pos, destination, nextdirection);						
			}
			//update the drone values when going in the nextdirection
			pos = pos.nextPosition(nextdirection);
			poset.add(pos);
			set.add(nextdirection);
			power-= 1.25;
			tempcoinholder.add(coins);
			temppowerholder.add(power);
			moves--;
			
			//if the drone gets stuck, skip that power station till later
			if (checkStuck(set)) {
				skip = true;
				//values don't matter, only thing being returned that is used is the value of skip
				returnvalue.updatedpos = null;
				returnvalue.positionset = null;
				returnvalue.skip = skip;
				returnvalue.power = powertemp;
				returnvalue.moves = movestemp;
				return returnvalue;
			}
		//run until the drone is within distance of the destination and doesn't run out of moves & power
		} while (!pos.withinDistance(destination) && moves > 0);
		
		//return values that were calculated (set of directions the drone went in, the new position of the drone
		//the set of position the drone went and the skip value of the destination)
		returnvalue.updatedpos = poset.get(poset.size()-1);
		returnvalue.positionset = poset;
		returnvalue.skip = skip;
		returnvalue.powerholder = temppowerholder;
		returnvalue.coinholder = tempcoinholder;
		
		return returnvalue;
	}
	
	//function for producing a new direction if the current direction to a destination is not valid
	//(going into negative station or out of thhe map)
	public int avoidNegative(Position currentPos, Position destination, int currentDirection) {
		
		//same as stateless, arraylist for directions you should not go in
		ArrayList<Integer> cantGo = new ArrayList<Integer>();
		int newDirection = currentDirection;
		//will always be a distance smaller than 1000 since map length is < 1000
		double smallestDistance = 1000;
		
		//find all directions that will result in charging from a negative station or going out of the play area
		for (int i = 0; i < 16; i++) {
			//finds closest charging station overall (positive and negative) after move in direction i
			int closest = Closest(currentPos.nextPosition(i), 1);
			//if it is negatative and within distance of a move in direction i or i makes the drone leave the play area
			//add the direction to cantGo
			if (locations.get(closest).coins < 0 && currentPos.nextPosition(i).withinDistance(locations.get(closest).pos) || !currentPos.nextPosition(i).inPlayArea()) {
				cantGo.add(i);
			}
		}
		
		//find the direction that will be closest to destination that is not an invalid direction
		for (int i = 0; i < 16; i++) {
			double distancebetween = currentPos.nextPosition(i).distanceBetween(destination);
			if (!cantGo.contains(i)) {
				if (smallestDistance > distancebetween) {
					smallestDistance = distancebetween;
					newDirection = i;
				}
			}
		}
		
		//travel through negative
		if (cantGo.size() == 16) {
			
			return newDirection;
		}
		
		
		return newDirection;
	}
	
	//function given a direction will calculate the opposite direction
	public int opDirection(int direction) {
		
		if (direction >= 9 && direction <= 16) {
			return (direction - 8);
		} else if (direction >= 0 && direction <= 8) {
			return (8 + direction);
		}
		
		return 0;
	}
	
	//function given the set of direction the drone has travelled in
	//returns whether or not the drone is stuck (repeating opposite moves)
	public boolean checkStuck(ArrayList<Integer> currentMoves) {
		
		int countermove = 0;
		//if 2 moves are opposites of eachother, increment countermove counter
		for (int i = 0; i < currentMoves.size()-1; i++) {
			if (currentMoves.get(i+1) == opDirection(currentMoves.get(i))) {
				countermove++;
			}
		}
		
		//5 series of counter moves take place, the drone is therefore stuck, return true
		if (countermove > 5) {
			//System.out.println("DRONE GETS STUCK!\n\n");
			return true;
		}
		
		return false;
	}

	//the stateful execution
	public void statefulsim(boolean actuallycharge) throws IOException {
		
		//collecting the most coins on the map
		for (int i = 0; i < posLocations.size(); i++) {
			maxCoins += posLocations.get(i).coins;
			maxPower += posLocations.get(i).power;
		}

		Position currentPos = new Position(startLat, startLong);
		
		//setting the first values for the file data holders
		ArrayList<Position> positionset = new ArrayList<Position>();
		positionset.add(currentPos);
		powerholder.add(power);
		coinholder.add(coins);
		
		//initalising the variables used 
		int closest = 0;
		DronesMoves temp = new DronesMoves();
		boolean checkSkipped = false;
		
		do {
			//check to see if you can go to a skipped station at every successful charge from
			//a previously unvisited station
			if (skippedStations.size() != 0) {
				checkSkipped = true;
				avoidChecking.clear();
			}
			//if there is at least one skipped station, try visit it each visit to a powerstation
			if (checkSkipped == true) {
				
				do {
					
					//mode 2 for finding closest for skipped powerstations
					closest = Closest(currentPos, 2);
					temp = setofdirections(currentPos, skippedStations.get(closest).pos);
					//same as the start destination except its over the skipped stations not the positive stations
					if (temp.skip == false) {
						coins += skippedStations.get(closest).coins;
						power+= skippedStations.get(closest).power;
						currentPos = temp.updatedpos;
						
						for (int j = 0; j < temp.positionset.size(); j++) {
							positionset.add(temp.positionset.get(j));
							powerholder.add(temp.powerholder.get(j));
							coinholder.add(temp.coinholder.get(j));
						}
						powerholder.remove(powerholder.size()-1);
						powerholder.add(power);
						coinholder.remove(coinholder.size()-1);
						coinholder.add(coins);
						
						
						skippedStations.get(closest).coins = 0;
						skippedStations.get(closest).power = 0;
						
						visitedStations.add(skippedStations.get(closest).id);
						//station is visited, removed from skippedStations
						skippedStations.remove(closest);
						//System.out.println("Skipped accessed");
					//if it fails, station is added to avoidChecking arraylist (used in the search for the closest skipped station)
					//important for when their are more than 1 skippedStation to visit (mode 2 for closest function)
					} else if (temp.skip == true) {
						avoidChecking.add(skippedStations.get(closest).id);
						//restore power and moves values since station was skipped
						power = temp.power;
						moves = temp.moves;
					}
				//runs until all skippedstations are attempted to be visited
				} while (avoidChecking.size() != skippedStations.size() && skippedStations.size() != 0);
				
				checkSkipped = false;
				//if at this point, all stations have been visited, break out of the loop
				//since any left in skippedStations by the end of the program were unable to be visited
				//throughout the entire program
				if (skippedStations.size() + visitedStations.size() == posLocations.size()) {
					break;
				}
			}
			
			//if there does exist stations that have not been visited or skipped, check them
			if (checkSkipped == false) {

				//mode 0 to find closest positive powerstation
				closest = Closest(currentPos, 0);
				
				temp = setofdirections(currentPos, posLocations.get(closest).pos);
				
				//if the station is reached (not skipped due to being stuck) values are updated
				if (temp.skip == false) {
					//System.out.println("Visited " + closest);
					coins+= posLocations.get(closest).coins;
					power+= posLocations.get(closest).power;
					currentPos = temp.updatedpos;
					for (int j = 0; j < temp.positionset.size(); j++) {
						positionset.add(temp.positionset.get(j));
						powerholder.add(temp.powerholder.get(j));
						coinholder.add(temp.coinholder.get(j));
					}
					powerholder.remove(powerholder.size()-1);
					powerholder.add(power);
					coinholder.remove(coinholder.size()-1);
					coinholder.add(coins);
					
					
					posLocations.get(closest).coins = 0;
					posLocations.get(closest).power = 0;
					
					
					visitedStations.add(posLocations.get(closest).id);
				//else station is skipped and added to skippedStation arraylist for further checking 
				} else if (temp.skip == true && !skippedStations.contains(posLocations.get(closest))){
					skippedStations.add(posLocations.get(closest));
					//restore power and moves values since station was skipped
					power = temp.power;
					moves = temp.moves;
				}
				
			}
		//run until visitedStatios has all of the positive stations or move/power run out
		} while (visitedStations.size() != posLocations.size() && moves > 0 && power > 1.25);
		//the coins charged from the last station isnt updated
		//update maunally
		coinholder.remove(coinholder.size()-1);
		coinholder.add(coins);
		
		//calculates the last 2 moves done by the drone 
		int[] last2moves = new int[2];
		last2moves[0] = currentPos.direction(positionset.get(positionset.size()-2));
		last2moves[1] = currentPos.nextPosition(last2moves[0]).direction(currentPos);
		
		int movesholder = moves;
		//go back and forth to use up all 250 moves (uses 251 moves when drone completes map on an odd number of moves)
		do {
			currentPos = currentPos.nextPosition(last2moves[0]);
			positionset.add(currentPos);
			coinholder.add(coinholder.get(coinholder.size()-1));
			power-=1.25;
			powerholder.add(power);
			movesholder--;
			currentPos = currentPos.nextPosition(last2moves[1]);
			positionset.add(currentPos);
			coinholder.add(coinholder.get(coinholder.size()-1));
			power-=1.25;
			powerholder.add(power);
			movesholder--;
			
		} while (movesholder > 0);
		
		//remove last move since it is move 251 on odd number of completing moves
		if (moves % 2 == 1) {
			positionset.remove(positionset.size()-1);
			powerholder.remove(powerholder.size()-1);
			coinholder.remove(coinholder.size()-1);
			power+=1.25;
		}
		
		//moveslist is set to the positionset for use in writemap
		moveslist = positionset;
		
		//return the results of this simulation using the given start
		movesToComplete = 250-moves;
		
		//if this is the best simulation of drone, print the .txt file for it
		if (actuallycharge) {
			
			File file = new File(Map.filenameTxt);
			file.delete();
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			for (int i = 1; i < moveslist.size(); i++) {
				String filecontent = moveslist.get(i-1).latitude + "," + moveslist.get(i-1).longitude + "," + Position.IntToDirection(moveslist.get(i-1).direction(moveslist.get(i))) + "," + moveslist.get(i).latitude + "," + moveslist.get(i).longitude + "," + coinholder.get(i) + "," + powerholder.get(i);
				System.out.println("Move: " + i +  " " + filecontent);
				bw.write(filecontent);
				if (i != 250) {
					bw.newLine();
				}
			}
			System.out.println(moveslist.size());
			System.out.println(coinholder.size());
	        bw.close();
		}
		
		//for testing purposes: accuracy of the drone
		//System.out.println("--------------------DRONE " + start + " RESULTS------------------");
		//System.out.println("\nCoins collected: " + coins + "\nPower collected: " + (power-250));
		//System.out.println("\nMax Coins: " + maxCoins + "\nMax Power: " + maxPower);
		//if ((coins/maxCoins)*100 > 100 || (coins/maxCoins)*100 > 99.999 && (coins/maxCoins)*100 < 100) {
		//	System.out.println("\nACCURACY: " + 100.0 + "%");
		//} else {
		//	System.out.println("\nACCURACY: " + (coins/maxCoins)*100 + "%");
		//}
		//System.out.println("-------------------------END-------------------------");
		
	}

}
