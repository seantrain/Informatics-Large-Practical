package uk.ac.ed.inf.powergrab;

import java.util.*;

public class Position {

	public double latitude;
	public double longitude;
	
	public Position (double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public Position nextPosition(int direction) {
		
		//variables to hold new position of drone
		double newlongitude = 0;
		double newlatitude = 0;
		
		//distance that drone moves
		double r = 0.0003;
		
		double w = 0;
		double h = 0;
		
		//calculating the correct position depending on the direction given
		if (direction == 0) {
			newlatitude = latitude + r;
			newlongitude = longitude;
			
		} else if (direction == 1) {
			w = r* Math.cos(Math.toRadians(67.5));
			h = r*Math.sin(Math.toRadians(67.5));
			newlongitude = longitude + w;
			newlatitude = latitude + h;
			
		} else if (direction == 2) {
			w = r*Math.cos(Math.toRadians(45));
			h = r*Math.sin(Math.toRadians(45));
			newlongitude = longitude + w;
			newlatitude = latitude + h;
			
		} else if (direction == 3) {
			w = r*Math.cos(Math.toRadians(22.5));
			h = r*Math.sin(Math.toRadians(22.5));
			newlongitude = longitude + w;
			newlatitude = latitude + h;
			
		} else if (direction == 4) {
			newlongitude = longitude + r;
			newlatitude = latitude;
			
		} else if (direction == 5) {
			w = r*Math.cos(Math.toRadians(22.5));
			h = r*Math.sin(Math.toRadians(22.5));
			newlongitude = longitude + w;
			newlatitude = latitude - h;
			
		} else if (direction == 6) {
			w = r*Math.cos(Math.toRadians(45));
			h = r*Math.sin(Math.toRadians(45));
			newlongitude = longitude + w;
			newlatitude = latitude - h;
			
		} else if (direction == 7) {
			w = r* Math.cos(Math.toRadians(67.5));
			h = r*Math.sin(Math.toRadians(67.5));
			newlongitude = longitude + w;
			newlatitude = latitude - h;
			
		} else if (direction == 8) {
			newlongitude = longitude;
			newlatitude = latitude - r;
			
		} else if (direction == 9) {
			w = r* Math.cos(Math.toRadians(67.5));
			h = r*Math.sin(Math.toRadians(67.5));
			newlongitude = longitude - w;
			newlatitude = latitude - h;
			
		} else if (direction == 10) {
			w = r*Math.cos(Math.toRadians(45));
			h = r*Math.sin(Math.toRadians(45));
			newlongitude = longitude - w;
			newlatitude = latitude - h;
			
		} else if (direction == 11) {
			w = r*Math.cos(Math.toRadians(22.5));
			h = r*Math.sin(Math.toRadians(22.5));
			newlongitude = longitude - w;
			newlatitude = latitude - h;
			
		} else if (direction == 12) {
			newlongitude = longitude - r;
			newlatitude = latitude;
			
		} else if (direction == 13) {
			w = r*Math.cos(Math.toRadians(22.5));
			h = r*Math.sin(Math.toRadians(22.5));
			newlongitude = longitude - w;
			newlatitude = latitude + h;
			
		} else if (direction == 14) {
			w = r*Math.cos(Math.toRadians(45));
			h = r*Math.sin(Math.toRadians(45));
			newlongitude = longitude - w;
			newlatitude = latitude + h;
			
		} else if (direction == 15) {
			w = r* Math.cos(Math.toRadians(67.5));
			h = r*Math.sin(Math.toRadians(67.5));
			newlongitude = longitude - w;
			newlatitude = latitude + h;
		}

		Position newPosition = new Position(newlatitude,newlongitude);
	
		return newPosition;
	}
	
	//converts the direction from an interger into string for printing to file
	public static String IntToDirection(int a) {
		
		if (a == 0) {
			return "N";
		} else if (a == 1) {
			return "NNE";
		} else if (a == 2) {
			return "NE";
		}else if (a == 3) {
			return "ENE";
		}else if (a == 4) {
			return "E";
		}else if (a == 5) {
			return "ESE";
		}else if (a == 6) {
			return "SE";
		}else if (a == 7) {
			return "SSE";
		}else if (a == 8) {
			return "S";
		}else if (a == 9) {
			return "SSW";
		}else if (a == 10) {
			return "SW";
		}else if (a == 11) {
			return "WSW";
		}else if (a == 12) {
			return "W";
		}else if (a == 13) {
			return "WNW";
		}else if (a == 14) {
			return "NW";
		}else if (a == 15) {
			return "NNW";
		}
		
		return "";
	}

	//calculates the distance between current position and a given position
	public double distanceBetween(Position a) {
		
		double distance = Math.sqrt(Math.pow(a.longitude-longitude, 2)+Math.pow(a.latitude-latitude, 2));
		
		return distance;
	}
	
	//checks if the current position is within distance to charge (0.003) of a given position
	public boolean withinDistance(Position a) {
		
		double distance = distanceBetween(a);
		
		if (distance <= 0.00025) {
			return true;
		}
		
		return false;
	}
	
	//checks over all powerstations if the current position is within distance to any station
	public boolean withinDistanceAny(ArrayList<Powerstation> locations) {
		
		
		for (int i = 0; i < locations.size(); i++) {
			if (withinDistance(locations.get(i).pos)) {
				return true;
			}
			
		}
		
		return false;
		
	}
	
	//checks if the current position is within the play area of the map
	public boolean inPlayArea() {
		
		//check that given position of the drone is within the specified map area
		if (longitude < -3.184319 && longitude > -3.192473) {
			if (latitude < 55.946233 && latitude > 55.942617) {
				
				return true;
			}
		}
		
		return false;
		
	}
	
	//returns direction from one position to another
	public int direction(Position destination) {
		
		int travelin = 0;
		
		double xlong = destination.longitude - longitude;
		double ylat = destination.latitude - latitude;
		
		double d = Math.atan2(ylat,xlong);
		
		d = Math.toDegrees(d);
		
		d = (d + 360) % 360;
		
		if ( d > 348.75 || d <= 11.25) {
			travelin = 4;
		} else if (d > 11.25 && d <= 33.75) {
			travelin = 3;
		} else if (d > 33.75 && d <= 56.25) {
			travelin = 2;
		} else if (d > 56.25 && d <= 78.75) {
			travelin = 1;
		} else if (d > 78.75 && d <= 101.25) {
			travelin = 0;
		} else if (d > 101.25 && d <= 123.75) {
			travelin = 15;
		} else if (d > 123.75 && d <= 146.25) {
			travelin = 14;
		} else if (d > 146.25 && d <= 168.75) {
			travelin = 13;
		} else if (d > 168.75 && d <= 191.25) {
			travelin = 12;
		} else if (d > 191.25 && d <= 213.75) {
			travelin = 11;
		} else if (d > 213.75 && d <= 236.25) {
			travelin = 10;
		} else if (d > 236.25 && d <= 258.75) {
			travelin = 9;
		} else if (d > 258.75 && d <= 281.25) {
			travelin = 8;
		} else if (d > 281.25 && d <= 303.75) {
			travelin = 7;
		} else if (d > 303.75 && d <= 326.25) {
			travelin = 6;
		} else if (d > 326.25 && d <= 348.75) {
			travelin = 5;
		}
		
		return travelin;
		
	}
	
	
}
