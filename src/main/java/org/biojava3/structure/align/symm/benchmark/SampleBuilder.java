/*
 * BioJava development code
 * 
 * This code may be freely distributed and modified under the terms of the GNU Lesser General Public Licence. This
 * should be distributed with the code. If you do not have a copy, see:
 * 
 * http://www.gnu.org/copyleft/lesser.html
 * 
 * Copyright for this code is held jointly by the individual authors. These should be listed in @author doc comments.
 * 
 * For more information on the BioJava project and its aims, or to join the biojava-l mailing list, visit the home page
 * at:
 * 
 * http://www.biojava.org/
 * 
 * Created on 2013-03-03
 */
package org.biojava3.structure.align.symm.benchmark;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.biojava3.structure.align.symm.census2.Result;
import org.biojava3.structure.align.symm.census2.Results;

/**
 * Converts a {@link Results} (see symmetry project) to a {@link Sample}. In other words, takes a table of known space
 * groups, with the predictions of symmetry by an algorithm, and generates a benchmark XML file.
 * 
 * @author dmyerstu
 * @see {@link org#biojava3#structure#align#symm#benchmark#comparison}, for ways of using the generated XML files
 */
public class SampleBuilder {

	private static final Logger logger = LogManager.getLogger(SampleBuilder.class.getName());

	public static void buildSample(File input, File output, File ordersFile) throws IOException {
		buildSample(input, output, getOrders(ordersFile.getPath()));
	}

	public static void buildSample(File input, File output, Map<String, KnownInfo> knownInfos) throws IOException {
		Results results = Results.fromXML(input);
		logger.info("File " + input + " contains " + results.size() + " entries");
		Sample sample = new Sample();
		for (Result result : results.getData()) {
			if (result == null) continue;
			Case c = new Case();
			c.setResult(result);
			c.setKnownInfo(knownInfos.get(result.getScopId()));
			sample.add(c);
		}
		BufferedWriter br = new BufferedWriter(new FileWriter(output));
		br.write(sample.toXML());
		br.close();
	}

	public static List<String> getNames(File knownInfoFile) throws IOException {
		List<String> list = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(knownInfoFile));
		String line = "";
		while ((line = br.readLine()) != null) {
			String[] parts = line.split("\t"); // domain tab group
			list.add(parts[0]);
		}
		br.close();
		return list;
	}

	public static Map<String, KnownInfo> getOrders(File knownInfoFile) throws IOException {
		Map<String, KnownInfo> map = new HashMap<String, KnownInfo>();
		BufferedReader br = new BufferedReader(new FileReader(knownInfoFile));
		String line = "";
		while ((line = br.readLine()) != null) {
			String[] parts = line.split("\t"); // domain tab group
			KnownInfo info = new KnownInfo(parts[1]);
			map.put(parts[0], info);
		}
		br.close();
		return map;
	}

	public static Map<String, KnownInfo> getOrders(String file) throws IOException {
		return getOrders(new File(file));
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 2 || args.length > 3) {
			System.err.println("Usage: " + SampleBuilder.class.getSimpleName()
					+ " input-results-file output-sample-file [tab-seperated-groups-file]");
			return;
		}
		File input = new File(args[0]);
		File output = new File(args[1]);
		File ordersFile = new File("src/main/resources/domain_symm_benchmark.tsv");
		if (args.length > 2) {
			ordersFile = new File(args[2]);
		}
		Map<String, KnownInfo> infos = getOrders(ordersFile);
		buildSample(input, output, infos);
	}

}
