package edu.ucsf.rbvi.bioCycApp.internal.tasks;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.bioCycApp.internal.model.BioCycManager;

public class SearchPathwaysTaskFactory extends AbstractTaskFactory {
	BioCycManager manager;
	String database = null;
	String searchText = null;

	public SearchPathwaysTaskFactory (BioCycManager manager) {
		this.manager = manager;
	}

	public SearchPathwaysTaskFactory (BioCycManager manager, String database, String searchText) {
		this.manager = manager;
		this.database = database;
		this.searchText = searchText;
	}

	public TaskIterator createTaskIterator() {
		if (searchText == null)
			return new TaskIterator(new SearchPathwaysTask(manager));

		return new TaskIterator(new SearchPathwaysTask(manager, database, searchText));
	}
}
