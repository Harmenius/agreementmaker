package am.app.ontology;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;


import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.impl.OntResourceImpl;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import am.userInterface.vertex.*;
/**
 * This class represents an element of the ontology to be aligned.
 * we could use the Resource class of Jena directly, but accessing information would be slower
 * so we use our own structure, even though we keep the reference to the Jena structure
 *
 */
public class Node {
	
	/**This is the reference of the Resource in the ontology, this is a Jena class, when parsing an XML ontology there will be no resource	
	 *  the node is built taking information from this resource
	 *  if we have loaded an OWL ontology this resource will be instance of OntResource
	 *  if this node is a ClassNode, the resource will be instance of OntClass
	 *  Right now we don't need this information but we may use it in the future to access any other information in the ontology connected to this one
	 *   */
	private Resource resource;
	/**The OWL/RDF uri identifier, that is namespace#localname, all these informations are kept into the resource attribute but we keep them separate to access them easily*/
	private String uri = "";
	/**
	 * One of the string which can be used to compare two nodes
	 * In general the URI = namespace#localname, in the OWL ontology, often the localname is the same of label	
	 * usually is defined with rdf: ID */
	private String localName = "";
	/**
	 * Another string which can be used to compare nodes
	 * This should be a human readable version of localname. In a RDF or XML ontologies there are no labels
	 * In OWL ontology we can have a label, even though often is the same of name.
	 * rdfs:label
	 */
	private String label = "";
	/**
	 * Another string which can be used to compare nodes
	 * This is a longer description (more than one word) for this resource.In a RDF or XML ontologies there are no comments
	 * In OWL ontology we can have a comment.
	 * rdfs:comment
	 */
	private String comment = "";
	//SOME MORE INFORMATIONS THAT MY BE USED
	private String isDefinedByLabel = "";
	private String isDefinedByURI = "";
	private String isDefinedByComment = "";
	private String seeAlsoLabel = "";
	private String seeAlsoURI = "";
	private String seeAlsoComment = "";
	
	//if the node is a prop node then it this list contains the list of classes localnames which declare this prop
	//if the node is a class node then this list contains the list of properties declared by this class
	private ArrayList<String> propOrClassNeighbours = new ArrayList<String>();
	private ArrayList<String> individuals = new ArrayList<String>();
	
	
	/**
	 * A Class or Property in a OWL hierarchy may have more than one father. In this case all nodes in the subtree of the node with more fathes will be represented with duplicates in the hierarchy tree
	 * but it will be aligned only once. We won't be able to access the vertex of this node with getVertex(), because this node is represetend by more than one vertex
	 * so we will have to scan a the list of vertex of this node, unless we want to access any of his vertex.
	 *List of vertex representing the node in the graphical tree hierarchy
	 * Usually each node is represeted only by one vertex, but if the node hasDuplicate, it's represented more times
	 */
	private ArrayList<Vertex> vertexList = new ArrayList<Vertex>();
	/**RDF-node, OWL-classnode, OWL-propnode, OWL-unsatconcept, XML-node*/
	private String type;
	public final static String RDFNODE = "rdf-node";
	public final static String OWLCLASS = "owl-classnode";
	public final static String OWLPROPERTY = "owl-propertynode";
    public final static String XMLNODE = "xml-node";
	/**UNIQUE KEY IN THE RANGE OF TYPE to be used as index to retrieve this node from the list and from the matrix*/
	int index;
	
	//These fields are for the PRAMatching technique
	private Node matchedTo;
	private boolean matched;
	private int color;
	private Node parent;
	private int depth;
	/**
	 * XML constructor
	 * @param key
	 * @param name
	 * @param desc
	 * @param type
	 */
	public Node(int key, String name, String type) {
		localName = name;
		this.type = type;
		index = key;
	}
	
