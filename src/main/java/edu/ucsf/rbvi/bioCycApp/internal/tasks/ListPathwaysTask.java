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
import edu.ucsf.rbvi.bioCycApp.internal.model.Pathway;


public class ListPathwaysTask extends AbstractTask implements ObservableTask {
	BioCycManager manager;
	List<Pathway> pathwayList;

	@Tunable (description="Pathway name", context="nogui")
	public String pathway = null;

	@Tunable (description="Database OrgID", context="nogui")
	public String database = null;


	public ListPathwaysTask (BioCycManager manager) {
		this.manager = manager;
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
		pathwayList = manager.getPathways(database, pathway);
	}

  public <R> R getResults(Class<? extends R> type) {
		if (type.equals(String.class)) {
			return (R)pathwayList.toString();
		} else
			return (R)pathwayList;
  }
	
}
