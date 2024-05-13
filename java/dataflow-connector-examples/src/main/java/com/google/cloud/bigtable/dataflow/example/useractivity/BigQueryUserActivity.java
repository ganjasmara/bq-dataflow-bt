package com.google.cloud.bigtable.dataflow.example.useractivity;

import com.google.api.services.bigquery.model.TableRow;
import java.io.Serializable;
import org.apache.avro.reflect.Nullable;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

@DefaultCoder(AvroCoder.class)
public class BigQueryUserActivity implements Serializable {
  
  public static BigQueryUserActivity fromTableRow(TableRow tableRow) {
    String id = (String) tableRow.get("id");
    String userId = (String) tableRow.get("user_id");
    String rawActivityType = (String) tableRow.get("raw_activity_type");
    String occuredAt = (String) tableRow.get("occured_at");
    String rawActivity = (String) tableRow.get("raw_activity");
    String eventId = (tableRow.get("event_id") != null) ? (String) tableRow.get("event_id") : null;
    String eventIdentityId = (String) tableRow.get("event_identity_id");
    String eventSessionId = (tableRow.get("event_session_id") != null) ? (String) tableRow.get("event_session_id") : null;
    String createdAt = (String) tableRow.get("created_at");
    String updatedAt = (String) tableRow.get("updated_at");
    
    return new BigQueryUserActivity(
        id,
        userId,
        rawActivityType,
        occuredAt,
        rawActivity,
        eventId,
        eventIdentityId,
        eventSessionId,
        createdAt,
        updatedAt
    );
  }
  
  public BigQueryUserActivity(
      String id,
      String userId,
      String rawActivityType,
      String occuredAt,
      String rawActivity,
      String eventId,
      String eventIdentityId,
      String eventSessionId,
      String createdAt,
      String updatedAt
  ) {
    this.id = id;
    this.userId = userId;
    this.rawActivityType = rawActivityType;
    this.occuredAt = occuredAt;
    this.rawActivity = rawActivity;
    this.eventId = eventId;
    this.eventIdentityId = eventIdentityId;
    this.eventSessionId = eventSessionId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String id;
  public String userId;
  public String rawActivityType;
  public String occuredAt;
  public String rawActivity;
  @Nullable
  public String eventId;
  public String eventIdentityId;
  @Nullable
  public String eventSessionId;
  public String createdAt;
  public String updatedAt;
}
