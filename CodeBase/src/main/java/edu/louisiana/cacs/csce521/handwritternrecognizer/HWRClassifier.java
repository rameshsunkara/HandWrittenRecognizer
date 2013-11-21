package edu.louisiana.cacs.csce521.handwritternrecognizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;
import weka.filters.unsupervised.attribute.NumericToNominal;
import edu.louisiana.cacs.csce521.handwritternrecognizer.exception.HWRException;
import edu.louisiana.cacs.csce521.handwritternrecognizer.util.Configurator;

/**
 * Reads the Pixel data of digitized numbers and builds the classifier
 * specified.
 * 
 * Then it uses the trained classifier to classify the test data.
 * 
 * @author rsunkara
 * @version 1.0
 * @since November, 14,2013
 * 
 */
public class HWRClassifier {

	private static Log m_logger = LogFactory.getLog(HWRClassifier.class);

	private Configurator m_hwrConfig = null;

	private Classifier m_Classifier = null;

	private Instances m_TrainingData = null;

	private Instances m_TestData = null;

	/**
	 * Constructs a HWRClassifier.
	 * 
	 * @param p_hwrConfig
	 *            - A <code>Configurator</code> object which holds all the
	 *            configured values of the system.
	 * @param p_ClassifierName
	 *            - A <code>String</code> representing the classifier name which
	 *            has to be used.
	 * @param p_options
	 *            - A <code>String[]</code> which holds the classifier options.
	 */
	public HWRClassifier(Configurator p_hwrConfig, String p_ClassifierName,
			String[] p_options) {
		m_hwrConfig = p_hwrConfig;
		try {
			// m_Classifier = Classifier.forName(p_ClassifierName, p_options);
			m_Classifier = new NaiveBayes();
			// m_Classifier.setOptions(p_options);
		} catch (Exception e) {
			m_logger.error("Exception caught while loading classifier", e);
		}
	}

	/**
	 * It first reads training and test instances. Then it makes the data
	 * classifiable and builds the classifier with cleaned up train data.
	 * 
	 * It classifies each instance present in the test data and prints the
	 * classified instances to output file and also print classified labels
	 * alone to a different file.
	 * 
	 * It uses best effort strategy i.e., if a error occurred during classifying
	 * a particular instance, it continues to classify the rest of the
	 * instances.
	 * 
	 * @throws HWRException
	 *             - Throws when a error occurs while building the classifier.
	 * @version 1.0
	 */
	public void classifyData() throws HWRException {

		// 1.Read Data
		preProcess();

		// 2.Build Classifier ( Train )
		try {
			m_Classifier.buildClassifier(m_TrainingData);
		} catch (Exception e) {
			m_logger.error("Exception caught while building classifier", e);
			throw new HWRException("Exception caught while building classifier");
		}

		// 3.Classify Test data
		Instances xClassifiedData = new Instances(m_TestData);
		double[] xObtainedClassLabels = new double[m_TestData.numInstances()];
		for (int i = 0; i < m_TestData.numInstances(); i++) {
			try {
				xObtainedClassLabels[i] = m_Classifier
						.classifyInstance(m_TestData.instance(i));
			} catch (Exception e) {
				m_logger.error("Error caught while classifying the instance:"
						+ m_TestData.instance(i));
			}
			xClassifiedData.instance(i).setClassValue(
					(int) xObtainedClassLabels[i]);
		}

		// 4.Prints the classified information
		printClassifiedData(xClassifiedData);
		printClassifiedData(xObtainedClassLabels);
	}

	/**
	 * It reads the Training data and test data.
	 * 
	 * It also modifies the data to make it classifiable.
	 * 
	 * @throws HWRException
	 *             - A custom exception will be thrown in case of any error
	 *             occurred during reading train/test data or while making data
	 *             classifiable.
	 * @since 1.0
	 */
	private void preProcess() throws HWRException {

		m_TrainingData = readData(m_hwrConfig.get_sample_train_file());
		m_TestData = readData(m_hwrConfig.get_sample_test_file());

		try {
			makeTrainDataClassifiable();
		} catch (Exception e) {
			m_logger.error("Error caught while making train data classifiable",
					e);
			throw new HWRException(
					"Error caught while making train data classifiable");
		}

		try {
			makeTestDataClassifiable();
		} catch (Exception e) {
			m_logger.error("Error caught while making test data classifiable",
					e);
			throw new HWRException(
					"Error caught while making test data classifiable");
		}
	}

