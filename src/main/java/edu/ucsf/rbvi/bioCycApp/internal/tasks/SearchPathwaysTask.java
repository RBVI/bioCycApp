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


public class SearchPathwaysTask extends AbstractTask implements ObservableTask {
	BioCycManager manager;
	List<Pathway> pathwayList;

	@Tunable (description="Pathway, gene, or protein string", context="nogui", required=true)
	public String search = null;

	@Tunable (description="Database OrgID", context="nogui")
	public String database = null;


	public SearchPathwaysTask (BioCycManager manager) {
		this.manager = manager;
	}

	public SearchPathwaysTask (BioCycManager manager, String database, String search) {
		this.manager = manager;
		this.database = database;
		this.search = search;
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
		pathwayList = manager.searchPathways(database, search);
		monitor.showMessage(TaskMonitor.Level.INFO, "Found "+pathwayList.size()+" pathways");
	}

	@ProvidesTitle
	public String getTitle() {
		return "Searching "+database+" for "+search;
	}

  public <R> R getResults(Class<? extends R> type) {
		if (type.equals(String.class)) {
			return (R)pathwayList.toString();
		} else
			return (R)pathwayList;
  }
	
}
