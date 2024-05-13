package com.google.cloud.bigtable.dataflow.example.battery;

public class Battery {
  public static class Core {
    public static final String FAMILY = "core";

    public static final String COLUMN_QUALIFIER_ID = "id";
    public static final String COLUMN_QUALIFIER_BATTERY = "battery";
    public static final String COLUMN_QUALIFIER_DEVICE_ID = "device_id";
    public static final String COLUMN_QUALIFIER_DATE = "date";
    public static final String COLUMN_QUALIFIER_IP_ADDRESS = "ip_address";
    public static final String COLUMN_QUALIFIER_CREATED_AT = "created_at";
    public static final String COLUMN_QUALIFIER_UPDATED_AT = "updated_at";
  }
}
