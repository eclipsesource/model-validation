/*******************************************************************************
 * Copyright (c) 2019-2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ******************************************************************************/
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
