Read me:
This project was written and run from Netbeans, which affects the structure of its packages.
The main() method is in ClientControl.

1.)To run this project:
generate .jar file:
	Eclipse: file->export->Java->select Jar, then select destination
	Netbeans: run->clean and build, Jar file is generated in the dist folder for the project

VNC to a unm system that can connect to the server running the server version of AntWorld:
The server that this project is run on can be accessed from moons.cs.unm.edu. FTP the jar file to a desired
directory on moons (we used FileZilla). Ssh into moons, login, then enter 'vncserver'. Use the output from this
command to forward a port on you system to tunnel into a moons server (optionally, tunnel into a machine closer to
the running machine, in b146, such as b146-41). To open this tunnel, we used putty. Once the tunnel is open, open a
VNC session (we used tightVNC). In the VNC session, navigate to the directory where the jar file for the project was saved.
Then run the program with the command 'java -jar AntWorld.jar b146-XX' where XX is the id of the server you wish to run on,
such as 76 or 26.

2.) description of the program
control starts in ClientControl class. The main() creates a new ClientControl object, the constructor of which begins the
game logic. It connects the server, sends and receives data, and then begins the mainGameLoop. This begins
chooseActionOfAllAnts which calls chooseActionOfAnts, where the next action of each individual ant is determined. In addition
to choosing the next action this method populates a queue of actions for each ant, that will allow the next action in the queue
to be popped while moves remain, with some tests to determine if the actions in the queue should be interrupted for a more important
task, such as returning to base to heal. This method is setup to test the most pertinent factors first by if/else,
then see if the ant has moves in its queue and to follow them if it does, also controlled through if/else. If the ant does not
have actions in its queue, the program calls a class that builds them ActionQueue, which we have created an object of: commandAnts.
CommandAnts contains a hash map from ant.id to linkedList<AntActions>, this linkedList is where we add actions for the ant to take.
2 methods of the ActionQueue are collectFood and nestToHeal, which are used to build a complete queue for healing and collecting.
When building queues from commandAnts, the BLine movement (straight line) is used. CommandAnts also has a hashmap from ant.id to an
AntAction of specific type. We use this type to encode what quest the ant is on:

PICKUP=collect and return with food
HEAL=return to base to heal
ENTER_NEST=Ant is being called back home for being too far from base
MOVE=Ant is wandering to uncover food
ATTACK=Ant is going to attack enemy ant

3.)UnitTesting
our unit testing consists a buffered image and booleans that control the print output and the output drawn on the image

booleans controlling testing in ClientControl:
	DEBUG=prints output related to server connection issues and commData parameters
	TRACKACTION=prints each ants action each turn
	SCORING=prints values of food in foodData as well as number of ants
	DRAW=draws the map, centered on our base, updates and movement each turn
	DRAWTASK= draws ant trails ant trails 100 turns, then clears them, leaves colored trails for ants carrying food
		our ants appear as white with a  red center pixel, and enemies appear as black with a blue center pixel.
	DRAWLINES=tells the game to brith new ants based on food values

boolean controlling testing in BLine	
	BLINEDRAW=draws the path an ant will travel on as a redline

