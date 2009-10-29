package am.app.feedback.measures;

import am.app.feedback.CandidateMapping;
import am.app.feedback.CandidateSelection;
import am.app.feedback.ExtendedAlignment;
import am.app.mappingEngine.AlignmentSet;
import am.app.ontology.Node;

public class FamilialSimilarity extends AbstractRMeasure {

	double STEP1_MULTIPLIER = 1.00d;
	
	public FamilialSimilarity(CandidateSelection cs) {
		super();
		// TODO Auto-generated constructor stub
	}

	public AlignmentSet<CandidateMapping> getCandidates() {
		
		AlignmentSet<CandidateMapping> candidates = new AlignmentSet<CandidateMapping>();
		
		candidates.addAll(computeStep1Relevances());
		
		//candidates.sort();
		
		return null;
		
	}

	/**
	 * Computes step1 relevances.
	 * @return
	 */
	private AlignmentSet<CandidateMapping> computeStep1Relevances() {
		AlignmentSet<CandidateMapping> step1Candidates = new AlignmentSet<CandidateMapping>();
		
		AlignmentSet<ExtendedAlignment> currentAlignments = null;
		
				
		for( int i = 0; i < currentAlignments.size(); i++ ) {
			ExtendedAlignment p = currentAlignments.getAlignment(i);
			
			Node sourceNode = p.getEntity1();
			Node targetNode = p.getEntity2();
			
			int n = sourceNode.getChildren().size();
			int m = targetNode.getChildren().size();
			
			double relevance = (n + m) * STEP1_MULTIPLIER;
			
			CandidateMapping q = new CandidateMapping( p, relevance);
			
			step1Candidates.addAlignment(q);
			
		}
		
		
		
		return null;
	}
	
}