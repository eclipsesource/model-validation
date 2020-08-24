package org.eclipse.emfcloud.validation;

import org.eclipse.emf.common.util.BasicDiagnostic;

public class ValidationResult {

    private String identifier;
    private BasicDiagnostic diagnostic;

    public ValidationResult(String identifier, BasicDiagnostic diagnostic){
        this.identifier = identifier;
        this.diagnostic = diagnostic;
    }

    public String getIdentifier(){
        return identifier;
    }

    public BasicDiagnostic getDiagnostic(){
        return diagnostic;
    }
    
}
