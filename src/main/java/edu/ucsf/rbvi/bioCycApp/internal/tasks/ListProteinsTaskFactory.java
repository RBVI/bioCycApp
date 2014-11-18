package edu.ucsf.rbvi.bioCycApp.internal.tasks;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.bioCycApp.internal.model.BioCycManager;

public class ListProteinsTaskFactory extends AbstractTaskFactory {
	BioCycManager manager;

	public ListProteinsTaskFactory (BioCycManager manager) {
		this.manager = manager;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ListProteinsTask(manager));
	}
}
