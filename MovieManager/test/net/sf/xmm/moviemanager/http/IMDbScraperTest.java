/**
 * @(#)IMDBTest.java 1.0 23.03.05 (dd.mm.yy)
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

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.xpath.XPathExpressionException;

import net.sf.xmm.moviemanager.http.HttpUtil.HTTPResult;
import net.sf.xmm.moviemanager.imdblib.IMDb;
import net.sf.xmm.moviemanager.imdblib.IMDbScraper;
import net.sf.xmm.moviemanager.imdblib.XPathParser;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbEntry;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbListHit;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbSearchHit;
import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/*
 * Used for simple testing of methods in IMDbScraper
 */
public class IMDbScraperTest  {

	boolean setup = false;
	
	@Before
	public void setUp() throws Exception
	{
		if (setup)
			return;
		
		setup =  true;
	
		BasicConfigurator.configure();
				
		try {
    		/* Disable HTTPClient logging output */
    		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog"); //$NON-NLS-1$ //$NON-NLS-2$
    				
    	} catch (java.security.AccessControlException s) {    
    		s.printStackTrace();
    	}
	}

	//@Test
	public void decodeClassInfoTest() {
		
		IMDbScraper imdb = null;
		try {
			imdb = new IMDbScraper();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//StringBuffer buff = new StringBuffer("<hr />      <h3>Additional Details</h3><div class=\"info\"><h5>Also Known As:</h5><div class=\"info-content\">       \"El Exterminator 2\" - USA <em>(Spanish title)</em><br>\"T2\" - USA <em>(promotional abbreviation)</em><br>\"T2 - Terminator 2: Judgment Day\" - <br>\"T2: Extreme Edition\" - USA <em>(video box title)</em><br>\"T2: Ultimate Edition\" - USA <em>(video box title)</em><br>\"Terminator 2 - Le jugement dernier\" - France<br>\"Terminator 2: el juicio final\" - Argentina, Peru, Spain<br>\"&#x422;&#x435;&#x440;&#x43C;&#x438;&#x43D;&#x430;&#x442;&#x43E;&#x440; 2: &#x421;&#x443;&#x434;&#x43D;&#x44B;&#x439; &#x434;&#x435;&#x43D;&#x44C;\" - Russia<br>\"Exolothreftis 2 - Mera krisis\" - Greece<br>\"Exterminador Implac&#xE1;vel 2: O Dia do Julgamento\" - Portugal<br>\"O Exterminador do Futuro 2: O Julgamento Final\" - Brazil<br>\"Termin&#xE1;tor 2. - Az &#xED;t&#xE9;let napja\" - Hungary<br>\"Termin&#xE1;tor 2: Den z&#xFA;ctov&#xE1;n&#xED;\" - Czechoslovakia <em>(Czech title)</em><br>\"Termin&#xE1;tor 2: Den z&#xFA;ctovania\" - Czechoslovakia <em>(Slovak title)</em><br>\"Terminateur 2: Le jugement dernier\" - Canada <em>(French title)</em><br>\"Terminator 2\" - Japan <em>(English title)</em><br>\"Terminator 2\" - Poland <em>(TV title)</em><br>\"Terminator 2 - El d&#xED;a del juicio\" - Mexico<br>\"Terminator 2 - Mahser g&#xFC;n&#xFC;\" - Turkey <em>(Turkish title)</em><br>\"Terminator 2 - Sodni dan\" - Slovenia<br>\"Terminator 2 - Sudnji dan\" - Serbia<br>\"Terminator 2 - Tag der Abrechnung\" - Germany<br>\"Terminator 2 - domedagen\" - Sweden<br>\"Terminator 2 - domens dag\" - Finland <em>(Swedish title)</em><br>\"Terminator 2 - il giorno del giudizio\" - Italy<br>\"Terminator 2 - tuomion p&#xE4;iv&#xE4;\" - Finland<br>\"Terminator 2: Dommedag\" - Denmark<br>\"Terminator 2: Dzien sadu\" - Poland<br>\"Terminator 2: Sudnji dan\" - Croatia<br>\"Terminator 2: Sudnyi den&#x27;\" - Soviet Union <em>(Russian title)</em><br><a class=\"tn15more\" href=\"/title/tt0103064/releaseinfo#akas\" onClick=\"(new Image()).src='/rg/title-tease/akas/images/b.gif?link=/title/tt0103064/releaseinfo#akas';\">See more</a>&nbsp;&raquo;</div></div> <div class=\"info\"><h5><a href=\"/mpaa\">MPAA</a>:</h5><div class=\"info-content\">Rated");
		//StringBuffer buff = new StringBuffer("<hr />      <h3>Additional Details</h3><div class=\"info\"><h5>Also Known As:</h5><div class=\"info-content\">       \"El Exterminator 2\" - USA <em>(Spanish title)</em><br>\"T2\" - USA <em>(promotional abbreviation)</em><br>\"T2 - Terminator 2: Judgment Day\" - <br>\"T2: Extreme Edition\" - USA <em>(video box title)</em><br>\"T2: Ultimate Edition\" - USA <em>(video box title)</em><br>\"Terminator 2 - Le jugement dernier\" - France<br> Russia<br>\"Exolothreftis 2 - Mera krisis\" - Greece<br>\"Exterminador Implac& - Czechoslovakia <em>(Czech title)</em><br>\"Termin raquo;</div></div> <div class=\"info\"><h5><a href=\"/mpaa\">MPAA</a>:</h5><div class=\"info-content\">Rated");
		StringBuffer buff = new StringBuffer("<hr />      <h3>Additional Details</h3><div class=\"info\"><h5>Also Known As:</h5><div class=\"info-content\">      itle)</em><br>\"Termin raquo;</div></div> <div class=\"info\"><h5><a href=\"/mpaa\">MPAA</a>:</h5><div class=\"info-content\">Rated");
		
		buff = new StringBuffer("h3><div class=\"info\">ddddddddddd <h5>A <div class=\"info\"> sdfsfdsdsfdsfsdfdf <div class=\"info\"> sdfsfsdfsdfsdfsdfsdfdsf  <div class=\"info\">");
			
		HashMap<String, String> map = imdb.decodeClassInfo(buff);
		
	}
}
