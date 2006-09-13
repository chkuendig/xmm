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

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.MovieManagerConfig;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Authenticator;
import java.net.URL;

class HttpUtil {
    
    static Logger log = Logger.getRootLogger();
    
    static StringBuffer readDataToStringBuffer(URL url) throws Exception {
	
	StringBuffer data = null;
	BufferedInputStream stream;
	
	//long time = System.currentTimeMillis();
	
	SimpleHttpConnectionManager connectionManager = new SimpleHttpConnectionManager();
	
	HttpConnectionManagerParams params = connectionManager.getParams();
	
	//params.setSoTimeout(2000);
	params.setConnectionTimeout(8000);
	
	connectionManager.setParams(params);
	
	HttpClient client = new HttpClient(connectionManager);
	
	setProxySetting(client);
	
	GetMethod method = new GetMethod(url.toString());
	//method.setDoAuthentication(true);
	
	int statusCode = client.executeMethod(method);
	
	if (statusCode == HttpStatus.SC_OK) {
	    
	    stream = new BufferedInputStream(method.getResponseBodyAsStream());
	    int buffer;
	    
	    /* Saves the page data in a string buffer... */
	    data = new StringBuffer();
	    
	    while ((buffer = stream.read()) != -1) {
		data.append((char) buffer);
	    }
	    
	    stream.close();
	}
	return data;
    }

    
    
    static byte [] readDataToByteArray(URL url) throws Exception {
	
	byte[] data = {-1};
	
	//long time = System.currentTimeMillis();
	
	SimpleHttpConnectionManager connectionManager = new SimpleHttpConnectionManager();
	
	HttpConnectionManagerParams params = connectionManager.getParams();
	params.setConnectionTimeout(2000);
	connectionManager.setParams(params);
	
	HttpClient client = new HttpClient(connectionManager);
	//HostConfiguration config = new HostConfiguration();
	setProxySetting(client);

	GetMethod method = new GetMethod(url.toString());
	//method.setDoAuthentication(true);
	
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
	    
	} catch (Exception e) {
	    throw new Exception(e.getMessage());
	} finally {
	    method.releaseConnection();
	}
	
	return data;
    }
    
    
    static void setProxySetting(HttpClient client) {
	
	MovieManagerConfig mmc = MovieManager.getConfig();
	
	
	if (mmc.getProxyEnabled()) {
	    
	    //HostConfiguration config = client.getHostConfiguration();
	    Credentials defaultcreds = null;
	    
	    /*Adds proxy settings*/
	    java.util.Properties systemSettings = System.getProperties();
	    
	    if (mmc.getProxyType().equals("HTTP")) {
		systemSettings.put("proxySet", "true");
		systemSettings.put("socksProxySet", "false");
		
		/* Removing SOCKS properties */
		systemSettings.remove("socksProxyHost");
		systemSettings.remove("socksProxyPort");
		
		Authenticator.setDefault(null);
		
		if (mmc.getAuthenticationEnabled()) {
		    log.debug("user:" + mmc.getProxyUser());
		    log.debug("pass:" + mmc.getProxyPassword());
		    defaultcreds = new UsernamePasswordCredentials(mmc.getProxyUser(), mmc.getProxyPassword());
		}
		
		client.getHostConfiguration().setProxy(mmc.getProxyHost(), Integer.parseInt(mmc.getProxyPort()));
		
		client.getState().setProxyCredentials(new AuthScope(mmc.getProxyHost(), Integer.parseInt(mmc.getProxyPort()), AuthScope.ANY_REALM), defaultcreds);
	    }
	    else {
		
		systemSettings.put("socksProxySet", "true");
		systemSettings.put("proxySet", "false");
		
		systemSettings.put("socksProxyHost", mmc.getProxyHost());
		systemSettings.put("socksProxyPort", mmc.getProxyPort());
		 
		 if (mmc.getAuthenticationEnabled()) {
		     /*Adds authentication*/
		     Authenticator.setDefault(new MyAuth(mmc.getProxyUser(), mmc.getProxyPassword()));
		 }
		 else /*Removes authentication*/
		     Authenticator.setDefault(null);
	    }
	    
	    /*Saves proxy settings*/
	    System.setProperties(systemSettings);
	}
    }

    
    public static void writeToFile(String fileName, StringBuffer data) {
	
	try {
	    
	    FileOutputStream fileStream = new FileOutputStream(new File(fileName));
	    for (int u = 0; u < data.length(); u++)
		fileStream.write(data.charAt(u));
	    fileStream.close();
	
	} catch (Exception e) {
	    log.error("Exception:"+ e.getMessage());
	}
    }
    
    /**
     * Decodes a html string and returns its unicode string.
     **/
    protected static String decodeHTML(String toDecode) {
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
	} catch (Exception e) {
	    log.error("Exception: " + e);
	} 
	/* Returns the decoded string... */
	return decoded;
    }

    /* Creates the URL and sets the appropriate proxy values */
    //protected static URL makeURL(String url) {
    static void setProxySetting2(HttpClient client) {
	
	//url = makeURL(urlType+title.replaceAll("[\\p{Blank}]+","%20"));
	
	MovieManagerConfig mm = MovieManager.getConfig();
	//URL urlData = null;
	
	try {
	    if (mm.getProxyEnabled()) {
		
		String host = mm.getProxyHost();
		String port = mm.getProxyPort();
		
		/*Adds proxy settings*/
		java.util.Properties systemSettings = System.getProperties();
		
		if (mm.equals("HTTP")) {
		    systemSettings.put("proxySet", "true");
		    systemSettings.put("proxyHost", host);
		    systemSettings.put("proxyPort", port);
		}
		else {
		    systemSettings.put("socksProxySet", "true");
		    systemSettings.put("socksProxyHost", host);
		    systemSettings.put("socksProxyPort", port);
		}
		
		/*Saves proxy settings*/
		System.setProperties(systemSettings);
		
		if (mm.getAuthenticationEnabled()) {
		    String user = mm.getProxyUser();
		    String password = mm.getProxyPassword();
		    
		    /*Adds authentication*/
		    Authenticator.setDefault(new MyAuth(user, password));
		}
		else
		    Authenticator.setDefault(null);/*Removes authentication*/
	    }
	    else {
		/*Removes proxy settings*/
		java.util.Properties systemSettings = System.getProperties();
		systemSettings.remove("proxySet");
		systemSettings.remove("proxyHost"); 
		systemSettings.remove("proxyPort");
		
		systemSettings.remove("socksProxySet");
		systemSettings.remove("socksProxyHost"); 
		systemSettings.remove("socksProxyPort");
		System.setProperties(systemSettings);
		
		/*Removes authentication*/
		Authenticator.setDefault(null);
	    }
	    //urlData = new URL(url);
	}
	catch (Exception e) {
	    log.error("Exception: " + e);
	}
    }
}
