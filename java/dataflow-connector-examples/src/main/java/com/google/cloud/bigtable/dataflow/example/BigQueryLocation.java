package com.google.cloud.bigtable.dataflow.example;

import com.google.api.services.bigquery.model.TableRow;
import org.apache.avro.reflect.Nullable;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

import java.io.Serializable;

@DefaultCoder(AvroCoder.class)
public class BigQueryLocation implements Serializable {

  public static BigQueryLocation fromTableRow(TableRow tableRow) {
    String id = (String) tableRow.get("id");
    String deviceId = (String) tableRow.get("device_id");
    String location = (String) tableRow.get("location");
    String label = (tableRow.get("label") != null) ? (String) tableRow.get("label") : null;
    String timestamp = (tableRow.get("timestamp") != null) ? (String) tableRow.get("timestamp") : null;
    Boolean isMockLocation = (tableRow.get("is_mock_location") != null) ? (Boolean) tableRow.get("is_mock_location") : null;
    String createdAt = (String) tableRow.get("created_at");
    String updatedAt = (String) tableRow.get("updated_at");

    return new BigQueryLocation(
        id,
        deviceId,
        location,
        label,
        timestamp,
        isMockLocation,
        createdAt,
        updatedAt
    );
  }

  public BigQueryLocation(
      String id,
      String deviceId,
      String location,
      String label,
      String timestamp,
      Boolean isMockLocation,
      String createdAt,
      String updatedAt
  ) {
    this.id = id;
    this.deviceId = deviceId;
    this.location = location;
    this.label = label;
    this.timestamp = timestamp;
    this.isMockLocation = isMockLocation;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String id;
  public String deviceId;
  public String location;
  @Nullable
  public String label;
  @Nullable
  public String timestamp;
  @Nullable
  public Boolean isMockLocation;
  public String createdAt;
  public String updatedAt;
}
