package net.sf.xmm.moviemanager.util;

import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.swing.filechooser.FileSystemView;


import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogMovieManager;
import net.sf.xmm.moviemanager.util.plugins.MovieManagerConfigHandler;

import org.apache.log4j.Logger;

//import xeus.jcl.JarClassLoader;
// import xeus.jcl.JclObjectFactory;

public class SysUtil {

	static Logger log = Logger.getLogger(SysUtil.class);

	/**
	 * Getting the 'root directory' of the app.
	 **/
	public static String getUserDir() {
		
		String path = ""; //$NON-NLS-1$

		try {
			java.net.URL url = FileUtil.class.getProtectionDomain().getCodeSource().getLocation();
			
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
	 * 
	 * @throws Exception 
	 **/
	public static File getConfigDir() throws Exception {
		
		File dir = null;
		
		try {
			
			/* If running in a mac application bundle, we can't write in the application-directory, so we use the /Library/Application Support */
			if (isMac()) {
				String path = System.getProperty("user.home") + "/Library/Application Support/MovieManager/";
				dir = new File(path);
			}
			else if (isWindowsVista() || isWindows7()) {
				
				String path = System.getenv("APPDATA");
				
				if (path == null)
					path = System.getProperty("user.home") + "/" + "Application Data";
					
				dir = new File(path, "MovieManager");
			}
	
			if (dir != null) {
								
				if(!dir.exists() && !dir.mkdir()) {
					log.error("Could not create settings folder.");
					throw new Exception("Could not create settings folder:" + dir);
				}
			}
			else
				dir = new File(getUserDir(), "config");
						
		}
		catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}

		return dir;
	}
	
	
	public static URL getConfigURL() {

		URL url = null;
		
		try {
			int appMode = MovieManager.getAppMode();

			// Applet
			if (appMode == 1)
				url = FileUtil.getFileURL("config/Config_Applet.ini", DialogMovieManager.applet);
			else if (appMode == 2) { // Java Web Start
				MovieManagerConfigHandler configHandler = MovieManager.getConfig().getConfigHandler();

				if (configHandler != null)
					url = configHandler.getConfigURL();

			} else {

				String conf = "Config.ini";

				if (SysUtil.isMac())
					url = new File(SysUtil.getConfigDir(), conf).toURL();
				else {

					boolean checkOldInstallDir = true;
					long t = FileUtil.getFile(conf).lastModified();

					// The newest config file will be used
					if (FileUtil.getFile("config/" + conf).lastModified() > t)
						conf = "config/" + conf;

					// Change default location on Vista from program directory to System.getenv("APPDATA")
					if (SysUtil.isWindowsVista() || SysUtil.isWindows7()) {
						File newConfig = new File(SysUtil.getConfigDir(), "Config.ini");

						if (newConfig.isFile()) {
							url = newConfig.toURL();
							checkOldInstallDir = false;
						}
						else 
							url = FileUtil.getFileURL(conf);
					}
					else {
						url = FileUtil.getFileURL(conf);

						if (new File(url.toString()).isFile())
							checkOldInstallDir = false;
					}
					// changed default install directory in cross-platform installer from "MeD's Movie Manager" to "MeDs-Movie-Manager"
					if (checkOldInstallDir) {

						File userDir = new File(getUserDir());

						if (userDir.getName().equals("MeDs-Movie-Manager")) {

							// Check old install dir
							File oldInstallDir = new File(userDir.getParentFile(), "MeD's Movie Manager");
							File tmpConfig = new File(oldInstallDir, "config/Config.ini");

							if (tmpConfig.isFile()) {
								url = tmpConfig.toURL();
							}
							else if ((tmpConfig = new File(oldInstallDir, "Config.ini")).isFile()) {
								url = tmpConfig.toURL();
							}						
						}
					}
				}
			}
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
		return url;
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
				Class<?> classForName = Class.forName(className);
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
		return os != null && os.toLowerCase().startsWith("mac"); //$NON-NLS-1$
	}

	public static boolean isOSX() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return os != null && os.toLowerCase().startsWith("Mac OS X"); //$NON-NLS-1$
	}

	public static boolean isLinux() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return os != null && os.toLowerCase().startsWith("linux"); //$NON-NLS-1$
	}

