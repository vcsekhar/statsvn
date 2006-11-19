/*
 StatCvs - CVS statistics generation 
 Copyright (C) 2002  Lukasz Pekacki <lukasz@pekacki.de>
 http://statcvs.sf.net/
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 $RCSfile: LogSyntaxException.java,v $ 
 Created on $Date: 2003/03/18 10:33:57 $ 
 */

package net.sf.statsvn.input;

/**
 * Indicates there was a syntax error while parsing the log.
 * 
 * @author Anja Jentzsch
 * @author Richard Cyganiak
 * @version $Id$
 */
public class LogSyntaxException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 9092148307785767455L;

    /**
     * @see java.lang.Object#Object()
     */
    public LogSyntaxException() {
        super();
    }

    /**
     * @see java.lang.Throwable#Throwable(String)
     */
    public LogSyntaxException(final String message) {
        super(message);
    }
}
