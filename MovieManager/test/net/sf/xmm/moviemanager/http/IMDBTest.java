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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import net.sf.xmm.moviemanager.http.HttpUtil.HTTPResult;
import net.sf.xmm.moviemanager.imdblib.IMDbScraper;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbEntry;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbSearchHit;
import net.sf.xmm.moviemanager.util.FileUtil;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;


public class IMDBTest  {

	boolean setup = false;
	IMDbScraper imdb;
	
	@Before
	public void setUp() throws Exception {
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

		imdb = new IMDbScraper();
	}


	@Test
	public void getSimpleMatchesTest() throws Exception {
		String input = "La fille sur le";
		ArrayList<ModelIMDbSearchHit> matches = imdb.getSimpleMatches(input);
		
		assertEquals("The number of results does not match the expected for input " + input, 11, matches.size());
		assertEquals("The expected IMDb ID did not match", "0144201", matches.get(0).getUrlID());
		
//		for (ModelIMDbSearchHit m : matches) {
//			System.out.println("Title:" + m.getTitle() + " ID:" + m.getUrlID());
//		}
	}
	
	@Test
	public void getSimpleMatchesTitleWithAmpersandTest() throws Exception {
		String input = "Marly & Me";
		ArrayList<ModelIMDbSearchHit> matches = imdb.getSimpleMatches(input);
		
		assertEquals("The number of results does not match the expected for input " + input, 18, matches.size());
		assertEquals("The expected IMDb ID did not match", "0822832", matches.get(0).getUrlID());
		
		//for (ModelIMDbSearchHit m : matches) {
		//	System.out.println("Title:" + m.getTitle() + " ID:" + m.getUrlID());
		//}
	}

	
	@Test
	public void getSimpleMatchesStraigtToTitleTest() throws Exception {
		// Special case with only one hit, where one is directly referred to the movie page
		String input = "La fille sur le pont";

		ArrayList<ModelIMDbSearchHit> matches = imdb.getSimpleMatches(input);
		assertEquals("The number of results does not match the expected for input " + input, 1, matches.size());
		assertEquals("The expected IMDb ID did not match", "0144201", matches.get(0).getUrlID());
	}

	
	@Test
	public void seriesSimpleMatchesTest() throws Exception {
		String input = "Buffy the Vampire Slayer";

		ArrayList<ModelIMDbSearchHit> matches = null;
		matches = imdb.getSimpleMatches(input);
		
//		for (int i = 0; i < matches.size(); i++)
//			System.out.println("matches.get("+i+"):" + matches.get(i));
			
		assertEquals("The number of results does not match the expected for input " + input, 5, matches.size());
	}
	