	/**RDF OWL Constructor*/
	public Node(int key, Resource r, String type) {
		String language = "EN";
		resource = r;
		uri = r.getURI();
		localName = r.getLocalName();
		this.type = type;
		if(r.canAs(OntResource.class)) {
			OntResource or = (OntResource)r.as(OntResource.class);
			label = or.getLabel(language);
			if(label == null || label == "")
				label = or.getLabel(null);
				if(label == null)
					label = "";
			//COmments
			comment = "";
			Literal l = null;
			OntResourceImpl ol = null;
			ExtendedIterator it = or.listComments(language);
			if(!it.hasNext())
				it = or.listComments(null);
			while(it.hasNext()) {
				l = (Literal)it.next();
				if(l!=null) comment+= l+" ";
			}
			//ANNOTATIONS: isDefBy and seeAlso I'm not considering "sameAs" "differentFrom" "disjointWith"
			it = or.listIsDefinedBy();
			isDefinedByLabel = "";
			isDefinedByURI = "";
			isDefinedByComment = "";
			l = null;
			OntResource or2;
			if(it.hasNext()) {
				ol = (OntResourceImpl)it.next();
				if(ol!= null && ol.canAs(OntResource.class)){
					or2 = (OntResource)ol.as(OntResource.class);
					isDefinedByLabel = or2.getLabel(language);
					if(isDefinedByLabel == null || isDefinedByLabel == "")
						isDefinedByLabel = or2.getLabel(null);
						if(isDefinedByLabel == null)
							isDefinedByLabel = "";
					isDefinedByComment = or2.getComment(language);
					if(isDefinedByComment == null || isDefinedByComment == "")
						isDefinedByComment = or2.getLabel(null);
						if(isDefinedByComment == null)
							isDefinedByComment = "";
					isDefinedByURI = ol.getURI();
				}
			}

			it = or.listSeeAlso();
			seeAlsoLabel = "";
			seeAlsoComment = "";
			seeAlsoURI = "";
			l = null;
			if(it.hasNext()) {
				ol = (OntResourceImpl)it.next();
				if(ol!= null && ol.canAs(OntResource.class)){
					or2 = (OntResource)ol.as(OntResource.class);
					seeAlsoLabel = or2.getLabel(language);
					if(seeAlsoLabel == null || seeAlsoLabel == "")
						seeAlsoLabel = or2.getLabel(null);
						if(seeAlsoLabel == null)
							seeAlsoLabel = "";
					seeAlsoComment = or2.getComment(language);
					if(seeAlsoComment == null || seeAlsoComment == "")
						seeAlsoComment = or2.getLabel(null);
						if(seeAlsoComment == null)
							seeAlsoComment = "";
						seeAlsoURI = ol.getURI();
				}
			}
			//properties and invidviduals lists only for classes
			if(!or.canAs(OntProperty.class)) {//remember is important to check on prop instead of class to avoid a jena bug that a prop canAs ontClass
				OntClass cls = (OntClass)or.as(OntClass.class);
				it = cls.listDeclaredProperties(true);
				String localname;
				while(it.hasNext()) {
					OntProperty op = (OntProperty)it.next();
					if(!op.isAnon()) {
						localname = op.getLocalName();
						propOrClassNeighbours.add(localname);
					}

				}
				it = cls.listInstances(true);
				while(it.hasNext()) {
					Individual ind = (Individual)it.next();
					localname = ind.getLabel(null);
					if(localname != null && !localname.equals(""))
						individuals.add(localname);
				}
			}
			else {
				OntProperty prop = (OntProperty)or.as(OntProperty.class);
				it = prop.listDeclaringClasses(true);
				String localname;
				while(it.hasNext()) {
					OntClass op = (OntClass)it.next();
					if(!op.isAnon()) {
						localname = op.getLocalName();
						propOrClassNeighbours.add(localname);
					}
				}
			}
		}
		index = key;
	}
	
