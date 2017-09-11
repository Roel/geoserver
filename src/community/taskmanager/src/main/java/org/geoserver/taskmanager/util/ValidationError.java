package org.geoserver.taskmanager.util;

public class ValidationError {
    
    public enum ValidationErrorType {MISSING, INVALID_PARAM, INVALID_VALUE}
    
    private ValidationErrorType type;
    
    private String paramName;
    
    private String paramValue;
    
    private String taskType;
    
    public ValidationError(ValidationErrorType type, String paramName, String paramValue, String taskType) {
        this.type = type;
        this.paramName = paramName;
        this.paramValue = paramValue;
        this.taskType = taskType;
    }

    public ValidationErrorType getType() {
        return type;
    }

    public String getParamName() {
        return paramName;
    }

    public String getParamValue() {
        return paramValue;
    }
    
    public String getTaskType() {
        return taskType;
    }

    public String toString() {
        switch (type) {
        case MISSING:
            return paramName + " is missing but required for task type " + taskType;
        case INVALID_PARAM:
            return paramName + " is not a valid parameter for task type " + taskType;
        case INVALID_VALUE:
            return paramValue + " is not a valid parameter value for parameter " + paramName
                    + " in task type " + taskType;
        default:
            return "validationError";
        }
    }
}