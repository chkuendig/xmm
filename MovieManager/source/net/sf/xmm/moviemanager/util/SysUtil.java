package net.sf.xmm.moviemanager.util;

import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.filechooser.FileSystemView;

import net.sf.xmm.moviemanager.http.IMDB;
import net.sf.xmm.moviemanager.util.plugins.MovieManagerLoginHandler;

import org.apache.log4j.Logger;

//import xeus.jcl.JarClassLoader;
// import xeus.jcl.JclObjectFactory;

public class SysUtil {

	static Logger log = Logger.getRootLogger();

	/**
	 * Getting the 'root directory' of the app.
	 **/
	public static String getUserDir() {
		
		String path = ""; //$NON-NLS-1$

		try {
			java.net.URL url = FileUtil.class.getProtectionDomain().getCodeSource().getLocation();
			
			//System.err.println("\n\n\ngetUserDir url:" + url);
			
			File file = new File(java.net.URLDecoder.decode(url.getPath(), "UTF-8")); //$NON-NLS-1$
		
			// If running in a jar file the parent is the root dir 
			if (file.isFile())
				path = file.getParentFile().getAbsolutePath();
			else
				path = file.getAbsolutePath();

		}
		catch (UnsupportedEncodingException e) {
			path = System.getProperty("user.dir"); //$NON-NLS-1$
		}

		if (!path.endsWith(getDirSeparator()))
			path += getDirSeparator();
		
		return path;
	}
	
	
	/**
	 * Getting the 'root directory' of the app.
	 **/
	public static String getConfigDir() {
		
		String path = ""; //$NON-NLS-1$

		try {
			java.net.URL url = FileUtil.class.getProtectionDomain().getCodeSource().getLocation();
			
			File file = new File(java.net.URLDecoder.decode(url.getPath(), "UTF-8")); //$NON-NLS-1$
			
			// If running in a jar file the parent is the root dir 
			if (file.isFile())
				path = file.getParentFile().getAbsolutePath();
			else
				path = file.getAbsolutePath();
	
			/* If running in a mac application bundle, we can't write in the application-directory, so we use the /Library/Application Support */
			if (isMac()) {
			
				path = System.getProperty("user.home") + "/Library/Application Support/MovieManager/";
				File dir = new File(path);
		
				if (!dir.exists()) {
					if(!dir.mkdir()) {
						log.error("Could not create settings folder.");
					}
				}
			}
		}
		catch (UnsupportedEncodingException e) {
			path = System.getProperty("user.dir"); //$NON-NLS-1$
		}

		if (!path.endsWith(getDirSeparator()))
			path += getDirSeparator();
		
		return path;
	}
	
	
	public static String getDriveDisplayName(File path) {

		FileSystemView fsv = new javax.swing.JFileChooser().getFileSystemView();

		if (fsv != null) {

			File tmp = path;

			while (tmp.getParentFile() != null)
				tmp = tmp.getParentFile();

			String displayName = fsv.getSystemDisplayName(tmp);

			if (!displayName.trim().equals(""))
				return displayName;

			return "";
		}

		return null;
	}

 
	public static Object getClass(String className) {
		
		if (className != null) {

			try {
				Class classForName = Class.forName(className);
				Object classInstance = classForName.newInstance();
				log.debug("Successfully loaded LoginHandler");
				return classInstance;

			} catch (ClassNotFoundException e) {
				log.error("ClassNotFoundException. Failed to load class " + className);
			} catch (IllegalAccessException e) {
				log.error("IllegalAccessException. Failed to load class " + className);
			} catch (InstantiationException e) {
				log.error("InstantiationException. Failed to load class " + className);
			}
		}
		return null;
	}
	
    
    /* Adds all the files ending in .jar to the classpath */
    public static void includeJarFilesInClasspath(String path) {
        
        URL url = FileUtil.getFileURL(path);
        
        if (url.toExternalForm().startsWith("http://")) //$NON-NLS-1$
            return;
        
        File dir = new File(url.getPath());
        
        try {
            File [] jarList = dir.listFiles();
            
            if (jarList != null) {
                
                String absolutePath = ""; //$NON-NLS-1$
                for (int i = 0; i < jarList.length; i++) {
                    
                    absolutePath = jarList[i].getAbsolutePath();
                     
                    if (absolutePath.endsWith(".jar")) { //$NON-NLS-1$
                    	net.sf.xmm.moviemanager.util.ClassPathHacker.addFile(absolutePath);
                        log.debug(absolutePath+ " added to classpath"); //$NON-NLS-1$
                    }
                }
            }
        }
        catch (Exception e) {
            log.error("Exception:" + e.getMessage()); //$NON-NLS-1$
        }
    }
    
    
    public static void cleaStreams(Process p) {
    	
    	/**
		 * Clears the streams to avoid having the subprocess hang
		 * @author Bro
		 */
		class StreamHandler extends Thread {

			InputStream inpStr;
			String strType;

			public StreamHandler(InputStream inpStr, String strType) {
				this.inpStr = inpStr;
				this.strType = strType;
			}

			public void run() {
				try {
					InputStreamReader inpStrd = new InputStreamReader(inpStr);
					BufferedReader buffRd = new BufferedReader(inpStrd);
					String line = null;
					String str = "";
					
					while((line = buffRd.readLine()) != null) {
						str += line;
					}
					
					log.debug(line);  
					buffRd.close();
				} catch (IOException e) {
					log.error("1Exception:" + e.getMessage(), e);  
				}
			}
		}
    	
		StreamHandler input = new StreamHandler(p.getInputStream(), "INPUT");
		StreamHandler err = new StreamHandler(p.getErrorStream(), "ERROR");

		input.start();
		err.start();
    }
	
