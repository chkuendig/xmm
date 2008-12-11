package net.sf.xmm.moviemanager.commands.guistarters;

import net.sf.xmm.moviemanager.gui.DialogIMDB;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.util.GUIUtil;

public class MovieManagerCommandDialogIMDB {

	public boolean cancel = false;
	public boolean cancelAll = false;

	public synchronized String getIMDBKey(String movieTitle) {

		if (cancelAll)
			return null;

		ModelMovieInfo modelInfo = new ModelMovieInfo(false, true);
		modelInfo.model.setTitle(movieTitle);

		DialogIMDB dialogImdb = new DialogIMDB(modelInfo.model, true, movieTitle);
		GUIUtil.showAndWait(dialogImdb, true);

		cancel = dialogImdb.cancelSet;
		cancelAll = dialogImdb.cancelAllSet;

		return modelInfo.model.getUrlKey();
	}
}
