package net.sf.xmm.moviemanager.extentions;

import java.io.*;
import java.net.URL;
import java.awt.datatransfer.*;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;

public class CoverTransferHandler extends TransferHandler {
   static Logger log = Logger.getRootLogger();

   private ModelMovieInfo movieInfo;
   
   public CoverTransferHandler(ModelMovieInfo info) {
       this.movieInfo = info;
   }

   
  /*
    * (non-Javadoc)
    * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
    */
   public boolean importData(JComponent c, Transferable t) {

      if (!canImport(c, t.getTransferDataFlavors())) {
           return false;
       }
       try {
           if (hasFileFlavor(t.getTransferDataFlavors())) {

               java.util.List files = (java.util.List) t.getTransferData(DataFlavor.javaFileListFlavor);
               for (int i = 0; i < files.size(); i++) {
                   File file = (File) files.get(i);
                   return importCover(file.toURL());
               }
               return true;
           } else if (hasStringFlavor(t.getTransferDataFlavors())) {
               String file = (String) t.getTransferData(DataFlavor.stringFlavor);
               return importCover(new URL(file));

           }
       } catch (UnsupportedFlavorException ufe) {
           log.debug("could not import data: " + ufe);
           return false;
       } catch (IOException e) {
           log.debug("could not import data: " + e);
           return false;
       }
       return false;
   }

   /*
    * Imports the Cover from the specified url, returns true if import succeeded
    */
   private boolean importCover(URL url) {

       try {
           /* Loads the image... */
           InputStream inputStream = url.openStream();
           byte[] buf = new byte[4 * 1024];
           ByteArrayOutputStream data = new ByteArrayOutputStream();
           int pos;
           while ((pos = inputStream.read(buf)) != -1) {
               data.write(buf, 0, pos);
           }
           byte[] _coverData = data.toByteArray();
           inputStream.read(_coverData);
           String coverName = (new File(url.getPath()).getName());
           /* Get image format
             ((ImageReader) ImageIO.getImageReaders(
             ImageIO.createImageInputStream(new ByteArrayInputStream(_coverData))).next())
             .getFormatName();
            */
           inputStream.close();
           ImageIcon image = new ImageIcon(_coverData);
           if (image.getIconHeight() > 1 && image.getIconWidth() > 1) {
               movieInfo.setCover(coverName, _coverData);
               /*
               movieInfo.getCover().setIcon(
                       new ImageIcon(Toolkit.getDefaultToolkit().createImage(_coverData).getScaledInstance(97, 145,
                               Image.SCALE_FAST)));
               */
               
               movieInfo.setSaveCover(true);
               return true;

           } else {
               log.debug("could not import image: " + url);
               return false;
           }

       } catch (FileNotFoundException e) {
           log.debug("could not import image: " + e);
       } catch (IOException e) {
           log.debug("could not import image: " + e);
       }
       return false;
   }

   /*
    * (non-Javadoc)
    * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
    */
   public int getSourceActions(JComponent c) {
       return COPY;
   }
   
   /*
    * (non-Javadoc)
    * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
    */
   public boolean canImport(JComponent c, DataFlavor[] flavors) {
       if (hasFileFlavor(flavors)) {
           return true;
       }
       if (hasStringFlavor(flavors)) {
           return true;
       }
       return false;
   }
   
   private boolean hasFileFlavor(DataFlavor[] flavors) {
       for (int i = 0; i < flavors.length; i++) {
           if (DataFlavor.javaFileListFlavor.equals(flavors[i])) {
               return true;
           }
       }
       return false;
  }

   private boolean hasStringFlavor(DataFlavor[] flavors) {
       for (int i = 0; i < flavors.length; i++) {
           if (DataFlavor.stringFlavor.equals(flavors[i])) {
               return true;
           }
       }
       return false;
   }
}