	public Resource getResource() {
		return resource;
	}
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public String getLocalName() {
		return localName;
	}
	public void setLocalName(String localName) {
		this.localName = localName;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public boolean hasDuplicates() {
		return vertexList.size() > 1;
	}

	public ArrayList<Vertex> getVertexList() {
		return vertexList;
	}
	
	public void addVertex(Vertex v) {
		 vertexList.add(v);
	}
	
	public Vertex getVertex() {
		return vertexList.get(0);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	/**Owl classes or all rdf nodes or all xml nodes their are considered classes, so nodes in the first of the two trees*/
	public boolean isClass() {
		return type.equals(OWLCLASS) || type.equals(RDFNODE) ||  type.equals(XMLNODE);
	}
	
	public boolean isProp() {
		return type.equals(OWLPROPERTY);
	}
	
	public boolean equals(Object o) {
		if(o instanceof Node) {
			Node n = (Node)o;
			return n.getIndex() == index;
		}
		return false;
	}
	/*
	public int hashCode() {
		return index;
	}
	*/
	
	public String toString() {
		return index+" "+localName;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getIsDefinedByLabel() {
		return isDefinedByLabel;
	}

	public void setIsDefinedByLabel(String isDefinedBy) {
		this.isDefinedByLabel = isDefinedBy;
	}

	public String getSeeAlsoLabel() {
		return seeAlsoLabel;
	}

	public void setSeeAlsoLabel(String seeAlso) {
		this.seeAlsoLabel = seeAlso;
	}

	public ArrayList<String> getpropOrClassNeighbours() {
		return propOrClassNeighbours;
	}

	public ArrayList<String> getIndividuals() {
		return individuals;
	}

	public String getDescriptionsString() {
		String result = "";
		result+= "URI identifier: (namespace#localname)\n";
		result += uri+"\n\n";
		
		result += "Label: (rdfs:label)\n";
		result += label+"\n\n";
		
		return result;
	}

	public String getPropOrClassString() {
		String result = "Properties or Classes declared by this concept:\n\n";
		if(propOrClassNeighbours.size() == 0) {
			result+="The list is empty";
		}
		for(int i = 0; i < propOrClassNeighbours.size(); i++) {
			result+= propOrClassNeighbours.get(i)+"\n";
		}
		return result;
	}
	

	public String getAnnotationsString() {
		String result = "";
		result+= "Comments: (rdfs:comment)\n";
		result += comment+"\n\n";
		
		result += "SeeAlso: (owl:seeAlso)\n";
		result += seeAlsoLabel+"\n\n";
		
		result += "IsDefinedBy: (owl:isDefinedBy)\n";
		result += isDefinedByLabel+"\n\n";
		return result;
	}

	public String getIndividualsString() {
		String result = "List of individuals:\n\n";
		if(individuals.size() == 0) {
			result+="No instances found for this class";
		}
		for(int i = 0; i < individuals.size(); i++) {
			result+= individuals.get(i)+"\n";
		}
		return result;
	}
	
	//**********************Methods for printing*************************
	
	
	//**********************Methods for managing the node as a DAG node see also TreeToDagConverter********************************************
	//TO BE DONE isRoot, getSiblings, getParents, getAllDescendants, getAllAncestors, getSiblingsOfAParent(Node parent
	public int getLevel() {
		return getVertex().getLevel() - TreeToDagConverter.REALROOTSLEVEL;
	}
	
	public boolean isLeaf() {
		Vertex v = getVertex();
		if(v.isLeaf())
			return true;
		return false;
	}
	
	public boolean isRoot() {
		Vertex v = getVertex();
		if(v.getLevel() == TreeToDagConverter.REALROOTSLEVEL)
			return true;
		return false;
	}
	
	public ArrayList<Node> getChildren(){
		ArrayList<Node> result = new ArrayList<Node>();
		Vertex v = getVertex();
		if(!v.isLeaf()) {
			Enumeration<Vertex> c = v.children();
			Vertex child;
			while(c.hasMoreElements()) {
				//no need to check duplicates because the same vertex can't have two duplicates as sons
				child = (Vertex)c.nextElement();
				result.add(child.getNode());
			}
		}
		return result;
	}
	
	public ArrayList<Node> getParents(){
		ArrayList<Node> result = new ArrayList<Node>();
		if(!isRoot()){
			ArrayList<Vertex> list = getVertexList();
			
			if(list.size() == 1) { //I'm not a duplicate therefore I just have one original father
				
				Vertex tempVertex = list.get(0);
				Vertex parentVertex = (Vertex) tempVertex.getParent();
				if( parentVertex != null ) {  // we are not at the root
					Node parentNode = parentVertex.getNode();
					result.add(parentNode);
				}
			}
			else{
				//Being a duplicate means having more parents OR being the son of an ancestor with more fathers
				Vertex v;
				Vertex parent;
				Node parentNode;
				HashSet<Node> processed = new HashSet<Node>();
				for(int i = 0; i < list.size(); i++){
					v = list.get(i);
					parent = (Vertex)v.getParent();
					parentNode = parent.getNode();
					if(!processed.contains(parentNode)){
						result.add(parentNode);
						processed.add(parentNode);
					}
				}
			}
			
		}
		return result;
	}
	
	/**n is a descendant of this? same as vertex.isNodeDescendant*/
	public boolean isNodeDescendant(Node n){
		Vertex ancestor = getVertex();
		Iterator<Vertex> it = n.getVertexList().iterator();
		while(it.hasNext()) {
			Vertex v = it.next();
			if(ancestor.isNodeDescendant(v)) {
				return true;
			}
		}
		return false;
	}

	public String getIsDefinedByURI() {
		return isDefinedByURI;
	}

	public void setIsDefinedByURI(String isDefinedByURI) {
		this.isDefinedByURI = isDefinedByURI;
	}

	public String getIsDefinedByComment() {
		return isDefinedByComment;
	}

	public void setIsDefinedByComment(String isDefinedByComment) {
		this.isDefinedByComment = isDefinedByComment;
	}

	/**
	 * @param matchedTo the matchedTo to set
	 */
	public void setMatchedTo(Node matchedTo) {
		this.matchedTo = matchedTo;
	}

	/**
	 * @return the matchedTo
	 */
	public Node getMatchedTo() {
		return matchedTo;
	}

	/**
	 * @param matched the matched to set
	 */
	public void setMatched(boolean matched) {
		this.matched = matched;
	}

	/**
	 * @return the matched
	 */
	public boolean isMatched() {
		return matched;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(int color) {
		this.color = color;
	}

	/**
	 * @return the color
	 */
	public int getColor() {
		return color;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Node parent) {
		this.parent = parent;
	}

	/**
	 * @return the parent
	 */
	public Node getParent() {
		return parent;
	}

	/**
	 * @param depth the depth to set
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}

	/**
	 * @return the depth
	 */
	public int getDepth() {
		return depth;
	}




}