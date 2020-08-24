package org.eclipse.emfcloud.validation;

import java.util.List;

public abstract class ValidationResultChangeListener {

    public abstract void changed(List<ValidationResult> newResult);

}
