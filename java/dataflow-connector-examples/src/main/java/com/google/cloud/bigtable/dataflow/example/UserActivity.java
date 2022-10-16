package com.google.cloud.bigtable.dataflow.example;

public class UserActivity {
  
  public static final String SIGNUM_EVENT_FIELD_PREFIX = "$event.";
  
  public static class Core {
    public static final String FAMILY = "core";
    
    public static final String COLUMN_QUALIFIER_ID = "id";
    public static final String COLUMN_QUALIFIER_USER_ID = "user_id";
    public static final String COLUMN_QUALIFIER_ACTIVITY_TYPE = "activity_type";
    public static final String COLUMN_QUALIFIER_OCCURED_AT = "occured_at";
    public static final String COLUMN_QUALIFIER_EVENT_IDENTITY_ID = "event_identity_id";
    public static final String COLUMN_QUALIFIER_EVENT_ID = "event_id";
    public static final String COLUMN_QUALIFIER_EVENT_SESSION_ID = "event_session_id";
    public static final String COLUMN_QUALIFIER_CREATED_AT = "created_at";
    public static final String COLUMN_QUALIFIER_UPDATED_AT = "updated_at";
  }
  
  public static class Activity {
    public static final String FAMILY = "activity";
    
    public static final String COLUMN_QUALIFIER_JSON = "json";
  }
  
  public static class Signum {
    public static final String FAMILY = "signum";
    
    public static final String COLUMN_QUALIFIER_JSON = "json";
  }
}
