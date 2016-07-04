
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
import ENCDEC.Event;
import ENCDEC.EventOK;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author harshit
 *
 * This class implements the thread that runs sending the events after a request
 * is sent from the client.
 *
 * It's called from NpServer after a TAG = 4 is received.
 *
 */
public class Register_Messages implements Runnable {

    Connection con;
    String group;
    private Boolean busy = false;
    //public AtomicBoolean busy = new AtomicBoolean(true);

    public synchronized Boolean isBusy() {
        return busy;
    }

    public synchronized void setBusy(Boolean busy) {
        this.busy = busy;
    }


    InetAddress inet;
    int udpPort;

    Thread t;

    public void setUdpParameters(Connection con, String group, InetAddress inet, int udpPort) {
        this.con = con;
        this.group = group;
        this.inet = inet;
        this.udpPort = udpPort;
    }

    @Override
    public void run() {
        try {
            this.busy = true;
            this.t = Thread.currentThread();
            DatagramSocket clientSocket = null;

            //create an instance of calendar
            Calendar requestCal = Calendar.getInstance();

            //here we set the calendar for 24 hours after the request data
            //All the events that are happening withing 24 hours are returned to
            //the client.
            requestCal.add(Calendar.HOUR, 24);

            DateFormat format = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss.SSS");

            System.out.println(format.format(requestCal.getTime()));
            try {
                clientSocket = new DatagramSocket();
            } catch (SocketException ex) {
                Logger.getLogger(Register_Messages.class.getName()).log(Level.SEVERE, null, ex);
            }
            String query = "select * from np_project where "
                    + "cs_group = \'" + group + "\' "
                    + "and starting_date||starting_time <= \'" + format.format(requestCal.getTime()) + "\' "
                    + "and status = 'Valid'";

            String nrows = "select count(*) as count from np_project where "
                    + "cs_group = \'" + group + "\' "
                    + "and starting_date||starting_time <= \'" + format.format(requestCal.getTime()) + "\' "
                    + "and status = 'Valid'";

            DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss.SSS");
            //we create an Object Answer to encode the reply;

            PreparedStatement stmt;
            ResultSet rs;

            stmt = con.prepareStatement(nrows);
            rs = stmt.executeQuery();
            rs.next();

            int rowCount = rs.getInt(1);

            if (rowCount == 0) {
                EventOK evOk = new EventOK();
                //code 2 is for no events with that group
                evOk.setCode(2);

                try {

                    DatagramPacket sp = new DatagramPacket(evOk.encode(),
                            evOk.encode().length,
                            inet,
                            udpPort);
                    clientSocket.send(sp);

                    busy = false;
                } catch (IOException ex) {
                    System.err.println(ex);
                }

            }

            long re_ini_time = (System.currentTimeMillis() / 1000);
            //Query the database to get all the events of the defined group

            //Loops for 1 hour
            while (busy && ((System.currentTimeMillis() / 1000) - re_ini_time) < 3600) {

                try {

                    stmt = con.prepareStatement(query);
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        Calendar cal = Calendar.getInstance();
                        java.util.Date event_time = df2.parse(rs.getString("starting_date") + ":" + rs.getString("starting_time"));
                        cal.setTime(event_time);

                        Event event = new Event();

                        event.setEventCal(cal);
                        event.setEventDesc(rs.getString("description"));
                        event.setEventGroup(rs.getString("cs_group"));

                        try {

                            DatagramPacket sp = new DatagramPacket(event.encode(),
                                    event.encode().length,
                                    inet,
                                    udpPort);
                            clientSocket.send(sp);
                        } catch (IOException ex) {
                            System.err.println(ex);
                        }
                        this.t.sleep(1000);
                    }

                    //here we add the event array to the Answer object
                    //}
                    stmt.close();

                } catch (SQLException | ParseException | InterruptedException ex) {
                    Logger.getLogger(Register_Messages.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (((System.currentTimeMillis() / 1000) - re_ini_time) > 3600) {

                EventOK evOk = new EventOK();

                //code 3 is for 1 hour timeout
                evOk.setCode(3);

                try {

                    DatagramPacket sp = new DatagramPacket(evOk.encode(),
                            evOk.encode().length,
                            inet,
                            udpPort);
                    clientSocket.send(sp);

                    busy = false;
                } catch (IOException ex) {
                    System.err.println(ex);
                }

            } else if ((busy == false) && rowCount != 0) {

                EventOK evOk = new EventOK();

                //code 4 is for leave command
                evOk.setCode(4);

                try {

                    DatagramPacket sp = new DatagramPacket(evOk.encode(),
                            evOk.encode().length,
                            inet,
                            udpPort);
                    clientSocket.send(sp);

                    busy = false;
                } catch (IOException ex) {
                    System.err.println(ex);
                }

            }

        } catch (SQLException ex) {
            Logger.getLogger(Register_Messages.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
