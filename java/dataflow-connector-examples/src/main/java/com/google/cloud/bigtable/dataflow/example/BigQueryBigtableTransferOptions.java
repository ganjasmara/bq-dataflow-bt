package com.google.cloud.bigtable.dataflow.example;

import org.apache.beam.sdk.options.Description;

public interface BigQueryBigtableTransferOptions extends CloudBigtableOptions {
  @Description("Table for BigQuery")
  String getBqTable();
  void setBqTable(String value);
}
