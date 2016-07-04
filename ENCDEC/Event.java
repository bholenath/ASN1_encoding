
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

package ENCDEC;
import java.util.Calendar;
import ASN1.*;
import java.util.TimeZone;




/**
 *
 * @author cruzj2012
 * 
 * This class provides methods to encode and decode the following ASN1 notation:
 * Event ::= [1] SEQUENCE {time GeneralizedTime, group UTF8String, description UTF8String}
 * 
 * Note that for decoding an object event, after calling the decode methods
 * you have to access the values using the get Functions written for each
 * of the variables in the Event object;
 */
 public class Event  extends ASNObjArrayable{
     Calendar eventCal;
     String eventGroup;
     String eventDesc;
     TAG tags = new TAG();

    public Calendar getEventCal() {
        return eventCal;
    }

    public void setEventCal(Calendar eventCal) {
        
        this.eventCal = eventCal;
        this.eventCal.setTimeZone(TimeZone.getTimeZone("EDT"));
    }

    public String getEventGroup() {
        return eventGroup;
    }

    public void setEventGroup(String eventGroup) {
        this.eventGroup = eventGroup;
    }

    public String getEventDesc() {
        return eventDesc;
    }

    public void setEventDesc(String eventDesc) {
        this.eventDesc = eventDesc;
    }
     
     @Override
    public Encoder getEncoder() {
        
        Encoder enc = new Encoder().initSequence(); //creates the sequence
        enc.addToSequence(new Encoder(eventCal).setASN1Type(Encoder.TAG_GeneralizedTime));
        enc.addToSequence(new Encoder(eventGroup)).setASN1Type(Encoder.TAG_UTF8String);
        enc.addToSequence(new Encoder(eventDesc)).setASN1Type(Encoder.TAG_UTF8String);
        
        return enc.setASN1Type(Encoder.CLASS_APPLICATION, 1, tags.EVENT);
    }


     @Override
    public Event decode(Decoder decd) throws ASN1DecoderFail{
        Decoder dec = new Decoder();
         try {
              dec = decd.getContent();
         } catch (Exception e) {
             e.printStackTrace();
         }
         
        eventCal = dec.getFirstObject(true).getGeneralizedTimeCalender(Encoder.TAG_GeneralizedTime);
        eventGroup = dec.getFirstObject(true).getString(Encoder.TAG_UTF8String);
        eventDesc = dec.getFirstObject(true).getString(Encoder.TAG_UTF8String);
        return null;
    }

     @Override
    public ASNObjArrayable instance() throws CloneNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
   
}
