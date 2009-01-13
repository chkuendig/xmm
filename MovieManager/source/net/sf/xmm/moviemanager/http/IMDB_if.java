package net.sf.xmm.moviemanager.http;

import javax.swing.DefaultListModel;

import net.sf.xmm.moviemanager.models.ModelIMDbSearchHit;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbEntry;


public abstract class IMDB_if {


	public abstract IMDB getIMDB(String urlID) throws Exception;
	public abstract IMDB getIMDB(String urlID, StringBuffer data) throws Exception;
	public abstract IMDB getIMDB(String urlID, HttpSettings settings) throws Exception;
	public abstract IMDB getIMDB(HttpSettings settings) throws Exception;
	public abstract ModelIMDbEntry grabInfo(String urlID) throws Exception;
	public abstract ModelIMDbEntry grabInfo(String urlID, StringBuffer data) throws Exception;
	public abstract DefaultListModel getSimpleMatches(String title);
	public abstract StringBuffer getEpisodesStream(ModelIMDbSearchHit modelSeason);
	public static DefaultListModel getEpisodes(ModelIMDbSearchHit modelSeason, StringBuffer stream) {return null;}
	public abstract DefaultListModel getSeasons(ModelIMDbSearchHit modelSeries);
	public abstract ModelIMDbEntry getEpisodeInfo(ModelIMDbSearchHit episode) throws Exception;
	public abstract DefaultListModel getSeriesMatches(String title);

	public abstract String getUrlID();
	public abstract String getDate();
	public abstract String getIMDbTitle();
	public abstract String getCorrectedTitle(String title);
	public abstract String getDirectedBy();
	public abstract String getWrittenBy();
	public abstract String getGenre();
	public abstract String getRating();
	public abstract String getColour();
	public abstract String getCountry();
	public abstract String getLanguage();
	public abstract String getPlot();
	public abstract String getCast();
	public abstract String getAka();
	public abstract String getMpaa();
	public abstract String getSoundMix();
	public abstract String getRuntime();
	public abstract String getCertification();
	public abstract String getAwards();
	public abstract String getCoverName();
	public abstract String getCoverURL();
	public abstract byte [] getCover();

	public abstract boolean getCoverOK();
	
}
