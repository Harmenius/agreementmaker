package am.extension.userfeedback.ui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import am.Utility;
import am.extension.userfeedback.UFLExperiment;
import am.extension.userfeedback.UFLExperimentSetup;
import am.extension.userfeedback.UFLRegistry.CSEvaluationRegistry;
import am.extension.userfeedback.UFLRegistry.CandidateSelectionRegistry;
import am.extension.userfeedback.UFLRegistry.ExperimentRegistry;
import am.extension.userfeedback.UFLRegistry.FeedbackPropagationRegistry;
import am.extension.userfeedback.UFLRegistry.InitialMatcherRegistry;
import am.extension.userfeedback.UFLRegistry.PropagationEvaluationRegistry;
import am.extension.userfeedback.UFLRegistry.UserValidationRegistry;
import am.extension.userfeedback.experiments.UFLControlLogic;
import am.userInterface.UI;

public class UFLControlGUI extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -967696425990716259L;
	
	public final static String UNLIMITED 			= "Unlimited";
	public final static String A_MAPPING_CORRECT 	= "Validate selected candidate mapping";
	public final static String A_ALL_MAPPING_WRONG 	= "Unvalidate all candidate mappings";
	public final static String A_CONCEPT_WRONG 		= "Unvalidate selected candidate concept";
	public final static String A_ALL_CONCEPT_WRONG 	= "Unvalidate all candidate concepts";
	

    // the parts of the experiment
    private UFLExperiment				experimentSetup;
    
    public enum ActionCommands {
    	INITSCREEN_cmbExperiment,
    	INITSCREEN_cmbMatcher, 
    	INITSCREEN_btnStart, 
    	INITSCREEN_cmbCandidate,
    	INITSCREEN_cmbCSEvaluation,
    	INITSCREEN_cmbUserFeedback,
    	INITSCREEN_cmbPropagationEvaluation,  
    	INITSCREEN_cmbPropagation,
    	
    	EXECUTION_SEMANTICS_DONE, 
    	CANDIDATE_SELECTION_DONE, 
    	CS_EVALUATION_DONE, 
    	USER_FEEDBACK_DONE, 
    	PROPAGATION_DONE, 
    	PROPAGATION_EVALUATION_DONE,
    	;
    }
    
    
	private UFLControlGUI_InitialSettingsPanel panel;

	UI ui;
	
	
	public UFLControlGUI(UI u) {
		ui = u;
	}


	
	//****************UI Functions************************
	
	/**
	 * This is the screen that gets displayed when the UFL GUI is first shown to the user.
	 */
	public void displayInitialScreen() {
		
		removeAll();
		panel=new UFLControlGUI_InitialSettingsPanel();
		panel.addActionListener(this);
		
		this.setLayout(new FlowLayout(FlowLayout.CENTER));
		this.add(panel);
		
		repaint();
	}
	
	public void displayPanel( JPanel panel ) {
		removeAll();		
		this.setLayout(new FlowLayout(FlowLayout.CENTER));
		this.add(panel);
		repaint();
	}
	
	
	/* actionPerformed.  Almost all the real work is done here. */
	public void actionPerformed(ActionEvent e) {
		
		System.out.println(e.getActionCommand());  // TODO: Remove this.
		
		if( experimentSetup != null && experimentSetup.experimentHasCompleted() ) return; // check stop condition
		
		try{
	
			if( e.getActionCommand() == ActionCommands.INITSCREEN_btnStart.name() ) {
				
				UFLExperimentSetup setup = new UFLExperimentSetup();
				setup.im = (InitialMatcherRegistry) panel.cmbMatcher.getSelectedItem();
				setup.cs = (CandidateSelectionRegistry) panel.cmbCandidate.getSelectedItem();
				setup.cse = (CSEvaluationRegistry) panel.cmbCSEvaluation.getSelectedItem();
				setup.uv = (UserValidationRegistry) panel.cmbUserFeedback.getSelectedItem();
				setup.fp = (FeedbackPropagationRegistry) panel.cmbPropagation.getSelectedItem();
				setup.pe = (PropagationEvaluationRegistry) panel.cmbPropagationEvaluation.getSelectedItem();
				
				// the experiment is starting, or we have just completed an iteration of the loop (assuming the propagation evaluation is done last)

				// Step 1.  experiment is starting.  Initialize the experiment setup.
				ExperimentRegistry experimentRegistryEntry = (ExperimentRegistry) panel.cmbExperiment.getSelectedItem();
				experimentSetup = experimentRegistryEntry.getEntryClass().newInstance();
				experimentSetup.gui = this;
				experimentSetup.setup = setup;
				
				final UFLControlLogic logic = experimentSetup.getControlLogic();
				
				Thread thread = new Thread(new Runnable(){

					@Override
					public void run() {
						logic.runExperiment(experimentSetup);
					}
					
				});
				
				thread.start();
				
				return;
			}
			
		}
		catch(Exception ex) {
			ex.printStackTrace();
			Utility.displayErrorPane(Utility.UNEXPECTED_ERROR + "\n\n" + ex.getMessage(), Utility.UNEXPECTED_ERROR_TITLE);
		}
	}
	
}
