package com.google.cloud.bigtable.dataflow.example.location;

public class Location {

  public static class Core {
    public static final String FAMILY = "core";

    public static final String COLUMN_QUALIFIER_ID = "id";
    public static final String COLUMN_QUALIFIER_DEVICE_ID = "device_id";
    public static final String COLUMN_QUALIFIER_LOCATION = "location";
    public static final String COLUMN_QUALIFIER_LABEL = "label";
    public static final String COLUMN_QUALIFIER_IS_MOCK_LOCATION = "is_mock_location";
    public static final String COLUMN_QUALIFIER_TIMESTAMP = "timestamp";
    public static final String COLUMN_QUALIFIER_CREATED_AT = "created_at";
    public static final String COLUMN_QUALIFIER_UPDATED_AT = "updated_at";
  }
}
