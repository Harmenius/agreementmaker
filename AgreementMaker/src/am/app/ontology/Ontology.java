package am.app.ontology;

import java.util.ArrayList;
import java.util.HashMap;

import am.GlobalStaticVariables;
import am.app.mappingEngine.AbstractMatcher.alignType;
import am.app.mappingEngine.qualityEvaluation.metrics.joslyn.JoslynStructuralQuality;
import am.userInterface.sidebar.vertex.Vertex;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;

/**
 * This class contains all information about one of the two ontologies to be compared
 * You get access to it via the Core instance
 *
 */
public class Ontology {
	
	public static final int ID_NONE = -1;  // used when there is no ontology id.
	
	// local title
	public static final String TARGETTITLE = "Target Ontology";
	// ontology title
	public static final String SOURCETITILE = "Source Ontology";
	//	OWL File type representation
	public static final int SOURCENODE = 0;
	//	OWL File type representation
	public static final int TARGETNODE = 1;
	public static final int XMLFILE = 2;
	public static final int OWLFILE = 1;
	public static final int RDFSFILE = 0;
	public static final int TABBEDTEXT = 3;
	//public static final int DAMLFILE = 3;
	
	public static final int RDFXML = 0;
	public static final int RDFXMLABBREV = 1;
	public static final int NTRIPLE = 2;
	public static final int N3  = 3;
	public static final int TURTLE = 4;

	public final static String SYNTAX_RDFXML = "RDF/XML";
	public final static String SYNTAX_RDFXMLABBREV = "RDF/XML-ABBREV";
	public final static String SYNTAX_NTRIPLE = "N-TRIPLE";
	public final static String SYNTAX_N3 = "N3";
	public final static String SYNTAX_TURTLE = "TURTLE";
	public final static String[] syntaxStrings  = {SYNTAX_RDFXML, SYNTAX_RDFXMLABBREV, SYNTAX_NTRIPLE, SYNTAX_N3, SYNTAX_TURTLE};
	public final static String LANG_RDFS = "RDFS";
	public final static String LANG_OWL = "OWL";
	public final static String LANG_XML = "XML";
	public final static String LANG_TABBEDTEXT = "Tabbed TEXT";
	public static final String[] languageStrings = {LANG_RDFS, LANG_OWL, LANG_XML, LANG_TABBEDTEXT};
	
	public static final int SOURCE = GlobalStaticVariables.SOURCENODE;
	public static final int TARGET = GlobalStaticVariables.TARGETNODE;
	
	
	/** 
	 * <p>It may be SOURCE or TARGET.  Use the final static int values in GSV to set this. (GlobalStaticVariables.SOURCENODE or GlobalStaticVariables.TARGETNODE)</p>
	 * <p>TODO: Change this to an enum.</p> 
	 * */
	private int sourceOrTarget;
	
	
	private String filename;//file name with all the path
	
	
	private String title;//usually is the name of the file without the path and is the name of the root vertex
	
	/**It may be XML, OWL, RDF*/
	private String language;
	/**For example RDF/XML for OWL language, in XML lanaguage is null*/
	private String format;
	/**reference to the Jena model class, for an OWL ontology it may be an OntModel, right now we don't use this element,  in XML lanaguage is null*/
	private OntModel model;
	
	/**List of class nodes to be aligned, IN THE CASE OF AN XML OR RDF ONTOLOGY ALL NODES ARE KEPT IN THIS STRUCTURE, so there will be only classes and no properties*/
	private ArrayList<Node> classesList = new ArrayList<Node>();
	/**List of property nodes to be aligned, IN THE CASE OF AN XML OR RDF ONTOLOGY there are no properties*/
	private ArrayList<Node> propertiesList = new ArrayList<Node>();
	
	/**The root of the classes hierarchy, is not the root of the whole tree but is the second node, the root vertex itself is fake doesn't refers to any node to be aligned, all sons of this node are classes to be aligned*/
	private Vertex classesTree;//in a XML or RDF ontology this will be the only tree
	/**The root of the properties hierarchy, is not the root of the whole tree but is the third node, the root vertex itself is fake doesn't refers to any node to be aligned, all sons of this node are classes to be aligned*/
	private Vertex propertiesTree;//in a XML or RDF ontology this will be null, while in a OWL ontology it contains at least the fake root "prop hierarchy"
	
	private Vertex deepRoot; // for the Canvas
	
	private boolean skipOtherNamespaces;
	
	private String URI;
	
	private ArrayList<DatatypeProperty> dataProperties;
	public ArrayList<DatatypeProperty> getDataProperties() {
		return dataProperties;
	}
	public void setDataProperties(ArrayList<DatatypeProperty> dtps) {
		dataProperties = dtps;
	}
	
	private ArrayList<ObjectProperty> objectProperties;
	public ArrayList<ObjectProperty> getObjectProperties() {
		return objectProperties;
	}
	public void setObjectProperties(ArrayList<ObjectProperty> ops) {
		objectProperties = ops;
	}	
	
	//END Instance related fields and functions
	
	/**
	 * This value is not used in the AM system right now, it is only used in the Conference Track when more than two ontologies are involved in the process.
	 */
	private int Index = 0;  // TODO: Maybe get rid of index, and work only with ID?
	private int ontID = 0;  // Index is used in the conference track, ID is used system wide.
	private int treeCount;
	
