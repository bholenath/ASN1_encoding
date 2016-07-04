
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

/**
 *
 * @author cruzj2012
 * This class is only to provide implicit tags for the ASN1 classes.
 * 
 * Tags as specified in the project description here are defined 
 * 
 * 
    Event ::= [1] SEQUENCE {time GeneralizedTime, group UTF8String, description UTF8String}
    EventOK ::= [0] SEQUENCE {code INTEGER}
    Request ::= [2] SEQUENCE {group UTF8String, after_time GeneralizedTime}
    Answer ::= [3] SEQUENCE OF Event
    Register ::= [3] SEQUENCE {group UTF8String}
    Leave ::= [4] Register
 * 
 */
public class TAG {
    public byte EVENT_OK = (byte) 0;
    public byte EVENT = (byte) 1;
    public byte REQUEST = (byte) 2;
    public byte ANSWER = (byte)3;
    public byte REGISTER = (byte) 3;
    public byte LEAVE = (byte) 4;
}
