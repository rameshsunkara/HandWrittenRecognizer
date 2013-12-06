package edu.louisiana.cacs.csce521.handwritternrecognizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
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
public class HWRClassifier implements IClassifier{

	//Logger object to log the messages
	private static Log m_logger = LogFactory.getLog(HWRClassifier.class);

	//Configuration object which holds all the configured properties
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
	 * @throws HWRException 
	 */
	public HWRClassifier(Configurator p_hwrConfig, String p_ClassifierName,
			String[] p_options) throws HWRException {
		m_hwrConfig = p_hwrConfig;
		try {
			m_Classifier = (Classifier) Class.forName(p_ClassifierName)
					.newInstance();
			m_logger.trace("Succesfully loaded classifier:" + p_ClassifierName);
		} catch (Exception e) {
			m_logger.error("Exception caught while instantiating classifier", e);
			throw new HWRException("Unable to Instantiate Classifer");
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
		m_logger.debug("Enter classifyData()");
		// 1.Read Data
		preProcess();

		// 2.Build Classifier ( Train )
		m_logger.debug("Training the  classifier.....");
		try {
			m_Classifier.buildClassifier(m_TrainingData);
		} catch (Exception e) {
			m_logger.error("Exception caught while building classifier", e);
			throw new HWRException("Exception caught while building classifier");
		}

		m_logger.debug("Classifier.....Trained Successfully...Classifying data");

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

		// 5. Evaluate ( 10 Fold Cross Validation
		Evaluation xEvalutionResult = evaluate();
		printEvaluationResult(xEvalutionResult);
		m_logger.debug("Exit classifyData()");
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
		m_logger.debug("Enter preProcess()");
		m_TrainingData = readData(m_hwrConfig.get_train_data_file());
		m_TestData = readData(m_hwrConfig.get_test_data_file());

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
		m_logger.debug("Exit preProcess()");
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
			m_logger.error("Error caught while modifying test data", e);
			throw new HWRException("Error caught while modifying test data");
		}

		try {
			m_TestData = Filter.useFilter(m_TestData, dummyLabel);
		} catch (Exception e) {
			m_logger.error("Error caught while applying filter on test data", e);
			throw new HWRException(
					"Error caught while applying filter on test data");
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
			csvSaver.setFile(new File(m_hwrConfig.get_classifier_name()+"_classified_data"));
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
					m_hwrConfig.get_classifier_name()+"_labels"));
		} catch (FileNotFoundException e) {
			m_logger.error("Unable to print classified labels data", e);
			throw new HWRException("Unable to print classified labels data");
		}
		for (double classLabel : p_clsLabels) {
			xClassifiedDataPrinter.println((int) classLabel);
		}
		xClassifiedDataPrinter.flush();
		xClassifiedDataPrinter.close();

	}

	/**
	 * Evaluates the classifier with 10 fold cross validation.
	 * @return xEvaluationResult
	 * 			- <code>weka.classifiers.Evaluation</code> object which holds
	 *            the evaluation result.
	 * @since 1.0	
	 */
	public Evaluation evaluate() {
		m_logger.debug("Enter evaluate()");
		Evaluation xEvaluationResult = null;
		try {
			xEvaluationResult = new Evaluation(m_TrainingData);
			xEvaluationResult.crossValidateModel(m_Classifier, m_TrainingData,10,
					m_TrainingData.getRandomNumberGenerator(1));
		} catch (Exception e) {
			m_logger.error("Exception caught while evaluating", e);
		}
		m_logger.debug("Exit evaluate()");
		return xEvaluationResult;
	}

	/**
	 * Print the evaluation result in to a file.
	 * 
	 * @param p_Evaluation
	 *            - <code>weka.classifiers.Evaluation</code> object which holds
	 *            the evaluation result.
	 * @since 1.0
	 */
	private void printEvaluationResult(Evaluation p_Evaluation) {
		m_logger.info(p_Evaluation.toSummaryString());
		PrintWriter xEvaluationReportWriter = null;
		try {
			xEvaluationReportWriter = new PrintWriter(
					m_hwrConfig.get_output_dir() + File.separator
							+ m_hwrConfig.get_classifier_name()+"_EvalResults");
		} catch (FileNotFoundException e) {
			m_logger.error(
					"FileNotFoundException caught while writing evaluaiton result",
					e);
		}
		xEvaluationReportWriter.print(p_Evaluation.toSummaryString());
		xEvaluationReportWriter.flush();
		if (xEvaluationReportWriter != null)
			xEvaluationReportWriter.close();
	}
}
