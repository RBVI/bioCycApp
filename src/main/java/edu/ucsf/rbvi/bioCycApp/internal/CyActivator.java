package edu.ucsf.rbvi.bioCycApp.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ucsf.rbvi.bioCycApp.internal.model.BioCycManager;
import edu.ucsf.rbvi.bioCycApp.internal.tasks.ListDatabasesTaskFactory;
import edu.ucsf.rbvi.bioCycApp.internal.tasks.ListGenesTaskFactory;
import edu.ucsf.rbvi.bioCycApp.internal.tasks.ListPathwaysTaskFactory;
import edu.ucsf.rbvi.bioCycApp.internal.tasks.ListProteinsTaskFactory;
import edu.ucsf.rbvi.bioCycApp.internal.tasks.ListReactionsTaskFactory;
// import edu.ucsf.rbvi.bioCycApp.internal.tasks.LoadPathwayTaskFactory;
import edu.ucsf.rbvi.bioCycApp.internal.tasks.OpenResourceTaskFactory;
import edu.ucsf.rbvi.bioCycApp.internal.tasks.SetDatabaseTaskFactory;
import edu.ucsf.rbvi.bioCycApp.internal.webservices.BioCycClient;

public class CyActivator extends AbstractCyActivator {
	private static Logger logger = LoggerFactory
			.getLogger(edu.ucsf.rbvi.bioCycApp.internal.CyActivator.class);

	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		try {
		// See if we have a graphics console or not
		boolean haveGUI = true;
		CySwingApplication cySwingApplication = null;
		ServiceReference ref = bc.getServiceReference(CySwingApplication.class.getName());

		if (ref == null) {
			haveGUI = false;
			// Issue error and return
		} else {
			cySwingApplication = getService(bc, CySwingApplication.class);
		}

		// Get some services we'll need
		CyApplicationManager appManager = getService(bc, CyApplicationManager.class);
		CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);

		System.out.println("Creating manager");

		// Create our manager object
		BioCycManager manager = new BioCycManager(appManager, serviceRegistrar);

		System.out.println("Creating client");

		// Create our webservices client
		BioCycClient client = new BioCycClient(manager);
		registerAllServices(bc, client, new Properties());

		System.out.println("Loading databases");

		// Launch a thread to load the databases
		manager.loadDatabases(true);

		// Commands
		// Load Databases Command
		ListDatabasesTaskFactory listDatabases = new ListDatabasesTaskFactory(manager);
		Properties listDatabasesProps = new Properties();
    listDatabasesProps.setProperty(COMMAND, "list databases");
    listDatabasesProps.setProperty(COMMAND_NAMESPACE, "biocyc");
    listDatabasesProps.setProperty(COMMAND_DESCRIPTION, "List available databases");
    listDatabasesProps.setProperty(IN_MENU_BAR, "false");
    registerService(bc, listDatabases, TaskFactory.class, listDatabasesProps);

		// List Genes
		ListGenesTaskFactory listGenes = new ListGenesTaskFactory(manager);
		Properties listGenesProps = new Properties();
    listGenesProps.setProperty(COMMAND, "list genes");
    listGenesProps.setProperty(COMMAND_NAMESPACE, "biocyc");
    listGenesProps.setProperty(COMMAND_DESCRIPTION, "List all of the genes that meet the criteria");
    listGenesProps.setProperty(IN_MENU_BAR, "false");
    registerService(bc, listGenes, TaskFactory.class, listGenesProps);

		// List Pathways
		ListPathwaysTaskFactory listPathways = new ListPathwaysTaskFactory(manager);
		Properties listPathwaysProps = new Properties();
    listPathwaysProps.setProperty(COMMAND, "list pathways");
    listPathwaysProps.setProperty(COMMAND_NAMESPACE, "biocyc");
    listPathwaysProps.setProperty(COMMAND_DESCRIPTION, "List all of the pathways that meet the criteria");
    listPathwaysProps.setProperty(IN_MENU_BAR, "false");
    registerService(bc, listPathways, TaskFactory.class, listPathwaysProps);

		// List Proteins
		ListProteinsTaskFactory listProteins = new ListProteinsTaskFactory(manager);
		Properties listProteinsProps = new Properties();
    listProteinsProps.setProperty(COMMAND, "list proteins");
    listProteinsProps.setProperty(COMMAND_NAMESPACE, "biocyc");
    listProteinsProps.setProperty(COMMAND_DESCRIPTION, "List all of the proteins that meet the criteria");
    listProteinsProps.setProperty(IN_MENU_BAR, "false");
    registerService(bc, listProteins, TaskFactory.class, listProteinsProps);

		// List Reactions
		ListReactionsTaskFactory listReactions = new ListReactionsTaskFactory(manager);
		Properties listReactionsProps = new Properties();
    listReactionsProps.setProperty(COMMAND, "list reactions");
    listReactionsProps.setProperty(COMMAND_NAMESPACE, "biocyc");
    listReactionsProps.setProperty(COMMAND_DESCRIPTION, "List all of the reactions that meet the criteria");
    listReactionsProps.setProperty(IN_MENU_BAR, "false");
    registerService(bc, listReactions, TaskFactory.class, listReactionsProps);

		// Search Pathways
		// Search Reactions

		/*
		// Load Pathway 
		LoadPathwayTaskFactory loadPathway = new LoadPathwayTaskFactory(manager);
		Properties loadPathwayProps = new Properties();
    loadPathwayProps.setProperty(COMMAND, "load pathway");
    loadPathwayProps.setProperty(COMMAND_NAMESPACE, "biocyc");
    loadPathwayProps.setProperty(COMMAND_DESCRIPTION, "Load a pathway in biopax format");
    loadPathwayProps.setProperty(IN_MENU_BAR, "false");
    registerService(bc, loadPathway, TaskFactory.class, loadPathwayProps);
		*/

		// Open Resource
		OpenResourceTaskFactory openResource = new OpenResourceTaskFactory(manager);
		Properties openResourceProps = new Properties();
    openResourceProps.setProperty(COMMAND, "open resource");
    openResourceProps.setProperty(COMMAND_NAMESPACE, "biocyc");
    openResourceProps.setProperty(COMMAND_DESCRIPTION, "Open a Pathway Tools Resource");
    openResourceProps.setProperty(IN_MENU_BAR, "false");
    registerService(bc, openResource, TaskFactory.class, openResourceProps);


		// Set Database Command
		SetDatabaseTaskFactory setDatabase = new SetDatabaseTaskFactory(manager);
		Properties setDatabaseProps = new Properties();
    setDatabaseProps.setProperty(COMMAND, "set database");
    setDatabaseProps.setProperty(COMMAND_NAMESPACE, "biocyc");
    setDatabaseProps.setProperty(COMMAND_DESCRIPTION, "Set default database");
    setDatabaseProps.setProperty(IN_MENU_BAR, "false");
    registerService(bc, setDatabase, TaskFactory.class, setDatabaseProps);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
