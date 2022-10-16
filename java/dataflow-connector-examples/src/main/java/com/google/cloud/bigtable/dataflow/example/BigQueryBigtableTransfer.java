/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.bigtable.dataflow.example;

import static com.google.cloud.bigtable.dataflow.example.UserActivity.SIGNUM_EVENT_FIELD_PREFIX;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.cloud.bigtable.beam.CloudBigtableIO;
import com.google.cloud.bigtable.beam.CloudBigtableTableConfiguration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;

/**
 * <p>
 * This is an example of Bigtable with Dataflow using a Sink. The main method adds the data from
 * BigQuery into the pipeline, converts them to Puts, and then writes the Puts to a Cloud Bigtable
 * table of your choice. In this example, the item key is auto-generated using UUID. This has to be
 * designed/modified according to the access pattern in your application.
 * <p>
 * Prerequisites: Create a Cloud Bigtable instance/cluster, and create the table. Expecting column
 * family 'cf' create 'bigquery_to_bigtable_test','cf'
 */

public class BigQueryBigtableTransfer {
  
  public static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  
  public static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
      .append(DateTimeFormatter.ISO_LOCAL_DATE)
      .appendLiteral(" ")
      .append(DateTimeFormatter.ISO_LOCAL_TIME)
      .appendPattern("[SSS SSSSSS]")
      .appendPattern("X")
      .toFormatter();
  
  public static final DateTimeFormatter dateTimeBtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  
  static final DoFn<BigQueryUserActivity, Mutation> MUTATION_TRANSFORM = new DoFn<BigQueryUserActivity, Mutation>() {
    
    private OffsetDateTime convertStringToOffsetDateTime(String stringDateTime) {
      return ZonedDateTime.parse(stringDateTime, formatter).withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
    }
  
    private JsonNode convertRawActivityToJsonNode(String rawActivity) {
      try {
        return objectMapper.readTree(rawActivity);
      } catch (JsonProcessingException e) {
        e.printStackTrace();

        return null;
      }
    }
    
    private String constructRowKey(
        String userId,
        String activityType,
        String occuredAt,
        String eventIdentityId,
        String eventId
    ) {
      OffsetDateTime occurredAt = convertStringToOffsetDateTime(occuredAt);
      
      Long reverseTimestamp = Long.MAX_VALUE - occurredAt.toInstant().toEpochMilli();
  
      String usedUniqueId = (eventId != null)
          ? eventId
          : eventIdentityId;
  
      return String.format(
          "%s#%s#%s#%s",
          userId,
          activityType,
          reverseTimestamp,
          usedUniqueId
      );
    }

    @ProcessElement
    public void processElement(DoFn<BigQueryUserActivity, Mutation>.ProcessContext c) throws Exception {
      BigQueryUserActivity row = c.element();
      
      String constructedRowKey = this.constructRowKey(
          row.userId,
          row.rawActivityType,
          row.occuredAt,
          row.eventIdentityId,
          row.eventId
      );
  
      JsonNode rawActivity = convertRawActivityToJsonNode(row.rawActivity);
      JsonNode eventPayload = JacksonUtil.removeFieldsFromJsonNode(rawActivity, SIGNUM_EVENT_FIELD_PREFIX);
      JsonNode signumPayload = JacksonUtil.retainFieldsFromJsonNode(rawActivity, SIGNUM_EVENT_FIELD_PREFIX);
      
      Put p = new Put(constructedRowKey.getBytes());
      
      p.addColumn(UserActivity.Core.FAMILY.getBytes(), UserActivity.Core.COLUMN_QUALIFIER_ID.getBytes(), row.id.getBytes());
      p.addColumn(UserActivity.Core.FAMILY.getBytes(), UserActivity.Core.COLUMN_QUALIFIER_USER_ID.getBytes(), row.userId.getBytes());
      p.addColumn(UserActivity.Core.FAMILY.getBytes(), UserActivity.Core.COLUMN_QUALIFIER_ACTIVITY_TYPE.getBytes(), row.rawActivityType.getBytes());
      p.addColumn(UserActivity.Core.FAMILY.getBytes(), UserActivity.Core.COLUMN_QUALIFIER_EVENT_IDENTITY_ID.getBytes(), row.eventIdentityId.getBytes());
      
      OffsetDateTime occuredAt = convertStringToOffsetDateTime(row.occuredAt);
      OffsetDateTime createdAt = convertStringToOffsetDateTime(row.createdAt);
      OffsetDateTime updatedAt = convertStringToOffsetDateTime(row.updatedAt);
      
      p.addColumn(UserActivity.Core.FAMILY.getBytes(), UserActivity.Core.COLUMN_QUALIFIER_OCCURED_AT.getBytes(), dateTimeBtFormatter.format(occuredAt).getBytes());
      p.addColumn(UserActivity.Core.FAMILY.getBytes(), UserActivity.Core.COLUMN_QUALIFIER_CREATED_AT.getBytes(), dateTimeBtFormatter.format(createdAt).getBytes());
      p.addColumn(UserActivity.Core.FAMILY.getBytes(), UserActivity.Core.COLUMN_QUALIFIER_UPDATED_AT.getBytes(), dateTimeBtFormatter.format(updatedAt).getBytes());
      
      if (row.eventId != null) {
        p.addColumn(UserActivity.Core.FAMILY.getBytes(), UserActivity.Core.COLUMN_QUALIFIER_EVENT_ID.getBytes(), row.eventId.getBytes());
      }
  
      if (row.eventSessionId != null) {
        p.addColumn(UserActivity.Core.FAMILY.getBytes(), UserActivity.Core.COLUMN_QUALIFIER_EVENT_SESSION_ID.getBytes(), row.eventSessionId.getBytes());
      }
      
      p.addColumn(UserActivity.Activity.FAMILY.getBytes(), UserActivity.Activity.COLUMN_QUALIFIER_JSON.getBytes(), eventPayload.toString().getBytes());
      p.addColumn(UserActivity.Signum.FAMILY.getBytes(), UserActivity.Signum.COLUMN_QUALIFIER_JSON.getBytes(), signumPayload.toString().getBytes());
      
      c.output(p);
    }
  };