	public static String getLineSeparator() {
		return System.getProperty("line.separator"); //$NON-NLS-1$
	}


	public static String getDirSeparator() {
		return File.separator;
	}

	public static boolean isCtrlPressed(InputEvent event) {
		return ((event.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK);
	}

	public static boolean isShiftPressed(InputEvent event) {
		return ((event.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK);
	}	

	public static boolean isMac() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return os != null && os.toLowerCase().startsWith("mac") ? true : false; //$NON-NLS-1$
	}

	public static boolean isOSX() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return os != null && os.toLowerCase().startsWith("Mac OS X") ? true : false; //$NON-NLS-1$
	}

	public static boolean isLinux() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return os != null && os.toLowerCase().startsWith("linux") ? true : false; //$NON-NLS-1$
	}

	public static boolean isSolaris() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return os != null && (os.toLowerCase().startsWith("sunos") || os.toLowerCase().startsWith("solaris")) ? true : false; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static boolean isWindows() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return os != null && os.toLowerCase().startsWith("windows") ? true : false; //$NON-NLS-1$
	}

	public static boolean isWindows98() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return os != null && os.toLowerCase().startsWith("Windows 98") ? true : false; //$NON-NLS-1$
	}

	public static boolean isWindowsVista() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return os != null && os.toLowerCase().indexOf("vista") != -1 ? true : false; //$NON-NLS-1$
	}

	public static void openFileLocationOnWindows(File file) {
		try {
			//Desktop.getDesktop().browse(file.toURI());
			Runtime runtime = Runtime.getRuntime();
			runtime.exec("explorer.exe /select,\"" + file.getAbsolutePath() );
		} catch(IOException ioe) {
			ioe.printStackTrace();
			//JOptionPane.showMessageDialog(null, "Could Not Open File Location: " + file.getAbsolutePath());
		}
	}

	  
    public static boolean isCurrentJRES14() {
    	
    	double javaVersion = Double.parseDouble(System.getProperty("java.version").substring(0, 3));
    		
    	if (javaVersion < 1.5) {
    		//log.error("Version:" + javaVersion + " is not supported. Must be 1.5 or higher.");
    		return true;
    	}
    	return false;
    }
    

    public static String getDefaultPlatformBrowser() {
        String browser = "";
        
        if (SysUtil.isWindows())
            browser = "Default";
        else if (SysUtil.isMac())
            browser = "Safari";
        else
            browser = "Firefox";
        
        return browser;
    }
    
    
    /*
    public static IMDB_if getIMDBInstance2() throws Exception {
    
    	JarClassLoader jcl = new JarClassLoader();
    	jcl.add(System.getProperty("user.dir") + "/MovieManager/lib/" + "IMDB.jar"); //Load jar file   
    	
    	JclObjectFactory factory = JclObjectFactory.getInstance();   
    	  
    	//Create object of loaded class   
    	Object obj = factory.create(jcl, "net.sf.xmm.moviemanager.http.IMDB_if");
    	
    	return (IMDB_if) obj;
    }
    
    public static IMDB_if getIMDBInstance() throws Exception {
    	
    	File imdb = new File(System.getProperty("user.dir") + "/MovieManager/lib/" + "IMDB.jar");
    	
    	System.err.println("imdb:" + imdb);
    	
    	if (imdb.isFile()) {

    		System.err.println("IMDB.jar found!");
    		
    		try {
    		
    			System.err.println("new IMDB().getClass().getClassLoader():" + new IMDB().getClass().getClassLoader());
    			
    			File f = imdb;
    		
    			
    			
    			URLClassLoader ucl = new URLClassLoader(new URL[]{f.toURL()}, IMDB.class.getClass().getClassLoader());
    			ZipClassLoader zl = new ZipClassLoader(ucl, f);
    			
    			//Class c = zl.loadClass("net.sf.xmm.moviemanager.http.IMDB", true);
    			Class c = zl.loadClass("net.sf.xmm.moviemanager.http.IMDB");
    			
    			
    			Object o = c.newInstance();
    			
    			//f.delete();
    			
    			System.err.println("getClass:" + o.getClass());
    			
    			System.err.println("getClass().getName():" + o.getClass().getName());
    			
    			return (IMDB_if) o;

    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    	}
    	
    	IMDB_if i = new IMDB();
    	
    	System.err.println("Returning default IMDB");
    	
    	System.err.println("getClass:" + i.getClass());
    	System.err.println("getClass().getName():" + i.getClass().getName());
    	
    	return i;
    }
    */
}
