# ASN1_encoding
Communication using ASN1 encoding

Note: After start receiving the messages on sending a RECEIVE, a LEAVE command should
be sent in a new client. The server keeps track of the IP address and the current group that is
being sent over UDP.

List of files in the folder harshit_bhatt_josemar_dacruz
	MAIN FOLDER/PACKAGE:
	- NpServer.java - the server file that initiates a TCP & UDP server and process SQLITE queries.
	- NpClient.java - contains the client that is sending the requests to the server initiating either TCP/UDP connection.
	- Encode.java - java file that is encoding and decoding the input and output values for server and client and displaying that data.
	- Register_Messages.java - java file that handles the 1 hour event messages to the client.
	- ClientUDPMessages.java - java files responsible for the client UDP messages of Register and Leave
	- makefile - invoked by the Make program in Unix, compile the NpServer.java and GetOpt.java files in the folder.
	- run.sh - required to run the program. Sets the classpath and pass the port numbers and SQLlite database name to the NpServer.
	- client.sh - shell script executing Npclient.java file with parameters as server address, port, input value
	- np.db - it is the SQLite database file. If this file is deleted, the NpServer will create a new will once it starts.
	- documentation.pdf - this documentation file.
	- sqlite-jdbc-3.8.7.jar - java library containing class for SQLITE.

	FOLDER/PACKAGE ASN1:
		- All files in this folder are part of the ASN1 library

	FOLDER/PACKAGE ENCDEC:
		- Answer.java - provides Encode/Decode for Answer ::= [3] SEQUENCE OF Event
		- Event.java - provides Encode/Decode for Event Event ::= [1] SEQUENCE {time GeneralizedTime, group UTF8String, description UTF8String}
		- EventOK.java - provides Encode/Decode for EventOK ::= [0] SEQUENCE {code INTEGER}
		- Leave.java - provides Encode/Decode for Leave ::= [4] Register
		- Register.java - provides Encode/Decode for Register ::= [3] SEQUENCE {group UTF8String}
		- Request.java - provides Encode/Decode for Request ::= [2] SEQUENCE {group UTF8String, after_time GeneralizedTime}
		- TAG.java - provides specific byte TAGs to be use in the above ASN1 classes.

	FOLDER/PACKAGE Util:
	- GetOpt.java - contains the GetOpt class which is necessary to parse the command line arguments.
- Util.java - part of ASN1 library


CLIENT
The client is organized as follows:
1. Getopt parameters parsing (port number and database path);
2. Encodes the data into ASN1 with implicit TAG values;
13. Sends the data via Socket, either TCP or UDP to the Server;
4. Gets the answer from the Server;
5. Decodes the ASN1 and displays;

The functionality of sending REGISTER;GROUP or
LEAVE;GROUP over UDP is done in this. This is done in a separate thread.
After starting receiving the events that it registered for, if the client wants to stops the
messages, a new client with the command LEAVE should be started.
We treated it separately, because since the client keeps on printing events, it is difficult to
type the leave command and send to the server.
In the new client, if the LEAVE;GROUP command has a different group, the server will not
stop sending the data.


SERVER
The server is organized as follows:
1. Getopt parameters parsing (port number and database path);
2. Database creation if it doesn’t exists;
3. Call the TCP constructor and start the TCP thread which calls tcp_connect();
4. Call the UDP constructor and run the udp_connect();
5. Funtion tcp_connect() handles TCP connections and client input which is the parameter
to call function display(), and calls expireCheck() to update the event in case it is expired
as well as decoding the ASN1 from the client;
6. Function udp_connect() handles UDP connections and client ASN1 input over UDP and
also calls function display() and function expireCheck(). On udp_connect(), the Thread
to handle REGISTER commands is started;
7. Function display() receives an array of STRINGS which is the organized input from
the client, process them and return ASN1 encoded data either to tcp_connect() or
udp_connet().


