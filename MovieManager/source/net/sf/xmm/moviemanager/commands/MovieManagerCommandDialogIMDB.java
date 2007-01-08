package net.sf.xmm.moviemanager.commands;

import net.sf.xmm.moviemanager.DialogIMDB;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.util.ShowGUI;

public class MovieManagerCommandDialogIMDB {

    public static synchronized String getIMDBKey(String movieTitle) {
      
      ModelMovieInfo modelInfo = new ModelMovieInfo(false, true);
      modelInfo.model.setTitle(movieTitle);
      
      DialogIMDB dialogImdb = new DialogIMDB(modelInfo, true);
      ShowGUI.showAndWait(dialogImdb, true);
      
      return modelInfo.model.getUrlKey();
    }
 }
