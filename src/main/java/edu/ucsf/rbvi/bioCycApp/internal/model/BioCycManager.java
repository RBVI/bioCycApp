// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package edu.ucsf.rbvi.bioCycApp.internal.model;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskObserver;

import edu.ucsf.rbvi.bioCycApp.internal.commands.QueryHandler;
import edu.ucsf.rbvi.bioCycApp.internal.model.Database;
import edu.ucsf.rbvi.bioCycApp.internal.webservices.BioCycClient;

/**
 */
public class BioCycManager {
	CyApplicationManager appManager;
	CyServiceRegistrar serviceRegistrar;
	SynchronousTaskManager syncTaskManager;
	TaskManager taskManager;
	LoadNetworkURLTaskFactory loadNetworkTaskFactory;
	List<Database> databases = null;
	Map<String, List<Database>> speciesMap = null;
	QueryHandler handler;
	BioCycClient client = null;
	Database currentDatabase = null;
	String base_uri = "http://websvc.biocyc.org/";

	private enum TopSpecies {
		BSUBITILIS("Bacillus subtilis", "BSUB"),
		ECOLI("Escherichia coli", "ECOLI"),
		HUMAN("Homo sapiens", "HUMAN"),
		MOUSE("Mus musculus", "MOUSE"),
		TB("Mycobacterium tuberculosis", "MTBRV"),
		METACYC("MetaCyc", "META"),
		YEAST("Saccharomyces cerevisiae", "YEAST");
		// Add "Other", and do a species->strain double selection?

		private String species;
		private String orgID;
		TopSpecies(String species, String orgID) {
			this.species = species;
			this.orgID = orgID;
		}
		public String getSpecies() { return species; }
		public String getOrgID() { return orgID; }
	};

	public BioCycManager(CyApplicationManager appManager, CyServiceRegistrar serviceRegistrar) {
		speciesMap = new HashMap<String, List<Database>>();
		this.appManager = appManager;
		this.serviceRegistrar = serviceRegistrar;
		syncTaskManager = serviceRegistrar.getService(SynchronousTaskManager.class);
		taskManager = serviceRegistrar.getService(TaskManager.class);
		loadNetworkTaskFactory = serviceRegistrar.getService(LoadNetworkURLTaskFactory.class);
		this.handler = null;
	}

	public String getURI() {
		return base_uri;
	}

	public void setURI(String uri) {
		base_uri = uri;
	}

	public void loadDatabases(boolean async) {
		if (handler == null) handler = new QueryHandler(this);
		if (async) {
			Thread t = new Thread() {
				public void run() {
					databases = null;
					System.out.println("Getting databases");
					databases = Database.getDatabases(handler.query("dbs"));
					createMap(databases);
					System.out.println("Found "+databases.size()+" databases");
					if (client != null)
						client.updateDatabases();
				}
			};	
			t.start();
		} else {
			databases = Database.getDatabases(handler.query("dbs"));
			createMap(databases);
			if (client != null)
				client.updateDatabases();
		}
	}

	public void setClient(BioCycClient client) {
		this.client = client;
	}

	public List<Database> getDatabases() {
		return databases;
	}

	public List<Gene> getGenes(String database, String name) {
		if (handler == null) handler = new QueryHandler(this);
		String queryString = handler.geneQuery(database, name);
		List<Gene> genes = Gene.getGenes(handler.query(queryString));
		return genes;
	}

	public List<Protein> getProteins(String database, String name) {
		if (handler == null) handler = new QueryHandler(this);
		String queryString = handler.proteinQuery(database, name);
		List<Protein> proteins = Protein.getProteins(handler.query(queryString));
		return proteins;
	}

	public List<Compound> getCompounds(String database, String name) {
		if (handler == null) handler = new QueryHandler(this);
		String queryString = handler.compoundQuery(database, name);
		List<Compound> compounds = Compound.getCompounds(handler.query(queryString));
		return compounds;
	}

	public List<Pathway> getPathways(String database, String name) {
		if (handler == null) handler = new QueryHandler(this);
		String queryString = handler.pathwayQuery(database, name);
		List<Pathway> pathways = Pathway.getPathways(handler.query(queryString));
		return pathways;
	}

	public List<Reaction> getReactions(String database, String name) {
		if (handler == null) handler = new QueryHandler(this);
		String queryString = handler.reactionQuery(database, name);
		List<Reaction> reactions = Reaction.getReactions(handler.query(queryString));
		return reactions;
	}

	public List<Pathway> searchPathways(String database, String text) {
		if (handler == null) handler = new QueryHandler(this);
		return handler.searchForPathways(database, text);
	}

	public List<Reaction> searchReactions(String database, String name) {
		if (handler == null) handler = new QueryHandler(this);
		String query = "[x:x<-"+database+"^^reactions";
		if (name != null) {
			query = query+","+database+"~"+name+" in (reaction-to-genes x)";
		}
		query = query + "]";

		List<Reaction> reactions = Reaction.getReactions(handler.query(query));
		return reactions;
	}

	public void loadPathway(String database, String pathway) throws MalformedURLException {
		String query = getURI()+database+"/pathway-biopax?type=2&object="+pathway;
		System.out.println("Looking for network "+query);
		syncTaskManager.execute(loadNetworkTaskFactory.loadCyNetworks(new URL(query)));
	}

	public Database getDatabase(String orgID) {
		for (Database d: databases) {
			if (d.getOrgID().equalsIgnoreCase(orgID))
				return d;
		}
		return null;
	}

	public List<Database> getMainDatabases() {
		List<Database> topDatabases = new ArrayList<Database>();
		for (TopSpecies ts: TopSpecies.values()) {
			if (speciesMap.containsKey(ts.getSpecies())) {
				for (Database db: speciesMap.get(ts.getSpecies())) {
					if (db.getOrgID().equals(ts.getOrgID()))
						topDatabases.add(db);
				}
			}
		}
		return topDatabases;
	}

	public void setDefaultDatabase(Database db) {
		currentDatabase = db;
	}

	public Database getDefaultDatabase() { return currentDatabase; }

	public void execute(TaskFactory factory, TaskObserver observer) {
		if (observer == null)
			taskManager.execute(factory.createTaskIterator());
		else
			taskManager.execute(factory.createTaskIterator(), observer);
	}

	private void createMap(List<Database> dbs) {
		speciesMap.clear();
		for (Database db: dbs) {
			if (!speciesMap.containsKey(db.getSpecies()))
				speciesMap.put(db.getSpecies(), new ArrayList<Database>());
			speciesMap.get(db.getSpecies()).add(db);
		}
	}

}
