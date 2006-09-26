/**
 * @(#)MyAuth.java 1.0 14.11.05 (dd.mm.yy)
 *
 * Copyright (2003) Bro3
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Boston, MA 02111.
 * 
 * Contact: bro3@users.sourceforge.net
 **/

package net.sf.xmm.moviemanager.http;

/* Handles user authorization if required by proxy server */
public class MyAuth extends java.net.Authenticator{
    
    String user;
    String password;
    int counter = 0;
    int counterTarget = 1;
    
    public MyAuth (String user, String password) {
	this.user = user;
	this.password = password;
    }
    
    protected java.net.PasswordAuthentication getPasswordAuthentication() {
	
	/*If username of password is invalid the program would sometimes hang
	  This test takes care of that.
	*/
	if (counter == counterTarget) {
	    IMDB.setException("Server redirected too many  times");
	    counterTarget++;
	    return null;
	}
	counter++;
	return new java.net.PasswordAuthentication(user, password.toCharArray());
    }
}
