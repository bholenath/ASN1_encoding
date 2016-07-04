
/* ------------------------------------------------------------------------- */
/*   Copyright (C) 2015 
 Authors:  Harshit Bhatt hbhatt2014@my.fit.edu
 Josemar Faustino da Cruz cruzj2012@my.fit.edu
 Florida Tech, Department of Computer Sciences
   
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation; either the current version of the License, or
 (at your option) any later version.
   
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
  
 You should have received a copy of the GNU Affero General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.              */
/* ------------------------------------------------------------------------- */
import ASN1.Decoder;
import ENCDEC.EventOK;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *
 * @author cruzj2012
 *
 * This class is responsible for starting a new thread to register for the UDP
 * messages that are being received. And write it to standard output;
 *
 */
public class ClientUDPMessages implements Runnable {

    String group;
    String server;
    int portNumber;

    public ClientUDPMessages(int portNumber, String server, String group) {
        this.portNumber = portNumber;
        this.server = server;
        this.group = group;
    }

    @Override
    public void run() {
        String[] verify = group.split(";");
        //Check for bad input with more than one semicolon (1 ";" means 2 words)
        if (verify.length > 2) {
            System.err.println("Input cannot be parsed! Check it and try again!");
            System.exit(0);
        }

        if (verify.length == 1) {
            try {
                sendRequest("REGISTER;" + group);
            } catch (Exception ex) {
                System.err.println(ex);
            }
        } else {
            try {
                sendRequest(group);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    synchronized void sendRequest(String request) throws Exception {
        Encode encode = new Encode(request);

        DatagramSocket clientSocket;

        clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(server);
        byte[] sendData;
        byte[] receiveData = new byte[clientSocket.getReceiveBufferSize()];

        sendData = encode.enc.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(sendData,
                sendData.length, IPAddress, portNumber);

        clientSocket.send(sendPacket);

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        int length = receivePacket.getLength();
        byte[] data = new byte[length];
        System.arraycopy(receiveData, 0, data, 0, data.length);

        EventOK evOK = new EventOK();

        evOK.decode(new Decoder(data));

        if (evOK.getCode() != 0) {
            /**
             * Code different than 0 means something that server refused to
             * process the request!
             */
            if (evOK.getCode() == 1) {
                System.err.println(evOK.getCode()+"\nThe server is already sending events!");
            } else if (evOK.getCode() == 3) {
                System.err.println(evOK.getCode()+"\nTrying to leave without registering!");
            } else if (evOK.getCode() == 4) {
                System.err.println(evOK.getCode()+"\nNot registered for that group, or different IP Address!");
            }
        } else if (request.contains("REGISTER")) {

            receiveEvents(clientSocket);

        } else {
            System.out.println(evOK.getCode()+"\nEvent messages stopped!");
        }

    }

    /**
     *
     * @param clientSocket The socket to receive the events from server
     * @throws Exception DatagramSocket Exception
     *
     * This function is recursive and keeps reading the SocketBuffer from any
     * incoming messages
     */
    synchronized void receiveEvents(DatagramSocket clientSocket) throws Exception {

        clientSocket.setSoTimeout(4000); // if there'no message from server in 4 seconds, we assume it has stopped
        byte[] receiveData = new byte[clientSocket.getReceiveBufferSize()];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        int length = receivePacket.getLength();
        byte[] data = new byte[length];
        System.arraycopy(receiveData, 0, data, 0, data.length);

        Decoder dec = new Decoder(data);

        if (dec.tagVal() == 0) {
            EventOK evOk = new EventOK();
            evOk.decode(dec);
            if (evOk.getCode() == 2) {
                System.err.println(evOk.getCode()+"\nNo events for group " + group);
                System.exit(0);
            } else if (evOk.getCode() == 3) {
                System.err.println(evOk.getCode()+"\nEvents advertising timeout");
                System.exit(0);

            } else if (evOk.getCode() == 4) {
                System.out.println(evOk.getCode()+"\nRequisition to end event messages was sent");
                System.exit(0);

            }

        }

        Encode encode = new Encode(dec);
        System.out.println();
        receiveEvents(clientSocket);
    }

}
