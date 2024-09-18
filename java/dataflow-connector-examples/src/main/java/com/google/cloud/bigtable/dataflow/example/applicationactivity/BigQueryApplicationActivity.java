package com.google.cloud.bigtable.dataflow.example.applicationactivity;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.api.services.bigquery.model.TableRow;
import java.io.Serializable;
import org.apache.avro.reflect.Nullable;

public class BigQueryApplicationActivity implements Serializable {

  public static BigQueryApplicationActivity fromTableRow(TableRow tableRow) {
    // data
    String action = (tableRow.get("action") != null) ? (String) tableRow.get("action") : null;
    String activityType = (String) tableRow.get("activity_type");
    String additionalInfo = (tableRow.get("additionalInfo") != null) ? (String) tableRow.get("additionalInfo") : null;
    String deltaOfChanges = (tableRow.get("deltaOfChanges") != null) ? (String) tableRow.get("deltaOfChanges") : null;
    String entityId = (String) tableRow.get("entity_id");
    String id = (String) tableRow.get("id");
    String orderId = (String) tableRow.get("orderId");
    String status = (tableRow.get("status") != null) ? (String) tableRow.get("status") : null;
    String statusDetail = (tableRow.get("statusDetail") != null) ? (String) tableRow.get("statusDetail") : null;
    String userId = (tableRow.get("userId") != null) ? (String) tableRow.get("userId") : null;
    String userName = (tableRow.get("userName") != null) ? (String) tableRow.get("userName") : null;
    String userRole = (tableRow.get("userRole") != null) ? (String) tableRow.get("userRole") : null;

    // signum attributes
    String eventId = (tableRow.get("__event_id") != null) ? (String) tableRow.get("__event_id") : null;
    String eventIdentityId = (String) tableRow.get("__event_identity_id");
    String eventIpIpv4 = (String) tableRow.get("__event_ip_ipv4");
    String eventSourceHost = (String) tableRow.get("__event_source_host");
    String eventSourceName = (String) tableRow.get("__event_source_name");
    String eventSourceType = (String) tableRow.get("__event_source_type");
    String eventTimeClientOverride = null;
    String eventTimeClientOverrideUtc = null;
    String eventTimePublishedUtc = (String) tableRow.get("__event_time_published_utc");
    String eventTimeServerAcceptUtc = (String) tableRow.get("__event_time_server_accept_utc");
    String eventTimeSource = (String) tableRow.get("__event_time_source");
    String eventTimeUtc = (String) tableRow.get("__event_time_utc");
    String eventTimeClientSentUtc = (String) tableRow.get("__event_time_client_sent_utc");
    String eventSessionId = (tableRow.get("__event_session_id") != null) ? (String) tableRow.get("__event_session_id") : null;
    String eventTimeClientSent = (String) tableRow.get("__event_time_client_sent");

    return new BigQueryApplicationActivity(
        // data
        action,
        activityType,
        additionalInfo,
        deltaOfChanges,
        entityId,
        id,
        orderId,
        status,
        statusDetail,
        userId,
        userName,
        userRole,

        // attributes
        eventId,
        eventIdentityId,
        eventSessionId,
        eventTimeClientSent,
        eventIpIpv4,
        eventSourceHost,
        eventSourceName,
        eventSourceType,
        eventTimeClientOverride,
        eventTimeClientOverrideUtc,
        eventTimePublishedUtc,
        eventTimeServerAcceptUtc,
        eventTimeSource,
        eventTimeUtc,
        eventTimeClientSentUtc
    );
  }
  
  public BigQueryApplicationActivity(
      // data
      String action,
      String activityType,
      String additionalInfo,
      String deltaOfChanges,
      String entityId,
      String id,
      String orderId,
      String status,
      String statusDetail,
      String userId,
      String userName,
      String userRole,

      // attributes
      String eventId,
      String eventIdentityId,
      String eventSessionId,
      String eventTimeClientSent,
      String eventIpIpv4,
      String eventSourceHost,
      String eventSourceName,
      String eventSourceType,
      String eventTimeClientOverride,
      String eventTimeClientOverrideUtc,
      String eventTimePublishedUtc,
      String eventTimeServerAcceptUtc,
      String eventTimeSource,
      String eventTimeUtc,
      String eventTimeClientSentUtc
  ) {
    // data
    this.action = action;
    this.activityType = activityType;
    this.additionalInfo = additionalInfo;
    this.deltaOfChanges = deltaOfChanges;
    this.entityId = entityId;
    this.id = id;
    this.orderId = orderId;
    this.status = status;
    this.statusDetail = statusDetail;
    this.userId = userId;
    this.userName = userName;
    this.userRole = userRole;

    // attributes
    this.eventId = eventId;
    this.eventIdentityId = eventIdentityId;
    this.eventSessionId = eventSessionId;
    this.eventTimeClientSent = eventTimeClientSent;
    this.eventIpIpv4 = eventIpIpv4;
    this.eventSourceHost = eventSourceHost;
    this.eventSourceName = eventSourceName;
    this.eventSourceType = eventSourceType;
    this.eventTimeClientOverride = eventTimeClientOverride;
    this.eventTimeClientOverrideUtc = eventTimeClientOverrideUtc;
    this.eventTimePublishedUtc = eventTimePublishedUtc;
    this.eventTimeServerAcceptUtc = eventTimeServerAcceptUtc;
    this.eventTimeSource = eventTimeSource;
    this.eventTimeUtc = eventTimeUtc;
    this.eventTimeClientSentUtc = eventTimeClientSentUtc;
  }

  // data
  @Nullable
  public String action;
  public String activityType;

  @Nullable
  public String additionalInfo;

  @Nullable
  public String deltaOfChanges;
  public String entityId;
  public String id;
  @Nullable
  public String orderId;
  @Nullable
  public String status;
  @Nullable
  public String statusDetail;
  @Nullable
  public String userId;
  @Nullable
  public String userName;
  @Nullable
  public String userRole;

  // attributes
  public String eventId;
  public String eventIdentityId;
  public String eventSessionId;
  public String eventTimeClientSent;
  public String eventIpIpv4;
  public String eventSourceHost;
  public String eventSourceName;
  public String eventSourceType;
  @Nullable
  public String eventTimeClientOverride;
  @Nullable
  public String eventTimeClientOverrideUtc;
  public String eventTimePublishedUtc;
  public String eventTimeServerAcceptUtc;
  public String eventTimeSource;
  public String eventTimeUtc;
  public String eventTimeClientSentUtc;
}
