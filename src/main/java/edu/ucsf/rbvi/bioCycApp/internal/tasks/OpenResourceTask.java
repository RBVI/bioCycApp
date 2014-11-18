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


public class OpenResourceTask extends AbstractTask implements ObservableTask {
	BioCycManager manager;
	List<Database> databases;

	@Tunable (description="URL of Pathway Tools resource", context="nogui", required=true)
	public String url = null;

	public OpenResourceTask (BioCycManager manager) {
		this.manager = manager;
	}

	public void run(TaskMonitor monitor) {
		manager.setURI(url);
		monitor.setStatusMessage("Changed resource to "+url);
		manager.loadDatabases(false);
		monitor.setStatusMessage("Found "+manager.getDatabases().size()+" databases");
	}

  public <R> R getResults(Class<? extends R> type) {
		if (type.equals(String.class)) {
			return (R)databases.toString();
		} else
			return (R)databases;
  }
	
}
