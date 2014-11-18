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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.ucsf.rbvi.bioCycApp.internal.model.BioCycManager;
import edu.ucsf.rbvi.bioCycApp.internal.model.Gene;
import edu.ucsf.rbvi.bioCycApp.internal.model.Pathway;
import edu.ucsf.rbvi.bioCycApp.internal.model.Protein;

/**
 * 
 */
public class QueryHandler {
	DocumentBuilder builder = null;
	BioCycManager manager = null;

	public QueryHandler(BioCycManager manager) {
		this.manager = manager;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
		} catch (Exception e) {
			e.printStackTrace();
			// manager.log().error("Unable to create a new document: "+e.getMessage());
		}
	}

	public Document query(String queryString) {
		InputStream input;
		Document result;
		String queryUrl = queryString;
		String baseUrl = manager.getURI();
		System.out.println("Executing query: "+baseUrl+"xmlquery?query="+queryString);
		try {
			queryUrl = baseUrl+"xmlquery?query="+URLEncoder.encode(queryString, "UTF-8");
			// System.out.println("Executing query: "+queryString);
			URL url = new URL(queryUrl);
			input = url.openStream();
			// result = builder.parse(teeInput(input));
			result = builder.parse(teeInput(input));
		} catch (Exception e) {
			System.out.println("Caught exception: "+e.getMessage());
			// Special handling for the circumstance where we can't connect
			/* to the server
			if (queryString.equals("dbs")) {
				logger.warning("Unable to connect to server: "+e.getMessage(),e);
			} else {
				logger.error("Unable to process query "+queryUrl+": "+e.getMessage(),e);
			}
			*/
			return null;
		}
		return result;
	}

	public Document findPathways(String database, String text) {
			String queryString = null;
			queryString = "[x:x<-"+database+"^^pathways";
			if (text != null) {
				queryString = queryString+","+database+"~"+text+" in (pathway-to-genes x)";
			}
			queryString = queryString+"]";
			return query(queryString);
	}

	public void loadNetwork(String queryString) throws MalformedURLException {
		// Unfortunately, the following would be the easiest approach, but
		// biopax isn't automatically recognized, so we need to do this "the hard way"
		// LoadNetworkTask.loadURL(new URL(baseUrl+queryString), false);
		String baseUrl = manager.getURI();
		// logger.info("Executing query: "+baseUrl+queryString);

/*
		ImportHandler inputHandler = Cytoscape.getImportHandler();
		try {
			// Add a listener so we can fix up some attributes
			Cytoscape.getPropertyChangeSupport()
			                  .addPropertyChangeListener( Cytoscape.NETWORK_LOADED, new ParseCMLAttributes(logger) );
			LoadNetworkTask.loadURL(new URL(baseUrl+queryString), false);

			File rawFile = inputHandler.downloadFromURL(new URL(baseUrl+queryString), null);
			File bioPaxFile = new File(rawFile.getAbsolutePath()+".xml");
			rawFile.renameTo(bioPaxFile);
			// Add a listener so we can fix up some attributes
			Cytoscape.getPropertyChangeSupport()
			                  .addPropertyChangeListener( Cytoscape.NETWORK_LOADED, new ParseCMLAttributes(logger) );

			LoadNetworkTask.loadFile(bioPaxFile, false);
			logger.info("Loading file into: "+bioPaxFile.getAbsolutePath());
			bioPaxFile.delete();
		} catch (Exception e) {
			// logger.error("Error loading network from "+baseUrl+queryString+": "+e.getMessage(), e);
			throw new MalformedURLException("Unable to load network from "+baseUrl+queryString+" ("+e.getMessage()+")");
		}
*/
	}

	public Map<String, Pathway> andPathways(Map<String, Pathway>pathways1, List<Pathway>pathways2) {
		return null;
	}

	public Map<String, Pathway> orPathways(Map<String, Pathway>pathways1, List<Pathway>pathways2) {
		return null;
	}

	public List<Pathway> searchForPathways(String database, String text) {
		Map<String,Pathway> pathwayMap = new HashMap<String,Pathway>();

		String queryString = "{y: y<- "+pathwayQuery(database, text)+ " ++ " +
		                     pathwaysWithGeneQuery(database, text) + " ++ " +
		                     pathwaysWithProteinQuery(database, text) + " ++ " +
		                     pathwaysWithCompoundQuery(database, text) + " }";

		List<Pathway> pathways = Pathway.getPathways(query(queryString));
		if (pathways != null) {
			for (Pathway p: pathways) {
				if (!pathwayMap.containsKey(p.getFrameID()))
					pathwayMap.put(p.getFrameID(), p);
				/*
				System.out.println("Found pathway '"+p.getID()+"':");
				System.out.println("    orgID: "+p.getOrgID());
				System.out.println("    frameID: "+p.getFrameID());
				System.out.println("    commonName: "+p.getCommonName());
				System.out.println("    comment: "+p.getComment());
				if (p.getReactions() == null)
					System.out.println("    reactions: none");
				else
					System.out.println("    reactions: "+p.getReactions().size());
				*/
			}
		}

		return new ArrayList<Pathway>(pathwayMap.values());
	}

	public String pathwayQuery(String database, String text) {
		return objectQuery("pathways", database, text);
	}

	public String geneQuery(String database, String text) {
		return objectQuery("genes", database, text);
	}

	public String proteinQuery(String database, String text) {
		return objectQuery("proteins", database, text);
	}

	public String compoundQuery(String database, String text) {
		return objectQuery("compounds", database, text);
	}

	public String reactionQuery(String database, String text) {
		return objectQuery("reactions", database, text);
	}

	public String pathwaysWithGeneQuery(String database, String text) {
		if (text == null || text.length() == 0)
			return "[p:x<-"+database+"^^genes, p<-"+database+"^^pathways, x in (pathway-to-genes p)]";
		return "[p:x<-"+database+"^^genes,\""+text+"\" instringci x^names, p<-"+database+"^^pathways, x in (pathway-to-genes p)]";
	}

	public String pathwaysWithProteinQuery(String database, String text) {
		if (text == null || text.length() == 0)
			return "{p:x<-"+database+"^^proteins, g<-genes-of-protein(x), p<-"+ database+"^^pathways, g in (pathway-to-genes p)}";
		return "{p:x<-"+database+"^^proteins,\""+text+"\" instringci x^names, g<-genes-of-protein(x), p<-"+
		        database+"^^pathways, g in (pathway-to-genes p)}";
	}

	public String pathwaysWithCompoundQuery(String database, String text) {
		if (text == null || text.length() == 0)
			return "{p:x<-"+database+"^^compounds, p<-"+database+"^^pathways, x in (compounds-of-pathway p)}";
		return "{p:x<-"+database+"^^compounds,\""+text+"\" instringci x^names, p<-"+
		        database+"^^pathways, x in (compounds-of-pathway p)}";
	}

	private String objectQuery (String object, String database, String text) {
		String queryString = "[x:x<-"+database+"^^"+object;
		if (text == null || text.length() == 0)
			return queryString + "]";
		return queryString + ",\""+text+"\" instringci x^names]";
	}

	private InputSource teeInput(InputStream input) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		StringBuilder sBuilder = new StringBuilder();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				// System.out.println(": "+line);
				sBuilder.append(line);
			}
		} catch (Exception e) {
			// logger.error("",e);
		}
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(sBuilder.toString()));
		return is;
	}
}
