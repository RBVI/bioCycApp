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


public class ListDatabasesTask extends AbstractTask implements ObservableTask {
	BioCycManager manager;
	List<Database> databases;

	@Tunable (description="Species pattern", context="nogui")
	public String species = null;

	@Tunable (description="Strain pattern", context="nogui")
	public String strain = null;


	public ListDatabasesTask (BioCycManager manager) {
		this.manager = manager;
	}

	public void run(TaskMonitor monitor) {
		List<Database> databases = new ArrayList<Database>(manager.getDatabases());
		ListIterator<Database> dIter = databases.listIterator();
		while (dIter.hasNext()) {
			Database d = dIter.next();
			if (species != null && !d.getSpecies().matches(species)) {
				dIter.remove();
				continue;
			}
			if (strain != null && d.getStrain() != null && !d.getStrain().matches(strain)) {
				dIter.remove();
			}
		}
		monitor.setStatusMessage("Found "+databases.size()+" databases");
	}

  public <R> R getResults(Class<? extends R> type) {
		if (type.equals(String.class)) {
			return (R)databases.toString();
		} else
			return (R)databases;
  }
	
}
