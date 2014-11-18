package edu.ucsf.rbvi.bioCycApp.internal.tasks;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.bioCycApp.internal.model.BioCycManager;
import edu.ucsf.rbvi.bioCycApp.internal.model.Pathway;

public class LoadPathwayTaskFactory extends AbstractTaskFactory {
	BioCycManager manager;
	Pathway pathway = null;

	public LoadPathwayTaskFactory (BioCycManager manager) {
		this.manager = manager;
	}

	public LoadPathwayTaskFactory (BioCycManager manager, Pathway pathway) {
		this.manager = manager;
		this.pathway = pathway;
	}

	public TaskIterator createTaskIterator() {
		if (pathway == null)
			return new TaskIterator(new LoadPathwayTask(manager));

		return new TaskIterator(new LoadPathwayTask(manager, pathway));
	}
}
