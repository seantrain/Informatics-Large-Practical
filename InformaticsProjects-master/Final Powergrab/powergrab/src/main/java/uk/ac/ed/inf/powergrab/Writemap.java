package uk.ac.ed.inf.powergrab;

import java.io.*;
import java.util.*;

//writes the geojson map with the lineString of all the moves from moveslist
public class Writemap {

	public static void writeline(ArrayList<Position> moveslist, String mapSource) throws IOException {
		
		String formatedcoord = "";
		String setofcoords = "{" + "\"type\":\"Feature\"," + "\"properties\":{" + "" + 	"}," + "\"geometry\":{" + "\"type\":\"LineString\"," + "\"coordinates\":[";
		
		//goes through moveslist arraylist, formats the coordinates and creates the files content
		for (int i = 0; i < moveslist.size(); i++) {
			if (i+1 == moveslist.size()) {
				formatedcoord = "[" + moveslist.get(i).longitude + ", " + moveslist.get(i).latitude + "]]}}]}";
			} else {
				formatedcoord = "[" + moveslist.get(i).longitude + ", " + moveslist.get(i).latitude + "],";
			}
			setofcoords = setofcoords + formatedcoord;
			
		}
		
		//concatenates the geojson file with the lineString created
		String newMapSource = mapSource.substring(0,mapSource.length() - 10) + "," + setofcoords;
		
		//creates the geojson file
		String filename = Map.filenameGeojson;
		File file = new File(filename);
		
		//removes the already existing file if it exists, prevents adding to an existing file
		if (file.exists()) {
			file.delete();
			file.createNewFile();
		}
		
		//write to the file
		FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(newMapSource);
        bw.newLine();
        bw.close();
		
	}
	
}
