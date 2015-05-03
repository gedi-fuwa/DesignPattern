package com.ibm.rhapsody.samples.cppUserSimplifiers.simplifiers;

import com.ibm.rhapsody.samples.cppUserSimplifiers.utils.Reporter;
import com.telelogic.rhapsody.core.IRPAttribute;
import com.telelogic.rhapsody.core.IRPClass;
import com.telelogic.rhapsody.core.IRPCollection;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPOperation;
import com.telelogic.rhapsody.core.IRPStereotype;
import java.util.List;

public class HFUSingeltonSimplifier extends AbstractSimplifier {
	private String singletonName = "";
	private String singletonType = "";

	protected boolean doesApplyFor(IRPModelElement userElement) {
		return userElement instanceof IRPClass;
	}

	protected void onAbortSimplification() {}
	protected void onBeginSimplification() {}
	protected void onEndSimplification() {}
	protected void onExitSimplification() {	}
	protected void onSimplify(IRPModelElement userElement, IRPModelElement simplifiedElementOwner, String simplificationRequested) {}

	// ---------------------------------------- Simplification Steuerklasse ----------------------------------------
	protected void onPostSimplification(IRPModelElement userClass, IRPModelElement mainSimplifiedElement, String simplificationRequested) {
		Reporter.report("\n\n");
		Reporter.report("# User Simplification gestartet");
		if (shouldBeSingleton(userClass)) {
			boolean success = simplifyToSingltonPattern((IRPClass) mainSimplifiedElement);
			if (success) {
				Reporter.report("Successfully simplified class '" + mainSimplifiedElement.getName() + "' to meet \"Singleton\" pattern.");
			} else {
				Reporter.report("Failed to simplified class '" + mainSimplifiedElement.getName() + "' to meet \"Singleton\" pattern.");
			}
		}
	}

	// ---------------------------------------- Stereotype überprüfen ----------------------------------------
	private boolean shouldBeSingleton(IRPModelElement userElement) {
		if ((userElement instanceof IRPClass)) {
			List<IRPStereotype> stereotypes = userElement.getStereotypes().toList();
			for (IRPStereotype stereotype : stereotypes) {
				String stereotypeName = stereotype.getName();
				if (stereotypeName.compareTo("SingletonPattern") == 0) {
					return true;
				}
			}
		}
		return false;
	}

