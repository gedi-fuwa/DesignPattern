package com.ibm.rhapsody.samples.cppUserSimplifiers.simplifiers;
import com.ibm.rhapsody.samples.cppUserSimplifiers.utils.Reporter;
import com.telelogic.rhapsody.core.IRPAttribute;
import com.telelogic.rhapsody.core.IRPOperation;

public class Roundtrip {

	// Function to find the comment in a function which will be generated during the first 
	// simplification
	public boolean findComment(String comment, IRPOperation operation) {
			return operation.getBody().contains(comment);
	}
	
	// Function to find the comment for a attribute which will be generated during the first 
	// simplification
	public boolean findAttribute(String comment, IRPAttribute attribute){
		Reporter.report(comment);
		Reporter.report(attribute.getDescription());
		return attribute.getDescription().contains(comment);
	}

}
