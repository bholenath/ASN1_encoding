
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

import java.text.SimpleDateFormat;
import ASN1.*;

/**
 *
 * @author cruzj2012
 * 
 * This class provides methods to encode and decode the following ASN1 notation:
 * Answer ::= [3] SEQUENCE OF Event
 * 
 * Note that, when calling Answer.decode, the method will decode and print
 * whatever comes from the socket on the standard output.
 */
public class Answer extends ASNObj {

    Encoder events[];
    int size;
    TAG tags = new TAG();
    
    public void setEvents(Encoder[] events) {
        this.events = events;
    }

    public void setSize(int size) {
        this.size = size;
    }


    public Encoder[] getEvents() {
        return events;
    }


    @Override
    public Encoder getEncoder() {
        Encoder enc = new Encoder().initSequence();
        enc.addToSequence(new Encoder(size)).setASN1Type(Encoder.TAG_INTEGER);
        
        for (int i = 0; i < size; i++){
            enc.addToSequence(events[i]).setASN1Type(Encoder.TAG_SEQUENCE);
        }
        
        return enc.setASN1Type(Encoder.CLASS_APPLICATION, 1, tags.ANSWER);
    }


    @Override
    public Answer decode(Decoder dec) throws ASN1DecoderFail {
        Decoder content = dec.getContent();   
        size = content.getFirstObject(true).getInteger().intValue();
        System.out.print("EVENTS;"+size+";");
        for (int i = 0; i < size; i++) {
            Event event = new Event();
            Decoder _content = content.getFirstObject(true);
            event.decode(_content);
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd:HH'h'mm'm'ss's'S'Z'");
            System.out.print("EVENT_DEFINITION;"+sdf1.format(event.getEventCal().getTime()) + ";");
            System.out.print(event.getEventDesc()+";");
            System.out.print(event.getEventGroup()+";");
            
        }
        return null;
    }

}