	// ---------------------------------------- Prüfen welche Methoden vorhanden sind ----------------------------------------
	private boolean simplifyToSingltonPattern(IRPClass toSingleton)
	{
    Reporter.report("## Klasse >>" + toSingleton.getName() + "<< wird simplifiziert");
    boolean success = false;
    
    boolean hasDefaultConstructor = false;
    boolean hasCopyConstructor = false;
    boolean hasDefaultDestructor = false;
    boolean hasGetInstance = false;
    boolean hasVarmInstance = false;
    
    this.singletonName = toSingleton.getName();
    this.singletonType = (this.singletonName + "*");
    if (toSingleton != null)
    {
      List<IRPOperation> operations = toSingleton.getOperations().toList();
      for (IRPOperation operation : operations)
      {
    	// ---------------------------------------- Überprüfe DefaultConstructor
        if (operation.getIsCtor() == 1) {
		  if (operation.getArguments().getCount() == 0)
          {
            hasDefaultConstructor = true;
            continue;
          }
        }
        
        // ---------------------------------------- Überprüfe CopyConstructor
        if (operation.getIsCtor() == 1) {
          if (operation.getArguments().getCount() == 1)
          {
            hasCopyConstructor = true;
            continue;
          }
        }
        
        // ---------------------------------------- Überprüfe DefaultDestructor
        if (operation.getIsDtor() == 1) {
          if (operation.getArguments().getCount() == 0)
          {
            hasDefaultDestructor = true;
            continue;
          }
        }
		
        // ---------------------------------------- Überprüfe ob getInstance Funktion vorhanden ist
        if ((operation.getIsCtor() == 0) && (operation.getIsDtor() == 0)) {
          if (operation.getName().equals("getInstance")) {
            if (operation.getArguments().getCount() == 0)
            {
              Reporter.report("### Funktion getInstance ist bereits vorhanden!!!");
              hasGetInstance = true;
              continue;
            }
          }
        }
		
      List<IRPAttribute> attributes = toSingleton.getAttributes().toList();
      
      // ---------------------------------------- Überprüfe ob die mInstance Variable vorhanden ist
	  for (IRPAttribute attribute : attributes) {
        if (attribute.getName().equals("mInstance"))
        {
          Reporter.report("### Attribut mInstance bereits vorhanden!!!"); 
          hasVarmInstance = true;
        }
      }
    }
    Reporter.report("#--------------------------------------------");
    Reporter.report("#\t\tCreating the following Constructors");
    Reporter.report("");
	
    // ---------------------------------------- DefaultConstructor modifizieren oder erstellen
    if (!hasDefaultConstructor)
    {
      createDCTOR(toSingleton);
    }
    else
    {
      modifyDCTOR(toSingleton);
      Reporter.report("##\t\tDefault-Konstruktor wurde modifiziert");
    }
    
    // ---------------------------------------- CopyConstructor modifizieren oder erstellen
    if (!hasCopyConstructor)
    {
      createCCTOR(toSingleton);
      Reporter.report("##\t\tCopy-Konstruktor wurde erstellt");
    }
    else
    {
      modifyCCTOR(toSingleton);
      Reporter.report("##\t\tCopy-Konstruktor wurde modifiziert");
    }
    
    // ---------------------------------------- Destructor modifizieren oder erstellen
    if (!hasDefaultDestructor)
    {
      createDDTOR(toSingleton);
    }
    else
    {
      modifyDDTOR(toSingleton);
      Reporter.report("##\t\tDefault-Destruktor wurde modifiziert");
    }
    Reporter.report("");
    Reporter.report("#--------------------------------------------");
    Reporter.report("#\t\tCreating the following functions");
    Reporter.report("");
    
    // ---------------------------------------- GetInstance Funktion erstellen falls nicht vorhanden
    if (!hasGetInstance)
    {
      createGetInstanceOperation(toSingleton);
      Reporter.report("##\t\tFunction getInstance was created");
    }

    Reporter.report("");
    Reporter.report("#--------------------------------------------");
    Reporter.report("#\t\tCreating the following attributes");
    Reporter.report("");
	
    // ---------------------------------------- mInstance Variable erstellen falls nicht vorhanden
    if (!hasVarmInstance)
    {
      createInstanceAttribute(toSingleton);
      Reporter.report("##\t\tAttribute mInstance was created");
    }
    Reporter.report("");
    Reporter.report("#--------------------------------------------\n\n");
    
    // ---------------------------------------- Falls die Funktion oder Variable fälschlicherweise vom User erstellt wurde -> Abbrechen
	if(hasGetInstance || hasVarmInstance){
		return false;
	}
    return true;
  }

	private void createDCTOR(IRPClass toSingleton) {
		IRPOperation defaultCtor = toSingleton.addConstructor(null);
		defaultCtor.setVisibility("private");
	}

	// ---------------------------------------- DefaultConstructor modifizieren
	private void modifyDCTOR(IRPClass toSingleton) {
		List<IRPOperation> operations = toSingleton.getOperations().toList();
		for (IRPOperation operation : operations) {
			if (operation.getName().equals(toSingleton.getName())) {
				IRPOperation defaultCtor = operation;
				defaultCtor.setVisibility("private");
				defaultCtor.setBody("");
			}
		}
	}

	// ---------------------------------------- DefaultConstructor erstellen
	private void createCCTOR(IRPClass toSingleton) {
		IRPOperation copyCtor = toSingleton.addConstructor("instance, "	+ this.singletonType);
		copyCtor.setVisibility("private");
	}

	// ---------------------------------------- CopyConstructor modifizieren
	private void modifyCCTOR(IRPClass toSingleton) {
		List<IRPOperation> operations = toSingleton.getOperations().toList();
		for (IRPOperation operation : operations) {
			if ((operation.getName().equals(toSingleton.getName()))	&& (operation.getArguments().getCount() == 1)) {
				IRPOperation copyCtor = operation;
				copyCtor.setVisibility("private");
				copyCtor.setBody("");
			}
		}
	}

	// ---------------------------------------- Destructor erstellen
	private void createDDTOR(IRPClass toSingleton) {
		IRPOperation defaultDtor = toSingleton.addDestructor();
		defaultDtor.setVisibility("private");
	}

	// ---------------------------------------- Destructor modifizieren
	private void modifyDDTOR(IRPClass toSingleton) {
		List<IRPOperation> operations = toSingleton.getOperations().toList();
		for (IRPOperation operation : operations) {
			if (operation.getIsDtor() == 1) {
				IRPOperation defaultDtor = operation;
				defaultDtor.setVisibility("private");
				defaultDtor.setBody("");
			}
		}
	}

	// ---------------------------------------- mInstance Variable erstellen
	private void createInstanceAttribute(IRPClass toSingleton) {
		IRPAttribute instanceAttribute = toSingleton.addAttribute("mInstance");
		instanceAttribute.setVisibility("private");
		instanceAttribute.setTypeDeclaration(this.singletonType);
		instanceAttribute.setDefaultValue("NULL");
		instanceAttribute.setPropertyValue("CPP_CG.Attribute.InitializationStyle", "ByAssignment");
		instanceAttribute.setIsStatic(1);
	}

	// ---------------------------------------- GetInstance Funktion erstellen
	private void createGetInstanceOperation(IRPClass toSingleton) {
		IRPOperation getInstanceOperation = toSingleton.addOperation("getInstance");
		getInstanceOperation.setVisibility("public");
		getInstanceOperation.setReturnTypeDeclaration(this.singletonType);
		getInstanceOperation.setIsStatic(1);
		getInstanceOperation.setBody("if ( mInstance == NULL )\n{\n\tmInstance = new " +
										this.singletonName + "();\n" + "}\n" + "else\n" + "{\n"
										+ " \tmInstance = NULL;\n" + "}\n"
										+ "return mInstance;");
	}
}
