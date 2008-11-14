package agreementMaker.application.mappingEngine;

import java.util.ArrayList;
import java.util.Iterator;

import agreementMaker.Utility;
import agreementMaker.application.Core;
import agreementMaker.application.ontology.Node;
import agreementMaker.application.ontology.Ontology;

public abstract class AbstractMatcher implements Matcher{
	
	/**Unique identifier of the algorithm used in the JTable list as index
	 * if an algorithm gets deleted we have to decrease the index of all others by one
	 * */
	protected int index;
	/**Name of the algorithm, there should be also a final static String in the instance class
	 * in the constructor of the non-abstract class should happen "name = FINALNAME"
	 * */
	protected String name;
	/**User mapping should be the only one with this variable equal to false*/
	protected boolean isAutomatic;
	/**True if the algorithm needs additional parameter other than threshold, in this case the developer must develop a JFrame to let the user define them*/
	protected boolean needsParam;
	/**Parameter of this method, if needsParam this item will be generated by the userinterface if not this will be automatically generated with default values*/
	protected AbstractParameters param;
	/**True means that AM should show its alignments*/
	protected boolean isShown;
	protected boolean modifiedByUser;
	protected double threshold;
	
	/**ANY means any numer of relations for source or target*/
	public final static int ANY_INT = Integer.MAX_VALUE;
	protected int maxSourceAlign;
	protected int maxTargetAlign;
	
	/**Contain alignments, NULL if alignment has not been calculated*/
	protected AlignmentSet propertiesAlignmentSet;
	protected AlignmentSet classesAlignmentSet;
	
	/**Structure containing similarity values between classes nodes, matrix[source][target]
	 * should not be accessible outside of this class, the system should only be able to access alignments sets
	 * */
	protected AlignmentMatrix classesMatrix;
	/**Structure containing similarity values between classes nodes, matrix[source][target]*/
	protected AlignmentMatrix propertiesMatrix;
	
	/**Reference to the Core istances*/
	protected Ontology sourceOntology;
	protected Ontology targetOntology;
	
	/**If the algo calculates prop alignments*/
	protected boolean alignProp;
	/**If the algo calculates prop alignments*/
	protected boolean alignClass;
	/***Some algorithms may need other algorithms as input*/
	protected ArrayList<AbstractMatcher> inputMatchers;
	/**Minum and maximum number of input matchers
	 * a generic matcher which doesn't need any inputs should have 0, 0
	 * */
	protected int minInputMatchers;
	protected int maxInputMatchers;
	/**Keeps info about reference evaluation of the matcher. is null until the algorithm gets evaluated*/
	protected ResultData refEvaluation;
	
	
	public AbstractMatcher(int key) {
		index = key;
		name = "Empty Algorithm";
		isAutomatic = true;
		needsParam = false;
		isShown = true;
		modifiedByUser = false;
		threshold = 0.75;
		maxSourceAlign = ANY_INT;
		maxTargetAlign = 1;
		alignClass = true;
		alignProp = true;
		minInputMatchers = 0;
		maxInputMatchers = 0;
		//ALIGNMENTS LIST MUST BE NULL UNTIL THEY ARE CALCULATED
		sourceOntology = Core.getInstance().getSourceOntology();
		targetOntology = Core.getInstance().getTargetOntology();
		inputMatchers = new ArrayList<AbstractMatcher>();
	}
	
	//***************************ALL METHODS TO PERFORM THE ALIGNMENT**********************************
    public void match() {
    	if(maxInputMatchers > 0 && inputMatchers.size() > 0) {
    		analyzeInputMatchers();
    	}
    	align();
    	selectAndSetAlignments();						
    }

    private void analyzeInputMatchers() {
    	//TO BE OVERRIDDEN IN THE MATCHER IF IT HAS maxInputMatchers > 0
	}

	private void align() {
		if(alignClass) {
			ArrayList<Node> sourceClassList = sourceOntology.getClassesList();
			ArrayList<Node> targetClassList = targetOntology.getClassesList();
			classesMatrix = alignClasses(sourceClassList,targetClassList );			
		}
		if(alignProp) {
			ArrayList<Node> sourcePropList = sourceOntology.getPropertiesList();
			ArrayList<Node> targetPropList = targetOntology.getPropertiesList();
			propertiesMatrix = alignProperties(sourcePropList, targetPropList );					
		}

	}

	private AlignmentMatrix alignProperties(ArrayList<Node> sourcePropList, ArrayList<Node> targetPropList) {
		return alignNodesOneByOne(sourcePropList, targetPropList);
	}

	private AlignmentMatrix alignClasses(ArrayList<Node> sourceClassList, ArrayList<Node> targetClassList) {
		return alignNodesOneByOne(sourceClassList, targetClassList);
	}
	
	private AlignmentMatrix alignNodesOneByOne(ArrayList<Node> sourceList, ArrayList<Node> targetList) {
		AlignmentMatrix matrix = new AlignmentMatrix(sourceList.size(), targetList.size());
		Node source;
		Node target;
		Alignment alignment; //Temp structure to keep sim and relation between two nodes, shouldn't be used for this purpose but is ok
		for(int i = 0; i < sourceList.size(); i++) {
			source = sourceList.get(i);
			for(int j = 0; j < targetList.size(); j++) {
				target = targetList.get(j);
				alignment = alignTwoNodes(source, target);
				matrix.set(i,j,alignment);
			}
		}
		return matrix;
	}

