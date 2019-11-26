package uk.ac.ed.inf.powergrab;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

public class App 
{
    public static void main(String[] args) throws MalformedURLException, IOException, IllegalArgumentException {
    	
Map newMap = new Map(args[0], args[1], args[2]);
    	
		double startlat = Double.parseDouble(args[3]);
		double startlong = Double.parseDouble(args[4]);
		int seed = Integer.parseInt(args[5]);
		String mode = args[6];
		
		if (mode.equals("stateless")) {
			long start = System.nanoTime();
	    	Stateless newStateless = new Stateless(newMap, seed, startlat, startlong);
	    	long end = System.nanoTime();
	    	System.out.println(end-start);
			Writemap.writeline(newStateless.moveslist, newMap.mapSource, newStateless.currentmap.psarray, mode);
		} else if(mode.equals("stateful")) {
			int posLocationCount = Map.seperateLocations(newMap.psarray).get(0).size();
			Results[] totalresults = new Results[posLocationCount];
			//for accuracy testing purposes
			double maxcoins = 0;
			double maxpower = 0;
			for (int i = 0; i < posLocationCount; i++) {
				Stateful newStateful = new Stateful(newMap, startlat, startlong, i, false);
				totalresults[i] = newStateful.results;
				maxcoins = newStateful.maxCoins;
				maxpower = newStateful.maxPower;
				//System.out.println("Start " + i + ": coins: " + newStateful.results.totalcoins + " moves: " + newStateful.results.numberofmoves);
				
			}
			
			Results max = totalresults[0];
			int maxver = 0;
			int minmoves = 0;
			for (int i = 0 ; i < posLocationCount; i++) {

				if ((Math.round(totalresults[i].totalcoins) >= Math.round(max.totalcoins)) && (totalresults[i].numberofmoves < max.numberofmoves)) {
					max = totalresults[i];
					maxver = i;
					minmoves = totalresults[i].numberofmoves;
				}
			}
			
			//the run of the drone that will be mapped and where powerstation values are updated when charging from
			Stateful officialrun = new Stateful(newMap, startlat, startlong, maxver, true);

			System.out.println("\n---------------BEST DRONE(" + maxver + ") RESULTS----------------");
			System.out.println("Stuck drone: " + max.stuckcounter);
			System.out.println("Number of moves (initially): " + minmoves);
			System.out.println("\nCoins collected: " + max.totalcoins + "\nPower collected: " + (max.totalpower-250));
			System.out.println("\nMax Coins: " + maxcoins + "\nMax Power: " + maxpower);
			if ((max.totalcoins/maxcoins)*100 > 100 || (max.totalcoins/maxcoins)*100 > 99.999 && (max.totalcoins/maxcoins)*100 < 100) {
				System.out.println("\nACCURACY: " + 100.0 + "%");
			} else {
				System.out.println("\nACCURACY: " + (max.totalcoins/maxcoins)*100 + "%");
			}
			System.out.println("-------------------------END-------------------------");
			Writemap.writeline(officialrun.moveslist, newMap.mapSource, newMap.psarray, mode);
		}
		
    }
}
