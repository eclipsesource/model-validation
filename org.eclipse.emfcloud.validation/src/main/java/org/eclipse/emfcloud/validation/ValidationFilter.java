package org.eclipse.emfcloud.validation;

public class ValidationFilter {

    private int code;
    private String source;

    public ValidationFilter(int code, String source){
        this.code = code;
        this.source = source;
    }

    public int getCode(){
        return code;
    }

    public String getSource(){
        return source;
    }
    
}
