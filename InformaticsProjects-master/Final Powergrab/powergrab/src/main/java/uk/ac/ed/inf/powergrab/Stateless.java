package uk.ac.ed.inf.powergrab;

import java.io.*;
import java.util.*;

public class Stateless {
	
	public int seed;
	Random random;
	//arraylist that stores the positions of the drone after each move
	public ArrayList<Position> moveslist = new ArrayList<Position>();
	public Map currentmap;
	public double startlat;
	public double startlong;
	public int moves = 250;
	public double coins = 0.0;
	public double power = 250.0;
	//this is purely for testing, does not get used in the execution of the stateless drone
	public double maxCoins = 0;
	
	public Stateless(Map currentmap, int seed, double startlat, double startlong, boolean writefile) throws IOException {
		
		this.currentmap = currentmap;
		this.seed = seed;
		this.startlat = startlat;
		this.startlong = startlong;
		this.random = new Random(seed);
		
		maxCoins = this.testingCalcMaxCoins(); 
		//the execution of the stateless drone
		this.stateless(writefile);
		
	}
	//TESTING ONLY, NOT USED BY THE STATELESS DRONE IN ANY WAY 
	//used in App for calculating the accuracy of the drone
	public double testingCalcMaxCoins() {
		
		ArrayList<Powerstation> positive = Map.sepLocations(currentmap.psarray).positive;
		double maxCoinsHolder = 0;
		for (int i = 0 ; i < positive.size(); i++) {
			maxCoinsHolder += positive.get(i).coins;
		}
		
		return maxCoinsHolder;
	}
	
	//updates the drones coins and power when charging from given direction using the set of closestStations
	public void updateDrone(ClosestStation[] closestStations, int direction) {
		
		//coins cannot be negative, must check that the stations coins before adding
		if (coins > Math.abs(closestStations[direction].totalcoins)) {
			coins = coins + closestStations[direction].totalcoins;
		} else {
			if (closestStations[direction].totalcoins >= 0) {
				coins = coins + closestStations[direction].totalcoins;
			} else if (closestStations[direction].totalcoins < 0) {
				coins = 0;
			}
		}
		
		if (power > Math.abs(closestStations[direction].totalpower)) {
			power = power + closestStations[direction].totalpower -1.25;
		} else {
			if (closestStations[direction].totalpower >= 0) {
				power = power + closestStations[direction].totalpower -1.25;;
			} else {
				power = 0;
			}
		}

	}
	
	//updates the powerstations values when the station is charged from
	public void updatePowerstation(ClosestStation[] closestStations, int direction, ArrayList<Powerstation> locations) {
		
		//if powerstation.coins is negative and its abs values is larger than the drones coins
		//calcuate the difference of the abs value and the coins value and leave in the powerstation
		//else if the powerstation.coins is positive, set to 0 (drone has taken them all)
		if (Math.abs(closestStations[direction].totalcoins) > coins) {
			if (closestStations[direction].totalcoins < 0) {
				locations.get(closestStations[direction].index).coins += coins;
			} else {
				locations.get(closestStations[direction].index).coins = 0;
			}
		} else {
			locations.get(closestStations[direction].index).coins = 0;
		}
		//same for power
		if (Math.abs(closestStations[direction].totalpower) > coins) {
			if (closestStations[direction].totalpower < 0) {
				locations.get(closestStations[direction].index).power += power;
			} else {
				locations.get(closestStations[direction].index).power = 0;
			}
		} else {
			locations.get(closestStations[direction].index).power = 0;
		}
		
	}
	
