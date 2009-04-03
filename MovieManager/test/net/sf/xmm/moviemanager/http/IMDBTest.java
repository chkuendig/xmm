package net.sf.xmm.moviemanager.http;

import static org.junit.Assert.*;

import javax.swing.DefaultListModel;

import org.junit.Test;


public class IMDBTest {


	public void getSimpleMatchesTest() {

		IMDB imdb = null;
		try {
			imdb = new IMDB();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String input = "La fille sur le pon";

		DefaultListModel matches = imdb.getSimpleMatches(input);
		assertTrue("The number of results does not match the expected for input " + input, matches.getSize() ==  20);


		input = "Marly & Me";

		matches = imdb.getSimpleMatches(input);
		assertTrue("The number of results does not match the expected for input " + input, matches.getSize() ==  20);


		for (int i = 0; i < matches.getSize(); i++)
			System.out.println("matches.get("+i+"):" + matches.get(i));	

	}



	@Test
	public void getMatchesTest() {

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

		IMDB imdb = null;
		try {
			imdb = new IMDB();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StringBuffer testData = new StringBuffer();

		testData.append("<title>IMDb Search</title>");
		
		testData.append(imdb.movieHitCategory[0]);
		
		testData.append("<a href=\"/title/tt0496424/\" onclick=\"(new Image()).src='/rg/find-title-1/title_popular/images/b.gif?link=/title/tt0496424/';\">&#34;30 Rock&#34;</a> (2006) <small>(TV series)</small>     <div style=\"font-size: small\">&#160;&#45;&#160;Season 3, Episode 11: ");
		testData.append("<a href=\"/title/tt0074853/\">The Man in the Iron Mask</a> (1977) (TV)</td></tr>");
		
		testData.append(imdb.movieHitCategory[1]);
		
		testData.append("<a href=\"/title/tt0120744/\">The Man in the Iron Mask</a> (1998/I)</td></tr>");
		testData.append("<a href=\"/title/tt0103064/\">Terminator 2: Judgment Day</a> (1991)<br>&#160;aka <em>\"Terminator 2 - Le jugement dernier\"</em> - France<br>&#160;aka <em>\"T2 - Terminator 2: Judgment Day\"</em></td></tr>");
	
		testData.append(imdb.movieHitCategory[2]);
		
		testData.append("<a href=\"/title/tt0822832/\" onclick=\"(new Image()).src='/rg/find-title-1/title_popular/images/b.gif?link=/title/tt0822832/';\">Marley &#x26; Me</a> (2008)     </td></tr></table>");
		testData.append("<a href=\"/title/tt0960144/\" onclick=\"(new Image()).src='/rg/find-title-1/title_popular/images/b.gif?link=/title/tt0960144/';\">You Don&#x27;t Mess with the Zohan</a> (2008)     </td></tr></table>");
		
		// Should NOT match because of (VG) which means Video Game
		testData.append("<a href=\"/title/tt0311669/\" onclick=\"(new Image()).src='/rg/find-title-2/title_exact/images/b.gif?link=/title/tt0311669/';\">Predator</a> (1989) (VG)     </td></tr></table>");
		
		DefaultListModel matches = imdb.getMatches(testData);
		
		assertTrue("The number of results does not match the expected", matches.getSize() ==  6);
		

		for (int i = 0; i < matches.getSize(); i++)
			System.out.println("matches.get("+i+"):" + matches.get(i));

	}
}
