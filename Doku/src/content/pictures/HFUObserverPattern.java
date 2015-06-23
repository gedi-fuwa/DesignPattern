/**
 * 
 * ClassSimplifier to Simplify a Class with Stereotype "Observer" in C++
 * 
 * This Class is based on the "Rhapsody C++ User Simplifiers sample" out of the Sample-Project-Directory
 * 
 * author:		Aymen Gam AIB 6
 * 
 * version:		1.0
 * date:		Jan 11th 2015
 * 
 * (c) Copyright IBM 2009 && HFU-Furtwangen
 * 
 */

package com.ibm.rhapsody.samples.cppUserSimplifiers.simplifiers;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SetProperty;

import com.ibm.rhapsody.samples.cppUserSimplifiers.utils.Reporter;
import com.sun.org.apache.regexp.internal.REProgram;
import com.telelogic.rhapsody.core.IRPApplication;
import com.telelogic.rhapsody.core.IRPArgument;
import com.telelogic.rhapsody.core.IRPAttribute;
import com.telelogic.rhapsody.core.IRPClass;
import com.telelogic.rhapsody.core.IRPClassifier;
import com.telelogic.rhapsody.core.IRPCollection;
import com.telelogic.rhapsody.core.IRPConfiguration;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPOperation;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPProject;
import com.telelogic.rhapsody.core.IRPStereotype;

/**
 * 
 * Dies ist die Simplifier-Klasse für das Observer Design-Patten. Die
 * Simplifikation wird als "PostSimplifikation" durchgeführt. D.h. Rhapsody
 * genieriert zuerst die User-Klassen aus dem Model. Erst im Anschluss daran
 * wird die hier implementierte Simplification durchgeführt.
 */
public class HFUObserverPattern extends AbstractSimplifier {

	private String observerType = "";
	private String observerName = "";
	static int count = 0;
	private List<IRPClass> observerlist = new ArrayList<IRPClass>();
	private IRPClass observerparent = null;

	protected boolean doesApplyFor(IRPModelElement userElement) {
		return userElement instanceof IRPClass;
	}

	@Override
	protected void onAbortSimplification() {
	}

	@Override
	protected void onBeginSimplification() {
		Reporter.report("\n\n" + "# User Simplification gestartet");
	}

	@Override
	protected void onEndSimplification() {

		if (!observerlist.isEmpty() && observerparent != null) {
			Reporter.report("Set Generalization");
			observerlist.get(0).addGeneralization(observerparent);
		}

		if (count < 2) {
			Reporter.report("### <Observer> oder <Subject> nicht vorhanden ###");
		}
		Reporter.report("\n\n" + "# Simplification abgeschlossen");

	}

	@Override
	protected void onExitSimplification() {
	}

	@Override
	protected void onSimplify(IRPModelElement userElement,
			IRPModelElement simplifiedElementOwner,
			String simplificationRequested) {
	}

	/**
	 * Methode führt Transformation der übergebenen Klasse durch,
	 * Vorraussetzungen werden geprüft
	 * 
	 * @param userClass
	 *            - Die Klasse, welche transformiert werden soll
	 * @param mainSimplifiedElement
	 *            - Klasse welche während der Code-Generation erzeugt wird.
	 * 
	 */
	protected void onPostSimplification(IRPModelElement userClass,
			IRPModelElement mainSimplifiedElement,
			String simplificationRequested) {
		IRPClassifier Observer = null;
		IRPCollection observerCollection;
		Reporter.report(userClass.toString());
		Reporter.report(mainSimplifiedElement.toString());
		Reporter.report(simplificationRequested);

		observerCollection = userClass.getProject().getClasses();
		List<IRPClass> classes = userClass.getProject().getClasses().toList();

		for (IRPClass obj : classes) {
			Reporter.report("IN FOR SCHLEIFE");
			Reporter.report(obj.getName());
			String classname = obj.getName();

			if (classname.compareTo("Observer") == 0) {

				Reporter.report("### <Observer> gefunden ###");

				((IRPClassifier) userClass).addGeneralization(Observer);

			}
		}
		// userClass.addGeneralization(Observer);

		observerexists(userClass, mainSimplifiedElement);
		subjectexists(userClass, mainSimplifiedElement);
		observerparent(userClass, mainSimplifiedElement);
	}

