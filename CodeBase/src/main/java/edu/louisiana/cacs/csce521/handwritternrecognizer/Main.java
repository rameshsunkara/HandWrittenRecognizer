package edu.louisiana.cacs.csce521.handwritternrecognizer;

import java.io.File;

import edu.louisiana.cacs.csce521.handwritternrecognizer.util.Configurator;

/**
 * <p>Main class which calls required constructors like 
 *      
 *  </p>
 *  <p>It also configures the system by reading the HandWrittenRecognizer.properties.<p>
 * @author rsunkara
 * @since November 16,2013
 *
 */
public class Main {

	public static void main(String []args){
		
		Configurator xConfigurator = new Configurator();
		String xPropertiesFilePath = System.getProperty("user.dir") + File.separator
				+ "src" + File.separator + "main" + File.separator
				+ "resources" + File.separator + "HandWrittenRecognizer.properties";
		//If any error in system configuration, stop the system
		if(!xConfigurator.loadConfigValues(xPropertiesFilePath))
			return;
		
		HWRClassifier hwrClassifier = new HWRClassifier(xConfigurator);
		hwrClassifier.classifyData();
		
	}

}