	@Test
	public void getSeriesMatchesTest() throws Exception {
		String input = "Buffy the Vampire Slayer";

		ArrayList<ModelIMDbSearchHit> matches = null;
		matches = imdb.getSeriesMatches(input);
		assertEquals("The number of results does not match the expected for input " + input, 1, matches.size());
		
		input = "Nikita";
		matches = imdb.getSeriesMatches(input);
		assertEquals("The number of results does not match the expected for input " + input, 3, matches.size());
		
		input = "The Simpsons";
		matches = imdb.getSeriesMatches(input);
		assertEquals("The number of results does not match the expected for input " + input, 1, matches.size());

//		for (int i = 0; i < matches.size(); i++)
//			System.out.println("matches.get("+i+"):" + matches.get(i));
	}
		
	
	@Test
	public void getMatchesTest() throws Exception {

		/*
		 Troublesome titles:
		 Marly & Me

		 Mr. Bean's Holiday
		 You don't mess with the Zohan

		 	// <a href="/title/tt0496424/" onclick="(new Image()).src='/rg/find-title-1/title_popular/images/b.gif?link=/title/tt0496424/';">&#34;30 Rock&#34;</a> (2006) <small>(TV series)</small>     <div style="font-size: small">&#160;&#45;&#160;Season 3, Episode 11: 
			// <a href="/title/tt0074853/">The Man in the Iron Mask</a> (1977) (TV)</td></tr>
			// <a href="/title/tt0120744/">The Man in the Iron Mask</a> (1998/I)</td></tr>
			// <a href="/title/tt0103064/">Terminator 2: Judgment Day</a> (1991)<br>&#160;aka <em>"Terminator 2 - Le jugement dernier"</em> - France<br>&#160;aka <em>"T2 - Terminator 2: Judgment Day"</em></td></tr>

			// <a href="/title/tt0822832/" onclick="(new Image()).src='/rg/find-title-1/title_popular/images/b.gif?link=/title/tt0822832/';">Marley &#x26; Me</a> (2008)     </td></tr></table> 

			// <a href="/title/tt0311669/" onclick="(new Image()).src='/rg/find-title-2/title_exact/images/b.gif?link=/title/tt0311669/';">Predator</a> (1989) (VG)     </td></tr></table>
		 
		 */

		StringBuffer testData = new StringBuffer();

		testData.append("<title>IMDb Search</title>");
		
		testData.append(imdb.movieHitCategory[0]);
		
		testData.append("<a href=\"/title/tt0496424/\" onclick=\"(new Image()).src='/rg/find-title-1/title_popular/images/b.gif?link=/title/tt0496424/';\">&#34;30 Rock&#34;</a> (2006) <small>(TV series)</small>     <div style=\"font-size: small\">&#160;&#45;&#160;Season 3, Episode 11: ");
		testData.append("<a href=\"/title/tt0074853/\">The Man in the Iron Mask</a> (1977) (TV)</td></tr>");
				
		// Troublesome because two movies with the same name was created that year (1998/I)
		testData.append("<a href=\"/title/tt0120744/\">The Man in the Iron Mask</a> (1998/I)</td></tr>");
		testData.append("<a href=\"/title/tt0103064/\">Terminator 2: Judgment Day</a> (1991)<br>&#160;aka <em>\"Terminator 2 - Le jugement dernier\"</em> - France<br>&#160;aka <em>\"T2 - Terminator 2: Judgment Day\"</em></td></tr>");
	
		
		// "Marley & Me" - troublesome because if &
		testData.append("<a href=\"/title/tt0822832/\" onclick=\"(new Image()).src='/rg/find-title-1/title_popular/images/b.gif?link=/title/tt0822832/';\">Marley &#x26; Me</a> (2008)     </td></tr></table>");
		
		// "You Don't Mess with the Zohan" - troublesome because if '
		testData.append("<a href=\"/title/tt0960144/\" onclick=\"(new Image()).src='/rg/find-title-1/title_popular/images/b.gif?link=/title/tt0960144/';\">You Don&#x27;t Mess with the Zohan</a> (2008)     </td></tr></table>");
		
		// Should NOT match because of (VG) which means Video Game
		testData.append("<a href=\"/title/tt0311669/\" onclick=\"(new Image()).src='/rg/find-title-2/title_exact/images/b.gif?link=/title/tt0311669/';\">Predator</a> (1989) (Video Game)   </td></tr></table>");
		
		HTTPResult result = new HTTPResult(new URL("http://"), new URI("http://"), testData, null);
		ArrayList<ModelIMDbSearchHit> matches = imdb.getMatches(result);
		assertEquals("The number of results does not match the expected", 6, matches.size());
		
		
//		for (int i = 0; i < matches.size(); i++)
//			System.out.println("matches.get("+i+"):" + matches.get(i));

	
		/*
		  
		 
		// "30 Rock" (2006) 
		matches = imdb.getSimpleMatches("30 Rock");
		assertTrue("Movie search result did not include the expected title", hasUrlKey(matches, "0496424"));
		
		// The Man in the Iron Mask</a> (1977) (TV)
		matches = imdb.getSimpleMatches("The Man in the Iron Mask");
		assertTrue("Movie search result did not include the expected title", hasUrlKey(matches, "0074853"));
		
		// The Man in the Iron Mask</a> (1998/I)
		matches = imdb.getSimpleMatches("The Man in the Iron Mask");
		assertTrue("Movie search result did not include the expected title", hasUrlKey(matches, "0120744"));
		
		// Terminator 2: Judgment Day
		matches = imdb.getSimpleMatches("Terminator 2: Judgment Day");
		assertTrue("Movie search result did not include the expected title", hasUrlKey(matches, "0103064"));

		// "Marley & Me" - troublesome because if &
		matches = imdb.getSimpleMatches("Marley & Me");
		assertTrue("Movie search result did not include the expected title", hasUrlKey(matches, "0822832"));

		// "You Don't Mess with the Zohan"
		matches = imdb.getSimpleMatches("You Don't Mess with the Zohan");
		assertTrue("Movie search result did not include the expected title", hasUrlKey(matches, "0960144"));

		// Predator (1989) (VG) (Should NOT match because of (VG) which means Video Game)
		matches = imdb.getSimpleMatches("Predator");
		assertFalse("Movie search result included a title which it shouldn't", hasUrlKey(matches, "0311669"));
		
		*/
				
	}
	
