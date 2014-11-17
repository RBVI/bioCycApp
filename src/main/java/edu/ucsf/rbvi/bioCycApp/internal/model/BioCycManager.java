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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.CyServiceRegistrar;

import edu.ucsf.rbvi.bioCycApp.internal.commands.QueryHandler;
import edu.ucsf.rbvi.bioCycApp.internal.model.Database;
import edu.ucsf.rbvi.bioCycApp.internal.webservices.BioCycClient;

/**
 */
public class BioCycManager {
	CyApplicationManager appManager;
	CyServiceRegistrar serviceRegistrar;
	List<Database> databases = null;
	Map<String, List<Database>> speciesMap = null;
	QueryHandler handler;
	BioCycClient client = null;
	Database currentDatabase = null;

	private enum TopSpecies {
		BSUBITILIS("Bacillus subtilis", "BSUB"),
		ECOLI("Escherichia coli", "MG1655"),
		HUMAN("Homo sapiens", "HUMAN"),
		MOUSE("Mus musculus", "MOUSE"),
		TB("Mycobacterium tuberculosis", "MTBRV"),
		METACYC("MetaCyc", "META"),
		YEAST("Saccharomyces cerevisiae", "YEAST");

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
		this.handler = null;
	}

	public String getURI() {
		return "http://websvc.biocyc.org/";
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

	private void createMap(List<Database> dbs) {
		speciesMap.clear();
		for (Database db: dbs) {
			if (!speciesMap.containsKey(db.getSpecies()))
				speciesMap.put(db.getSpecies(), new ArrayList<Database>());
			speciesMap.get(db.getSpecies()).add(db);
		}
	}

}
