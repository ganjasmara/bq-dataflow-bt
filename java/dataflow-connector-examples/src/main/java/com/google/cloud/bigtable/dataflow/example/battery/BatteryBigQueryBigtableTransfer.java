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
package com.google.cloud.bigtable.dataflow.example.battery;

import com.google.cloud.bigtable.beam.CloudBigtableIO;
import com.google.cloud.bigtable.beam.CloudBigtableTableConfiguration;
import com.google.cloud.bigtable.dataflow.example.BigQueryBigtableTransferOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

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

public class BatteryBigQueryBigtableTransfer {

  public static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
      .append(DateTimeFormatter.ISO_LOCAL_DATE)
      .appendLiteral(" ")
      .append(DateTimeFormatter.ISO_LOCAL_TIME)
      .appendPattern("[SSS SSSSSS]")
      .appendPattern("X")
      .toFormatter();

  public static final DateTimeFormatter dateTimeBtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

  static final DoFn<BigQueryBattery, Mutation> MUTATION_TRANSFORM = new DoFn<BigQueryBattery, Mutation>() {

    private OffsetDateTime convertStringToOffsetDateTime(String stringDateTime) {
      return ZonedDateTime.parse(stringDateTime, formatter).withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
    }

    private String constructRowKey(
        String deviceId,
        String date,
        String id
    ) {
      OffsetDateTime convertedDate = convertStringToOffsetDateTime(date);

      Long reverseDate = Long.MAX_VALUE - convertedDate.toInstant().toEpochMilli();

      return String.format(
          "%s#%s#%s",
          deviceId,
          reverseDate,
          id
      );
    }

    @ProcessElement
    public void processElement(DoFn<BigQueryBattery, Mutation>.ProcessContext c) throws Exception {
      BigQueryBattery row = c.element();

      String constructedRowKey = this.constructRowKey(
          row.deviceId,
          row.date,
          row.id
      );

      Put p = new Put(constructedRowKey.getBytes());

      p.addColumn(Battery.Core.FAMILY.getBytes(), Battery.Core.COLUMN_QUALIFIER_ID.getBytes(), row.id.getBytes());
      p.addColumn(Battery.Core.FAMILY.getBytes(), Battery.Core.COLUMN_QUALIFIER_BATTERY.getBytes(), row.battery.getBytes());
      p.addColumn(Battery.Core.FAMILY.getBytes(), Battery.Core.COLUMN_QUALIFIER_DEVICE_ID.getBytes(), row.deviceId.getBytes());

      OffsetDateTime convertedDate = convertStringToOffsetDateTime(row.date);
      OffsetDateTime createdAt = convertStringToOffsetDateTime(row.createdAt);
      OffsetDateTime updatedAt = convertStringToOffsetDateTime(row.updatedAt);

      p.addColumn(Battery.Core.FAMILY.getBytes(), Battery.Core.COLUMN_QUALIFIER_DATE.getBytes(), dateTimeBtFormatter.format(convertedDate).getBytes());
      p.addColumn(Battery.Core.FAMILY.getBytes(), Battery.Core.COLUMN_QUALIFIER_CREATED_AT.getBytes(), dateTimeBtFormatter.format(createdAt).getBytes());
      p.addColumn(Battery.Core.FAMILY.getBytes(), Battery.Core.COLUMN_QUALIFIER_UPDATED_AT.getBytes(), dateTimeBtFormatter.format(updatedAt).getBytes());

      if (row.ipAddress != null) {
        p.addColumn(Battery.Core.FAMILY.getBytes(), Battery.Core.COLUMN_QUALIFIER_IP_ADDRESS.getBytes(), row.ipAddress.getBytes());
      }

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
            MapElements.into(TypeDescriptor.of(BigQueryBattery.class)).via(BigQueryBattery::fromTableRow)
        )
        .apply(ParDo.of(MUTATION_TRANSFORM))
        .apply(CloudBigtableIO.writeToTable(config));

    p.run().waitUntilFinish();

  }
}