	boolean hasUrlKey(ArrayList<ModelIMDbSearchHit> matches, String urlID) {
		for (int i = 0; i < matches.size(); i++) {
			if (matches.get(i).getUrlID().equals(urlID))
				return true;
		}
		return false;
	}
	
	@Test
	public void getSeasonsTest() throws Exception {

		String urlKey = "0118276"; //Buffy the Vampire Slayer (TV Series 1997–2003)
		ModelIMDbSearchHit seriesHit = new ModelIMDbSearchHit(urlKey, "Buffy");
		ArrayList<ModelIMDbSearchHit> matches = imdb.getSeasons(seriesHit);
		assertEquals("The number of results does not match the expected", 7, matches.size());
				
		urlKey = "0096697"; //The Simpsons
		seriesHit = new ModelIMDbSearchHit(urlKey, "Simpsons");
		matches = imdb.getSeasons(seriesHit);
		assertEquals("The number of results does not match the expected", 25, matches.size());
	}
	
	
	@Test
	public void dataRetrievalTest() throws Exception {
						
		// Terminator 2
		StringBuffer data = imdb.getURLData("0103064").getData();
		
		ModelIMDbEntry movie = imdb.grabInfo("0103064", data);
		
		String expectedTitle = "Terminator 2: Judgment Day";
		String expectedDate = "1991";
		String expectedDirector = "James Cameron";
		String expectedWriter = "James Cameron, William Wisher Jr.";
		String expectedGenre = "Action, Sci-Fi, Thriller";
		String expectedCountry = "USA, France";
		String expectedLanguage = "English, Spanish";
		String expectedPlot = "Nearly 10 years have passed since Sarah Connor was targeted for termination by a cyborg from the future. Now her son, John, the future leader of the resistance, is the target for a newer, more deadly terminator. Once again, the resistance has managed to send a protector back to attempt to save John and his mother Sarah.";
		String expectedCast = "Arnold Schwarzenegger (The Terminator), Linda Hamilton (Sarah Connor), Edward Furlong (John Connor), Robert Patrick (T-1000), Earl Boen (Dr. Silberman), Joe Morton (Miles Dyson), S. Epatha Merkerson (Tarissa Dyson), Castulo Guerra (Enrique Salceda), Danny Cooksey (Tim), Jenette Goldstein (Janelle Voight), Xander Berkeley (Todd Voight), Leslie Hamilton Gearren (Twin Sarah), Ken Gibbel (Douglas), Robert Winley (Cigar Biker), Peter Schrum (Lloyd (as Pete Schrum)), Shane Wilder (Trucker), Michael Edwards (Old John Connor), Jared Lounsbery (Kid), Casey Chavez (Kid), Ennalls Berl (Bryant), Don Lake (Mossberg), Richard Vidan (Weatherby), Tom McDonald (Cop), Jim Palmer (Jock), Gerard G. Williams (Jock), Gwenda Deacon (Night Nurse), Don Stanton (Lewis the Guard), Dan Stanton (Lewis as T-1000), Colin Patrick Lynch (Attendant), Noel Evangelisti (Hospital Guard), Nikki Cox (Girl), Lisa Brinegar (Girl), DeVaughn Nixon (Danny Dyson (as De Vaughn Nixon)), Tony Simotes (Vault Guard), Diane Rodriguez (Jolanda Salceda), Dalton Abbott (Infant John Connor), Ron Young (Pool Cue Biker), Charles Robert Brown (Tattoo Biker), Abdul Salaam El Razzac (Gibbons), Mike Muscat (Moshier), Dean Norris (SWAT Team Leader), Charles A. Tamburro (Police Chopper Pilot (as Charles Tamburro)), J. Rob Jordan (Pickup Truck Driver), Terrence Evans (Tanker Truck Driver), Denney Pierce (Burly Attendant), Mark Christopher Lawrence (Burly Attendant), Pat Kouri (SWAT Leader), Van Ling (Cyberdyne Tech), Michael Albanese (SWAT Officer (uncredited)), Ed Arneson (SWAT Officer (uncredited)), Bret A. Arnold (Future Coda Man (uncredited)), Debra Casey (Mohawk Girl at Biker Bar (uncredited)), Jim Dahl (SWAT Officer (uncredited)), Takao Komine (Tourist Shot by T-1000 (uncredited)), Joel Kramer (Male Nurse (uncredited)), Anne Merrem (Psychiatric (uncredited)), Scott Shaw (Cyberdyne Tech (uncredited)), Steven Stear (SWAT Officer (uncredited)), Misty Jo Walker (Sarah's Granddaughter (scenes deleted) (uncredited)), Randy Walker (SWAT Officer (uncredited)), William Wisher Jr. (Galleria Photographer / Cop (uncredited))";
		String expectedWebRuntime = "137 min, USA:152 min (special edition), USA:154 min (extended special edition)";
		String expectedWebSoundMix = "70 mm 6-Track (analog 70 mm prints), CDS (digital 35 mm and 70 mm prints), Dolby SR (analog 35 mm prints)";
		String expectedAwards = "Won 4 Oscars. Another 20 wins & 19 nominations";
		String expectedMpaa = "Rated R for strong sci-fi action and violence, and for language";
		String expectedAka = 
		
		"\"El Exterminator 2\" - USA (Spanish title)\n" +
		"\"T2\" - USA (promotional abbreviation)\n" + 
		"\"T2 - Terminator 2: Judgment Day\" -\n" + 
		"\"T2: Extreme Edition\" - USA (video box title)\n" + 
		"\"T2: Ultimate Edition\" - USA (video box title)\n" + 
		"\"Terminator 2 - Le jugement dernier\" - France\n" + 
		"\"Terminator 2: el juicio final\" - Argentina, Peru, Spain\n" + 
		"\"Терминатор 2: Страшният съд\" - Bulgaria (Bulgarian title)\n" + 
		"\"Терминатор 2: Судный день\" - Russia\n" + 
		"\"Exolothreftis 2: Mera krisis\" - Greece (imdb display title)\n" + 
		"\"Exterminador Implacável 2: O Dia do Julgamento\" - Portugal\n" + 
		"\"Ke Huy Diet 2: Ngay Phan Xet\" - Vietnam (imdb display title)\n" + 
		"\"O Exterminador do Futuro 2: O Julgamento Final\" - Brazil\n" + 
		"\"Tarminetara 2: Pralaya ka dina\" - India (Hindi title) (dubbed version)\n" + 
		"\"Terminátor 2. - Az ítélet napja\" - Hungary\n" + 
		"\"Terminátor 2: Den zúctování\" - Czechoslovakia (Czech title)\n" + 
		"\"Terminátor 2: Den zúctovania\" - Czechoslovakia (Slovak title)\n" +
		"\"Terminátor 2: Soudný den\" - Czechoslovakia (imdb display title)\n" + 
		"\"Terminaator 2: Kohtupäev\" - Estonia (promotional title)\n" + 
		"\"Terminateur 2: Le jugement dernier\" - Canada (French title)\n" + 
		"\"Terminator 2\" - Japan (English title)\n" + 
		"\"Terminator 2\" - Poland (TV title)\n" + 
		"\"Terminator 2 - Domedagen\" - Sweden (imdb display title)\n" + 
		"\"Terminator 2 - El día del juicio\" - Mexico\n" + 
		"\"Terminator 2 - Il giorno del giudizio\" - Italy (imdb display title)\n" + 
		"\"Terminator 2 - Judgment Day\" - Sweden (DVD title)\n" + 
		"\"Terminator 2 - Mahser günü\" - Turkey (Turkish title)\n" + 
		"\"Terminator 2 - Sodni dan\" - Slovenia\n" + 
		"\"Terminator 2 - Sudnji dan\" - Serbia\n" + 
		"\"Terminator 2 - Tag der Abrechnung\" - Germany\n" + 
		"\"Terminator 2 - Tuomion päivä\" - Finland (imdb display title)\n" + 
		"\"Terminator 2 - domens dag\" - Finland (Swedish title)\n" + 
		"\"Terminator 2: Dommedag\" - Denmark\n" + 
		"\"Terminator 2: Dommens Dag\" - Norway (imdb display title)\n" + 
		"\"Terminator 2: Dzien sadu\" - Poland\n" + 
		"\"Terminator 2: Juicio Final\" - Mexico (imdb display title)\n" + 
		"\"Terminator 2: Sudnji dan\" - Croatia\n" + 
		"\"Terminator 2: Sudnyi den'\" - Soviet Union (Russian title)\n" + 
		"\"Terminator 2: Ziua judecatii\" - Romania (imdb display title)";
			
		String expectedCertification = "Canada:18 (Nova Scotia) (DVD rating), Canada:A (Nova Scotia) (original cut), Canada:AA (Ontario) (original cut), Canada:PA (Manitoba) (original cut), Finland:K-18 (original rating) (1991), Italy:T, USA:R (certificate #31159), Iceland:16, South Korea:15, Brazil:12, Malaysia:18SG, New Zealand:M, Netherlands:12 (edited TV version), Portugal:M/12, Argentina:13 (re-rating), Argentina:16 (original rating), Australia:M, Canada:13+ (Quebec), Canada:18A (Alberta) (re-rating) (1999), Canada:18A (Manitoba/Ontario) (DVD rating), Chile:14, Finland:K-16 (re-rating) (1991), France:-12, Germany:16, Ireland:15, Israel:PG, Japan:R-15, Netherlands:16, Norway:15 (video rating) (director's cut), Norway:18 (original rating), Peru:14, Singapore:NC-16, Singapore:PG (cut), Spain:18, Sweden:15, UK:15 (original rating) (cut), UK:15 (video rating) (1992) (cut), UK:15 (video re-rating) (2001) (uncut), UK:18 (laserdisc rating) (1992) (uncut), Iran:18+";
		String expectedColor = "Color";
		
		assertTrue(Double.parseDouble(movie.getRating()) > 5);
		
		assertEquals(expectedTitle, movie.getTitle());
		assertEquals(expectedDate, movie.getDate());
		assertEquals(expectedDirector, movie.getDirectedBy());
		assertEquals(expectedWriter, movie.getWrittenBy());
		assertEquals(expectedGenre, movie.getGenre());
		assertEquals(expectedCountry, movie.getCountry());
		assertEquals(expectedLanguage, movie.getLanguage());
		assertEquals(expectedPlot, movie.getPlot());
		assertEquals(expectedCast, movie.getCast());
		assertEquals(expectedWebRuntime, movie.getWebRuntime());
		assertEquals(expectedWebSoundMix, movie.getWebSoundMix());
		assertEquals(expectedAwards, movie.getAwards());
		assertEquals(expectedMpaa, movie.getMpaa());
		assertEquals(expectedAka, movie.getAka());
		assertEquals(expectedCertification, movie.getCertification());
		assertEquals(expectedColor, movie.getColour());
		
		assertNotNull("No cover available!", movie.getCoverData());
	}
	
