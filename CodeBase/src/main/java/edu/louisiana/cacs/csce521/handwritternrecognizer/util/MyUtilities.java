package edu.louisiana.cacs.csce521.handwritternrecognizer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Contains utility methods used in this application.
 * @author rsunkara
 *
 */
public class MyUtilities {
	
	private static Log m_logger = LogFactory.getLog(MyUtilities.class);
	
	/**
	 * Loads the given properties file into Properties object.
	 * @param p_PropertiesFilePath
	 * @return
	 */
	public static Properties loadProperties(String p_PropertiesFilePath) {
		m_logger.debug("Enter loadProperties()");
		if (m_logger.isTraceEnabled()) {
			m_logger.trace("File passed:" + p_PropertiesFilePath);
		}
		Properties xProperties = null;
		InputStream xPropertiesSteam = null;
		try {
			xPropertiesSteam = new FileInputStream(p_PropertiesFilePath);
			xProperties = new Properties();
			xProperties.load(xPropertiesSteam);
		} catch (FileNotFoundException e) {
			m_logger.error("FileNotFoundException caught while loading:"
					+ p_PropertiesFilePath, e);
			return null;
		} catch (IOException e) {
			m_logger.error("IOException caught while loading:"
					+ p_PropertiesFilePath, e);
			return null;
		} finally {
			if (xPropertiesSteam != null)
				try {
					xPropertiesSteam.close();
				} catch (IOException e) {
					m_logger.error(
							"Potential Memory leak, Exception occured while closing stream",
							e);
				}
		}
		if(m_logger.isTraceEnabled()){
			m_logger.trace("No.of properties loaded:"+xProperties.size());
		}
		m_logger.debug("Exit loadProperties()");
		return xProperties;
	}

	/**
	 * Finds the list of files in the given directory path
	 * @param p_inputDir
	 * @return
	 */
	public static File[] getDocumentList(String p_inputDir){
		File f = new File(p_inputDir);		
		return f.listFiles();
	}	
}