	/**
	 * Reads the data present in the given file and returns
	 * <code>Instances</code>.
	 * 
	 * @param p_FilePath
	 *            - A <code>String</code> representing the absolute file path.
	 * @return xDataInstances - An <code>Instances</code> objects which holds
	 *         the data instances present in the given file.
	 * @throws HWRException
	 *             - A custom exception will be thrown in case of any error
	 *             occurred during reading data from given path.
	 * @since 1.0
	 */
	public Instances readData(String p_FilePath) throws HWRException {
		DataSource xDataSource = null;
		Instances xDataInstances = null;
		try {
			xDataSource = new DataSource(p_FilePath);
			xDataInstances = xDataSource.getDataSet();
		} catch (Exception e) {
			m_logger.error("Error while reading the train data", e);
			throw new HWRException("Error caught while reading data from :"
					+ p_FilePath);
		}
		return xDataInstances;
	}

	/**
	 * Makes the class labels which were numerical to be treated as Nominal
	 * values and sets the class label index.
	 * 
	 * @throws Exception
	 */
	private void makeTrainDataClassifiable() throws Exception {
		NumericToNominal nn = new NumericToNominal();
		nn.setAttributeIndices("first");
		nn.setInputFormat(m_TrainingData);

		m_TrainingData = Filter.useFilter(m_TrainingData, nn);
		if (m_TrainingData.classIndex() == -1) {
			m_TrainingData.setClassIndex(0);
		}
	}

	/**
	 * Adds a dummy labels to test data to make it sync with train data. It add
	 * nominal values of the class label and then sets the index for class
	 * label.
	 * 
	 * @throws Exception
	 */
	private void makeTestDataClassifiable() throws HWRException {
		// Add dummy label at first
		Add dummyLabel = new Add();
		dummyLabel.setAttributeIndex("first");
		dummyLabel.setAttributeName("label");
		dummyLabel.setNominalLabels("0,1,2,3,4,5,6,7,8,9");
		try {
			dummyLabel.setInputFormat(m_TestData);
		} catch (Exception e) {
			m_logger.error("Error caught while modifying test data",e);
			throw new HWRException("Error caught while modifying test data");
		}

		try {
			m_TestData = Filter.useFilter(m_TestData, dummyLabel);
		} catch (Exception e) {
			m_logger.error("Error caught while applying filter on test data",e);
			throw new HWRException("Error caught while applying filter on test data");
		}

		if (m_TestData.classIndex() == -1) {
			m_TestData.setClassIndex(0);
		}
	}

	/**
	 * Prints classified data in the test data format in csv file.
	 * 
	 * @param labeled
	 */
	private void printClassifiedData(Instances labeled) {
		CSVSaver csvSaver = new CSVSaver();
		try {
			csvSaver.setFile(new File(m_hwrConfig.get_classified_data_file()));
			csvSaver.setInstances(labeled);
			csvSaver.writeBatch();
		} catch (IOException e) {
			m_logger.error("Error occurred while printing classified data", e);
		}

	}

	/**
	 * Prints class labels to the configured file.
	 * 
	 * @param clsLabel
	 * @throws HWRException
	 */
	private void printClassifiedData(double[] p_clsLabels) throws HWRException {
		PrintWriter xClassifiedDataPrinter = null;
		try {
			xClassifiedDataPrinter = new PrintWriter(new File(
					m_hwrConfig.get_classified_labels_file()));
		} catch (FileNotFoundException e) {
			m_logger.error("Unable to print classified labels data", e);
			throw new HWRException("Unable to print classified labels data");
		}
		for (double classLabel : p_clsLabels) {
			xClassifiedDataPrinter.println((int)classLabel);
		}
		xClassifiedDataPrinter.flush();
		xClassifiedDataPrinter.close();

	}
}
