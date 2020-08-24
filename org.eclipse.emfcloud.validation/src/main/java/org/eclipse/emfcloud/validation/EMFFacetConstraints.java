package org.eclipse.emfcloud.validation;

import java.util.List;

public class EMFFacetConstraints {
    
    private int whiteSpace;
    private List<String> enumeration;
    private List<String> pattern;
    private int totalDigets;
    private int fractionDigets;
    private int length;
    private int minLength;
    private int maxLength;
    private String minExclusive;
    private String maxExclusive;
    private String minInclusive;
    private String maxInclusive;

    public int getWhiteSpace() {
        return whiteSpace;
    }

    public void setWhiteSpace(int whiteSpace) {
        this.whiteSpace = whiteSpace;
    }

    public List<String> getEnumeration() {
        return enumeration;
    }

    public void setEnumeration(List<String> enumeration) {
        this.enumeration = enumeration;
    }

    public List<String> getPattern() {
        return pattern;
    }

    public void setPattern(List<String> pattern) {
        this.pattern = pattern;
    }

    public int getTotalDigets() {
        return totalDigets;
    }

    public void setTotalDigets(int totalDigets) {
        this.totalDigets = totalDigets;
    }

    public int getFractionDigets() {
        return fractionDigets;
    }

    public void setFractionDigets(int fractionDigets) {
        this.fractionDigets = fractionDigets;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public String getMinExclusive() {
        return minExclusive;
    }

    public void setMinExclusive(String minExclusive) {
        this.minExclusive = minExclusive;
    }

    public String getMaxExclusive() {
        return maxExclusive;
    }

    public void setMaxExclusive(String maxExclusive) {
        this.maxExclusive = maxExclusive;
    }

    public String getMinInclusive() {
        return minInclusive;
    }

    public void setMinInclusive(String minInclusive) {
        this.minInclusive = minInclusive;
    }

    public String getMaxInclusive() {
        return maxInclusive;
    }

    public void setMaxInclusive(String maxInclusive) {
        this.maxInclusive = maxInclusive;
    }

    public boolean isWhiteSpaceDefault() {
        return whiteSpace == 0;
    }

    public boolean isEnumerationDefault() {
        return enumeration.isEmpty();
    }

    public boolean isPatternDefault() {
        return pattern.isEmpty();
    }

    public boolean isTotalDigetsDefault() {
        return totalDigets == -1;
    }

    public boolean isFractionDigetsDefault() {
        return fractionDigets == -1;
    }

    public boolean isLengthDefault() {
        return length == -1;
    }

    public boolean isMinLengthDefault() {
        return minLength == -1;
    }

    public boolean isMaxLengthDefault() {
        return maxLength == -1;
    }

    public boolean isMinExclusiveDefault() {
        return minExclusive == null;
    }

    public boolean isMaxExclusiveDefault() {
        return maxExclusive == null;
    }

    public boolean isMinInclusiveDefault() {
        return minInclusive == null;
    }

    public boolean isMaxInclusiveDefault() {
        return maxInclusive == null;
    }

    public EMFFacetConstraints(int whiteSpace, List<String> enumeration, List<String> pattern, int totalDigets,
            int fractionDigets, int length, int minLength, int maxLength, String minExclusive, String maxExclusive,
            String minInclusive, String maxInclusive) {
        this.whiteSpace = whiteSpace;
        this.enumeration = enumeration;
        this.pattern = pattern;
        this.totalDigets = totalDigets;
        this.fractionDigets = fractionDigets;
        this.length = length;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.minExclusive = minExclusive;
        this.maxExclusive = maxExclusive;
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }


}
