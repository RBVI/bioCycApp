package edu.ucsf.rbvi.bioCycApp.internal.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.bioCycApp.internal.model.BioCycManager;
import edu.ucsf.rbvi.bioCycApp.internal.model.Database;


public class SetDatabaseTask extends AbstractTask {
	BioCycManager manager;

	@Tunable (description="Org ID of database", context="nogui")
	public String id = null;

	public SetDatabaseTask (BioCycManager manager) {
		this.manager = manager;
	}

	public void run(TaskMonitor monitor) {
		List<Database> databases = manager.getDatabases();
		for (Database d: databases) {
			if (d.getOrgID().equalsIgnoreCase(id)) {
				manager.setDefaultDatabase(d);
				monitor.setStatusMessage("Set default database to "+d);
				return;
			}
		}
		monitor.setStatusMessage("Can not find database: "+id);
	}

}
