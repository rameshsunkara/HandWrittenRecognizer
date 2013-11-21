package edu.louisiana.cacs.csce521.handwritternrecognizer;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.louisiana.cacs.csce521.handwritternrecognizer.exception.HWRException;
import edu.louisiana.cacs.csce521.handwritternrecognizer.util.Configurator;

/**
 * <p>Main class which calls required methods. 
 *      
 *  </p>
 *  <p>It also configures the system by reading the HandWrittenRecognizer.properties.<p>
 * @author rsunkara
 * @since November 16,2013
 *
 */
public class Main {
	
	private static Log m_logger = LogFactory.getLog(Main.class);

	public static void main(String []args){
		
		Configurator xConfigurator = new Configurator();
		String xPropertiesFilePath = System.getProperty("user.dir") + File.separator
				+ "src" + File.separator + "main" + File.separator
				+ "resources" + File.separator + "HandWrittenRecognizer.properties";
		//If any error in system configuration, stop the system
		if(!xConfigurator.loadConfigValues(xPropertiesFilePath)){
			m_logger.fatal("Unable to configure the system.Exiting it.");
			return;
		}
			
		String[] xClassifierOptions = new String[1];
		 xClassifierOptions[0] = "-U";
		HWRClassifier hwrClassifier = new HWRClassifier(xConfigurator,"J48",xClassifierOptions);
		try {
			hwrClassifier.classifyData();
		} catch (HWRException e) {
			m_logger.error("Error occurred while classifying the data",e);
		}
		
	}

}