	private Alignment alignTwoNodes(Node source, Node target) {
		//TO BE IMPLEMENTED BY THE ALGORITHM, THIS IS JUST A FAKE ABSTRACT METHOD
		double sim;
		String rel = Alignment.EQUIVALENCE;
		if(source.getLocalName().equals(target.getLocalName())) {
			sim = 1;
		}
		else {
			sim = 0;
		}
		return new Alignment(source, target, sim, rel);
	}
	
	private void selectAndSetAlignments() {
		if(maxSourceAlign == 1 && maxTargetAlign == 1) {
			//TO BE DEVELOPED USING MAX WEIGHTED MATCHING ON BIPARTITE GRAPH, SOLVED USING DAJKSTRA
			scanForMaxValues();//TO BE CHANGED
		}
		else if(maxSourceAlign != ANY_INT && maxTargetAlign != ANY_INT) {
			//TO BE DEVELOPED: I DON'T KNOW YET HOW TO DO THIS
			scanForMaxValues();//TO BE CHANGED
		}
		else {//AT LEAST ONE OF THE TWO CONSTRAINTs IS ANY, SO WE JUST HAVE TO PICK ENOUGH MAX VALUES TO SATISFY OTHER CONSTRAINT 
			scanForMaxValues();
		}
	}

	private void scanForMaxValues() {
		if(alignClass) {
			classesAlignmentSet = scanForMaxValuesMatrix(classesMatrix);
		}
		if(alignProp) {
			propertiesAlignmentSet = scanForMaxValuesMatrix(propertiesMatrix);
		}
	}
	
	private AlignmentSet scanForMaxValuesMatrix(AlignmentMatrix matrix){
		AlignmentSet aset;
		int numMaxValues;
		//IF both values are ANY we can have at most maxSourceRelations equals to the target nodes and maxTargetRelations equal to source node
		if(maxTargetAlign == ANY_INT  && maxSourceAlign == ANY_INT) {
			maxTargetAlign = matrix.getRows();
			maxSourceAlign = matrix.getColumns();
		}
		if(maxTargetAlign >= maxSourceAlign) {//Scan rows and then columns
			numMaxValues = maxSourceAlign;
			aset = scanForMaxValuesRowColumn(matrix, numMaxValues);
		}
		else {//scan column and then row
			numMaxValues = maxTargetAlign;
			aset = scanForMaxValuesColumnRow(matrix, numMaxValues);
		}
		return aset;
	}

	private AlignmentSet scanForMaxValuesRowColumn(AlignmentMatrix matrix, int numMaxValues) {
		AlignmentSet aset = new AlignmentSet();
		Alignment currentValue;
		Alignment currentMax;
		//temp structure to keep the first numMaxValues best alignments for each source
		//when maxRelations are both ANY we could have this structure too big that's why we have checked this case in the previous method
		Alignment[] maxAlignments = new Alignment[numMaxValues];
		for(int i = 0; i<matrix.getRows();i++) {
			for(int j = 0; j<matrix.getColumns();i++) {
				currentValue = matrix.get(i,j);
				int k = 0;
				currentMax = maxAlignments[k];
				for(k = 1; currentValue.getSimilarity() > currentMax.getSimilarity() && k < maxAlignments.length; k++) {
					maxAlignments[k-1] = currentValue;
					currentValue = currentMax;
					currentMax = maxAlignments[k];
				}
			}
			currentValue = maxAlignments[0];
			for(int e = 0;currentValue.getSimilarity() >= threshold && e < maxAlignments.length; e++) {
				currentValue = maxAlignments[e];
				aset.addAlignment(currentValue);
			}
		}
		return aset;
	}

	private AlignmentSet scanForMaxValuesColumnRow(AlignmentMatrix matrix,int numMaxValues) {
		AlignmentSet aset = new AlignmentSet();
		Alignment currentValue;
		Alignment currentMax;
		Alignment[] maxAlignments = new Alignment[numMaxValues];//temp structure to keep the first numMaxValues best alignments for each source
		//Build the array with numMaxValues max alignment
		for (int j = 0; j<matrix.getColumns();j++){
			for (int i = 0; i<matrix.getRows();i++){
				currentValue = matrix.get(i,j);
				int k = 0;
				currentMax = maxAlignments[k];
				for(k = 1; currentValue.getSimilarity() > currentMax.getSimilarity() && k < maxAlignments.length; k++) {
					maxAlignments[k-1] = currentValue;
					currentValue = currentMax;
					currentMax = maxAlignments[k];
				}
			}
			//build an alignmentSet from the array with max alignments
			currentValue = maxAlignments[0];
			for(int e = 0;currentValue.getSimilarity() >= threshold && e < maxAlignments.length; e++) {
				currentValue = maxAlignments[e];
				aset.addAlignment(currentValue);
			}
		}
		return aset;
	}

