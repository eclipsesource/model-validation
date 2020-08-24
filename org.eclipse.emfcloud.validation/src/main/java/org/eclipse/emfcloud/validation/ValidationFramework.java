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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emfcloud.modelserver.client.ModelServerClient;
import org.eclipse.emfcloud.modelserver.client.ModelServerNotification;
import org.eclipse.emfcloud.modelserver.client.Response;
import org.eclipse.emfcloud.modelserver.client.SubscriptionListener;
import org.jetbrains.annotations.NotNull;

public class ValidationFramework {

    private String defaultURL = "http://localhost:8081/api/v1/";

    private String modelUri;

    private ModelServerClient modelServerClient;

    public List<ValidationResult> recentValidationResult = new ArrayList<>();

    public ValidationResultChangeListener changeListener;

    public Map<Integer, Map<Integer, EMFFacetConstraints>> inputValidationMap = new HashMap<>();

    private List<ValidationFilter> validationFilterList = new ArrayList<>();

    public ValidationFramework(String modelUri, ValidationResultChangeListener changeListener) throws MalformedURLException {
        this.modelUri = modelUri;
        this.modelServerClient = new ModelServerClient(defaultURL);
        this.changeListener = changeListener;
    }

    public ValidationFramework(String modelUri, ModelServerClient modelServerClient, ValidationResultChangeListener changeListener) {
        this.modelUri = modelUri;
        this.modelServerClient = modelServerClient;
        this.changeListener = changeListener;
    }

    public CompletableFuture<Void> validate() throws IOException, InterruptedException, ExecutionException {
        return this.modelServerClient.validate(modelUri).thenAccept(s -> {
            try {
                readData(modelUri, s.body());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Void> getConstraintList() {
        return this.modelServerClient.getConstraints(modelUri).thenAccept(s -> {
            try {
                readConstraintList(s.body());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }

    public void subscribeToValidation() {
        this.modelServerClient.validationSubscribe(modelUri, new SubscriptionListener() {
            @Override
            public void onOpen(Response<String> response) {
                System.out.println("Connected: " + response.getMessage());
                try {
                    validate();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void onClosing(int code, @NotNull String reason) {
                System.out.println("Closing: Code " + code);
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println("Failed: ");
                t.printStackTrace();
            }

            @Override
            public void onClosed(int code, @NotNull String reason) {
                System.out.println("Connection closed: Reason " + reason);
            }

            @Override
            public void onFailure(Throwable t, Response<String> response) {
                System.out.println("Failed: " + response);
            }

            @Override
            public void onNotification(ModelServerNotification notification) {
                System.out.println("Notification: " + notification);
                if (notification.getType().equals("validationResult")) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        updateRecentValidationResult(
                                jsonToValidationResultList(mapper, mapper.readTree(notification.getData().get())));
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        System.out.println("failed to get Notification");
                    }
                }
            }
        });
    }

    public void unsubscribeFromValidation() {
        this.modelServerClient.unsubscribeValidation(modelUri);
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
        for(ValidationFilter f : filters){
            if(validationFilterList.contains(f)) validationFilterList.remove(f);
        }
        this.validate();
    }

    public void toggleValidationFilter(ValidationFilter filter){
        if(validationFilterList.contains(filter)){
            validationFilterList.remove((Object) filter);
        } else {
            validationFilterList.add(filter);
        }
    }

    private void readData(String modeluri, String body) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(body);
        if (node.get("type").asText().equals("validationResult")) {
            JsonNode responseData = node.get("data");
            updateRecentValidationResult(jsonToValidationResultList(mapper, responseData));
        } else
            System.out.println("No Response");
    }

    private void readConstraintList(String body) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
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
                    //Get all Facets
                    //First Create the two fields
                    List<String> enumerationValues = new ArrayList<>();
                    for (JsonNode enumeration : facets.get("enumeration")) {
                        enumerationValues.add(enumeration.asText());
                    }
                    List<String> patternValues = new ArrayList<>();
                    for (JsonNode pattern : facets.get("pattern")) {
                        patternValues.add(pattern.asText());
                    }
                    EMFFacetConstraints emfFacetConstraints = new EMFFacetConstraints(facets.get("whiteSpace").asInt(),
                        enumerationValues,
                        patternValues, 
                        facets.get("totalDigits").asInt(), 
                        facets.get("fractionDigits").asInt(), 
                        facets.get("length").asInt(), 
                        facets.get("minLength").asInt(),
                        facets.get("maxLength").asInt(), 
                        facets.get("minExclusive").asText(), 
                        facets.get("maxExclusive").asText(), 
                        facets.get("minInclusive").asText(), 
                        facets.get("maxInclusive").asText());
                    featuresMap.put(Integer.parseInt(featureKey), emfFacetConstraints);
                }
                this.inputValidationMap.put(Integer.parseInt(elementKey), featuresMap);
            }
        } else
            System.out.println("No Response");
    }

    private void updateRecentValidationResult(List<ValidationResult> validationResults) {
        this.recentValidationResult = validationResults;
        this.changeListener.changed(validationResults);
    }

    private List<ValidationResult> jsonToValidationResultList(ObjectMapper mapper, JsonNode responseData){
        List<ValidationResult> result = new ArrayList();
        //Filter out the Diagnosis Message
        if(!(responseData.get("code").asInt() == 0 && responseData.get("source").asText().equals("org.eclipse.emf.ecore"))){
            //Check if this is an Error and add it if it is not filtered
            if(responseData.get("severity").asInt() != 0){
                ValidationResult validationResult = jsonToValidationResult(mapper, responseData);
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

    private ValidationResult jsonToValidationResult(ObjectMapper mapper, JsonNode diagnosticData) {
        String identifier = diagnosticData.get("id").asText();
        BasicDiagnostic diagnostic = jsonToDiagnostic(mapper, diagnosticData);
        return new ValidationResult(identifier, diagnostic);
    }

    public BasicDiagnostic jsonToDiagnostic(ObjectMapper mapper, JsonNode responseData){
        Object[] dataField = mapper.convertValue(responseData.get("data"), Object[].class);
        List<BasicDiagnostic> children = new ArrayList<>();
        for (JsonNode child : responseData.get("children")) {
            children.add(jsonToDiagnostic(mapper, child));
        }
        BasicDiagnostic result;
        if (children.isEmpty()) {
            result = new BasicDiagnostic(responseData.get("severity").asInt(), responseData.get("source").asText(),
                    responseData.get("code").asInt(), responseData.get("message").asText(), dataField);
        } else {
            result = new BasicDiagnostic(responseData.get("source").asText(), responseData.get("code").asInt(),
                    children, responseData.get("message").asText(), dataField);
            result.recomputeSeverity();
        }
        return result;
    }
}
