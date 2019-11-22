package uk.ac.ed.inf.powergrab;

import java.io.*;
import java.net.*;
import java.util.*;
import com.mapbox.geojson.*;


public class Map {
	
	public String mapSource;
	public String linkString;
	public String day;
	public String month;
	public String year;
	public ArrayList<Powerstation> psarray = new ArrayList<Powerstation>();
	ArrayList<ArrayList<Powerstation>> totalLocations = new ArrayList<ArrayList<Powerstation>>();
	public static String filename;
	
	
	public Map(String day, String month, String year) throws MalformedURLException, IOException {
		this.day = day;
		this.month = month;
		this.year = year;
		this.link(day,month,year);
		this.readMap(linkString);
		this.powerstations(mapSource);
		
		this.Filename(day, month, year);
		
	}
	
	public void link(String day, String month, String year) {
		
		String givenDate = year + "/" + month + "/" + day;
		linkString = "http://homepages.inf.ed.ac.uk/stg/powergrab/" + givenDate + "/powergrabmap.geojson";
		
		
	}
	
		
	public void readMap(String linkString) throws IOException, MalformedURLException {
		
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
	
	public void powerstations(String mapSource) {
		
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
	
	public static ArrayList<ArrayList<Powerstation>> seperateLocations(ArrayList<Powerstation> locations) {
			
			ArrayList<ArrayList<Powerstation>> totalLocations = new ArrayList<ArrayList<Powerstation>>();
			ArrayList<Powerstation> posLocations = new ArrayList<Powerstation>();
			ArrayList<Powerstation> negLocations = new ArrayList<Powerstation>();
			for (int i = 0; i < locations.size(); i++) {
				if (locations.get(i).coins > 0) {
					posLocations.add(locations.get(i));
				} else {
					negLocations.add(locations.get(i));
				}
			}
			
			totalLocations.add(posLocations);
			totalLocations.add(negLocations);
			
			return totalLocations;
	}

	
	public void Filename(String day, String month, String year) {
		
		filename = "stateless" + "-" + day + "-" + month + "-" + year + ".txt";
		
	}
	
}
