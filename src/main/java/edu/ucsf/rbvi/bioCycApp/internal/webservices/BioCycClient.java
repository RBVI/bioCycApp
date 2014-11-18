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
package edu.ucsf.rbvi.bioCycApp.internal.webservices;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.cytoscape.io.webservice.NetworkImportWebServiceClient;
import org.cytoscape.io.webservice.SearchWebServiceClient;
import org.cytoscape.io.webservice.swing.AbstractWebServiceGUIClient;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;


import edu.ucsf.rbvi.bioCycApp.internal.model.BioCycManager;
import edu.ucsf.rbvi.bioCycApp.internal.model.Database;
import edu.ucsf.rbvi.bioCycApp.internal.model.Pathway;
import edu.ucsf.rbvi.bioCycApp.internal.tasks.SearchPathwaysTask;
import edu.ucsf.rbvi.bioCycApp.internal.tasks.SearchPathwaysTaskFactory;
import edu.ucsf.rbvi.bioCycApp.internal.tasks.LoadPathwayTaskFactory;

/**
 * WebserviceClient implementation, for accessing the
 * BioCyc webservice in a standard way from within Cytoscape.
 */
public class BioCycClient extends AbstractWebServiceGUIClient 
                          implements SearchWebServiceClient, NetworkImportWebServiceClient, 
	                                   TaskObserver, ActionListener {
	private static final String DISPLAY_NAME = "BioCyc Web Service Client";
	private static final String CLIENT_ID = "biocyc";
	private boolean initialized = false;
	final BioCycManager manager;

	private static final String ACTION_SEARCH = "Search";
	private static final String ACTION_SET_DATABASE = "Set Database";
	private static final String ACTION_SET_WEBSERVICE = "Set Web Service URL";

	Database defaultDatabase = null;
	JComboBox databaseCombo;
	JTextField searchText;
	JTextField webServiceText;
	JTable resultTable;
	ListWithPropertiesTableModel<ResultProperty, ResultRow> tableModel;

	public BioCycClient(final BioCycManager manager) {
		super(manager.getURI(), CLIENT_ID, DISPLAY_NAME);
		this.manager = manager;
		this.manager.setClient(this);

		System.out.println("BioCycClient");

		// Create the GUI and add it to the standard GUI
		databaseCombo = new JComboBox();
		databaseCombo.addActionListener(this);
		databaseCombo.setActionCommand(ACTION_SET_DATABASE);
		Object dbArray[] = getDatabases();
		databaseCombo.setModel(new DefaultComboBoxModel(dbArray));
		if (manager.getDefaultDatabase() != null)
			databaseCombo.setSelectedItem(manager.getDefaultDatabase());

		searchText = new JTextField();
		searchText.setActionCommand(ACTION_SEARCH);
		searchText.addActionListener(this);

		webServiceText = new JTextField();
		webServiceText.setActionCommand(ACTION_SET_WEBSERVICE);
		webServiceText.addActionListener(this);
		webServiceText.setText(manager.getURI());

		JButton searchBtn = new JButton("Search");
		searchBtn.setActionCommand(ACTION_SEARCH);
		searchBtn.addActionListener(this);

		resultTable = new JTable();
		resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int row = resultTable.getSelectedRow();
					ResultRow selected = tableModel.getRow(row);
					Pathway pathway = selected.getResult();
					LoadPathwayTaskFactory factory = new LoadPathwayTaskFactory(manager, pathway);
					manager.execute(factory, null);
				}
			}
		});

		super.gui = new JPanel();
		super.gui.setLayout(new FormLayout(
				"4dlu, pref, 2dlu, fill:pref:grow, 4dlu, pref, 4dlu, pref, 4dlu",
				"4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, fill:pref:grow, 4dlu"
		));
		CellConstraints cc = new CellConstraints();
		super.gui.add(new JLabel("Web Server:"), cc.xy(2, 2));
		super.gui.add(webServiceText, cc.xy(4, 2));
		super.gui.add(new JLabel("Database:"), cc.xy(2, 4));
		super.gui.add(databaseCombo, cc.xy(4, 4));
		super.gui.add(new JLabel("Search:"), cc.xy(2, 6));
		super.gui.add(searchText, cc.xy(4, 6));
		super.gui.add(searchBtn, cc.xy(8, 6));
		super.gui.add(new JScrollPane(resultTable), cc.xyw(2, 8, 7));
	}

	public TaskIterator createTaskIterator(Object obj) {
		return new TaskIterator();
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if(ACTION_SEARCH.equals(action)) {
			// Get the search text
			String queryText = searchText.getText();

			// Get the database
			String db = ((Database)databaseCombo.getSelectedItem()).getOrgID();
			if (db == null) db = manager.getDefaultDatabase().getOrgID();

			// Do the query as a task
			SearchPathwaysTaskFactory taskFactory = new SearchPathwaysTaskFactory(manager, db, queryText);
			manager.execute(taskFactory, this);
		} else if (ACTION_SET_DATABASE.equals(action)) {
			defaultDatabase = (Database) databaseCombo.getSelectedItem();
			manager.setDefaultDatabase(defaultDatabase);
		} else if (ACTION_SET_WEBSERVICE.equals(action)) {
			String url = webServiceText.getText();
			if (url == null || url.length() == 0)
				manager.setURI(url);
			webServiceText.setText(url);
			manager.loadDatabases(false);
			updateDatabases();
		}
	}

	public void updateDatabases() {
		System.out.println("Updating databases");
		databaseCombo.setModel(new DefaultComboBoxModel(getDatabases()));
		databaseCombo.setSelectedItem(manager.getDefaultDatabase());
	}

	private Object[] getDatabases() {
		if (manager.getDatabases() == null) {
			Object [] foo = new Object[1];
			foo[0] = "Initializing...";
			return foo;
		}
		List<Database>databases = new ArrayList<Database>(manager.getMainDatabases());
		Database defaultDb = manager.getDefaultDatabase();
		if (defaultDb != null) {
			// We've already set a default, make sure it's in our list
			boolean found = false;
			for (Database db: databases) {
				if (db.getOrgID().equals(defaultDb.getOrgID())) {
					found = true;
					break;
				}
			}
			if (found == false) databases.add(defaultDb);
		}
		Object[] dbArray = databases.toArray();
		Arrays.sort(dbArray);
		return dbArray;
	}

	/**
	 * Represents a hit, a single row in the query results table.
	 */
	class ResultRow implements RowWithProperties<ResultProperty> {
		Pathway result;

		public ResultRow(Pathway result) {
			this.result = result;
		}

		public Pathway getResult() {
			return result;
		}

		public String getProperty(ResultProperty prop) {
			switch(prop) {
			case ID: return result.getFrameID();
			case NAME: return result.getCommonName();
			case SPECIES: return result.getOrgID();
			}
			return null;
		}
	}

	public void taskFinished(ObservableTask task) {
		if (task instanceof SearchPathwaysTask) {
			// Get the results
			List<Pathway> results = (List<Pathway>)task.getResults(List.class);
			// Put them in the search box
			tableModel =
				new ListWithPropertiesTableModel<ResultProperty, ResultRow>();
			if(results != null) {
				tableModel.setColumns(new ResultProperty[] {
						ResultProperty.ID,
						ResultProperty.NAME,
						ResultProperty.SPECIES,
				});
				resultTable.setModel(tableModel);
				for(Pathway p : results) {
					tableModel.addRow(new ResultRow(p));
				}
			}
			resultTable.setModel(tableModel);
		}
	}

	public void allFinished(FinishStatus finishStatus) {
	}

}
