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

    @Override
    public boolean equals(Object obj) {
        ValidationFilter other = (ValidationFilter) obj;
        if (code == other.code)
            return true;
        if (source.equals(other.source)) {
            return true;
        }
        return false;
    }

}
