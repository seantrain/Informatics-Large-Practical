# Informatics-Large-Practical

Project created in 3rd year of University.

The PDF contains the detailed report for the project.

The aim of the project was to create a virtual drone that can travel to charging stations which reward the drone with power and coins.
Power is used by the drone to travel, with each move costing 1.25 power units. The coins are the drone's score, higher is better.
The drone can travel in 16 directions (N,NNE,NE,...) and will charge from the closest station within the stations radius.

Two drone strategies were implemented:
  1. A stateless verison of the drone: Had only 1 move look ahead and couldn't store any information such as previous moves.
     Moved entirely by random number generation over 16 different directions and would not travel in a direction that
     could result in charging from a negative powerstation unless all directions were negative in which case it would
     travel in the least negative direction.
     
  2. A stateful version fo the drone: Has unlimited lookahead and implemented a strategy of always travelling to the closest positive
     powerstation. The drone would detect the cloest positive station and travel in that direction. If there is a negative station
     in the way, the drone would travel in the direction which avoids the negative station but gets it the closest to the selected
     positive station. In certain cases, the drone would fail to reach the positive station, if this was the case, this attempt to visit
     the positive station was seen as a simulation and the drones power and moves values were not updated. This positive station would
     be added to a list of positive stations that the drone failed to visit. After every successful visit to a positive station,
     the drone would check to see if it can visit the stations from the list of failed to visit stations to ensure that every one of the 
     paths from the other powerstations to the skipped powerstation were attempted to ensure that the drone has the best chance of visiting
     this powerstation.