	@Test
	public void dataRetrievalSeriesTest() throws Exception {
				
		// Buffy
		StringBuffer data = imdb.getURLData("0118276").getData();
		
		ModelIMDbEntry series = imdb.grabInfo("0118276", data);
		
		assertTrue("Not detected as a series!", series.isSeries());
				
		String expectedTitle = "\"Buffy the Vampire Slayer\"";
		String expectedDate = "1997";
		String expectedDirector = "";
		String expectedWriter = "Joss Whedon";
		String expectedGenre = "Action, Drama, Fantasy";
		String expectedCountry = "USA";
		String expectedLanguage = "English";
		String expectedPlot = "At the young age of 15, Buffy Summers was chosen to hunt vampires, demons, and the forces of darkness. Joined by friends Willow and Xander and her watcher Giles, Buffy fights the challenges of High School and saves the world...a lot.";
		String expectedCast = "Sarah Michelle Gellar (Buffy Summers /, (145 episodes, 1996-2003)), Nicholas Brendon (Xander Harris (145 episodes, 1996-2003)), Alyson Hannigan (Willow Rosenberg (144 episodes, 1997-2003)), Anthony Head (Rupert Giles (123 episodes, 1996-2003)), James Marsters (Spike (97 episodes, 1997-2003)), Emma Caulfield (Anya (85 episodes, 1998-2003)), Michelle Trachtenberg (Dawn Summers (66 episodes, 2000-2003))";
		String expectedWebRuntime = "44 min (144 episodes)";
		String expectedWebSoundMix = "Dolby";
		String expectedAwards = "Nominated for Golden Globe. Another 42 wins & 98 nominations";
		String expectedMpaa = "";
		String expectedAka = 
			"\"BtVS\" - USA (promotional abbreviation)\n" +
			"\"Buffy\" - USA (short title)\n" +
			"\"Buffy, the Vampire Slayer: The Series\" - USA (long title)\n" +
			"\"Buffy, la cazavampiros\" - Argentina, Mexico, Venezuela\n" +
			"\"Бъфи - убийцата на вампири\" - Bulgaria (Bulgarian title)\n" +
			"\"Bafi, ubica vampira\" - Serbia\n" +
			"\"Buffy - Im Bann der Dämonen\" - Germany\n" +
			"\"Buffy - Izganjalka vampirjev\" - Slovenia\n" +
			"\"Buffy - Vampyrenes skrekk\" - Norway\n" +
			"\"Buffy - Vampyrernes skræk\" - Denmark\n" +
			"\"Buffy contre les vampires\" - France\n" +
			"\"Buffy i vampirofonissa\" - Greece (transliterated ISO-LATIN-1 title)\n" +
			"\"Buffy och vampyrerna\" - Sweden (cable TV title)\n" +
			"\"Buffy vampyrdödaren\" - Sweden\n" +
			"\"Buffy, Caçadora de Vampiros\" - Portugal\n" +
			"\"Buffy, a vámpírok réme\" - Hungary\n" +
			"\"Buffy, cazavampiros\" - Spain (imdb display title)\n" +
			"\"Buffy, l'ammazzavampiri\" - Italy\n" +
			"\"Buffy, vampyyrintappaja\" - Finland\n" +
			"\"Buffy: A Caça-Vampiros\" - Brazil (imdb display title)\n" +
			"\"Buffy: Postrach wampirów\" - Poland (imdb display title)\n" +
			"\"Nightfall\" - Japan (English title)";	
	
		
		
		String expectedCertification = "UK:12 (some episodes), UK:15 (some episodes), Australia:PG (some episodes), Portugal:M/12, New Zealand:M, USA:TV-14, Australia:M, Israel:PG, Singapore:M18 (DVD rating) (season 6) (season 7), Singapore:PG (season 1 to 5), USA:TV-14 (some episodes), USA:TV-PG (some episodes)";
		String expectedColor = "Color";
				
		assertTrue(Double.parseDouble(series.getRating()) > 5);
		
		assertEquals(expectedTitle, series.getTitle());
		assertEquals(expectedDate, series.getDate());
		assertEquals(expectedDirector, series.getDirectedBy());
		assertEquals(expectedWriter, series.getWrittenBy());
		assertEquals(expectedGenre, series.getGenre());
		assertEquals(expectedCountry, series.getCountry());
		assertEquals(expectedLanguage, series.getLanguage());
		assertEquals(expectedPlot, series.getPlot());
		assertEquals(expectedCast, series.getCast());
		assertEquals(expectedWebRuntime, series.getWebRuntime());
		assertEquals(expectedWebSoundMix, series.getWebSoundMix());
		assertEquals(expectedAwards, series.getAwards());
		assertEquals(expectedMpaa, series.getMpaa());
		assertEquals(expectedAka, series.getAka());
		assertEquals(expectedCertification, series.getCertification());
		assertEquals(expectedColor, series.getColour());		
		
		assertNotNull("No cover available!", series.getCoverData());
	}
	
	
	@Test
	public void getEpisodesTest() throws Exception {
			
		// Grey's Anatomy, season 1
		ModelIMDbSearchHit hit = new ModelIMDbSearchHit("0413573", null, 1);
				
		StringBuffer episodesStream = imdb.getEpisodesStream(hit);
		//net.sf.xmm.moviemanager.util.FileUtil.writeToFile("HTML-debug/episodesStream.html", episodesStream);
		
		ArrayList<ModelIMDbSearchHit> hits = imdb.getEpisodes(hit, episodesStream);
		
		assertEquals(9, hits.size());
	}
	
	
	@Test
	public void dataRetrievalEpisodeTest() throws Exception {
		
		// Seinfeld - The Cadillac (Season 7, Episode 14)
		StringBuffer data = imdb.getURLData("0697667").getData();
		
		ModelIMDbEntry series = imdb.grabInfo("0697667", data);
		
		assertTrue("Not detected as an episode!", series.isEpisode());
				
		String expectedTitle = "The Cadillac";
		String expectedDate = "1996";
		String expectedDirector = "Andy Ackerman";
		String expectedWriter = "Larry David, Jerry Seinfeld";
		String expectedGenre = "Comedy";
		String expectedCountry = "USA";
		String expectedLanguage = "English";
		String expectedPlot = "In this hour-long episode, Jerry performs the biggest show of his life in Atlantic City. He receives a rather generous pay check for the event, and decides to buy his father a Cadillac. Unfortunately, doing so puts Morty in the hot seat with the condo board of directors, where he serves as president. Morty is accused of stealing money from the board, and ultimately gets removed from his post as president and kicked out of the condo. Meanwhile, thanks to Elaine, George can score a date with \"My Cousin Vinnie\" star Marisa Tomei but Elaine objects because of George's engagement with Susan. Kramer turns the tables on the cable company when they want to disconnect his service.";
		String expectedCast = "Jerry Seinfeld (Jerry Seinfeld), Julia Louis-Dreyfus (Elaine Benes), Michael Richards (Cosmo Kramer), Jason Alexander (George Costanza), Marisa Tomei (Herself), Liz Sheridan (Helen), Barney Martin (Morty), Heidi Swedberg (Susan Ross), Walter Olkewicz (Nick), Annabelle Gurwitch (Katy), Sandy Baron (Jack Klompus), Ann Morgan Guilbert (Evelyn (as Ann Guilbert)), Frances Bay (Mrs. Choate), Bill Macy (Herb), Jesse White (Ralph), Annie Korzen (Doris), Daniel Zacapa (Power Guy), Golde Starger (Bldg 'A'), Janice Davies (Bldg 'B'), Art Frankel (Bldg 'C' (as Art Frankle)), Ruth Cohen (Ruthie Cohen (uncredited))";
		String expectedWebRuntime = "44 min";
		String expectedWebSoundMix = "Stereo";
		String expectedAwards = "";
		String expectedMpaa = "";
		String expectedAka = "";
		
		String expectedCertification = "Canada:PG (video rating)";
		String expectedColor = "Color";
				
		assertTrue(Double.parseDouble(series.getRating()) > 5);
		
		assertEquals(expectedTitle, series.getTitle());
		assertEquals(expectedDate, series.getDate());
		assertEquals(expectedDirector, series.getDirectedBy());
		assertEquals(expectedWriter, series.getWrittenBy());
		assertEquals(expectedGenre, series.getGenre());
		assertEquals(expectedCountry, series.getCountry());
		assertEquals(expectedLanguage, series.getLanguage());
		assertEquals(expectedPlot, series.getPlot());
		assertEquals(expectedCast, series.getCast());
		assertEquals(expectedWebRuntime, series.getWebRuntime());
		assertEquals(expectedWebSoundMix, series.getWebSoundMix());
		assertEquals(expectedAwards, series.getAwards());
		assertEquals(expectedMpaa, series.getMpaa());
		assertEquals(expectedAka, series.getAka());
		assertEquals(expectedCertification, series.getCertification());
		assertEquals(expectedColor, series.getColour());
		
		assertNull("No cover available!", series.getCoverData());
	}
	
