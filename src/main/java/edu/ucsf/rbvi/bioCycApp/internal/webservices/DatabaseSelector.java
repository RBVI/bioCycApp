package edu.ucsf.rbvi.bioCycApp.internal.webservices;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import edu.ucsf.rbvi.bioCycApp.internal.model.BioCycManager;
import edu.ucsf.rbvi.bioCycApp.internal.model.Database;

/**
 * This is a two-step selector widget for choosing databases
 */
public class DatabaseSelector extends JDialog implements ActionListener
{
	final BioCycManager manager;
	JComboBox<String> speciesCombo;
	JComboBox<Database> databasesCombo;

	BioCycClient client = null;

	final static String ACTION_SET_SPECIES = "setSpecies";
	final static String ACTION_SET_DATABASE = "setDatabase";
	final static String ACTION_CANCEL = "cancel";
	final static String ACTION_DONE = "done";

	public DatabaseSelector(BioCycManager manager, BioCycClient client) {
		super(client.getParentDialog(), true);
		this.manager = manager;
		this.client = client;

		setTitle("Database Selector");

		// Create species selector
		speciesCombo = new JComboBox<String>();
		speciesCombo.addActionListener(this);
		speciesCombo.setActionCommand(ACTION_SET_SPECIES);
		List<String> species = new ArrayList<String>();
		species.add("");
		species.addAll(manager.getSpecies());
		String speciesArray[] = species.toArray(new String[1]);
		Arrays.sort(speciesArray);
		speciesCombo.setModel(new DefaultComboBoxModel<String>(speciesArray));

		// Create databases selector
		databasesCombo = new JComboBox<Database>();
		databasesCombo.addActionListener(this);
		databasesCombo.setActionCommand(ACTION_SET_DATABASE);
		databasesCombo.setModel(new DefaultComboBoxModel<Database>(new Database[1]));

		JButton doneButton = new JButton("OK");
		doneButton.setActionCommand(ACTION_DONE);
		doneButton.addActionListener(this);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand(ACTION_CANCEL);
		cancelButton.addActionListener(this);

		JPanel panel = new JPanel();
		panel.setLayout(new FormLayout(
				"4dlu, pref, 2dlu, fill:pref:grow, 4dlu, pref, 4dlu",
				"4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu"
		));
		CellConstraints cc = new CellConstraints();
		panel.add(new JLabel("1. Select Species:"), cc.xy(2, 2));
		panel.add(speciesCombo, cc.xyw(4, 2, 3));
		panel.add(new JLabel("2. Select Database:"), cc.xy(2, 4));
		panel.add(databasesCombo, cc.xyw(4, 4, 3));
		panel.add(new JSeparator(), cc.xyw(2, 6, 5));
		panel.add(doneButton, cc.xy(2, 8));
		panel.add(cancelButton, cc.xy(6, 8));
		getContentPane().add(panel);
		pack();
	}

	public void actionPerformed(ActionEvent e) {
		String actionString = e.getActionCommand();
		if (actionString.equals(ACTION_SET_SPECIES)) {
			List<Database> dbs = manager.getDatabases((String)speciesCombo.getSelectedItem());
			Database databasesArray[] = dbs.toArray(new Database[1]);
			Arrays.sort(databasesArray);
			databasesCombo.setModel(new DefaultComboBoxModel<Database>(databasesArray));
		} else if (actionString.equals(ACTION_SET_DATABASE)) {
			// Nothing to do...
		} else if (actionString.equals(ACTION_CANCEL)) {
			setVisible(false);
			dispose();
		} else if (actionString.equals(ACTION_DONE)) {
			manager.setDefaultDatabase((Database)databasesCombo.getSelectedItem());
			client.updateDatabases();
			setVisible(false);
			dispose();
		}
	}

}
