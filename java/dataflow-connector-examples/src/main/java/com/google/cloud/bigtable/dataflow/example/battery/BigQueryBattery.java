package com.google.cloud.bigtable.dataflow.example.battery;


import com.google.api.services.bigquery.model.TableRow;
import org.apache.avro.reflect.Nullable;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

import java.io.Serializable;

@DefaultCoder(AvroCoder.class)
public class BigQueryBattery implements Serializable {
  public static BigQueryBattery fromTableRow(TableRow tableRow) {
    String id = (String) tableRow.get("id");
    String battery = (String) tableRow.get("battery");
    String deviceId = (String) tableRow.get("deviceId");
    String date = (String) tableRow.get("date");
    String ipAddress = (tableRow.get("ipAddress") != null) ? (String) tableRow.get("ipAddress") : null;
    String createdAt = (String) tableRow.get("createdAt");
    String updatedAt = (String) tableRow.get("updatedAt");

    return new BigQueryBattery(
        id,
        battery,
        deviceId,
        date,
        ipAddress,
        createdAt,
        updatedAt
    );
  }

  public BigQueryBattery(
      String id,
      String battery,
      String deviceId,
      String date,
      String ipAddress,
      String createdAt,
      String updatedAt
  ) {
    this.id = id;
    this.deviceId = deviceId;
    this.battery = battery;
    this.date = date;
    this.ipAddress = ipAddress;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String id;
  public String battery;
  public String deviceId;
  public String date;
  @Nullable
  public String ipAddress;
  public String createdAt;
  public String updatedAt;
}
