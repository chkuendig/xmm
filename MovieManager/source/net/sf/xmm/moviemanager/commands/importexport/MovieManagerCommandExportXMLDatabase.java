/**
 * @(#)MovieManagerCommandSaveChangedNotes.java 1.0 26.09.05 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.commands.importexport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.models.ModelExportXML;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelSeries;
import net.sf.xmm.moviemanager.util.FileUtil;

import org.apache.log4j.Logger;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;




public class MovieManagerCommandExportXMLDatabase extends MovieManagerCommandImportExportHandler {

	static Logger log = Logger.getRootLogger();

	File output;
	boolean cancelled = false;

	ArrayList movieList = null;

	ModelImportExportSettings importSettings;

	MovieManagerCommandExportXMLDatabase(ModelImportExportSettings importSettings) {
		this.importSettings = importSettings;
	}

	public void handleMovie(int i) {

	}

	public void execute() {

	}

	public boolean isCancelled() {
		return cancelled;
	}


	public void retrieveMovieList() throws Exception {

	}

	public String getTitle(int i) {

		modelMovieInfo.clearModel();

		String title = null;

		if (movieList.get(i) instanceof ModelMovie)
			title = ((ModelMovie) movieList.get(i)).getTitle();
		else if (movieList.get(i) instanceof ModelSeries) {
			title = ((ModelSeries) movieList.get(i)).getMovie().getTitle();
		}

		return title;
	}

	public int getMovieListSize() {

		if (movieList == null)
			return -1;

		return movieList.size();
	}

	public int addMovie(int i) {
		return 0;
	}


	public static void execute(String outputFile) {

		DefaultMutableTreeNode root = (DefaultMutableTreeNode) ((DefaultTreeModel) MovieManager.getDialog().getMoviesList().getModel()).getRoot();
		ModelEntry model;
		DefaultMutableTreeNode node;

		Enumeration enumeration = root.children();

		try {

			Mapping mapping = new Mapping(new ModelMovie().getClass().getClassLoader());

			URL mappingFile = FileUtil.getFileURL("config/mapping.xml");

			ModelExportXML exportXMLDatabase = new ModelExportXML(root.getChildCount());

			// 1. Load the mapping information from the file
			mapping.loadMapping(mappingFile);

			String encoding = "UTF-8";

			// 4. marshal the data with the total price back and print the XML in the console 
			Marshaller marshaller = new Marshaller(new OutputStreamWriter(new FileOutputStream(outputFile), encoding));
			marshaller.setEncoding(encoding);
			//marshaller.setMarshalAsDocument(true);
			marshaller.setMapping(mapping);

			while (enumeration.hasMoreElements()) {

				node = ((DefaultMutableTreeNode) enumeration.nextElement());
				model = (ModelEntry) node.getUserObject();

				if (!model.getHasGeneralInfoData()) {
					model.updateGeneralInfoData();
				}

				if (!model.getHasAdditionalInfoData()) {
					model.updateAdditionalInfoData();
				}

				/* Has no children */
				if (node.isLeaf()) {
					exportXMLDatabase.addModelMovie((ModelMovie) model);
				}
				else {
					ModelSeries serie = new ModelSeries((ModelMovie) model);

					Enumeration children = node.children();

					while (children.hasMoreElements()) {
						serie.addEpisode((ModelEpisode) ((DefaultMutableTreeNode) children.nextElement()).getUserObject());
					}

					exportXMLDatabase.addModelSerie(serie);
				}
			}

			marshaller.marshal(exportXMLDatabase);

		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
			return;
		}
	}
}