	public int  getIndex()          { return Index;  }
	public void setIndex(int index) { Index = index; }
	public int  getID()             { return ontID;     }
	public void setID(int id)       { ontID = id;       }
	
	public String getURI() {
		return URI;
	}
	public void setURI(String uri) {
		URI = uri;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public OntModel getModel() {
		return model;
	}
	public void setModel(OntModel model) {
		this.model = model;
	}
	public ArrayList<Node> getClassesList() {
		return classesList;
	}
	public void setClassesList(ArrayList<Node> classesList) {
		this.classesList = classesList;
	}
	public ArrayList<Node> getPropertiesList() {
		return propertiesList;
	}
	public void setPropertiesList(ArrayList<Node> propertiesList) {
		this.propertiesList = propertiesList;
	}

	
	public boolean isSource() {
		return sourceOrTarget == GlobalStaticVariables.SOURCENODE;
	}
	
	public boolean isTarget() {
		return sourceOrTarget == GlobalStaticVariables.TARGETNODE;
	}
	
	public void setSourceOrTarget(int s) {
		sourceOrTarget = s;
	}
	public Vertex getClassesTree()                   { return classesTree; }
	public void   setClassesTree(Vertex classesTree) { this.classesTree = classesTree; }
	public Vertex getDeepRoot()                      { return deepRoot; }
	public void   setDeepRoot(Vertex root)           { this.deepRoot = root; }
	
	public Vertex getPropertiesTree() {
		return propertiesTree;
	}
	public void setPropertiesTree(Vertex propertiesTree) {
		this.propertiesTree = propertiesTree;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	//used in UImenu.ontologyDetails()
	public String getClassDetails() {	
		return getDetails(classesList, classesTree);
	}
	
	//used in getClassDetails and getPropDetails
	private String getDetails(ArrayList<Node> list, Vertex tree) {
		TreeToDagConverter conv = new TreeToDagConverter(tree);
		
		int concepts = list.size();
		int depth = tree.getDepth()-1;
		int roots = conv.getRoots().size();
		int leaves = conv.getLeaves().size();
		JoslynStructuralQuality q = new JoslynStructuralQuality(); //the first two boolean dont matter here
		q.setParameters(true, true, true);
		double LCdiameter = q.getDiameter(list, conv);
		JoslynStructuralQuality q2 = new JoslynStructuralQuality(); //the first two boolean dont matter here
		q2.setParameters(true, true, true);
		double UCdiameter = q2.getDiameter(list, conv);
		
		return concepts+"\t"+depth+"\t"+UCdiameter+"\t"+LCdiameter+"\t"+roots+"\t"+leaves+"\n";
	}
	
	//used in UImenu.ontologyDetails()
	public String getPropDetails() {
		return getDetails(propertiesList, propertiesTree);
	}
	public boolean isSkipOtherNamespaces() {
		return skipOtherNamespaces;
	}
	public void setSkipOtherNamespaces(boolean skipOtherNamespaces) {
		this.skipOtherNamespaces = skipOtherNamespaces;
	}
	public int getSourceOrTarget() {
		
		return sourceOrTarget;
	}
	public void setTreeCount(int treeCount) { this.treeCount = treeCount; }
	public int  getTreeCount()              { return treeCount; }
	
	
	// used for mapping from OntResource to Nodes
	private HashMap<OntResource, Node> mapOntResource2Node_Classes = null;
	private HashMap<OntResource, Node> mapOntResource2Node_Properties = null;

	private String description;
	
	public void setOntResource2NodeMap(HashMap<OntResource, Node> processedSubs, alignType atype) {
		if( atype == alignType.aligningClasses ) {
			mapOntResource2Node_Classes = processedSubs;
		} else if( atype == alignType.aligningProperties ) {
			mapOntResource2Node_Properties = processedSubs;
		}
	}
	public Node getNodefromOntResource( OntResource r, alignType nodeType ) throws Exception {
		if( r == null ) {
			throw new Exception("Cannot search for a NULL resource.");
		}
		if( nodeType == alignType.aligningClasses ) {
			if( mapOntResource2Node_Classes.containsKey( r ) ){
				return mapOntResource2Node_Classes.get(r);	
			} else {
				throw new Exception("OntResource (" + r.toString() + ") is not a class in Ontology " + ontID + " (" + title + ").");
			}
		} else if( nodeType == alignType.aligningProperties ) {
			if( mapOntResource2Node_Properties.containsKey( r ) ){
				return mapOntResource2Node_Properties.get(r);	
			} else {
				throw new Exception("OntResource (" + r.toString() + ") is not a property in Ontology " + ontID + " (" + title + ").");
			}
		}
		
		throw new Exception("Cannot search for nodeType == " + nodeType.toString() );
	}
	
	public Node getNodefromIndex( int index, alignType aType ) throws Exception {
		if( aType == alignType.aligningClasses ) {
			if( index < classesList.size() ){
				return classesList.get(index);	
			}
		} else if( aType == alignType.aligningProperties ) {
			if( index < propertiesList.size() ){
				return propertiesList.get(index);	
			}
		}
		
		throw new Exception("Cannot search for nodeType == " + aType.toString() );
	}
	
	public void setDescription(String desc) { this.description = desc; }
	public String getDescription() { return description; }
	
}