	public static boolean isSolaris() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return os != null && (os.toLowerCase().startsWith("sunos") || os.toLowerCase().startsWith("solaris")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static boolean isWindows() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return os != null && os.toLowerCase().startsWith("windows"); //$NON-NLS-1$
	}

	public static boolean isWindows98() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return os != null && os.toLowerCase().startsWith("Windows 98"); //$NON-NLS-1$
	}
	
	public static boolean isWindowsXP() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		String osVersion = System.getProperty("os.version"); //$NON-NLS-1$
			
		return os != null && osVersion != null
			&& os.toLowerCase().indexOf("windows") != -1 &&
			osVersion.equals("5.1"); //$NON-NLS-1$
	}

	/**
	 * Bug in Java (not yet fixed in 1.6.0_13) causing System.getProperty("os.name")
	 * to return "Windows XP" on Windows Vista.
	 * System.getProperty("os.version") returns "6.0" on Windows Vista and "5.1" on XP
	 * @return
	 */
	public static boolean isWindowsVista() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		String osVersion = System.getProperty("os.version"); //$NON-NLS-1$
			
		return os != null && osVersion != null
			&& os.toLowerCase().indexOf("windows") != -1 &&
			osVersion.equals("6.0"); //$NON-NLS-1$
	}

	/**
	 * Bug in Java (not yet fixed in 1.6.0_13) causing System.getProperty("os.name")
	 * to return "Windows Vista" on Windows 7.
	 * System.getProperty("os.version") returns "6.1" on Windows 7 and "6.0" on Vista
	 * @return
	 */
	public static boolean isWindows7() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		String osVersion = System.getProperty("os.version"); //$NON-NLS-1$
			
		return os != null && osVersion != null
			&& os.toLowerCase().indexOf("windows") != -1 &&
			osVersion.equals("6.1"); //$NON-NLS-1$
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
    
    
    public static String getSystemInfo(String separator) {
    
    	String sep = separator == null ? "\\n" : separator;
    	
    	int freeMemory = (int) Runtime.getRuntime().freeMemory()/1024/1024;
    	int totalMemory = (int) Runtime.getRuntime().totalMemory()/1024/1024;
    	int maxMemory = (int) Runtime.getRuntime().maxMemory()/1024/1024;
    	
    	return "OS: " + System.getProperty("os.name") + 
    		   "  version: " + System.getProperty("os.version") + 
    		   "  Architecture: " + System.getProperty("os.arch") + 
    		   sep +
    		   "Java version: " + System.getProperty("java.runtime.version") +  
    		   "  Vendor:" + System.getProperty("java.vm.specification.vendor") + 
    		   sep + 
    		   "Free VM memory: " + freeMemory + " MB, " + "Total VM memory: " + totalMemory + " MB" + sep +
    		   "Max VM memory: " + maxMemory + " MB" ;
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
    	
    	if (imdb.isFile()) {
	
    		try {
    		
    			System.out.println("new IMDB().getClass().getClassLoader():" + new IMDB().getClass().getClassLoader());
    			
    			File f = imdb;
    		
    			
    			
    			URLClassLoader ucl = new URLClassLoader(new URL[]{f.toURL()}, IMDB.class.getClass().getClassLoader());
    			ZipClassLoader zl = new ZipClassLoader(ucl, f);
    			
    			//Class c = zl.loadClass("net.sf.xmm.moviemanager.http.IMDB", true);
    			Class c = zl.loadClass("net.sf.xmm.moviemanager.http.IMDB");
    			
    			
    			Object o = c.newInstance();
    			
    			//f.delete();
    			
    			System.out.println("getClass:" + o.getClass());
    			
    			System.out.println("getClass().getName():" + o.getClass().getName());
    			
    			return (IMDB_if) o;

    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    	}
    	
    	IMDB_if i = new IMDB();
    	
    	System.out.println("Returning default IMDB");
    	
    	System.out.println("getClass:" + i.getClass());
    	System.out.println("getClass().getName():" + i.getClass().getName());
    	
    	return i;
    }
    */
}
