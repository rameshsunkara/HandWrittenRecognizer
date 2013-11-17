package edu.louisiana.cacs.csce521.handwritternrecognizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import edu.louisiana.cacs.csce521.handwritternrecognizer.util.Configurator;

public class HWRClassifier {
	
	private static Log m_logger = LogFactory.getLog(HWRClassifier.class);
	
	private Configurator m_hwrConfig = null;
	
	public HWRClassifier(Configurator p_hwrConfig){
		m_hwrConfig = p_hwrConfig;
	}

	public void classifyData(){
		readData();
	}

	private void readData() {
		
		DataSource source = null;
		Instances data = null;
		try {
			
			System.out.println(m_hwrConfig.get_sample_train_file());
			source = new DataSource(m_hwrConfig.get_sample_train_file());
			 data = source.getDataSet();
		} catch (Exception e) {
			m_logger.error("Error while reading the test data", e);
		}
		 
		 // setting class attribute if the data format does not provide this information
		 // For example, the XRFF format saves the class attribute information as well
		 if (data.classIndex() == -1)
		   data.setClassIndex(data.numAttributes() - 1);
	}
	
}
