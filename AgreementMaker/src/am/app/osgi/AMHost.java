package am.app.osgi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class AMHost {
	private AMActivator m_activator = null;
    private Felix m_felix = null;
    private OSGiRegistry registry;
    
    public AMHost()
    {
        // Create a configuration property map.
        Map<String, Object> configMap = new HashMap<String, Object>();
        // Create host activator;
        m_activator = new AMActivator();
        List<BundleActivator> list = new ArrayList<BundleActivator>();
        list.add(m_activator);
        configMap.put(FelixConstants.FRAMEWORK_STORAGE_CLEAN, FelixConstants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT );
        configMap.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, list);
        configMap.put(FelixConstants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
                "am.app.mappingEngine," +
                "am.app.ontology," +
                "edu.smu.tspell.wordnet,"+
                "am,"+
                "am.app,"+
                "am.app.mappingEngine.StringUtil,"+ 
                "am.utility," +
                "am.app.ontology.profiling," +
                "am.userInterface," +
                "org.junit," +
                "com.hp.hpl.jena.ontology," +
                "com.hp.hpl.jena,"+
                "am.app.lexicon,"+
                "am.app.lexicon.subconcept,"+
                "uk.ac.shef.wit.simmetrics.similaritymetrics," +
                "am.app.mappingEngine.oaei," +
                "com.hp.hpl.jena.rdf.model," +
                "org.apache.log4j, " +
                "am.app.mappingEngine.similarityMatrix," +
                "com.wcohen.ss.api," +
                "simpack.measure.weightingscheme," +
                "am.app.mappingEngine.referenceAlignment," +
                "am.app.ontology.ontologyParser," +
                "am.output.alignment.oaei,"+
                "com.hp.hpl.jena.util.iterator,"+
                "com.hp.hpl.jena.graph,"+
                "edu.smu.tspell.wordnet,"+
                "am.app.mappingEngine.LinkedOpenData,"+
                "com.hp.hpl.jena.util,"+
                "am.utility.referenceAlignment,"+
                "am.visualization.graphviz.wordnet,"+
                "arq.examples.propertyfunction,"+
                "am.app.mappingEngine.hierarchy,"+
                "am.app.mappingEngine.qualityEvaluation"); 
        

        //File bundles[] = new File("plugins/").listFiles();

        configMap.put(AutoProcessor.AUTO_DEPLOY_ACTION_PROPERY, AutoProcessor.AUTO_DEPLOY_INSTALL_VALUE + "," + AutoProcessor.AUTO_DEPLOY_START_VALUE);
        configMap.put(AutoProcessor.AUTO_DEPLOY_DIR_PROPERY, "plugins/");
        try
        {
            // Now create an instance of the framework with
            // our configuration properties.
            m_felix = new Felix(configMap);
            // Now start Felix instance.
            m_felix.init();
            
            AutoProcessor.process(configMap, m_felix.getBundleContext());
            
            m_felix.start();
            
            //Core.getInstance().setContext(m_felix.getBundleContext());
            //create the registry
            registry = new OSGiRegistry(m_felix.getBundleContext());
        }
        catch (Exception ex)
        {
            System.err.println("Could not create framework: " + ex);
            ex.printStackTrace();
        }
    }

    public Bundle[] getInstalledBundles()
    {
        // Use the system bundle activator to gain external
        // access to the set of installed bundles.
        return m_activator.getBundles();
    }

    public void shutdownApplication()
    {
        // Shut down the felix framework when stopping the
        // host application.
        try {
			m_felix.stop();
			m_felix.waitForStop(0);
		} catch (Exception e) {e.printStackTrace();}
        
    }
    
    public OSGiRegistry getRegistry() { return registry; }
    
    public BundleContext getContext() { return m_felix.getBundleContext(); }
}
