
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
import ASN1.*;
import java.util.Calendar;
import java.util.TimeZone;
/**
 *
 * @author cruzj2012
 * 
 * This class provides methods to encode and decode the following ASN1 notation:
 * Request ::= [2] SEQUENCE {group UTF8String, after_time GeneralizedTime}
 * 
 * To use the values of a decoded Request, remember to use the getter function.
 * 
 * If you try to encode an object without proper setting its values using  the 
 * setters function, it will raise an error;
 */

public class Request extends ASNObj{
    String group;
    Calendar cal;
    TAG tags = new TAG();

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Calendar getCal() {
        return cal;
    }

    public void setCal(Calendar cal) {
        this.cal = cal;
        this.cal.setTimeZone(TimeZone.getTimeZone("EDT"));
    }
    

    @Override
    public Encoder getEncoder() {
        Encoder enc = new Encoder().initSequence();
        enc.addToSequence(new Encoder(group)).setASN1Type(Encoder.TAG_UTF8String);
        enc.addToSequence(new Encoder(cal)).setASN1Type(Encoder.TAG_GeneralizedTime);
        return enc.setASN1Type(Encoder.CLASS_APPLICATION, 1, tags.REQUEST);
    }

    @Override
    public Object decode(Decoder dec) throws ASN1DecoderFail {
        Decoder content = new Decoder();
        try {
             content = dec.getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        setGroup(content.getFirstObject(true).getString()
        );
        
        setCal(content.getFirstObject(true)
                .getGeneralizedTimeCalender(Encoder.TAG_GeneralizedTime));
        
        return null;
    }
    
}
