
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

import ASN1.*;
import ENCDEC.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *   @author cruzj2012
 * 
 * This class provides a methods to Encoding/Decoding strings into ASN1 Objects
 * 
 * 
 */
public final class Encode {

    String input;
    String[] parts;
    Encoder enc;
    Decoder dec;
    
    //Those objects are only to be use for the Decode Event and Decode Requests;
    String decodedOutput;

    public Encode(String input) throws ASN1DecoderFail, ParseException {
        this.input = input;
        encodeInput();
    }

    public Encode(Decoder dec) throws ASN1DecoderFail, Exception {  
        this.dec = dec;
        decodeInput();
    }

    void encodeInput() throws ASN1DecoderFail, ParseException {
        for (int i = 0; i < input.length(); i++) {
            parts = input.trim().split(";");
        }
        if (null != parts[0]) {
            switch (parts[0]) {
                case "EVENT_DEFINITION":
                    encEvent();
                    break;
                case "GET_NEXT_EVENTS":
                    requestEvents();
                    break;
                case "REGISTER":
                    register();
                    break;
                case "LEAVE":
                    leave();
                    break;                           
                default:
                    System.err.println("Could not parse your input!");
                    System.exit(0);
                    break;
                    
            }
        }
    }

    void encEvent() throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd:HH'h'mm'm'ss's'S");
        Calendar cal = Calendar.getInstance();
        cal.setTime(df.parse(parts[1].substring(0, parts[1].length() - 1)));
        Event ev = new Event();
        ev.setEventCal(cal);
        ev.setEventDesc(parts[2]);
        ev.setEventGroup(parts[3]);
        enc = ev.getEncoder();
    }

    void requestEvents() throws ParseException{
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd:HH'h'mm'm'ss's'S");
        Calendar cal = Calendar.getInstance();
        cal.setTime(df.parse(parts[2].substring(0, parts[2].length() - 1)));    
        Request requ = new Request();
        requ.setCal(cal);
        requ.setGroup(parts[1]);     
        enc = requ.getEncoder();
    }
    
    void register() {
        Register reg = new Register();
        reg.setGroup(parts[1]);
        enc = reg.getEncoder();
    }

    
    void leave(){
        Leave leave = new Leave();
        leave.setGroup(parts[1]);
        enc = leave.getEncoder();
    }

    void decodeInput() throws ASN1DecoderFail{
        int tagValue = dec.tagVal();
        switch(tagValue){
            case 0: // Decoded object is a EventOK ::= [0] SEQUENCE {code INTEGER}
                decodeEventOK();
                break;
            case 1: // Decoded object is a Event ::= [1] SEQUENCE {time GeneralizedTime, group UTF8String, description UTF8String}              
                decodeEvent();
                break;
            case 2: // Decoded object is a Request ::= [2] SEQUENCE {group UTF8String, after_time GeneralizedTime}
                decodeRequest();
                break;
            case 3: //Decoded objec is a Answer ::= [3] SEQUENCE OF Event
                decodeAnswer();
                break;
            case 4: // Decoded object is Leave ::= [4] Register
                decodeLeave();
                break;
            default: // Anything different than above, raises an error.
                System.err.println("No implicit tag defined in this Decoder object!");
                    
        }
    }
    
    /*
    If the object Decode in the contructor paramenter has the tag of EventOK.
    The code returned from the Server is printed to standart output.
    TAG = 0;
    */
    void decodeEventOK() throws ASN1DecoderFail {
        EventOK evOK = new EventOK();
        System.out.println(evOK.decode(dec));
    }
  /**  
  * This function provides a better approach to decoding an Event object.
  *  
  *  By inputing and Event object in the constructor of this class, 
  *  the object event can be accessed through this class, or a String array containing
  *  already formated outputs to be displayed.
  *  TAG = 1
  **/
    void decodeEvent() throws ASN1DecoderFail {
        Event event = new Event();
        event.decode(dec);
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd:HH'h'mm'm'ss's'S'Z'");
        String stringdateTime = sdf1.format(event.getEventCal().getTime());
        decodedOutput = "EVENT_DEFINITION;";
        decodedOutput +=  stringdateTime+";";
        decodedOutput += event.getEventDesc()+";";
        decodedOutput += event.getEventGroup()+";";
        System.out.println(decodedOutput);
       
    }
    
/**
 * This function provides decodes an Request object.
 * 
 * After calling passing the request parameter in the class constructor, call 
 * the req
 * TAG = 2;
**/
    
    void decodeRequest() throws ASN1DecoderFail{
        
        Request request = new Request();
        request.decode(dec);
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd:HH'h'mm'm'ss's'S'Z'");
        String stringdateTime = sdf1.format(request.getCal().getTime());
        decodedOutput = "GET_NEXT_EVENTS;";
        decodedOutput += request.getGroup()+";";
        decodedOutput += stringdateTime+";";
        System.out.println(decodedOutput);
    }
    
/**
 * This function provides decodes an Answer object.
 * 
 * The only code here is to instantiate an object Answer, since the
 * printing is carried in the class Answer on its method decode;
 * TAG = 3;
**/
    void decodeAnswer() throws ASN1DecoderFail{
        Answer an = new Answer();
        an.decode(dec);
    }
    
/**
 * This function decodes the input from the class constructor and
 * saves it to the outputString that can be accessed to print.
 * 
 * The decision of what to do with an object Register or Leave, should be taken 
 * in the client class/file or server class/file.
 * 
 * The purpose here is only to decode. TAG 4
 */
    
    void decodeRegister() throws ASN1DecoderFail{
        Register req = new Register();
        decodedOutput = "REGISTER;";
        decodedOutput = req.decode(dec).toString();
        System.out.println(decodedOutput);
    }
    
/**
 * This function decodes the input from the class constructor and
 * saves it to the outputString that can be accessed to print.
 * 
 * The purpose here is only to decode. TAG 5
*/
   
    void decodeLeave() throws ASN1DecoderFail{
        Leave leave = new Leave();
        decodedOutput = "LEAVE;";
        decodedOutput += leave.decode(dec).toString();
        System.out.println(decodedOutput);
    }
}
