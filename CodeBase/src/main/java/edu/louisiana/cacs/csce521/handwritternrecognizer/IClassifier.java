package edu.louisiana.cacs.csce521.handwritternrecognizer;

import edu.louisiana.cacs.csce521.handwritternrecognizer.exception.HWRException;
import weka.classifiers.Evaluation;

public interface IClassifier {
	
	public void classifyData() throws HWRException;
	public Evaluation evaluate() ;

}
