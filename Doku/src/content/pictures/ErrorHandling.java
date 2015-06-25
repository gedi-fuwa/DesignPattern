package com.ibm.rhapsody.samples.cppUserSimplifiers.simplifiers;

import com.telelogic.rhapsody.core.IRPClass;
import com.telelogic.rhapsody.core.IRPOperation;

public class ErrorHandling {

	// Function which will be called if a error in the simplification-process was found..
	// it generates a class with syntax-error and the description
	public void ClassenthaeltFehler(IRPClass simplifiedClass, String ErrorMessage) {
		
		IRPOperation getInstanceOperation = simplifiedClass
				.addOperation("Fehler");
		getInstanceOperation.setVisibility("public");
		getInstanceOperation.setReturnTypeDeclaration("Error_Function");
		getInstanceOperation.setIsStatic(1);
		getInstanceOperation
				.setBody("Bei der Simplifizierung ist ein Fehler aufgetreten: " + ErrorMessage);
	}
}
