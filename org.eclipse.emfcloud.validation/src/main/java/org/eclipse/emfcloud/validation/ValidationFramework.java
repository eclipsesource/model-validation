/*******************************************************************************
 * Copyright (c) 2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ******************************************************************************/
package org.eclipse.emfcloud.validation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emfcloud.modelserver.client.ModelServerClient;
import org.eclipse.emfcloud.modelserver.client.ModelServerClientApi;
import org.eclipse.emfcloud.modelserver.emf.common.EMFFacetConstraints;
import org.eclipse.emfcloud.modelserver.emf.common.ValidationMapperModule;
import org.emfjson.jackson.module.EMFModule;

public class ValidationFramework {
	
	private static Logger LOG = Logger.getLogger(ValidationFramework.class.getSimpleName());

    private String defaultURL = "http://localhost:8081/api/v1/";

    private String modelUri;

    private ModelServerClientApi<EObject> modelServerApi;

    public List<ValidationResult> recentValidationResult = new ArrayList<>();

    public ValidationResultChangeListener changeListener;

    public Map<Integer, Map<Integer, EMFFacetConstraints>> inputValidationMap = new HashMap<>();

    private List<ValidationFilter> validationFilterList = new ArrayList<>();

    public ValidationFramework(String modelUri, ValidationResultChangeListener changeListener) throws MalformedURLException {
        this.modelUri = modelUri;
        this.modelServerApi = new ModelServerClient(defaultURL);
        this.changeListener = changeListener;
    }

    public ValidationFramework(String modelUri, ModelServerClientApi<EObject> modelServerApi, ValidationResultChangeListener changeListener) {
        this.modelUri = modelUri;
        this.modelServerApi = modelServerApi;
        this.changeListener = changeListener;
    }

    public CompletableFuture<Void> validate() throws IOException, InterruptedException, ExecutionException {
        return this.modelServerApi.validate(modelUri).thenAccept(s -> {
            try {
                readData(modelUri, s.body());
            } catch (IOException e) {
            	LOG.error("Cannot validate " + modelUri);
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Void> getConstraintList() {
        return this.modelServerApi.getConstraints(modelUri).thenAccept(s -> {
            try {
                readConstraintList(s.body());
            } catch (IOException e) {
                LOG.error("Could not retrieve the Constraint List for " + modelUri);
                e.printStackTrace();
            }
        });
    }

    public void subscribeToValidation() {
        this.modelServerApi.validationSubscribe(modelUri, new ValidationSubscriptionListener(this, modelUri));
    }

    public void unsubscribeFromValidation() {
        this.modelServerApi.unsubscribeValidation(modelUri);
    }

    public void addValidationFilter(List<ValidationFilter> filters)
        throws IOException, InterruptedException, ExecutionException {
        for(ValidationFilter f : filters){
            if(!validationFilterList.contains(f)) validationFilterList.add(f);
        }
        this.validate();
    }

    public void removeValidationFilter(List<ValidationFilter> filters)
        throws IOException, InterruptedException, ExecutionException {
        for(ValidationFilter filter : filters){
            if(validationFilterList.contains(filter)) validationFilterList.remove(filter);
        }
        this.validate();
    }

    public void toggleValidationFilter(ValidationFilter filter)
        throws IOException, InterruptedException, ExecutionException {
        if(validationFilterList.contains(filter)){
            validationFilterList.remove(filter);
        } else {
            validationFilterList.add(filter);
        }
        this.validate();
    }

    private void readData(String modeluri, String body) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ValidationMapperModule());
        mapper.registerModule(new ValidationResultModule());
        JsonNode node = mapper.readTree(body);
        if (node.get("type").asText().equals("validationResult")) {
            JsonNode responseData = node.get("data");
            updateRecentValidationResult(jsonToValidationResultList(mapper, responseData));
        }
    }

    private void readConstraintList(String body) throws IOException {
        ObjectMapper mapper = EMFModule.setupDefaultMapper();
        mapper.registerModule(new ValidationMapperModule());
        JsonNode node = mapper.readTree(body);
        if (node.get("type").asText().equals("success")){
            JsonNode listOfElements = node.get("data");
            //Iterate over all Elements
            Iterator<String> iterElements = listOfElements.fieldNames();
            while (iterElements.hasNext()){
                String elementKey = iterElements.next();
                HashMap<Integer, EMFFacetConstraints> featuresMap = new HashMap<>();
                JsonNode listOfFeatures = listOfElements.get(elementKey);
                //Iterate over all Features
                Iterator<String> iterFeature = listOfFeatures.fieldNames();
                while(iterFeature.hasNext()){
                    String featureKey = iterFeature.next();
                    JsonNode facets = listOfFeatures.get(featureKey);

                    EMFFacetConstraints emfFacetConstraints = mapper.treeToValue(facets, EMFFacetConstraints.class);
                    featuresMap.put(Integer.parseInt(featureKey), emfFacetConstraints);
                }
                this.inputValidationMap.put(Integer.parseInt(elementKey), featuresMap);
            }
        }
    }

    public void updateRecentValidationResult(List<ValidationResult> validationResults) {
        this.recentValidationResult = validationResults;
        this.changeListener.changed(validationResults);
    }

    List<ValidationResult> jsonToValidationResultList(ObjectMapper mapper, JsonNode responseData) throws JsonProcessingException{
        List<ValidationResult> result = new ArrayList<ValidationResult>();
        //Filter out the Diagnosis Message
        if(!(responseData.get("code").asInt() == 0 && responseData.get("source").asText().equals("org.eclipse.emf.ecore"))){
            //Check if this is an Error and add it if it is not filtered
            if(responseData.get("severity").asInt() != 0){
                ValidationResult validationResult = mapper.treeToValue(responseData, ValidationResult.class);
                ValidationFilter filter = new ValidationFilter(validationResult.getDiagnostic().getCode(),
                            validationResult.getDiagnostic().getSource());
                if(!validationFilterList.contains(filter)){
                    result.add(validationResult);
                }
            } 
        }
        //Call Method for all children
        for(JsonNode diagnosticData: responseData.get("children")){
            result.addAll(jsonToValidationResultList(mapper, diagnosticData));
        }
        return result;
    }
}