	//stateless drone execution (writefile determines whether to write the .txt file for the drone)
	public void stateless(boolean writefile) throws IOException {
				
		
		Position currentPos = new Position(startlat, startlong);
		//add the initial starting position of the drone to the moveslist
		moveslist.add(currentPos);
		
		ArrayList<Powerstation> locations = new ArrayList<Powerstation>();
		locations = currentmap.psarray;
		//arraylists for directions to avoid (cantGo) and directions available to go (canGo)
		ArrayList<Integer> cantGo = new ArrayList<Integer>();
		ArrayList<Integer> canGo = new ArrayList<Integer>();
		//array of custom data structure ClosestStations (holds the index, coins and power) of the closest stations in the 16 direction
		ClosestStation[] closestStations = new ClosestStation[16];
		
		//create .txt file for the drone simulation and removes an existing file is there is one
		File file = new File(Map.filenameTxt);
		file.delete();
		
		
		
		do {
			//clear the directions to avoid for each move
			cantGo.clear();
			canGo.clear();
			//check every direction for closest powerstation that is within one move of the current position
			for (int j = 0; j < 16; j++) {
				
				ClosestStation instance = new ClosestStation();
				//placeholder values that will be changed
				double closest = 1000;
				int closestindex = 1000;
				//check every powerstation so see if it is within range after 1 move in direction j
				for (int i = 0; i < locations.size(); i++) {
					//check the station within range has not been visited before and that moving in direction j is within the play area
					if ((currentPos.nextPosition(j).withinDistance(locations.get(i).pos)) && (currentPos.nextPosition(j).inPlayArea())) {
						double temp = currentPos.distanceBetween(locations.get(i).pos);
						//finds the closest station if multiple satisfy above if statement in direction j
						if (closest >= temp) {
							closest = temp;
							closestindex = i;
						}
					}
				}
				
				//updates the instance of ClosestStation with the values of the closest station in direction j
				//if there is no stations, default values for instance are applied
				if (closest != 1000 && closestindex != 1000) {
					instance.totalcoins += locations.get(closestindex).coins;
					instance.totalpower += locations.get(closestindex).power;
					
					instance.index = closestindex;
				//if no stations are in range, station index that is closest is -1 (no station)
				} else {
					instance.index = -1;
				}
				//stores the closest stations values in the
				closestStations[j] = instance;
									
			}
			
				
			//uses the closestStations array to seperate the direction the drone can go in 
			//into 2 arraylists, canGo and cantGo and finds the direction with the largest coin value
			int maxdirection = -1;
			double maxcoins = 0;
			for (int i = 0; i < 16; i++) {
				//adds the non negative station directions to canGo
				if (closestStations[i].totalcoins >= 0 && !cantGo.contains(i) && currentPos.nextPosition(i).inPlayArea()) {
					canGo.add(i);
					//finds the maximum direction out of the non-negative closest stations
					if (closestStations[i].totalcoins > maxcoins) {
						maxdirection = i;
						maxcoins = closestStations[i].totalcoins;
					}
				//add the negative station directions to cantGo
				} else {
					cantGo.add(i);
				}
			}
			
			//directionHolder is used when updating the drone position
			//both are used for the text file output
			int directionHolder = -1;
			Position temp = currentPos;
			
			//if the max direction is not -1 (there is station(s) with larger values than other stations)
			if (maxdirection != -1 && !cantGo.contains(maxdirection)) {
				directionHolder = maxdirection;
				
			//else if the max direction is negative (cantGo) or station in direction 0 has the largest/joint largest coins value
			} else if (cantGo.contains(maxdirection) || maxdirection == -1) {
				//check if there are non negative directions to travel in
				if (canGo.size() > 0) {
					//if canGo only has 1 direction, go in that direction
					if (canGo.size() == 1) {
						directionHolder = canGo.get(0);
					//if canGo has more than one direction, randomly choose the direction to go in from canGo
					//using seeded random number to choose index in canGo
					} else {
						int rd = random.nextInt(canGo.size()-1);
						directionHolder = canGo.get(rd);
					}
					
				//else if all direction to go in are negative/out of bounds (cantGo has all 16 directions)
				} else if (canGo.size() == 0) {
					double lowestnegative = -1000;
					//look in all directions bad directions, find those that stay in the play area
					for (int i = 0; i < cantGo.size(); i++) {
						if (currentPos.nextPosition(cantGo.get(i)).inPlayArea()) {
							//find the direction that has the lowest negative impact on the drones coins
							if (closestStations[cantGo.get(i)].totalcoins > lowestnegative) {
								lowestnegative = closestStations[cantGo.get(i)].totalcoins;
								directionHolder = cantGo.get(i);
							}
						}
					}
				}
			}

			
			//update the position of drone in the direction in directionHolder calculated above
			currentPos = currentPos.nextPosition(directionHolder);
			
			
			//updates the powerstations coins and power before the drone
			//since this checks if the drone can take all of the coins and power from the powerstation.
			//Using the updated drones coins value would give an incorrect result
			if (closestStations[directionHolder].index != -1) {
				updatePowerstation(closestStations, directionHolder, locations);
			//updates the drones coins and power
				updateDrone(closestStations, directionHolder);
			} else {
				power -=1.25;
			}

			//add new position to moveslist for writing flightpath line into the geojson
			moveslist.add(currentPos);

			//if the simulation file is to meant to be written based on App.java input
			//wont be written if all maps are being tested (more efficient, can test the values through System.out.print)
			if (writefile) {
				//create the required line of information
				String filecontent = temp.latitude + "," + temp.longitude + "," + Position.IntToDirection(directionHolder) + "," + currentPos.latitude + "," + currentPos.longitude + "," + coins + "," + power;
				//file doesn't exits, create it
				if (!file.exists()) {
					file.createNewFile();
				}
		        
				//write to file
				FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(filecontent);
				if (moves > 0) {
					bw.newLine();
				}
				bw.close();
			}
			//update moves by 1
			moves--;
		    //System.out.println("Move: " + (250 -moves) + "    Coins: " + coins + "  Power: " + power + "  Direction: " + directionHolder + "  Closest : " + closestStations[directionHolder].index + " Don't go: "+ cantGo.toString() +" Coinsgiven: " + closestStations[directionHolder].totalcoins + " Powergiven: " + closestStations[directionHolder].totalpower);
		
		//runs until drone runs out of moves or runs out of power
		} while (moves > 0 && power > 1.25);

	}
	
	
	
	
}