For the server, the initial parameters are defined in the GetOpt object are -t which is the TCP
port number, -u which is the UDP port number, and -d which is the database number. The
file run.sh call the files created in the compilation as well as set the proper classpath with the
sqlite JDBC driver jar file. As required in the project instructions, the server listens to both TCP
and UDP in the same port.

For the client, the options are also parsed in GetOpt, being -s for server (e.g. localhost); -t if
using TCP or -u if using UDP, both containing the port number; -i for the input that is being
sent;
If the client is receiving a REGISTER command, it will ignore the -t (for TCP port) option
and sends the data over UDP.
We provide a populated database with data for testing the REGISTER and LEAVE commands.
For REGISTER command, the server will reply via UDP to the client, events that are happening
within 24 hours from the request, and this UDP messages will last for 1 hour or until the client
sends a LEAVE command.
The DB name can be altered in the run.sh file.

The program is contained in a folder called harshit_bhatt_josemar_dacruz.tgz. After decom-
pression, enter the created folder and run the command make to compile the source files.
To start the server, inside the folder run the command: ./run.sh 2356 , where 2356 is the
port number. This will start the server, which will listen on both TCP and UDP on port 2356.

We used netcat to test the server since that supports both TCP and UDP connections. With
the server listening to port 2356, we run: nc localhost 2356 . After that we proceed with the
formatted input in plain text as follows:
EVENT_DEFINITION;2015-03-12:18h30m00s001Z;Meeting with the user.;CSE5232
This is the command to create an event. The values are separed by comma. When the server
receives the above command, it replies back appending the word “OK” in the front of the
string, as follows:
OK,EVENT_DEFINITION;2015-03-12:18h30m00s001Z;Meeting with the user.;CSE5232
The server also replies to specific input to retrieve information from the database, such as
the following:
GET_NEXT_EVENTS;CSE5232;2015-03-12:18h30m00s000Z
The above command retrieves all the events happening at the exact time of 2015-03-
12:18h30m00s000Z or after that. The reply is as follows:
3EVENTS;1;EVENT_DEFINITION;2015-03-12:18h30m00s001Z;Meeting with the user.;CSE5232
The answer from the server is the word EVENTS followed the the number of events in the set
that occur on the specified date or after it. All the events in that matches the input parameters
are appended to the replied string.
The same behavior can be achieved by running: nc -u localhost 2356

Few of the commnds that can be used in are like, 
client.sh localhost 2356 “EVENT_DEFINITION;2015-03-12:18h30m00s001Z;Meeting
with the user.;CSE5232”
If the input is successful, the server will answer with the number 0, if not it will return a number
1;
The client can also be tested using the command
client.sh localhost 2356 “GET_NEXT_EVENTS;CSE5232;2015-03-12:18h30m00s000Z”
In this case the server will reply back a list of EVENTS that occurs after the specified date;
Both client and server sends the data encoded in ASN1, and upon receiving, print the input
that was sent over the network;
By default, the client will connect to the server over TCP. But it is also capable of connecting
via UDP, in order to do that, you need to edit the file client.sh and change the option -p to
-u ;
For this milestone, the new functions can be tested as follows: The functionalities for
milestones 2 and 3 are kept and can be tested using these commands:
client.sh localhost 2356 “CSE5232”
Right after receiving that command, the server replies with an EventOK ASN1 object which
contains code = 0, if the server accepted and started the UDP messages, or code 6= 0 if it’s not
going to reply.
The server only sends messages for one group and for one client at the time. If it’s busy, it
will reply and EventOK with code 6= 0. It code has a meaning, and the client prints what as the
reason the server did not replied.
If the server is idle, it will reply with UDP messages all the events in group CSE5332 that are
happening within 24 hours. If the server is busy, or if there are no events with such group, the
server answers;
If the user wants to stop the messages, it should start a new client and send the proper
LEAVE command with the same group it was registered before. Then the server quits sending
messages and acknowledges with a EventOK which contains the reason why it stopped, then
4the client prints it.
