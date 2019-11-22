package uk.ac.ed.inf.powergrab;

import java.io.*;
import java.util.*;

public class Writemap {

	public static void writeline(ArrayList<Position> moveslist, String mapSource, ArrayList<Powerstation> locations, String mode) throws IOException {
		
		String formatedcoord = "";
		String setofcoords = "{" + "\"type\":\"Feature\"," + "\"properties\":{" + "" + 	"}," + "\"geometry\":{" + "\"type\":\"LineString\"," + "\"coordinates\":[";
		
		for (int i = 0; i < moveslist.size(); i++) {
			if (i+1 == moveslist.size()) {
				formatedcoord = "[" + moveslist.get(i).longitude + ", " + moveslist.get(i).latitude + "]]}}]}";
			} else {
				formatedcoord = "[" + moveslist.get(i).longitude + ", " + moveslist.get(i).latitude + "],";
			}
			setofcoords = setofcoords + formatedcoord;
			
		}
		//System.out.print(setofcoords);
		//System.out.println(mapSource.substring(0, mapSource.length() - 10));
		String newMapSource = mapSource.substring(0,mapSource.length() - 10) + "," + setofcoords;
		
		//System.out.println("Printing newmap: \n" + newMapSource);
		
		//filename creating
		String filename = Map.filename;
		
		if (mode.equals("stateless")) {
			filename = filename.substring(0,filename.length() -3) + "geojson";
			
		} else if (mode.equals("stateful")) {
			filename = "stateful" + filename.substring(9,filename.length() -3) + "geojson";
		}
		
		
		File file = new File(filename);
		
		if (file.exists()) {
			file.delete();
			file.createNewFile();
		}
		
		//write to file
		FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(newMapSource);
        bw.newLine();
        bw.close();
		
	}
	
}
