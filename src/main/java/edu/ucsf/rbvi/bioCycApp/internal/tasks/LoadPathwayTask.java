package edu.ucsf.rbvi.bioCycApp.internal.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.bioCycApp.internal.model.BioCycManager;
import edu.ucsf.rbvi.bioCycApp.internal.model.Database;
import edu.ucsf.rbvi.bioCycApp.internal.model.Pathway;


public class LoadPathwayTask extends AbstractTask {
	BioCycManager manager;

	@Tunable (description="Pathway", context="nogui", required=true)
	public String pathwayName;

	@Tunable (description="Database OrgID", context="nogui")
	public String database = null;


	public LoadPathwayTask (BioCycManager manager) {
		this.manager = manager;
	}

	public LoadPathwayTask (BioCycManager manager, Pathway pathway) {
		this.manager = manager;
		this.pathwayName = pathway.getFrameID();
		this.database = pathway.getOrgID();
	}

	public void run(TaskMonitor monitor) {
		Database db = manager.getDefaultDatabase();
		if (database != null) {
			db = manager.getDatabase(database);
			if (db == null) {
				monitor.showMessage(TaskMonitor.Level.ERROR, "Can't find database "+database);
				return;
			}
		}
		try {
			manager.loadPathway(database, pathwayName);
		} catch (Exception e) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Failed to load "+pathwayName+": "+e.getMessage());
			return;
		}
		monitor.showMessage(TaskMonitor.Level.INFO, "Loaded "+pathwayName);
	}

	@ProvidesTitle
	public String getTitle() {
		return "Getting pathway: "+pathwayName;
	}
	
}
