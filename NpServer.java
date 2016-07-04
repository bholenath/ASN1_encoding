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

import java.net.*;
import java.io.*;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.*;
import Util.GetOpt;
import ASN1.*;
import ENCDEC.*;


/**
 *
 * @author harshit 
 * 
 */

public final class NpServer implements Runnable {

    ServerSocket s1;
    DatagramSocket p1;
    int serverport;
    String database;
    Thread t = null;
    int flag;
    BufferedOutputStream bos = null;
    static Connection con = null;
    InetAddress inet = null;
    int udp_port;
    byte[] send_data = new byte[1500];
//    boolean control = Boolean.FALSE;
    Register_Messages rgmsg = new Register_Messages();

    //variables to control Leave command coming from UDP
    String currentGroup = "";
    String currentClientIP = "";
    

    public static void main(String args[]) throws Exception {

        GetOpt go = new GetOpt(args, "t:u:d:");
        go.optErr = true;
        int ch;
        int port = 0, udp_port = 0;
        String db = "";

        while ((ch = go.getopt()) != GetOpt.optEOF) {
            if ((char) ch == 't') {
                port = go.processArg(go.optArgGet(), port);
            } else if ((char) ch == 'd') {
                db = go.processArg(go.optArgGet(), db);
            } else if ((char) ch == 'u') {
                udp_port = go.processArg(go.optArgGet(), udp_port);
            } else {
                System.exit(1);
            }
        }

        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        TimeZone.setDefault(tz);

        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + db);
            DatabaseMetaData dbm = con.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "np_project", null);
            if (!tables.next()) {
                String query_insert = "create table np_project (id integer primary key autoincrement not null, starting_date char(20) null, starting_time char(20) null, description text null, cs_group char(20) null, status char(20) null)";
                int change;
                try (Statement stmt_create = con.createStatement()) {
                    change = stmt_create.executeUpdate(query_insert);
                }
                if (change > 0) {
                    System.out.println("Success");
                }

            }
        } catch (SQLException | ClassNotFoundException sqle) {
            System.out.println("Query not inserted");
            System.err.println(sqle);
        }

        System.out.println("Starting UDP/TCP Server...");
        NpServer sv = new NpServer(port);
        new Thread(sv).start();
        NpServer sv1 = new NpServer("polimorph", udp_port);

    }

    /**
     * Constructor for the TCP Server
     *
     * @param port
     */
    public NpServer(int port) {
        this.serverport = port;
    }

    /**
     * Constructor for the UDP Server The poli String is only to differentiate
     * between the TCP constructor by using polimorphism
     *
     * @param poli any string to force UDP constructor
     * @param udp port number
     */
    public NpServer(String poli, int udp) {
        try {
            p1 = new DatagramSocket(udp);
            udp_connect();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     * This is a contructor to access the write function from the thread
     * Register_messages
     *
     */
    public NpServer() {
    }

    /**
     * This is the runnable function for the TCP Server
     *
     */
    @Override
    public synchronized void run() {
        try {
            s1 = new ServerSocket(this.serverport);
            tcp_connect();
        } catch (Exception e) {
            System.out.println("Port not found");
            System.err.println(e);
            System.exit(0);
        }
    }

    /**
     * This is the function called by the TCP server thread.
     *
     * @throws java.lang.Exception
     */
    public void tcp_connect() throws Exception {
        Socket c;
        InputStream is = null;
        Scanner sc = null;
        try {
            c = s1.accept();
            if (c.isConnected() == true) {
                flag = 10;
                SocketAddress sa = c.getRemoteSocketAddress();
                System.out.println("TCP  : " + sa.toString());
                /*
                 Here we define a BufferentInputStream object with to store the 
                 data coming from the client.
                
                 We also assign the BufferedOutputStrem bos, which is a global 
                 variable, with the values from the newly connected socket.
                 */

                BufferedInputStream inFromClient = new BufferedInputStream(c.getInputStream());

                /*
                 In order to know how many bytes to read, we define a small byte
                 array with 10 positions. We read the socket buffer up  to 10.
                
                 */
                byte[] firstPckt = new byte[10];

                for (int j = 0; j < 10; j++) {
                    firstPckt[j] = (byte) inFromClient.read();
                }
                /*
                 After reading the small byte array, we create an object Decoder
                 first, and we calls the function getContentLenth. In doing so, 
                 we are able to know how many bytes the client is sending, so we
                 can limit the for-loop size up to data length that is being sent.
                
                 We can also save memory, and define the byte array of the remaini-
                 ing data to read, in the exact size it is;
                
                 In order to read the rest of the data, we run the for-loop, 
                 up to the remaining part of the data.
                 */

                Decoder first = new Decoder(firstPckt);
                int size = first.contentLength();

                //we create the buffer with size plus 2 because contentLenght
                //does not count the bytes for sequences.
                byte[] bf = new byte[size + 2];
                //here we copy the data that was ready to the array of the 
                //remaining data.
                System.arraycopy(firstPckt, 0, bf, 0, 10);

                //finally we read the entire data
                for (int i = 10; i < size + 2; i++) {
                    bf[i] = (byte) inFromClient.read();

                }

                /*
                 Then we decode the values that we read from the socket and store 
                 then in a String array
                 */
                Decoder dec = new Decoder(bf);

                /*We show the string we got from the client*/
                //System.out.println(values);
                /*             
                 Here we call the function display, passing the String array as
                 parameter;
                 */
                bos = new BufferedOutputStream(c.getOutputStream());
                display(dec);

            }
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            try {
                /* Here we close the connection with 
                 the client and try calls the same function 
                 tcp_connect() again, so we can receive data 
                 from anothe client
                 */
                System.out.println();
                Thread.sleep(1000);
                expiryCheck();
                bos.close();
                
                //call TCP again, for infinite recursion
                tcp_connect();
            } catch (SQLException | IOException | InterruptedException e) {
                System.err.println(e);
            }
        }
    }

    /**
     * This is the UDP server function. This function is called when the UDP
     * server is constructed. This function runs in the main thread.
     *
     * @throws ASN1.ASN1DecoderFail
     */
    public void udp_connect() throws Exception {

        try {
            
            byte[] rec_data = new byte[1500];
            while (true) {
                DatagramPacket dp = new DatagramPacket(rec_data, rec_data.length);
                try {
                    p1.receive(dp);
                } catch (Exception e) {
                    System.err.println(e);
                }
                
                System.out.println("\nUDP : "
                        + dp.getSocketAddress().toString());
                flag = 11;
                inet = dp.getAddress();
                udp_port = dp.getPort();
                
                
                Decoder dec = new Decoder(dp.getData());
                
                int tagValue = dec.tagVal();
                EventOK evOK = new EventOK();

                if (tagValue == 3) {
                    //Since tags are ambigous, Answer and Register are defined
                    //in the project page we have to show the prints here
                    Register register = new Register();                   
                    System.out.println(register.decode(dec));
                    
                    //Output formatting
                    System.out.println();
//                    try {
//                        rgmsg = new Register_Messages();
//                    } catch (Exception e) {
//                        System.err.println(e);
//                    }
                    if (!rgmsg.isBusy()) {
                        currentGroup = register.decode(dec).toString();
                        currentClientIP = dp.getAddress().toString();

                        evOK.setCode(0);

                        DatagramPacket sp = new DatagramPacket(
                                evOK.encode(),
                                evOK.encode().length, inet, udp_port
                        );

                        p1.send(sp);
//                        control = true;      
                        
                        //set values in new thread
                        rgmsg.setUdpParameters(con, currentGroup, inet, udp_port);
                        
                        new Thread(rgmsg).start();
                        
                        //here we wait 1 sec to proper set control value
                   
                        Thread.sleep(1000);
//                        control = rgmsg.isBusy();
                        
                    } else {
                        evOK.setCode(1); //code 1 for double registration
                        DatagramPacket sp = new DatagramPacket(
                                evOK.encode(),
                                evOK.encode().length, inet, udp_port);

                        p1.send(sp);
                    }
                    
                    //call UDP function again for recursion
                    udp_connect();

                } else if (tagValue == 4) { // here we are receiving a LEAVE request.
                    
                    //Use Encode to print the result
                    Encode enc = new Encode(dec);
                    
                    //Output formatting
                    System.out.println();
                    
                    if (!rgmsg.isBusy()) { //trying to leave, but nothing is running;
                        evOK.setCode(3); // Leave without enter;
                        DatagramPacket sp = new DatagramPacket(
                                evOK.encode(),
                                evOK.encode().length, inet, udp_port);
                        p1.send(sp);
                    
                    } else {
                        
                        Leave leave = new Leave();
                        String group = leave.decode(dec).toString();
                        if (currentGroup.equals(group) && (dp.getAddress().toString().equals(currentClientIP))) {
                            rgmsg.setBusy(false);
//                            control = false;
                            evOK.setCode(0);
                            DatagramPacket sp = new DatagramPacket(
                                    evOK.encode(),
                                    evOK.encode().length, inet, udp_port);
                            p1.send(sp);

                        } else {
                             evOK.setCode(4); //leaving incorrect group or not authorized (different IP)
                            DatagramPacket sp = new DatagramPacket(
                                    evOK.encode(),
                                    evOK.encode().length, inet, udp_port);
                            p1.send(sp);
                        }
                    }
                    udp_connect();
                }

                display(dec);

                p1.disconnect();
                System.out.println();
                Thread.sleep(1000);
                expiryCheck();
            }
        } catch (IOException e) {
            System.err.println("IO Error on UDP Socket.");
            System.err.println(e);
        } finally {
            try {
                p1.close();
                System.out.println("Bye-Bye! Connection closed.");
                Thread.currentThread();
                Thread.sleep(1000);
            } catch (Exception e) {
                System.err.print(e);
            }
        }
    }

    /**
     * This function checks for expired events and if found, they are set as
     * expired in the DB
     *
     */
    void expiryCheck() {
        try {
            String check_query = "select * from np_project";
            Statement check_stmt = con.createStatement();
            ResultSet rs1 = check_stmt.executeQuery(check_query);
            DateFormat df0 = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss.SSS");
            java.util.Date now = new java.util.Date();
            java.util.Date current_date = df0.parse(df0.format(now));

            while (rs1.next()) {
                java.util.Date event_date1 = df0.parse(rs1.getString("starting_date") + "." + rs1.getString("starting_time"));

                if (event_date1.before(current_date)) {
                    int c;
                    int get_id = rs1.getInt("id");
                    String update_query = "update np_project set status='Expired' where id=" + get_id;
                    Statement update_stmt = con.createStatement();
                    c = update_stmt.executeUpdate(update_query);
                    if (c == 0) {
                        System.out.println("Update query error");
                        System.exit(0);
                    } 
                }
            }

        } catch (SQLException | ParseException e) {
            System.err.println(e);
        }
    }

    /**
     * This function is called by both TCP and UDP servers. After receiving the
     * input from the client, the TCP/UDP threads call this function which look
     * into the input in order to store it in the database or to retrieve events
     * and send to the client
     *
     */
    void display(Decoder dec) throws Exception {
        //here we call the methods to properly show the data
        int tagValue = dec.tagVal();
        Encode encode = new Encode(dec);
        String[] parts = encode.decodedOutput.split(";");

        PreparedStatement stmt;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat df1 = new SimpleDateFormat("HH:mm:ss.SSS");
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss.SSS");

        if (tagValue == 1) { //Server received a EVENT_DEFINITION request TAG = 1
            String query = "insert into np_project(starting_date,starting_time,description,cs_group,status) values (?,?,?,?,?)";
            stmt = con.prepareStatement(query);
            String[] part_time = parts[1].split(":");
            String date_format = part_time[0];
            String hour = part_time[1].split("h")[0];
            String min = part_time[1].split("h")[1].split("m")[0];
            String sec = part_time[1].split("h")[1].split("m")[1].split("s")[0];
            String msec = part_time[1].split("h")[1].split("m")[1].split("s")[1].split("Z")[0];
            String time_format = hour + ":" + min + ":" + sec + "." + msec;
            stmt.setString(1, date_format);
            stmt.setString(2, time_format);
            stmt.setString(3, parts[2]);
            stmt.setString(4, parts[3]);
            stmt.setString(5, "Valid");

            int i = stmt.executeUpdate();
            stmt.close();
            if (i > 0) {
                if (flag == 10) {

                    /**
                     * Here we replying over TCP
                     *
                     * We encode an ASN1 EncodeOK object set to 0 and send to
                     * the client.
                     *
                     */
                    int code = 0;
                    EventOK eventOK = new EventOK();
                    eventOK.setCode(code);

                    bos.write(eventOK.encode());
                    bos.flush();
                    stmt.close();
                } else if (flag == 11) { //flag 11 is for UDP
                    /**
                     * In this case we are answering over UDP.
                     *
                     * Since all the database went smoothly, we write back to
                     * the client an EventOK ASN object with code 0;
                     *
                     */
                    int code = 0;
                    EventOK eventOK = new EventOK();
                    eventOK.setCode(code);
                    send_data = eventOK.encode();
                    DatagramPacket sp = new DatagramPacket(send_data,
                            send_data.length, inet, udp_port);
                    p1.send(sp);
                    stmt.close();
                }
            } else {
                if (flag == 10) { //flag 10 is for TCP
                    /**
                     * Here the commit did not succeeded
                     *
                     * We send the code 1 to the client over TCP (flag 10)
                     */
                    int code = 1;
                    EventOK eventOK = new EventOK();
                    eventOK.setCode(code);
                    bos.write(eventOK.encode());
                    bos.flush();
                    stmt.close();
                } else if (flag == 11) {
                    int code = 1;
                    EventOK eventOK = new EventOK();
                    eventOK.setCode(code);
                    send_data = eventOK.encode();
                    DatagramPacket sp = new DatagramPacket(send_data,
                            send_data.length, inet, udp_port);
                    p1.send(sp);
                    stmt.close();
                    udp_connect();
                }
                System.out.println("Bye-Bye! Connection closed.");
                t.sleep(1000);
                System.exit(1);
            }
        } else if (tagValue == 2) { //here we are receiving a GET_NEXT_EVENTS request.
            Request req = new Request();
            req.decode(dec);

            String group = parts[1];
            String[] part_time = parts[2].split(":");

            String hour = part_time[1].split("h")[0];
            String min = part_time[1].split("h")[1].split("m")[0];
            String sec = part_time[1].split("h")[1].split("m")[1].split("s")[0];
            String msec = part_time[1].split("h")[1].split("m")[1].split("s")[1].split("Z")[0];
            String time_format1 = hour + ":" + min + ":" + sec + "." + msec;
            java.util.Date time_format = req.getCal().getTime();//df1.parse(time_format1);

            //Query to count how many events matchs the input from the client
            String nrows = "select count(*) as count from np_project "
                    + "where starting_date||starting_time >= \'" + part_time[0] + "" + time_format1 + "\' and "
                    + "cs_group = \'" + group + "\'";
            
            //Query to retrieve the whole data matching the client's parameters
            String query = "select * from np_project "
                    + "where starting_date||starting_time >= \'" + part_time[0] + "" + time_format1 + "\' and "
                    + "cs_group = \'" + group + "\'";

            stmt = con.prepareStatement(nrows);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int rowcount = rs.getInt(1);

            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();

            //we create an array of Encoder object to store each of the events
            Encoder enco[] = new Encoder[rowcount];

            //we create an Object Answer to encode the reply;
            Answer answer = new Answer();
            answer.setSize(rowcount);
            int indexEvents = 0;
            
            /**
             * In the ResultSet loop, each event is a line that comes from the
             * database. 
             * 
             * This loop adds each event into a Event ASN1 object, and put each
             * Encoder object into an array which is passed as parameter to the
             * Answer object.
             * 
             * When the loops ends, the object Answer is written to the 
             * Socket;
             * 
             */
            while (rs.next()) {
                Calendar cal = Calendar.getInstance();
                java.util.Date event_time = df2.parse(rs.getString("starting_date") + ":" + rs.getString("starting_time"));
                cal.setTime(event_time);

                Event event = new Event();

                if (event_time.after(time_format) && rs.getString("cs_group").equals(group)) {

                    event.setEventCal(cal);
                    event.setEventDesc(rs.getString("description"));
                    event.setEventGroup(rs.getString("cs_group"));

                } else if (event_time.equals(time_format) && rs.getString("cs_group").equals(group)) {

                    event.setEventCal(cal);
                    event.setEventDesc(rs.getString("description"));
                    event.setEventGroup(rs.getString("cs_group"));

                }

                enco[indexEvents] = event.getEncoder();

                indexEvents++;
            }

            //here we add the event array to the Answer object
            answer.setEvents(enco);
            if (flag == 10) {
                bos.write(answer.encode());
                bos.flush();
                stmt.close();
            } else if (flag == 11) {
                send_data = answer.encode();
                DatagramPacket sp = new DatagramPacket(send_data, send_data.length, inet, udp_port);
                p1.send(sp);
                stmt.close();
            }
        }
    }

}

