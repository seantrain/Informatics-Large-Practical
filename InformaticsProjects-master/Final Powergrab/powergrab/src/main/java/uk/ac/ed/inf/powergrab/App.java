package uk.ac.ed.inf.powergrab;

import java.io.IOException;

public class App { 
	
    public static void main(String[] args) throws IllegalArgumentException, IOException {
    	//command line input arguments (day, month, year etc)
    	String day = args[0];
    	String month = args[1];
    	String year = args[2];
    	double startlat = Double.parseDouble(args[3]);
		double startlong = Double.parseDouble(args[4]);
		int seed = Integer.parseInt(args[5]);
		String mode = args[6];
		
		//for testing on maps (yes = all maps in given year | no = one map specified in command line input)
		String all = "yes";
		//set to 2019 or 2020 depending on the year to test
		String testyear = "2020";
    	
		//stateless drone execution
		if (mode.equals("stateless")) {
			//runs on all maps in specified year
			//does not write any files (writefile = false)
			if (all.equals("yes")) {
				double overallmaxcoins = 0;
				double overallcollected = 0;
				for(int i = 1; i < 13; i++) {
					for (int j = 1; j < 29; j++) {
						
						Map newMap = new Map(String.format("%02d", j), String.format("%02d", i), testyear, mode);
						Stateless newStateless = new Stateless(newMap, seed, startlat, startlong, false);
						System.out.println("Month: " + i + " Day: " + j + " Coins: " + newStateless.coins + " Power: " + newStateless.power);
						overallcollected += newStateless.coins;
						overallmaxcoins += newStateless.maxCoins;
					}
				}
				System.out.println("Overall Accuracy: " + (overallcollected/overallmaxcoins)*100);
			//runs on a specified map from command line input
			//writes both the .txt and .geojson files (writefile = true)
			//and the Writemap.writeline methhod is called
			} else if (all.equals("no")) {
		    	Map newMap = new Map(day, month, year, mode);
		    	Stateless newStateless = new Stateless(newMap, seed, startlat, startlong, true);
		    	System.out.println("Month: " + month + " Day: " + day + " Coins: " + newStateless.coins + " Power: " + newStateless.power);
		    	Writemap.writeline(newStateless.moveslist, newMap.mapSource);
		    	
			}
		//stateful drone execution
		} else if(mode.equals("stateful")) {
			//runs of all maps in specified year
			//doesn't write any files (actuallycharge = false)
			if (all.equals("yes")) {
				double overallmaxcoins = 0;
				double overallcollected = 0;
				for (int i = 1; i < 13; i++) {
					for (int j = 1; j < 29; j++) {
						Map newMap = new Map(String.format("%02d", j), String.format("%02d", i), testyear, mode);
						Stateful newStateful = new Stateful(newMap, startlat, startlong, false);
						overallmaxcoins+= newStateful.maxCoins;
						overallcollected += newStateful.coins;
						System.out.println("Month: " + i + " Day: " + j + " Coins: " + newStateful.coins + " Power: " + newStateful.power + " Accuracy: " + (newStateful.coins/newStateful.maxCoins));
					}
				}
				System.out.println("Overall Accuracy: " + (overallcollected/overallmaxcoins)*100);
			//runs on a specific map based on start arguments
			//run the best version with actuallycharge = true (powerstation values are changed)
			//writes both .txt and .geojson files 
			} else if (all.equals("no")) {
				
		    	Map newMap = new Map(day, month, year, mode);
				
				Stateful newStateful = new Stateful(newMap, startlat, startlong, true);
				System.out.println("Map: " + day + "/" + month + "/" + year + ": coins: " + newStateful.coins + " power: " + newStateful.power + " moves: " + newStateful.movesToComplete + " moves list size:" + newStateful.moveslist.size());
				//for accuracy testing purposes, gets the maximum coins & maximum power of the map
				double maxcoins = newStateful.maxCoins;
				double maxpower = newStateful.maxPower;
				
				//output format for checking accuracy, moves etc of the best drone simulation
				System.out.println("\n------------------DRONE RESULTS-------------------");
				System.out.println("Number of moves to complete map: " + newStateful.movesToComplete);
				System.out.println("\nCoins collected: " + newStateful.coins + "\nPower collected: " + (newStateful.power));
				System.out.println("\nMax coins available: " + maxcoins + "\nMax power available: " + maxpower);
				System.out.println("\nIs the power collected the most it can be? " + newStateful.power + " " + (maxpower-62.5));
				if ((newStateful.coins/maxcoins)*100 > 100 || (newStateful.coins/maxcoins)*100 > 99.999 && (newStateful.coins/maxcoins)*100 < 100) {
					System.out.println("\nACCURACY: " + 100.0 + "%");
				} else {
					System.out.println("\nACCURACY: " + (newStateful.coins/maxcoins)*100 + "%");
				}
				System.out.println("-----------------------END------------------------");
				//the best drone simulation .geojson file is written with the simulations flight path
				Writemap.writeline(newStateful.moveslist, newMap.mapSource);
			}
		}
		
    }
}
