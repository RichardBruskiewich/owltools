package owltools.gaf;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import owltools.graph.OWLGraphWrapper;

/**
 * Methods to simplify the work with the evidence code ontology (ECO). 
 */
public class TaxonTools {
	
	public static final String TAXON_PURL = "http://purl.obolibrary.org/obo/ncbitaxon/subsets/taxslim.owl";
	
	private final OWLGraphWrapper taxo;
	private final OWLReasoner reasoner;
	private final boolean disposeReasonerP;
	
	private final Map<String, Set<OWLClass>> mappingCache = new HashMap<String, Set<OWLClass>>();
	
	/**
	 * Create an instance for the given graph and reasoner.
	 * 
	 * @param graph
	 * @param reasoner
	 * @param disposeReasoner set to true, if the reasoner should also be disposed
	 * @throws UnknownOWLOntologyException
	 * @throws OWLOntologyCreationException
	 * 
	 * @see #dispose()
	 */
	public TaxonTools (OWLGraphWrapper graph, OWLReasoner reasoner, boolean disposeReasoner) throws UnknownOWLOntologyException, OWLOntologyCreationException {

		// This has bitten me, so let's try and bew specific...
		if( reasoner == null ){ throw new Error("No reasoner was specified for use with the TaxonTools. Add a reasoner for the command line"); }
		
		// assume the graph wrapper is more than eco
		// try to find ECO by its purl
		Set<OWLOntology> allOntologies = graph.getAllOntologies();
		OWLOntology taxo = null;
		for (OWLOntology owlOntology : allOntologies) {
			OWLOntologyID id = owlOntology.getOntologyID();
			IRI ontologyIRI = id.getOntologyIRI();
			if (ontologyIRI != null) {
				if (TAXON_PURL.equals(ontologyIRI.toString())) {
					taxo = owlOntology;
				}
			}
		}
		if (taxo != null) {
			// found eco create new wrapper
			this.taxo = new OWLGraphWrapper(taxo);
		}
		else {
			// did not find eco, use whole wrapper
			this.taxo = graph;
		}
		
		this.reasoner = reasoner;
		this.disposeReasonerP = disposeReasoner;
	}
			
	/**
	 * Wrapper method for the reasoner.
	 * 
	 * @param taxonClass
	 * @param reflexive
	 * @return set of super classes
	 */
	public Set<OWLClass> getAncestors(OWLClass taxonClass, boolean reflexive) {
		if (taxonClass == null) {
			return Collections.emptySet();
		}
		Set<OWLClass> result = new HashSet<OWLClass>();
		Set<OWLClass> set = reasoner.getSuperClasses(taxonClass, false).getFlattened();
		for (OWLClass cls : set) {
			if (cls.isBuiltIn() == false) {
				result.add(cls);
			}
		}
		if (reflexive) {
			result.add(taxonClass);
		}
		if (result.isEmpty()) {
			return Collections.emptySet();
		}
		return result;
		
	}
	
	/**
	 * Wrapper method for the reasoner
	 * 
	 * @param sources
	 * @param reflexive
	 * @return set of sub classes
	 */
	public Set<OWLClass> getDescendents(Set<OWLClass> sources, boolean reflexive) {
		if (sources == null || sources.isEmpty()) {
			return Collections.emptySet();
		}
		Set<OWLClass> result = new HashSet<OWLClass>();
		for (OWLClass source : sources) {
			Set<OWLClass> set = reasoner.getSubClasses(source, false).getFlattened();
			for (OWLClass cls : set) {
				if (cls.isBuiltIn() == false) {
					result.add(cls);
				}
			}
		}
		if (reflexive) {
			result.addAll(sources);
		}
		if (result.isEmpty()) {
			return Collections.emptySet();
		}
		return result;
	}
	
	/**
	 * Clean up the internal data structures, usually done as last operation.
	 */
	public void dispose() {
		mappingCache.clear();
		if (disposeReasonerP) {
			reasoner.dispose();
		}
	}

}