	//*****************Other methods ******************************************
	public AbstractMatcherParametersPanel getParametersPanel() {
		throw new RuntimeException("To be implemented in the real matcher subclass");
		//This method must create and return the AbstractMatcherParameter subclass so that the user can select additional parameters needed by the matcher
		//if the matcher doesn't need any parameter then the attribute needsParameters must be false and this method won't be invoked.
	}
	
	public AlignmentSet getAlignmentSet() {
    	AlignmentSet aligns = new AlignmentSet();
    	if(areClassesAligned()) {
    		aligns.addAll(classesAlignmentSet);
    	}
    	if(arePropertiesAligned()) {
    		aligns.addAll(propertiesAlignmentSet);
    	}
    	return aligns;
    }

    public AlignmentSet getClassAlignmentSet() {
    	return classesAlignmentSet;
    }

    public AlignmentSet getPropertyAlignmentSet() {
    	return propertiesAlignmentSet;
    }
    /**AgreementMaker doesn't calculate instances matching, if you add this you should also modify getAlignmenSet*/
    public AlignmentSet getInstanceAlignmentSet() {
    	throw new RuntimeException("trying to invoking a function not implemented yet");
    }
    
    public boolean areClassesAligned() {
    	return classesAlignmentSet != null;
    }
    
    public boolean arePropertiesAligned() {
    	return propertiesAlignmentSet != null;
    }
    
    public boolean isSomethingAligned() {
    	return areClassesAligned() || arePropertiesAligned();
    }
    
    public int getNumberClassAlignments() {
    	int numAlign = 0;
		if(areClassesAligned()) {
			numAlign += getClassAlignmentSet().size();
		}
		return numAlign;
    }
    
    public int getNumberPropAlignments() {
    	int numAlign = 0;
		if(arePropertiesAligned()) {
			numAlign += getPropertyAlignmentSet().size();
		}
		return numAlign;
    }
    
    public int getTotalNumberAlignments() {
    	return getNumberClassAlignments()+getNumberPropAlignments();
    }

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAutomatic() {
		return isAutomatic;
	}

	public void setAutomatic(boolean isAutomatic) {
		this.isAutomatic = isAutomatic;
	}

	public boolean needsParam() {
		return needsParam;
	}

	public void setNeedsParam(boolean needsParam) {
		this.needsParam = needsParam;
	}

	public AbstractParameters getParam() {
		return param;
	}

	public void setParam(AbstractParameters param) {
		this.param = param;
	}

	public boolean isShown() {
		return isShown;
	}

	public void setShown(boolean isShown) {
		this.isShown = isShown;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public int getMaxSourceAlign() {
		return maxSourceAlign;
	}

	public void setMaxSourceAlign(int maxSourceAlign) {
		this.maxSourceAlign = maxSourceAlign;
	}

	public int getMaxTargetAlign() {
		return maxTargetAlign;
	}

	public void setMaxTargetAlign(int maxTargetAlign) {
		this.maxTargetAlign = maxTargetAlign;
	}

	public int getMinInputMatchers() {
		return minInputMatchers;
	}

	public void setMinInputMatchers(int minInputMatchers) {
		this.minInputMatchers = minInputMatchers;
	}

	public int getMaxInputMatchers() {
		return maxInputMatchers;
	}

	public void setMaxInputMatchers(int maxInputMatchers) {
		this.maxInputMatchers = maxInputMatchers;
	}

	public ArrayList<AbstractMatcher> getInputMatchers() {
		return inputMatchers;
	}
    
	public void addInputMatcher(AbstractMatcher a) {
		inputMatchers.add(a);
	}

	public boolean isModifiedByUser() {
		return modifiedByUser;
	}

	public void setModifiedByUser(boolean modifiedByUser) {
		this.modifiedByUser = modifiedByUser;
	}

	public ResultData getRefEvaluation() {
		return refEvaluation;
	}

	public void setRefEvaluation(ResultData evaluation) {
		this.refEvaluation = evaluation;
	}
	
	public boolean isRefEvaluated() {
		return refEvaluation != null;
	}

	public boolean isAlignProp() {
		return alignProp;
	}

	public boolean isAlignClass() {
		return alignClass;
	}

	public void setAlignProp(boolean alignProp) {
		this.alignProp = alignProp;
	}

	public void setAlignClass(boolean alignClass) {
		this.alignClass = alignClass;
	}

	
	/**
	 * Matcher details you can override this method to add or change you matcher details if needed, it is only invoked clicking on the button view details in the control panel
	 * @return a string with details of the matchers
	 */
	public String getDetails() {
		// TODO Auto-generated method stub
		String s = "";
		s+= "Matcher: "+getName()+"\n\n";
		s+= "Additional parameters required: "+Utility.getYesNo(needsParam())+"\n";
		s+= "Min number of matchers in input: "+getMinInputMatchers()+"\n";
		s+= "Max number of matchers in input: "+getMaxInputMatchers()+"\n";
		s+= "Performs Classes alignment: "+Utility.getYesNo(isAlignClass())+"\n";
		s+= "Performs Properties alignment: "+Utility.getYesNo(isAlignProp())+"\n";
		return s;
	}
	
}
