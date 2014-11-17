package edu.ucsf.rbvi.bioCycApp.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.bioCycApp.internal.model.BioCycManager;

public class LoadDatabasesTask extends AbstractTask {
	BioCycManager manager;

	public LoadDatabasesTask (BioCycManager manager) {
		this.manager = manager;
	}

	public void run(TaskMonitor monitor) {
		manager.loadDatabases(false);
	}
	
}
