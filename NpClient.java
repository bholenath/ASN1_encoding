
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import ASN1.*;
import Util.GetOpt;

/**
 *
 * @author cruzj2012
 * 
 * 
 */
public class NpClient {

    public void tcpClient(String server, int port) {

    }

    public static void main(String args[]) throws Exception {
        GetOpt go = new GetOpt(args, "s:t:u:i:");

        go.optErr = true;
        int ch = -1;
        int tcp_port = 0;
        int udp_port = 0;
        String server = "";
        String input = "";

        while ((ch = go.getopt()) != GetOpt.optEOF) {
            if ((char) ch == 's') {
                server = go.processArg(go.optArgGet(), server);
            } else if ((char) ch == 't') {
                tcp_port = go.processArg(go.optArgGet(), tcp_port);
            } else if ((char) ch == 'u') {
                udp_port = go.processArg(go.optArgGet(), udp_port);
            } else if ((char) ch == 'i') {
                input = go.processArg(go.optArgGet(), input);
            } else {
                System.exit(1);
            }
        }

        /**
         * Here we have to check the input to decide what to do:
         *
         * If the input contains the strings GET_NEXT_EVENTS or
         * EVENT_DEFINITION, the client should encode it, send to server and
         * wait for the response.
         *
         * The other situation is that the client is registering to receive UDP
         * messages relevant to a group, in that case, a Thread is starting
         * listening to UDP, and printing the messages that comes from the
         * server.
         *
         * Also, it might be the case that the client already registered in the
         * past, and quit but the server stills keeps sending messages to that
         * address. In that case my assumption is that the client wants to stop
         * receiving such messages.
         *
         * In order to cancel the messages, we check the buffer to see if the
         * server is receiving messages of that group if yes, we assume the
         * client wants to stop. Then we send a message to the server to cancel
         * sending messages for that group. and the client halts.
         *
         * If the group that is being received is different from the one the
         * client is inputing , the systems message the user that the socket
         * it's already receiving messages of the other group, and cannot
         * receive messages of two groups at the same time and resume showing
         * the messages of the previously registered group.
         */
        if (tcp_port != 0 && udp_port != 0) {

            System.err.println("Define TCP or UDP, not both!");
            System.exit(1);

        } else if (tcp_port == 0 && udp_port == 0) {

            System.err.println("Undefined ports!");
            System.exit(1);

        } else if (tcp_port != 0 && udp_port == 0) {

            /**
             * Here we check the nature of the input. if it is a Request or
             * Event_Definition we just encode and send it to the server
             *
             * if it's only a group. It means the client is registering or
             * leaving the messages from the input group. Then it should be
             * treated in a separated thread.
             *
             */
            if (!(input.contains("GET_NEXT_EVENTS")) && !(input.contains("EVENT_DEFINITION"))) {
                System.out.println("UDP : port "+tcp_port);
                new Thread(new ClientUDPMessages(tcp_port, server, input)).start();

            } else {
                System.out.println("TCP : port " + tcp_port);
                Encode result = new Encode(input);

                try (
                        Socket client = new Socket(server, tcp_port);
                        /*
                         Here the buffers for sending and receiving data are defined
                         */
                        BufferedOutputStream outToServer
                        = new BufferedOutputStream(client.getOutputStream());
                        BufferedInputStream inFromServer
                        = new BufferedInputStream(client.getInputStream());) {

                    outToServer.write(result.enc.getBytes());
                    outToServer.flush();

                    /*
                     Now we have to read the buffer with the resuls that comes
                     from the server.
                     Since we don't know how much data the server is gonna send,
                     we have to create a 10 positions byte array. To read the first
                     part of the ASN1 structure.
                     */
                    byte[] first = new byte[10];
                    for (int i = 0; i < 10; i++) {
                        first[i] = (byte) inFromServer.read();
                    }
                    /*
                     Now we create a Decode object and get the content lenght of the 
                     data being sent by the server, then we loop up the lenth of the data
                     being sent;
                     */
                    Decoder firstDec = new Decoder(first);
                    int size = firstDec.contentLength();

                    byte[] bf = new byte[size + size];

                    if (bf.length >= 10) {
                        System.arraycopy(first, 0, bf, 0, 10);

                        for (int i = 10; i < bf.length; i++) {
                            bf[i] = (byte) inFromServer.read();
                        }
                    } else {
                        System.arraycopy(first, 0, bf, 0, bf.length);
                    }

                    //Creates and Encode (not Encoder) object to print the result;
                    Decoder deco = new Decoder(bf);
                    Encode decode = new Encode(deco);
                    System.out.println();

                } catch (UnknownHostException e) {
                    System.err.println("Don't know about host " + server);
                    System.exit(1);
                } catch (IOException e) {
                    System.err.println("Couldn't get I/O for the connection to "
                            + server);
                }

            } //finish the else part for the GET_NEXT_EVENTS or EVENT_DEFINITION

        } else if (!(input.contains("GET_NEXT_EVENTS")) && !(input.contains("EVENT_DEFINITION"))) {
            System.out.println("UDP : "+udp_port);
            new Thread(new ClientUDPMessages(udp_port, server, input)).start();

        } else { //this else it's for UDP
            Encode encode = new Encode(input);
            System.out.println("UDP port: " + udp_port);

            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(server);
            byte[] sendData;
            byte[] receiveData = new byte[clientSocket.getReceiveBufferSize()];

            sendData = encode.enc.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData,
                    sendData.length, IPAddress, udp_port);

            clientSocket.send(sendPacket);

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            int length = receivePacket.getLength();
            byte[] data = new byte[length];
            System.arraycopy(receiveData, 0, data, 0, data.length);
            Decoder dec = new Decoder(data);
            Encode enc = new Encode(dec);
            System.out.println();
        }

    }
}
