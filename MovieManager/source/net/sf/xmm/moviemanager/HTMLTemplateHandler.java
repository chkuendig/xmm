package net.sf.xmm.moviemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.models.ModelHTMLTemplate;
import net.sf.xmm.moviemanager.models.ModelHTMLTemplateStyle;
import net.sf.xmm.moviemanager.util.FileUtil;

public class HTMLTemplateHandler {

	static Logger log = Logger.getLogger(HTMLTemplateHandler.class);
	
	public HashMap<String, ModelHTMLTemplate> htmlTemplates = new HashMap<String, ModelHTMLTemplate>();


	public ModelHTMLTemplate getTemplate(String name) {
		return (ModelHTMLTemplate) htmlTemplates.get(name);
	}


	public HashMap<String, ModelHTMLTemplate> getHTMLTemplates() {
		return htmlTemplates;
	}

	
	void loadHTMLTemplates() {

		if (MovieManager.getConfig().getInternalConfig().getDisableHTMLView())
			return;

		try {

			File f = FileUtil.getFile(MovieManager.getConfig().HTMLTemplateRootDir);

			if (f != null && f.isDirectory()) {

				File [] templateFiles = f.listFiles();

				for (int i = 0; i < templateFiles.length; i++) {

					try {
						// For each template directory
						if (templateFiles[i].isDirectory()) {

							// Finding template.txt
							File template = new File(templateFiles[i], "template.txt");

							if (!template.isFile()) {
								log.debug("No template.txt file found in the directory of template " + templateFiles[i] + 
								"\n Template not added.");
								continue;
							}

							ArrayList<String> lines = FileUtil.readFileToArrayList(template);

							if (lines == null) {
								log.error("Failed to read file "  + template);
								throw new Exception("Failed to read file "  + template);
							}

							ModelHTMLTemplate newTemplate = new ModelHTMLTemplate(templateFiles[i].getName(), lines);

							if (htmlTemplates.containsKey(newTemplate.getName())) {
								log.warn("A template named " + newTemplate.getName() + " already exists! \r\n" + 
										templateFiles[i] + " is not added.");
								continue;
							}
							htmlTemplates.put(newTemplate.getName(), newTemplate);

							// Getting the styles
							File styles = new File(templateFiles[i], "Styles");

							if (!styles.isDirectory()) {
								log.debug("No styles found for HTML template " + templateFiles[i] + 
								"\n No template styles added.");
							} else {

								File [] styleFiles = styles.listFiles();

								for (int u = 0; u < styleFiles.length; u++) {

									// Style files end with .style.txt
									if (!styleFiles[u].getName().endsWith(".style.txt"))
										continue;

									// Getting all all available styles for this template
									lines = FileUtil.readFileToArrayList(styleFiles[u]);
									ModelHTMLTemplateStyle style = new ModelHTMLTemplateStyle(newTemplate, lines);

									newTemplate.addStyle(style);
								}
							}
						}
					} catch (Exception e) {
						log.warn(e.getMessage()+ "\n Failed to import template " + templateFiles[i], e);
					}
				}
			}
		} catch (Exception e) {
			log.error("Failed to read HTML temlplate files.", e);
		}

		log.debug("Done loading HTML templates."); //$NON-NLS-1$
	}
}