	@Test
	public void dataRetrievalMultipleDirectorsTest() throws Exception {
				
		// The Ladykillers
		StringBuffer data = imdb.getURLData("0335245").getData();
		
		ModelIMDbEntry movie = imdb.grabInfo("0335245", data);
						
		String expectedTitle = "The Ladykillers";
		String expectedDirector = "Ethan Coen, Joel Coen";
		String expectedWriter = "Joel Coen, Ethan Coen";
		
		assertEquals(expectedTitle, movie.getTitle());
		assertEquals(expectedDirector, movie.getDirectedBy());
		assertEquals(expectedWriter, movie.getWrittenBy());
	}
	
	@Test
	public void dataRetrievalTVSeriesTest() throws Exception {
				
		StringBuffer data = imdb.getURLData("0370053").getData();
		
		ModelIMDbEntry series = imdb.grabInfo("0370053", data);
		
		assertTrue("Not detected as a series!", series.isSeries());
				
		String expectedTitle = "\"Walking with Cavemen\"";
		String expectedDate = "2003";
		String expectedDirector = "";
		String expectedWriter = "";
		String expectedGenre = "Documentary, History";
		String expectedCountry = "UK";
		String expectedLanguage = "English";
		String expectedPlot = "The story of human evolution is told through the stories of representative members of the various species leading up to modern homo sapiens. It is ongoing climate changes that force human ancestors to develop, one by one, the unique characteristics of the modern humans. Though earlier species were superbly successful in their environments they were unsustainable when the environment changes.";
		String expectedCast = "Robert Winston (Himself - Presenter (4 episodes, 2003)), Alec Baldwin (Narrator (USA version) (4 episodes, 2003)), Christian Bradley (Various hominids (4 episodes, 2003)), Alex Palmer (Various hominids (4 episodes, 2003)), Oliver Parham (Various hominids (4 episodes, 2003)), David Rubin (Various hominids (4 episodes, 2003)), Florence Sparham (Various hominids (4 episodes, 2003)), Marva Alexander (Various hominids (3 episodes, 2003)), Rachel Essex (Various hominids (3 episodes, 2003)), Faroque Khan (Various hominids (3 episodes, 2003)), Suzanne Cave (Various hominids (2 episodes, 2003)), Ruth Dawes (Various hominids (2 episodes, 2003)), Anthony Taylor (Various hominids (2 episodes, 2003)), Badria Timimi (Various hominids (2 episodes, 2003))";
		String expectedWebRuntime = "UK:30 min (4 episodes), USA:100 min";
		String expectedWebSoundMix = "";
		String expectedAwards = "";
		String expectedMpaa = "";
		String expectedAka = 
			"\"Caminando entre cavernícolas\" - Spain\n" + 
			"\"I huleboernes verden\" - Denmark\n" + 
			"\"Luolamiesten matkassa\" - Finland\n" + 
			"\"Perpatontas me tous anthropous ton spilaion\" - Greece (transliterated ISO-LATIN-1 title)";

	
		String expectedCertification = "Singapore:PG";
		String expectedColor = "Color";
				
		assertTrue(Double.parseDouble(series.getRating()) > 5);
		
		assertEquals(expectedTitle, series.getTitle());
		assertEquals(expectedDate, series.getDate());
		assertEquals(expectedDirector, series.getDirectedBy());
		assertEquals(expectedWriter, series.getWrittenBy());
		assertEquals(expectedGenre, series.getGenre());
		assertEquals(expectedCountry, series.getCountry());
		assertEquals(expectedLanguage, series.getLanguage());
		assertEquals(expectedPlot, series.getPlot());
		assertEquals(expectedCast, series.getCast());
		assertEquals(expectedWebRuntime, series.getWebRuntime());
		assertEquals(expectedWebSoundMix, series.getWebSoundMix());
		assertEquals(expectedAwards, series.getAwards());
		assertEquals(expectedMpaa, series.getMpaa());
		assertEquals(expectedAka, series.getAka());
		assertEquals(expectedCertification, series.getCertification());
		assertEquals(expectedColor, series.getColour());		
		
		assertNotNull("No cover available!", series.getCoverData());
	}
	
	
	@Test
	public void authenticatedPersonVoteTest() throws Exception {
		
		String [] auth = getLocallyStoredIMDbUser();
		
		HttpSettings settings = new HttpSettings();
		settings.setIMDbAuthenticationEnabled(true);
		settings.setIMDbAuthenticationUser(auth[0]);
		settings.setIMDbAuthenticationPassword(auth[1]);
		
		IMDbScraper imdb = new IMDbScraper(settings);
		
		String urlID = "0093773"; // Predator
		urlID = "0468569"; // The Dark Knight
		
		StringBuffer data = imdb.getURLData(urlID).getData();
		ModelIMDbEntry movie = imdb.grabInfo(urlID, data);
				
		assertTrue("Failed to log in to IMDb", imdb.isLoggedIn());
	}
	
