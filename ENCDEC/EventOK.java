
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

/**
 *
 * This class provides methods to encode and decode the following ASN1 notation:
 * EventOK ::= [0] SEQUENCE {code INTEGER}
 * 
 */
public class EventOK extends ASNObj{
    int code;
    TAG tags = new TAG();

    public int getCode() {
         return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public Encoder getEncoder() {
        Encoder enc = new Encoder().initSequence();
        enc.addToSequence(new Encoder(code)).setASN1Type(Encoder.TAG_INTEGER);
        return enc.setASN1Type(Encoder.CLASS_APPLICATION, 1, tags.EVENT_OK);
        
    }

    @Override
    public Object decode(Decoder dec) throws ASN1DecoderFail {
        dec = dec.getContent();
        
        setCode(dec.getFirstObject(true)
                .getInteger(Encoder.TAG_INTEGER)
                .intValue()
        );
        return code;
    }

}
