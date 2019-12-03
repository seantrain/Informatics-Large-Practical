package uk.ac.ed.inf.powergrab;

import java.io.*;
import java.net.*;
import java.util.*;
import com.mapbox.geojson.*;

//class for downloading and parsing the map
public class Map {
	
	public String mapSource;
	public String linkString;
	public String day;
	public String month;
	public String year;
	public String mode;
	public ArrayList<Powerstation> psarray = new ArrayList<Powerstation>();
	public static String filenameTxt;
	public static String filenameGeojson;
	
	public Map(String day, String month, String year, String mode) throws MalformedURLException, IOException {
		this.day = day;
		this.month = month;
		this.year = year;
		this.mode = mode;
		this.link();
		this.readMap();
		this.powerStations();
		this.filenameTxt();
		this.filenameGeojson();
		
	}
	
	//creates the link using day, month and year from command line input
	public void link() {
		
		String givenDate = year + "/" + month + "/" + day;
		linkString = "http://homepages.inf.ed.ac.uk/stg/powergrab/" + givenDate + "/powergrabmap.geojson";
		
	}
	
	//downloads the map and creates a string mapSource that hold the entire file content
	public void readMap() throws IOException, MalformedURLException {
		
		InputStream in;
		
		URL mapURL = new URL(linkString);
		
		HttpURLConnection conn = (HttpURLConnection) mapURL.openConnection();
		
		conn.setReadTimeout(10000);
		conn.setConnectTimeout(15000);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		conn.connect();
		
		in = conn.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder out = new StringBuilder();
		String line;
		
		while ((line = reader.readLine()) != null) {
			out.append(line);
		}
		
		reader.close();
		
		mapSource = out.toString();
		
	}
	
	//takes in mapSource created above, parses the file and stores the powerstations into custom data structure Powerstation 
	public void powerStations() {
		
		Position pos;
		double coins = 0;
		double power = 0;
		String id;
		
		List<Feature> f = FeatureCollection.fromJson(mapSource).features();
		
		for (int i = 0; i < f.size(); i++) {
			Point a = (Point) f.get(i).geometry();
			pos = new Position(a.coordinates().get(1),a.coordinates().get(0));
			
			coins = f.get(i).properties().get("coins").getAsDouble();
			power = f.get(i).properties().get("power").getAsDouble();
			id = f.get(i).properties().get("id").getAsString();			
			Powerstation tempstation = new Powerstation(pos, coins, power, id);
			psarray.add(tempstation);
			
		}
		
	}
	
	//seperates all the powerstations into 
	public static SeparateStations sepLocations(ArrayList<Powerstation> locations) {
			
		SeparateStations stationHolder = new SeparateStations();
			
		for (int i = 0; i < locations.size(); i++) {
			if (locations.get(i).coins > 0) {
				stationHolder.positive.add(locations.get(i));
			} else {
				stationHolder.negative.add(locations.get(i));
			}
		}
		
		return stationHolder;
	}

	
	public void filenameTxt() {
		
		filenameTxt = mode + "-" + day + "-" + month + "-" + year + ".txt";
		
	}
	
	public void filenameGeojson() {
		
		filenameGeojson = mode + "-" + day + "-" + month + "-" + year + ".geojson";
		
	}
	
	
}
