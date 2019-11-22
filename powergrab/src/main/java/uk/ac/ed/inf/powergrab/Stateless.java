package uk.ac.ed.inf.powergrab;

import java.io.*;
import java.util.*;

public class Stateless {
	
	public int seed;
	Random random;
	public ArrayList<Position> moveslist = new ArrayList<Position>();
	public Map currentmap;
	public double startlat;
	public double startlong;
	public int moves = 250;
	public double coins = 0.0;
	public double power = 250.0;
	
	public Stateless(Map currentmap, int seed, double startlat, double startlong) throws IOException {
		
		this.currentmap = currentmap;
		this.seed = seed;
		this.startlat = startlat;
		this.startlong = startlong;
		this.random = new Random(seed);
		
		this.stateless();
		
	}
	
	public ArrayList<Double> UpdateDrone(Indexlist[] locate, int index) {
		
		ArrayList<Double> droneValues = new ArrayList<Double>();
		
		double newcoins = Math.abs(coins + locate[index].totalcoins);
		double newpower = Math.abs(power + locate[index].totalpower);

		droneValues.add(newcoins);
		droneValues.add(newpower);
		
		return droneValues;
		
		
	}
	
	public void UpdatePowerstation(double coins, double power, Indexlist[] locate, int index, ArrayList<Powerstation> locations) {
		
		
		
		//if powerstation.coins is negative and its abs values is larger than the drones coins
		//calcuate the difference of the abs value and the coins value and leave in the powerstation
		//else if the powerstation.coins is positive, set to 0 (drone has taken them all)
		if (Math.abs(locations.get(locate[index].index).coins) > coins) {
			locations.get(locate[index].index).coins += coins;
		} else {
			locations.get(locate[index].index).coins = 0;
		}
		//same for power
		if (Math.abs(locations.get(locate[index].index).power) > power) {
			locations.get(locate[index].index).power += power;
		} else {
			locations.get(locate[index].index).power = 0;
		}
		
		
		
	
	}
	
	
	public void stateless() throws IOException {
				
		int rand = random.nextInt(16);
		Position currentPos = new Position(startlat, startlong);
		moveslist.add(currentPos);
		
		ArrayList<Powerstation> locations = new ArrayList<Powerstation>();
		locations = currentmap.psarray;
		
		ArrayList<Integer> dontGo = new ArrayList<Integer>();
		ArrayList<Integer> visitedps = new ArrayList<Integer>();
		Indexlist[] locate = new Indexlist[16];
		
		File file = new File(Map.filename);
		file.delete();
		
		
		
		do {
			dontGo.clear();
			
			
			for (int j = 0; j < 16; j++) {
				
				Indexlist instance = new Indexlist();
				locate[j] = instance;
				double closest = 1000;
				int closestindex = 1000;
				
				for (int i = 0; i < locations.size(); i++) {
					if ((!visitedps.contains(i)) && (currentPos.nextPosition(j).withinDistance(locations.get(i).pos)) && (currentPos.nextPosition(j).inPlayArea())) {
						double temp = currentPos.distanceBetween(locations.get(i).pos);
						//finds the closeset powerstation that is in range by moving the drone in direction j
						if (temp < closest) {
							closest = temp;
							closestindex = i;
						}
					}
				}
				
				
				if (closest < 1000) {
					instance.totalcoins += locations.get(closestindex).coins;
					instance.totalpower += locations.get(closestindex).power;
					instance.index = closestindex;
					//System.out.println("Location: " + instance.pslist.get(l) + " Coins: " + viablelocations.get(l).coins);
				}
				//System.out.println("Total coins: " + instance.totalcoins + "\n");
				locate[j] = instance;
					
			}
			
			//System.out.println(locate[j].totalcoins + " Move: " + (250-moves) + " " + Position.IntToDirection(j) +" "+ locate[j].pslist);
				
			
			for (int k = 0; k < 16; k++) {
				if (locate[k].totalcoins < 0 && !dontGo.contains(k)) {
					dontGo.add(k);
				}
			}
			
			//find direction with the largest coin value overall
			int maxdirection = 1000;
			double maxcoins = locate[0].totalcoins;
			
			for (int i = 0; i < 16; i++) {
				if (locate[i].totalcoins > maxcoins) {
					maxdirection = i;
				}
			}
			
			//System.out.println(Position.IntToDirection(maxdirection) + " Empty " + (locate[maxdirection].pslist.size() == 0) + " " + locate[maxdirection].totalcoins);
			
			Position temp = currentPos;
			int directionHolder = -1;
			
			if (maxdirection != 1000 && !dontGo.contains(maxdirection)) {
				
				currentPos = currentPos.nextPosition(maxdirection);
				directionHolder = maxdirection;
				visitedps.add(locate[maxdirection].index);
				
			} else if (maxdirection == 1000 || dontGo.contains(maxdirection)){
				int counter = 0;
				do {
					//check that drones next move is a valid move and that the random direction it goes to does not contain negative coins
					if (currentPos.nextPosition(rand).inPlayArea() && !dontGo.contains(rand)) {
						currentPos = currentPos.nextPosition(rand);
						directionHolder = rand;
						rand = random.nextInt(16);
						break;
					//if all 16 moves produce negative coins, move in the least negative direction (which is the maxdirection)
					} else if (dontGo.size() == 16){
						currentPos = currentPos.nextPosition(maxdirection);
						directionHolder = maxdirection;
						break;
					//if both are not true, meaning there is available direction to travel to, loop until value that is not in dont go is found
					//this ensures that the program always runs the random seed
					} else {
						rand = random.nextInt(16);
					}
					//ensuring the loop will timeout (100000 attemtps) if it fails to find the next direction to go to that is valid
					counter++;
				} while (counter < 100000);	
			}
			//create copy for printing to file when updating power station
			double tempcoins = coins;
			double temppower = power;
			//change real coins and power values
			coins = UpdateDrone(locate, directionHolder).get(0);
			power = UpdateDrone(locate, directionHolder).get(1) - 1.25;
			//change the powerstation values
			UpdatePowerstation(tempcoins, temppower, locate, directionHolder, locations);
			
			//decrease number of moves left
			moves--;
			//add new position to moveslist for writing flightpath line into the geojson
			moveslist.add(currentPos);
			
			
			//print the required line of information
			String filecontent = temp.latitude + "," + temp.longitude + "," + Position.IntToDirection(directionHolder) + "," + currentPos.latitude + "," + currentPos.longitude + "," + coins + "," + power;
			
			//if the file doesn't exist, create it (done on first iteration of do loop)
			if (!file.exists()) {
				file.createNewFile();
			}
	        
			//write to file
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
	        BufferedWriter bw = new BufferedWriter(fw);
	        bw.write(filecontent);
	        bw.newLine();
	        bw.close();
	        
		    //System.out.println("Move: " + (250 -moves) + "    Coins: " + coins + "  Power: " + power + "  Direction: " + Position.IntToDirection(directionHolder) + "  Closest : " + locate[directionHolder].index + " Don't go: "+ dontGo.toString() +" Coinsgiven: " + locate[directionHolder].totalcoins + " Powergiven: " + locate[directionHolder].totalpower);
		    
		} while (moves > 0 && power > 1.25);

	}
	
	
	
	
}
