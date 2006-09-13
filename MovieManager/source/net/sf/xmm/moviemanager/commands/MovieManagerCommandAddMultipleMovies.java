package net.sf.xmm.moviemanager.commands;

import net.sf.xmm.moviemanager.DialogMovieInfo;


/**
 * @(#)MovieManagerCommandAddMultipleMovies.java 1.0 16.10.05 (dd.mm.yy)
 *
 * Copyright (2003) Mediterranean
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
 * Contact: mediterranean@users.sourceforge.net
 **/



abstract public class MovieManagerCommandAddMultipleMovies {
    
    DialogMovieInfo dialogMovieInfo;
    protected boolean cancelAll = false;
    protected boolean cancel = false;
    protected boolean dropImdbInfo = false;
    
    public void setCancelAll(boolean value) {
	cancelAll = value;
    }
    
    public void setCancel(boolean value) {
	cancel = value;
    }
    
    public void setDropImdbInfo(boolean value) {
	dropImdbInfo = value;
    }
}




    
  
