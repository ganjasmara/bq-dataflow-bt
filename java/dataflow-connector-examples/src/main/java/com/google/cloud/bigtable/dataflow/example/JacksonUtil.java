package com.google.cloud.bigtable.dataflow.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JacksonUtil {
  
  /**
   * Right now only supports most outer level
   */
  public static List<String> extractFieldNamesFromJsonNode(JsonNode jsonNode, String fieldPrefix) {
    List<String> fieldNames = new ArrayList<>();
    Iterator<String> iterator = jsonNode.fieldNames();
    
    while (iterator.hasNext()) {
      String fieldName = iterator.next();
      
      if (fieldName.startsWith(fieldPrefix)) {
        fieldNames.add(fieldName);
      }
    }
    
    return fieldNames;
  }
  
  /**
   * Right now only supports most outer level
   */
  public static JsonNode removeFieldsFromJsonNode(JsonNode jsonNode, List<String> toBeRemovedFields) {
    return ((ObjectNode) jsonNode.deepCopy()).remove(toBeRemovedFields);
  }
  
  /**
   * Right now only supports most outer level
   */
  public static JsonNode retainFieldsFromJsonNode(JsonNode jsonNode, List<String> toBeRetainedFields) {
    return ((ObjectNode) jsonNode.deepCopy()).retain(toBeRetainedFields);
  }
  
  /**
   * Right now only supports most outer level
   */
  public static JsonNode removeFieldsFromJsonNode(JsonNode jsonNode, String fieldPrefix) {
    List<String> toBeRemovedFields = JacksonUtil.extractFieldNamesFromJsonNode(jsonNode, fieldPrefix);
    
    return JacksonUtil.removeFieldsFromJsonNode(jsonNode, toBeRemovedFields);
  }
  
  /**
   * Right now only supports most outer level
   */
  public static JsonNode retainFieldsFromJsonNode(JsonNode jsonNode, String fieldPrefix) {
    List<String> toBeRetainedFields = JacksonUtil.extractFieldNamesFromJsonNode(jsonNode, fieldPrefix);
    
    return JacksonUtil.retainFieldsFromJsonNode(jsonNode, toBeRetainedFields);
  }
}