  /**
   * <p>Creates a dataflow pipeline that creates the following chain:</p>
   * <ol>
   *   <li> Gets the records into the Pipeline
   *   <li> Creates Puts from each of the records
   *   <li> Performs a Bigtable Put on the records
   * </ol>
   *
   * @param args Arguments to use to configure the Dataflow Pipeline.  The first three are required
   *   when running via managed resource in Google Cloud Platform.  Those options should be omitted
   *   for LOCAL runs.  The last four arguments are to configure the Bigtable connection.
   *        --runner=BlockingDataflowPipelineRunner
   *        --project=[dataflow project] \\
   *        --stagingLocation=gs://[your google storage bucket] \\
   *        --bigtableProject=[bigtable project] \\
   *        --bigtableInstanceId=[bigtable instance id] \\
   *        --bigtableTableId=[bigtable tableName]
   *
   * <p>Note:The Hbase-Bigtable client currently supports upto 100K columns in a single {@link Put}.
   *       If your data is exceeding 100K columns, please create multiple {@link Put} objects.
   */

  public static void main(String[] args) {
    // CloudBigtableOptions is one way to retrieve the options.  It's not required.
    BigQueryBigtableTransferOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(BigQueryBigtableTransferOptions.class);
    
    System.out.println(String.format("Test", options.getBqTable()));

    // CloudBigtableTableConfiguration contains the project, instance and table to connect to.
    CloudBigtableTableConfiguration config =
        new CloudBigtableTableConfiguration.Builder()
        .withProjectId(options.getBigtableProjectId())
        .withInstanceId(options.getBigtableInstanceId())
        .withTableId(options.getBigtableTableId())
        .build();

    Pipeline p = Pipeline.create(options);

    p
        .apply(BigQueryIO
            .readTableRows()
            .from(options.getBqTable())
            .withMethod(BigQueryIO.TypedRead.Method.DIRECT_READ)
        )
        .apply(
            "TableRows to DTO",
            MapElements.into(TypeDescriptor.of(BigQueryUserActivity.class)).via(BigQueryUserActivity::fromTableRow)
        )
        .apply(ParDo.of(MUTATION_TRANSFORM))
        .apply(CloudBigtableIO.writeToTable(config));

    p.run().waitUntilFinish();

  }
}
