package net.sf.xmm.moviemanager.util;

import java.util.zip.*;
import java.util.*;
import java.io.*;

import net.sf.xmm.moviemanager.http.IMDB;
 
public class ZipClassLoader extends ClassLoader {
	private Hashtable classes = new Hashtable();
	private File f;
 
	ClassLoader loader;
	
	/*
	public ZipClassLoader(String zipFileName) {
		this (new File(zipFileName));
	}
 */
	
	public ZipClassLoader(ClassLoader loader, File zipFile) {
		super(loader);
		System.err.println("ZipClassLoader.loader:" + loader);
		f = zipFile;
		//this.loader = loader;
	}
 
	public Class loadClass(String className) throws ClassNotFoundException {
		return (loadClass(className, true));
	}
 
	public synchronized Class loadClass(String className, boolean resolve) throws ClassNotFoundException {
		
		if (classes.containsKey(className))
			return (Class)classes.get(className);
		
		ZipFile zipFile = null;
		BufferedInputStream bis = null;
		byte[] res = null;
		try {
			zipFile = new ZipFile(f);
			ZipEntry zipEntry = zipFile.getEntry(className.replace('.', '/')+".class");
			res = new byte[(int)zipEntry.getSize()];
			bis = new BufferedInputStream(zipFile.getInputStream(zipEntry));
			bis.read(res, 0, res.length);
		} catch (Exception ex) {
		} finally {
			if (bis!=null) {
				try {
					bis.close();
				} catch (IOException ioex) {}
			}
			if (zipFile!=null) {
				try {
					zipFile.close();
				} catch (IOException ioex) {}
			}
		}
		
		if (res == null) {
			System.err.println("findSystemClass");
			return findSystemClass(className);
		}
 
		System.err.println("defineClass");
		Class clazz = defineClass(className, res, 0, res.length);
		
		if (clazz == null) 
			throw new ClassFormatError();
 
		System.err.println("resolveClass");
		
		if (resolve) resolveClass(clazz);
		classes.put(className, clazz);
		return(clazz);
	}
}
