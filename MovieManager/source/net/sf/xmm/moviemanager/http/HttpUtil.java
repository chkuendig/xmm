/**
 * @(#)HttpUtil.java 1.0 29.01.06 (dd.mm.yy)
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.StringUtil;
import net.sf.xmm.moviemanager.util.SysUtil;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.log4j.Logger;

public class HttpUtil {

	static Logger log = Logger.getRootLogger();

	public boolean imdbAuthenticationSetUp = false;
	public boolean setUp = false;
	
	private HttpClient client = null;
	//static SimpleHttpConnectionManager connectionManager = null;
	//static MultiThreadedHttpConnectionManager connectionManager = null;
	
	static HttpConnectionManagerParams params = null;
	private HttpSettings httpSettings = new HttpSettings();
		
	public HttpUtil() {setup();}
		
	public HttpUtil(HttpSettings httpSettings) {
		
		if (httpSettings != null)
			this.httpSettings = httpSettings;
		
		setup();
		setProxySettings();
		
		setUpIMDbAuthentication();			
	}
	
	
	public boolean isSetup() {
		return setUp;
	}
	
	
	public void setup() {
		client = new HttpClient(new MultiThreadedHttpConnectionManager());
		client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
		setUp = true;
	}
	
	
	public boolean isIMDbAuthSetup() {
		return imdbAuthenticationSetUp;
	}
	
	public boolean setUpIMDbAuthentication() {
	
		if (httpSettings == null) {
			log.warn("Authentication could not be set. Missing authentication settings.");
			return false;
		}

		if (httpSettings.getIMDbAuthenticationEnabled()) {

			try {

				if (!isSetup())
					setup();

				client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

				PostMethod postMethod = new PostMethod(("https://secure.imdb.com/register-imdb/login")); 

				NameValuePair[] postData = new NameValuePair[2];
				postData[0] = new NameValuePair("login", httpSettings.getIMDbAuthenticationUser());
				postData[1] = new NameValuePair("password", httpSettings.getIMDbAuthenticationPassword());

				postMethod.setRequestBody(postData);
				
				int statusCode = client.executeMethod(postMethod);

				 if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) 
					 imdbAuthenticationSetUp = true;
				 else
					 imdbAuthenticationSetUp = false;
 
			} catch (Exception e) {
				log.warn("error:" + e.getMessage());
			}
		}
		else
			imdbAuthenticationSetUp = false;

		return imdbAuthenticationSetUp;
	}
	
	
	
	protected void setProxySettings() {

		if (httpSettings.getProxyEnabled()) {

			Credentials defaultcreds = null;

			if (httpSettings.getProxyAuthenticationEnabled()) {
				log.debug("user:" + httpSettings.getProxyUser());
				log.debug("pass:" + httpSettings.getProxyPassword());
				defaultcreds = new UsernamePasswordCredentials(httpSettings.getProxyUser(), httpSettings.getProxyPassword());
			}

			client.getHostConfiguration().setProxy(httpSettings.getProxyHost(), Integer.parseInt(httpSettings.getProxyPort()));
			client.getState().setProxyCredentials(new AuthScope(httpSettings.getProxyHost(), Integer.parseInt(httpSettings.getProxyPort()), AuthScope.ANY_REALM), defaultcreds);
		}
	}


	
	public StringBuffer readDataToStringBuffer(URL url) throws Exception {

		StringBuffer data = null;
		
		if (!isSetup())
			setup();
		
		GetMethod method = new GetMethod(url.toString());
		int statusCode = client.executeMethod(method);
		
		if (statusCode != HttpStatus.SC_OK)
			log.debug("statusCode HttpStatus.SC_OK:" + (statusCode != HttpStatus.SC_OK));
		
		if (statusCode == HttpStatus.SC_OK) {
			
			data = new StringBuffer();
			BufferedInputStream stream = new BufferedInputStream(method.getResponseBodyAsStream());
			
			// Saves the page data in a string buffer... 
			int buffer;
			
			while ((buffer = stream.read()) != -1) {
				data.append((char) buffer);
			}
			stream.close();
		}
		
		return data;
	}



	public byte [] readDataToByteArray(URL url) throws Exception {
	
		byte[] data = {-1};

		if (!isSetup())
			setup();
		
		GetMethod method = new GetMethod(url.toString());
	
		try {
			int statusCode = client.executeMethod(method);
						
			if (statusCode == HttpStatus.SC_OK) {

				BufferedInputStream  inputStream = new BufferedInputStream(method.getResponseBodyAsStream());
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream(inputStream.available());

				int buffer;
				while ((buffer = inputStream.read()) != -1)
					byteStream.write(buffer);

				inputStream.close();
				data = byteStream.toByteArray();
			}
			else {
				log.warn("HttpStatus statusCode:" + statusCode);
				log.warn("HttpStatus.SC_OK:" + HttpStatus.SC_OK);
			}

		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
			throw new Exception(e.getMessage());
		} finally {
			method.releaseConnection();
		}

		return data;
	}


	

	/**
	 * Decodes a html string and returns its unicode string.
	 **/
	public static String decodeHTML(String toDecode) {
		String decoded = "";

		try {
			int end = 0;
			for (int i=0; i < toDecode.length(); i++) {
				if (toDecode.charAt(i)=='&' && toDecode.charAt(i+1)=='#' && (end=toDecode.indexOf(";", i))!=-1) {
					decoded += (char)Integer.parseInt(toDecode.substring(i+2,end));
					i = end;
				} else if (toDecode.charAt(i)=='<' && toDecode.indexOf('>', i) != -1) {
					i = toDecode.indexOf('>', i);
				} else {
					decoded += toDecode.charAt(i);
				}
			}

			// replacing html code like &quot; and &amp;
			decoded = decoded.replaceAll("&amp;", "&");
			decoded = decoded.replaceAll("&quot;", "\"");
			decoded = decoded.replaceAll("&nbsp;", " ");

		} catch (Exception e) {
			log.error("", e);
		} 

		/* Returns the decoded string... */
		return StringUtil.removeDoubleSpace(decoded);
	}


	/**
	 * Decodes a html string 
	 **/
	public static Object [] decodeHTMLtoArray(String toDecode) {
		ArrayList decoded = new ArrayList();
		String tmp = "";

		try {
			int end = 0;
			for (int i=0; i < toDecode.length(); i++) {
				if (toDecode.charAt(i)=='&' && toDecode.charAt(i+1)=='#' && (end=toDecode.indexOf(";", i)) != -1) {
					tmp += (char) Integer.parseInt(toDecode.substring(i+2,end));
					i = end;
				} else if (toDecode.charAt(i)=='<' && toDecode.indexOf('>', i) != -1) {
					i = toDecode.indexOf('>', i);

					if (!tmp.trim().equals(""))
						decoded.add(tmp.trim());

					tmp = "";
				} else {
					tmp += toDecode.charAt(i);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		} 
		/* Returns the decoded string... */
		return decoded.toArray();
	}

	 public static StringBuffer getHtmlNiceFormat(StringBuffer buffer) {

		 int index = 0;

//		 Format html
		 Pattern p = Pattern.compile("</.+?>");
		 Matcher m = p.matcher(buffer);

		 while (m.find(index)) {

			 index = m.start();

			 int index2 = buffer.indexOf(">", index) + 1;

			 buffer.insert(index2, SysUtil.getLineSeparator());
			 index++;
		 }
		 return buffer;
	 }
}