	@Test
	public void authenticatedTest() throws Exception {
				
		String [] auth = getLocallyStoredIMDbUser();
		
		HttpSettings settings = new HttpSettings();
		settings.setIMDbAuthenticationEnabled(true);
		settings.setIMDbAuthenticationUser(auth[0]);
		settings.setIMDbAuthenticationPassword(auth[1]);
		
		IMDbScraper imdb = new IMDbScraper(settings);
		String urlID = "0093773"; // Predator
		
		StringBuffer data = imdb.getURLData(urlID).getData();
		ModelIMDbEntry movie = imdb.grabInfo(urlID, data);
		
		assertTrue("Failed to log in to IMDb", imdb.isLoggedIn());
	}
	
	public String [] getLocallyStoredIMDbUser() {
		/*
		 The File should contain two lines:
		 User:<IMDbUsername>
		 Pass:<password>
		 */
		ArrayList<String> content;
		try {
			content = FileUtil.readFileToArrayList(new File("LocalIMDbAuth.txt"));
			String user = content.get(0);
			String pass = content.get(1);
			user = user.substring(user.indexOf(":") +1, user.length());
			pass = pass.substring(pass.indexOf(":") +1, pass.length());
			return new String[] {user, pass};
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("This test requires a local file (in the project root) containing a IMDb username and password!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
