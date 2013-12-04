package edu.louisiana.cacs.csce521.handwritternrecognizer.util;

import java.io.File;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Reads the given configuration file and acts as data renderer for other
 * classes.
 * 
 * @author rsunkara
 * 
 */
public class Configurator {

	private static final Log m_logger = LogFactory.getLog(Configurator.class);
	private Properties m_configFile = null;
	private String m_userdir = null;

	public Configurator() {
		m_configFile = null;
	}

	public boolean loadConfigValues(String p_configFilePath) {
		m_logger.debug("Enter loadConfigValues()");
		m_userdir = System.getProperty("user.dir") + File.separator;
		m_configFile = MyUtilities.loadProperties(p_configFilePath);
		if (m_configFile == null) {
			m_logger.fatal("Unable to load config values from:"
					+ p_configFilePath);
			return false;
		}
		m_logger.debug("Exit loadConfigValues()");
		return true;
	}

	public String get_resources_dir() {
		return m_userdir + m_configFile.getProperty("RESOURCES_DIR");
	}

	public String get_document_input_dir() {
		return get_resources_dir() + File.separator
				+ m_configFile.getProperty("DOC_INPUT_DIR");
	}

	public String get_output_dir() {
		return get_resources_dir() + File.separator
				+ m_configFile.getProperty("OUTPUT_DIR");
	}

	public String get_sample_train_file() {
		return get_document_input_dir() + File.separator
				+ m_configFile.getProperty("SAMPLE_TRAIN_FILE");
	}

	public String get_sample_test_file() {
		return get_document_input_dir() + File.separator
				+ m_configFile.getProperty("SAMPLE_TEST_FILE");
	}
	
	public String get_train_data_file() {
		return get_document_input_dir() + File.separator
				+ m_configFile.getProperty("TRAIN_FILE");
	}

	public String get_test_data_file() {
		return get_document_input_dir() + File.separator
				+ m_configFile.getProperty("TEST_FILE");
	}

	public String get_classified_labels_file() {
		return get_output_dir() + File.separator
				+ m_configFile.getProperty("CLASSIFIED_LABELS");
	}
	
	public String get_classified_data_file() {
		return get_output_dir() + File.separator
				+ m_configFile.getProperty("CLASSIFIED_DATA_FILE");
	}
	
	public String get_classifier(){
		return m_configFile.getProperty("CLASSIFIER");
	}
	
	public static void main(String[] args) {
		Configurator c = new Configurator();
		m_logger.debug(c.loadConfigValues(System.getProperty("user.dir")
				+ File.separator + "src" + File.separator + "main"
				+ File.separator + "resources" + File.separator
				+ "inforetrieval.properties"));
		m_logger.debug("INPUT DIR:" + c.get_document_input_dir());
	}

}
