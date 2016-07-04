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
