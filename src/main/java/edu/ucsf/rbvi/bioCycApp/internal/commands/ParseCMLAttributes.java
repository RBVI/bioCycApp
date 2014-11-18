/* vim: set ts=2: */
/**
 * Copyright (c) 2010 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package edu.ucsf.rbvi.bioCycApp.internal.commands;

import java.io.StringReader;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// Cytoscape imports
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;

import edu.ucsf.rbvi.bioCycApp.internal.model.BioCycManager;

public class ParseCMLAttributes implements NetworkAddedListener {
	final static String CML_ATTRIBUTE = "entityReference/structure/structureData";
	final static String SMILES_ATTRIBUTE = "smiles";
	BioCycManager manager = null;

	public ParseCMLAttributes(BioCycManager manager) {
		this.manager = manager;
	}


	public void handleEvent(NetworkAddedEvent evt) {
		CyNetwork network = evt.getNetwork();
		CyTable nodeTable = network.getDefaultNodeTable();
		if (nodeTable.getColumn(CML_ATTRIBUTE) != null) {
			try {
				if (nodeTable.getColumn(SMILES_ATTRIBUTE) == null) {
					nodeTable.createColumn(SMILES_ATTRIBUTE, String.class, false);
				}
				for (CyNode node: network.getNodeList()) {
					String CMLAttr = network.getRow(node).get(CML_ATTRIBUTE, String.class);
					if (CMLAttr != null && CMLAttr.length() > 0) {
						addSMILES(network, node, CMLAttr);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void addSMILES(CyNetwork network, CyNode node, String CML) {
		Document cml = null;
		// Find the SMILES string
		int offset = CML.indexOf("smiles");
		int end = CML.indexOf("<", offset+8);
		// System.out.println("SMILES: start = "+offset+" end="+end);
		// System.out.println("SMILES for node '"+node+"': "+CML.substring(offset+8, end));

		String smilesString = CML.substring(offset+8, end);
		network.getRow(node).set(SMILES_ATTRIBUTE, smilesString);
	}
}

