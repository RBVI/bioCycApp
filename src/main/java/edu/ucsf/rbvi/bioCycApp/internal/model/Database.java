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
package edu.ucsf.rbvi.bioCycApp.internal.model;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 */
public class Database implements Comparable<Database> {
	String orgid = null;
	String version = null;
	String species = null;
	String strain = null;
	List<DbLink> dbLink = null;

	public Database(Element database) {
		this.orgid = DomUtils.getAttribute(database,"orgid");
		this.version = DomUtils.getAttribute(database,"version");
		this.species = DomUtils.getChildData(database, "species");
		this.strain = DomUtils.getChildData(database, "strain");
		this.dbLink = DomUtils.getDbLinks(database);
	}

	public Database(String species, String orgid) {
		this.species = species;
		this.orgid = orgid;
	}

	public int compareTo(Database o2) {
		return toString().compareTo(o2.toString());
	}

	public String getOrgID() { return orgid; }
	public String getVersion() { return version; }
	public String getSpecies() { return species; }
	public String getStrain() { return strain; }
	public List<DbLink> getDbLinks() { return dbLink; }
	public String toString() {
		if (strain != null)
			return species+" "+strain+" ("+orgid+")";
		else
			return species+" ("+orgid+")";
	}

	public static List<Database> getDatabases(Document response) {
		List<Database> databases = new ArrayList<Database>();
		if (response != null) {
			NodeList dbNodes = response.getElementsByTagName("PGDB");
			if (dbNodes == null || dbNodes.getLength() == 0) return null;

			for (int index = 0; index < dbNodes.getLength(); index++) {
				databases.add(new Database((Element)dbNodes.item(index)));
			}
		}
		return databases;
	}

}
