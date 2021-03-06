package owltools.ontologyrelease.reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.semanticweb.owlapi.model.OWLClass;

import owltools.graph.OWLGraphWrapper;
import owltools.ontologyrelease.reports.OntologyReportGenerator.AbstractReport;
import owltools.ontologyrelease.reports.OntologyReportGenerator.OntologyReport;

/**
 * {@link OntologyReport} for obsolete terms with possible replacements (consider) 
 */
public class ObsoletesInExactReport extends AbstractReport {

	private final String filePrefix;

	/**
	 * @param graph
	 */
	public ObsoletesInExactReport(OWLGraphWrapper graph) {
		this(graph.getOntologyId().toUpperCase()+".");
	}
	
	/**
	 * @param filePrefix
	 */
	public ObsoletesInExactReport(String filePrefix) {
		super();
		this.filePrefix = filePrefix;
	}

	@Override
	public String getReportFileName() {
		return filePrefix+"obsoletes-inexact";
	}

	@Override
	protected String getFileHeader() {
		return "! Obsolete terms and possible annotation substitutes\n!\n"
				+ "!Obsolete [tab] Alternative\n";
	}

	@Override
	public void handleTerm(PrintWriter writer, OWLClass owlClass, OWLGraphWrapper graph) throws IOException {
		if (graph.isObsolete(owlClass)) {
			List<String> considerList = graph.getConsider(owlClass);
			if (considerList != null && !considerList.isEmpty()) {
				String id = graph.getIdentifier(owlClass);
				for (String consider : considerList) {
					writeTabs(writer, id, consider);
				}
			}
		}
	}

}
