package am.app.mappingEngine;

import javax.swing.JPanel;

public abstract class AbstractMatcherParametersPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -127558550285920273L;

	public AbstractMatcherParametersPanel() {
		super();
	}
	
	public AbstractParameters getParameters() {
		throw new RuntimeException("To be implemented in the real parameter class of the specific matcher");
	}
	
	public String checkParameters() {
		//If there are any constraints to be satisfied by matcher params
		//check them overriding this method
		//if there are no errors in parameters selected then return null or "", 
		//else return the message to be shown to the user to correct errors
		return null;
	}

}