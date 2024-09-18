package com.google.cloud.bigtable.dataflow.example.applicationactivity;

public class ApplicationActivity {

  public static class Core {
    public static final String FAMILY = "core";

    public static final String COLUMN_QUALIFIER_ID = "id";
    public static final String COLUMN_QUALIFIER_ENTITY_ID = "entity_id";
    public static final String COLUMN_QUALIFIER_ENTITY_TYPE = "entity_type";
    public static final String COLUMN_QUALIFIER_ACTIVITY_TYPE = "activity_type";
    public static final String COLUMN_QUALIFIER_ACTIVITY_TYPE_ID = "activity_type_id";
    public static final String COLUMN_QUALIFIER_OCCURRED_AT = "occurred_at";
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