	/**
	 * Funktion observerPatternexists Prüft ob beide Stereotypen vorhanden sind
	 */
	@SuppressWarnings("unchecked")
	private void observerexists(IRPModelElement userElement,
			IRPModelElement mainSimplifiedElement) {
		if ((userElement instanceof IRPClass)) {
			List<IRPStereotype> stereotypes = userElement.getStereotypes()
					.toList();
			for (IRPStereotype stereotype : stereotypes) {
				String stereotypeName = stereotype.getName();
				if (stereotypeName.compareTo("Observer") == 0) {
					count++;
					// Reporter.report("### <Observer> vorhanden ###");
					observerlist.add((IRPClass) userElement);
					simplifyObserver((IRPClass) mainSimplifiedElement);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void subjectexists(IRPModelElement userElement,	IRPModelElement mainSimplifiedElement) {
		if ((userElement instanceof IRPClass)) {
			List<IRPStereotype> stereotypes = userElement.getStereotypes()
					.toList();
			for (IRPStereotype stereotype : stereotypes) {
				String stereotypeName = stereotype.getName();
				if (stereotypeName.compareTo("Subject") == 0) {
					count++;
					// Reporter.report("### <Subject> vorhanden ###");
					simplifySubject((IRPClass) mainSimplifiedElement);
				}
			}
		}
	}

	private void observerparent(IRPModelElement userElement,
			IRPModelElement mainSimplifiedElement) {
		if ((userElement instanceof IRPClass)) {

			if (userElement.getName().compareTo("Observer") == 0) {
				observerparent = (IRPClass) userElement;
			}
		}
	}

	/**
	 * Funktion simplifiyObserver Klasse mit Stereotyp <Observer> wird
	 * simplifiziert
	 */
	@SuppressWarnings("unchecked")
	private void simplifyObserver(IRPClass toObserver) {
		boolean hasUpdate = false;
		this.observerType = (this.observerName);

		if (toObserver != null) {

			// ### Prüfen ob die Funktion update vorhanden ist ###
			List<IRPOperation> operations = toObserver.getOperations().toList();
			for (IRPOperation operation : operations) {
				if (operation.getName().equals("update")) {
					Reporter.report("### Funktion update ist bereits vorhanden!!!");
					hasUpdate = true;
					continue;
				}
			}
			if (!hasUpdate) {
				createUpdateOperation(toObserver);
			}
		}
	}

	/**
	 * Funktion simplifiySubject Klasse mit Stereotyp <Subject> wird
	 * simplifiziert
	 */
	@SuppressWarnings("unchecked")
	private void simplifySubject(IRPClass toSubject) {
		boolean has_observer = false;
		boolean has_register_observer = false;
		boolean has_delete_observer = false;
		boolean has_notify_observer = false;

		List<IRPAttribute> attributes = toSubject.getAttributes().toList();
		List<IRPOperation> operations = toSubject.getOperations().toList();

		// --------------------------------------------------### Attribute ###
		for (IRPAttribute attribute : attributes) {
			if (attribute.getName().equals("_observer")) {
				has_observer = true;
			}
		}

		if (has_observer == true) {
			// TODO sichtbarkeit, typ,...
		} else {
			create_observerAttribute(toSubject);
		}

		// --------------------------------------------------### Methoden ###
		for (IRPOperation operation : operations) {

			// ### Prüfe ob Operation register_observer vorhanden ###
			if (operation.getName().equals("register_observer")) {
				if (operation.getArguments().getCount() == 1) {
					has_register_observer = true;
				} else {
					has_register_observer = false;
				}
			}

			// ### Prüfe ob Operation delete_observer vorhanden ###
			if (operation.getName().equals("delete_observer")) {
				if (operation.getArguments().getCount() == 1) {
					has_delete_observer = true;
				} else {
					has_delete_observer = false;
				}
			}

			// ### Prüfe ob Operation notify_observer vorhanden ###
			if (operation.getName().equals("notify_observer")) {
				if (operation.getArguments().getCount() == 0) {
					has_notify_observer = true;
				} else {
					has_notify_observer = false;
				}
			}
		}

		// ### Rounttrip prüfen oder Methode erstellen ###
		for (IRPOperation operation : operations) {
			String body;

			// ### register_observer: Rounttrip prüfen bzw neu erstellen ###
			if (has_register_observer == true) {
				if (operation.getName().equals("register_observer")) {
					body = operation.getBody();
					if (body.indexOf("Generated by") > 0) {
						Reporter.report("Methode register_Observer wurde nicht neu erzeugt : Roundtrip");
					} else {
						// TODO ERROR für roundtrip
						Reporter.report(">>> Methode register_Observer Roundtrip Error <<<");
					}
				}
			}

			// ### delete_observer: Rounttrip prüfen bzw neu erstellen ###
			if (has_delete_observer == true) {
				if (operation.getName().equals("delete_observer")) {
					body = operation.getBody();
					if (body.indexOf("Generated by") > 0) {
						Reporter.report("Methode delete_Observer wurde nicht neu erzeugt : Roundtrip");
					} else {
						// TODO ERROR für roundtrip
						Reporter.report(">>> Methode delete_Observer Roundtrip Error <<<");
					}
				}
			}

			// ### notify_observer: Rounttrip prüfen bzw neu erstellen ###
			if (has_notify_observer == true) {
				if (operation.getName().equals("notify_observer")) {
					body = operation.getBody();
					if (body.indexOf("Generated by") > 0) {
						Reporter.report("Methode notify_Observer wurde nicht neu erzeugt : Roundtrip");
					} else {
						// TODO ERROR für roundtrip
						Reporter.report(">>> Methode notify_Observer Roundtrip Error <<<");
					}
				}
			}

		}

		if (has_delete_observer == false) {
			create_delete_observerOperation(toSubject);
		}

		if (has_register_observer == false) {
			create_register_observerOperation(toSubject);
		}

		if (has_notify_observer == false) {
			create_notify_observerOperation(toSubject);
		}
	}

	// ----------------------------------------------------- ### create ###

	/**
	 * ### toObserver Funktion: create_observerAttribute ### Erstellt das
	 * Attribut _observer
	 */
	private void create_observerAttribute(IRPClass toSubject) {
		if (toSubject != null) {
			IRPAttribute _observer = toSubject.addAttribute("_observer");
			_observer.setVisibility("private");
			_observer.setTypeDeclaration("std::list<Observer*> _observer");
			_observer.setDefaultValue("NULL");
			_observer.setPropertyValue("CPP_CG.Attribute.InitializationStyle",
					"ByAssignment");
		}
	}

	/**
	 * ### toObserver Funktion: createNotifyOperation ### Erstellt die Operation
	 * Notify
	 */
	private void createUpdateOperation(IRPClass toObserver) {
		IRPOperation updateOperation = toObserver.addOperation("update");
		updateOperation.setVisibility("public");
		updateOperation.setReturnTypeDeclaration("void");
		updateOperation.setBody("//TODO By User");
	}

	/**
	 * ### toSubject Funktion: create_register_observerOperation ### Erstellt
	 * die Operation register_observer
	 */
	private void create_register_observerOperation(IRPClass toSubject) {
		IRPOperation register_observer = toSubject
				.addOperation("register_observer");
		register_observer.setVisibility("public");
		register_observer.setReturnTypeDeclaration("void");
		IRPArgument arg1 = register_observer.addArgument("observer");
		arg1.setTypeDeclaration("Observer*");
		register_observer.setBody("// Generated by HFUObserverSimplifier \n\n"
				+ "_observer.push_back(observer);");
	}

	/**
	 * ### toSubject Funktion: create_delete_observerOperation ### Erstellt die
	 * Operation delete_observer
	 */
	private void create_delete_observerOperation(IRPClass toSubject) {
		IRPOperation delete_observer = toSubject
				.addOperation("delete_observer");
		delete_observer.setVisibility("public");
		delete_observer.setReturnTypeDeclaration("void");
		IRPArgument arg1 = delete_observer.addArgument("observer");
		arg1.setTypeDeclaration("Observer*");
		delete_observer.setBody("// Generated by HFUObserverSimplifier \n\n"
				+ "_observer.remove(observer);");
	}

	/**
	 * ### toSubject Funktion: create_notify_observerOperation ### Erstellt die
	 * Operation notify_observer
	 */
	private void create_notify_observerOperation(IRPClass toSubject) {
		IRPOperation notify_observer = toSubject
				.addOperation("notify_observer");
		notify_observer.setVisibility("public");
		notify_observer.setReturnTypeDeclaration("void");
		notify_observer
				.setBody("// Generated by HFUObserverSimplifier \n\n"
						+ "std::list<Observer*>::iterator iter = _observer.begin();\n\n"
						+ "\t	for ( ; iter != _observer.end(); iter++ )\n"
						+ "\t		{" + "\t\t			(*iter)->update();"

						+ "\t			}       ");
	}

}
