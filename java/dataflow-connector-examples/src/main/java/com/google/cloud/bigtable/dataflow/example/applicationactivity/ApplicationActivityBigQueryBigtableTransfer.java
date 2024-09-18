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
package com.google.cloud.bigtable.dataflow.example.applicationactivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.cloud.bigtable.beam.CloudBigtableIO;
import com.google.cloud.bigtable.beam.CloudBigtableTableConfiguration;
import com.google.cloud.bigtable.dataflow.example.BigQueryBigtableTransferOptions;
import com.google.cloud.bigtable.dataflow.example.JacksonUtil;
import com.google.cloud.bigtable.dataflow.example.applicationactivity.ApplicationActivity.Core;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
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

public class ApplicationActivityBigQueryBigtableTransfer {

  public static final Map<String, String> ACTIVITY_TYPE_MAP;

  public static final String ENTITY_TYPE = "APPLICATION_V2";

  // staging
//  static  {
//    ACTIVITY_TYPE_MAP = Map.<String, String>ofEntries(
//        Map.entry("CORE", "1e46729c-2e58-4e2d-9b6d-2f8c032dbd3d"),
//        Map.entry("WORK_INFORMATION", "12bf861b-3389-4421-9316-971806ac613c"),
//        Map.entry("VERIFY", "c62afeb9-e027-46d0-a159-ff71f960cba4"),
//        Map.entry("APPROVE", "34dd1ef9-c772-444d-9690-514d2a55110f"),
//        Map.entry("UNDEFINED", "d018fb0f-f1b9-4e3e-8504-f5145ac17e01"),
//        Map.entry("REMOVE_REFERRAL_CODE_SALES_AGENT", "8cdfd752-ddbf-4e98-904c-63d24b57b24c"),
//        Map.entry("PHONE_VERIFIED", "6e91bf41-887d-4eca-9ac1-52e33dc874dc"),
//        Map.entry("USE_REFERRAL_CODE_BORROWER", "5b43d983-6bd0-4631-b042-e12694ac59d0"),
//        Map.entry("USE_COUPON", "ad828c08-41ba-4c98-b45b-e74e7a4c7b5e"),
//        Map.entry("UPSERT VERIFIED APPLICATION_DATA", "08149c24-2aaa-4a9a-ac17-c7110121feaa"),
//        Map.entry("TELESALES_FOLLOWUP", "d99e28c3-d086-4988-8f6b-d546a0dbc523"),
//        Map.entry("SYNC_SEGMENTATION", "fced443d-05d3-44ef-9158-dca07624f3df"),
//        Map.entry("SUBMIT", "f6ea8148-a7d1-41ba-97b0-ba3976f6ef31"),
//        Map.entry("REVERT_REJECTION", "1719cd13-ff1a-48e0-831f-527980376da3"),
//        Map.entry("RETURN TO USER", "a2bd8a24-ee29-4b12-8cb0-800b706f385f"),
//        Map.entry("REJECT", "cae5a533-f416-4208-a522-95b3290371d4"),
//        Map.entry("REFERENCE_INFORMATION", "4bbfbf23-36b8-4089-8120-e4b7b8604f92"),
//        Map.entry("PREFILL_ID_NUMBER_IN_INCOMPLETE_APPLICATION",
//            "aed3e543-451c-48ba-a5b6-27a917debf09"),
//        Map.entry("PHONE_VERIFICATION_CALL_START", "f11fb01b-6033-4e51-95a4-52434697f3a7"),
//        Map.entry("OPEN", "46250596-4e97-461d-a73c-44becca33063"),
//        Map.entry("LIVING_ADDRESS", "e8ae0368-7a7f-431c-a4b4-4c8d8a287c6e"),
//        Map.entry("INCOME", "aa71886f-417b-45c1-ba63-4e86c0171904"),
//        Map.entry("IDENTITY", "8803fd52-a5a0-46e8-af47-39736177e5c3"),
//        Map.entry("FINISH_MANUAL_ENTRY", "cec9d7aa-3ceb-40a9-9b30-1c12e3332e9a"),
//        Map.entry("DOCUMENT", "232f26c3-3272-4599-bc02-a70c8aaa55c0"),
//        Map.entry("DISBURSE_REQUEST", "7fcdecf4-78f1-4b64-bb09-46fdb599bb25"),
//        Map.entry("DEACTIVATE_USER", "e8e4d9bf-e622-4f93-b0a3-03f775b75535"),
//        Map.entry("CUSTOMER_PHONE", "9a12577d-b99e-482c-9c74-a6358d2be026"),
//        Map.entry("CONDITION", "89bf972a-ceba-457a-9e0f-2f0a7dde0ec9"),
//        Map.entry("CLAIM_MANUAL_ENTRY", "49b095f1-4ce1-480f-8112-a1439d2648ba"),
//        Map.entry("CHECK_MATCHING_HOME_ADDRESS", "64902106-e845-4901-aaf1-30b1b77a683c"),
//        Map.entry("CHECK_MATCHING_COMPANY_ADDRESS", "4a2803d0-7869-4cc4-a68e-dea8d140e2f6"),
//        Map.entry("CHARACTER", "6ed95176-263f-4835-a290-4162a69c13e0"),
//        Map.entry("CHECK_MATCHING_CURRENT_ADDRESS", "ee8def5c-f487-4c1e-ae04-1c0faf8f58cd"),
//        Map.entry("CANCEL_DUPLICATE_APPLICATION", "9ba73b1b-1ad4-49d1-8ed9-b918088ff03f"),
//        Map.entry("Assign Phone Verificator", "4f59bbc1-0f08-4e5f-970a-d58d874bc617"),
//        Map.entry("ACCEPT_TNC", "d07bbc65-fe0b-44fa-a238-ce818a7e3de1"),
//        Map.entry("VALIDATE_COUPON", "3c6cb452-62f7-4776-b660-8417c4bd3734"),
//        Map.entry("USE_VOUCHER", "076c08b8-96e4-4110-8b78-776805517b85"),
//        Map.entry("USE_REFERRAL_CODE_SALES_AGENT", "8efe0281-ec66-4c5d-be3d-51b991bb2f93"),
//        Map.entry("UPDATE_AMOUNT_PERIOD", "c584683a-6d90-48ad-a404-524b73b9b72e"),
//        Map.entry("SUBMIT_RENEWAL_DATA", "7bc9a261-2b4f-4756-bade-31e468ec83c1"),
//        Map.entry("SANITIZE_DATE", "e14257e5-b5f8-41e1-93e1-43b070ea4678"),
//        Map.entry("REMOVE_REFERRAL_CODE_BORROWER", "c4d66432-a865-4f21-b224-db65ec79bf22"),
//        Map.entry("REFERENCE_PHONE", "b0cba16f-b639-4bf8-8b29-63a1b0424bf4"),
//        Map.entry("REDISBURSE", "f19b7d63-6137-4b4a-9ae8-18d69d2ae959"),
//        Map.entry("PREFILL_INCOMPLETE_APPLICATION", "2174614a-0c1f-434a-b364-fbfd514e02b3"),
//        Map.entry("PHONE_VERIFICATION_NOT_REACHABLE", "08cfae5c-7fad-45b2-854a-be9d59820a5f"),
//        Map.entry("OFFICE_PHONE", "d923bdde-ef92-4eaa-ab10-be8d1beb3561"),
//        Map.entry("LIABILITY", "de3d27c6-a88b-44f5-8643-2fc04e8c8371"),
//        Map.entry("KTP_DATA_UPDATE", "b2503494-99f4-4d93-9dee-873f9b422fc3"),
//        Map.entry("FIX INVALID SEQUENCE", "9b24258f-65bc-49ad-bbe8-71ccb0bcfb11"),
//        Map.entry("EXPENSE", "c71db6a2-b365-4479-80e2-a5ad9a16387e"),
//        Map.entry("DELETE", "36c611e0-ff6b-435f-838b-9cdfacdd447b"),
//        Map.entry("CREATE", "a3a4d858-ae70-48c5-9489-90e2cdbcd2fb"),
//        Map.entry("CLAIM_RISK_ANALYST", "06703303-0337-4b97-bc58-1a1e1c44725b"),
//        Map.entry("CHECK_MUTATION_REFLECTING_SALARY", "64a3c685-2379-4a9a-b2f8-39bb11129a4b"),
//        Map.entry("CAPACITY", "0d6684c5-0613-424a-a3a5-41e0b191eb26"),
//        Map.entry("UPDATE", "2337051b-010f-44bf-956d-47f87aff7cd0")
//    );
//  }
  // prod
  static {
    ACTIVITY_TYPE_MAP = Map.<String, String>ofEntries(
        Map.entry("CHECK_MATCHING_HOME_ADDRESS", "8a51439b-b10f-410c-87f2-424bbbee1663"),
        Map.entry("CHECK_MATCHING_CURRENT_ADDRESS", "21c4b63f-e4c2-44b0-83c3-db2006936841"),
        Map.entry("CHECK_MATCHING_COMPANY_ADDRESS", "c34bc881-20b0-4aa5-b132-6435f96a064a"),
        Map.entry("CHARACTER", "3092d096-88c2-46e7-94f1-9bf4ea9ae2c9"),
        Map.entry("CANCEL_DUPLICATE_APPLICATION", "e643d7ef-b06e-4500-9d85-3a18baa9af41"),
        Map.entry("Assign Phone Verificator", "1daac4a9-b8f2-4504-b8be-0dff5f623751"),
        Map.entry("CORE", "053f9f6a-f645-418f-90c2-dc6af8bbebd8"),
        Map.entry("APPROVE", "d7f08d23-556c-4d77-8c44-b171bb17129d"),
        Map.entry("UPDATE", "c5cb982e-7010-4202-bc63-302ce418bda5"),
        Map.entry("REJECT", "7ed30d42-51a7-4799-89ca-0fea89906981"),
        Map.entry("USE_VOUCHER", "d2b8a4e9-486e-40bb-9ae4-0aea68d83644"),
        Map.entry("CAPACITY", "52527a53-8771-4998-abbb-b9b0af822c4d"),
        Map.entry("CLAIM_MANUAL_ENTRY", "d2368e3a-9a7d-4feb-80a8-3163393dcd2c"),
        Map.entry("ACCEPT_TNC", "2d854dc9-85be-4427-afff-a9e0a6008271"),
        Map.entry("WORK_INFORMATION", "bea9ed76-615f-445d-98df-caeb3851e580"),
        Map.entry("VERIFY", "45f2e558-1b20-4223-8334-67e7798f27f3"),
        Map.entry("VALIDATE_COUPON", "be423ee8-27d6-4881-ba86-0f3c5a4e51ac"),
        Map.entry("USE_REFERRAL_CODE_SALES_AGENT", "224abc87-a698-45a1-93a8-67f64d425ce8"),
        Map.entry("USE_REFERRAL_CODE_BORROWER", "400a9688-737e-48d6-bf25-d522ec713f7f"),
        Map.entry("USE_COUPON", "83ca9cf2-f3c2-44a7-94cd-ae82cc267fca"),
        Map.entry("UPSERT VERIFIED APPLICATION_DATA", "cda4abf5-550e-47f1-85ec-d6390dad37d5"),
        Map.entry("UPDATE_AMOUNT_PERIOD", "88736262-5ee1-4545-aa1a-76d1b02060c7"),
        Map.entry("UNDEFINED", "984d077c-da21-490a-b187-eab2f00e8468"),
        Map.entry("TELESALES_FOLLOWUP", "0b39f611-4cbc-4821-9530-934fc270ac12"),
        Map.entry("SYNC_SEGMENTATION", "f4e4dcc5-0abc-45ae-85b2-4195eef86871"),
        Map.entry("SUBMIT_RENEWAL_DATA", "aae6991d-fee1-443a-bc77-3e54e42ed235"),
        Map.entry("SUBMIT", "e697292e-6503-49b2-a95e-d254264cb4d2"),
        Map.entry("SANITIZE_DATE", "2bd39320-406b-490d-826e-49dae5c1cb40"),
        Map.entry("REVERT_REJECTION", "fa5483c6-4788-4576-bb81-d6f523e207ab"),
        Map.entry("RETURN TO USER", "14961108-d9c0-4f90-ac98-0a903b568bdc"),
        Map.entry("REMOVE_REFERRAL_CODE_SALES_AGENT", "0bdc37cb-40dd-4f26-83dc-ac4f3a725055"),
        Map.entry("REMOVE_REFERRAL_CODE_BORROWER", "572b3b83-6434-4d5d-b047-25e09c657ef2"),
        Map.entry("REFERENCE_PHONE", "9dabe162-b1a1-44c9-825b-c9f0b20c8762"),
        Map.entry("REFERENCE_INFORMATION", "bc9c8934-e49c-4899-bc5f-d77324b56499"),
        Map.entry("REDISBURSE", "e9a2efff-e6d4-4aae-be65-2eb867cd8024"),
        Map.entry("PREFILL_INCOMPLETE_APPLICATION", "a0cdea1c-5c2c-4283-9596-fdafc3cac793"),
        Map.entry("PHONE_VERIFIED", "8bb3df28-a878-4ec4-bdd5-34e0e90f59d7"),
        Map.entry("PHONE_VERIFICATION_NOT_REACHABLE", "bfdf10f2-6259-4ebf-9bcb-c532d919cd18"),
        Map.entry("PHONE_VERIFICATION_CALL_START", "20b60b0e-69ea-4944-95f5-8790c1663f41"),
        Map.entry("OPEN", "a2c9d19d-d1d5-46e5-b931-819edb6bcb1b"),
        Map.entry("LIVING_ADDRESS", "b770c482-a709-43b2-be39-7d5051700534"),
        Map.entry("PREFILL_ID_NUMBER_IN_INCOMPLETE_APPLICATION",
            "9f62092b-7ce5-44d7-8bd0-17dd8d3e5e4c"),
        Map.entry("OFFICE_PHONE", "1880a371-0371-4eae-9a7e-7a389951b347"),
        Map.entry("FINISH_MANUAL_ENTRY", "0008c3ca-3d63-4ce1-aa9a-0fe253096ef0"),
        Map.entry("CREATE", "f28e5deb-8ed2-4798-8f73-072323735b76"),
        Map.entry("LIABILITY", "ee12bad2-f877-47fa-a279-f55f8c2b319a"),
        Map.entry("KTP_DATA_UPDATE", "3e85b77d-b46b-415d-8770-b53c95ab03a6"),
        Map.entry("INCOME", "85c2ea73-9cb3-452f-a134-caadd9b30123"),
        Map.entry("IDENTITY", "5931d9b1-963c-49bf-a21b-69dfce0cfc65"),
        Map.entry("FIX INVALID SEQUENCE", "fa431efc-383a-44c0-b056-d7d5f3ec825f"),
        Map.entry("EXPENSE", "8298f464-2d94-4c59-9492-9ce1ae49e64b"),
        Map.entry("DOCUMENT", "8829965f-b3bd-4249-b4cc-7fa312c63cf4"),
        Map.entry("DISBURSE_REQUEST", "ab259095-858c-4544-8c85-dbfd52966f65"),
        Map.entry("DELETE", "4bac353b-9f0b-4b4f-9d30-10751a24b04c"),
        Map.entry("DEACTIVATE_USER", "71abd4f4-4d9e-4778-a6ed-599bd5b20d6c"),
        Map.entry("CUSTOMER_PHONE", "749b20c6-f2c7-42e3-87e7-1409378f14f2"),
        Map.entry("CONDITION", "e6c356e8-d4ea-4f8b-bbaa-2ce50f8c3c38"),
        Map.entry("CLAIM_RISK_ANALYST", "5906f1c0-f77b-4126-9c8d-76ff80713edb"),
        Map.entry("CHECK_MUTATION_REFLECTING_SALARY", "ef9be830-92af-48bc-8f66-cec6c76f965d")
    );
  }
  
  public static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  public static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
      .appendPattern("yyyy-MM-dd HH:mm:ss")
      .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
      .appendPattern(" z")
      .toFormatter();
  
  public static final DateTimeFormatter dateTimeBtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  
  static final DoFn<BigQueryApplicationActivity, Mutation> MUTATION_TRANSFORM = new DoFn<BigQueryApplicationActivity, Mutation>() {

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

    public static final String SIGNUM_EVENT_FIELD_PREFIX = "$event.";
    
    private String constructRowKey(
        String entityId,
        String activityTypeId,
        String eventIdentityId,
        String eventIdParam,
        OffsetDateTime occurredAt
    ) {
      Long reverseTimestamp = Long.MAX_VALUE - occurredAt.toInstant().toEpochMilli();

      String eventId = eventIdParam != null
          ? eventIdParam
          : "null";

      return String.format(
          "%s#%s#%s#%s#%s#%s",
          entityId,
          ENTITY_TYPE,
          activityTypeId,
          reverseTimestamp,
          eventIdentityId,
          eventId
      );
    }

    private Long zoneDateTimeStringToInstant(String stringDateTime) {
      return ZonedDateTime.parse(stringDateTime, formatter)
          .withZoneSameInstant(ZoneOffset.UTC)
          .toInstant()
          .toEpochMilli();
    }

    private JsonNode createRawActivityFromBqRow(BigQueryApplicationActivity row) {
      ObjectNode rootNode = objectMapper.createObjectNode();

      rootNode.put("$event.id", row.eventId);
      rootNode.put("$event.identity.id", row.eventIdentityId);
      rootNode.put("$event.ip.ipv4", row.eventIpIpv4);
      rootNode.put("$event.source.host", row.eventSourceHost);
      rootNode.put("$event.source.name", row.eventSourceName);
      rootNode.put("$event.source.type", row.eventSourceType);
      rootNode.putNull("$event.time.client_override");
      rootNode.putNull("$event.time.client_override.utc");
      rootNode.put("$event.time.client_sent", dateTimeBtFormatter.format(convertStringToOffsetDateTime(row.eventTimeClientSent)));
      rootNode.put("$event.time.client_sent.utc", zoneDateTimeStringToInstant(row.eventTimeClientSentUtc));
      rootNode.put("$event.time.published.utc", zoneDateTimeStringToInstant(row.eventTimeClientSentUtc));
      rootNode.put("$event.time.server_accept.utc", zoneDateTimeStringToInstant(row.eventTimeClientSentUtc));
      rootNode.put("$event.time.source", row.eventTimeSource);
      rootNode.put("$event.time.utc", zoneDateTimeStringToInstant(row.eventTimeUtc));

      // New mappings
      rootNode.put("action", row.action);
      rootNode.put("activity_type", row.activityType);
      rootNode.put("deltaOfChanges", row.deltaOfChanges);
      rootNode.put("entity_id", row.entityId);
      rootNode.put("id", row.id);
      rootNode.put("orderId", row.orderId);
      rootNode.put("status", row.status);
      rootNode.put("statusDetail", row.statusDetail);
      rootNode.put("userId", row.userId);
      rootNode.put("userName", row.userName);
      rootNode.put("userRole", row.userRole);

      // Create signum_event_attributes object
      ObjectNode signumEventAttributes = rootNode.putObject("signum_event_attributes");
      signumEventAttributes.put("$event.identity.id", row.eventIdentityId);
      signumEventAttributes.put("$event.ip.ipv4", row.eventIpIpv4);
      signumEventAttributes.put("$event.source.host", row.eventSourceHost);
      signumEventAttributes.put("$event.source.name", row.eventSourceName);
      signumEventAttributes.put("$event.source.type", row.eventSourceType);
      signumEventAttributes.putNull("$event.time.client_override");
      signumEventAttributes.put("$event.time.client_sent", dateTimeBtFormatter.format(convertStringToOffsetDateTime(row.eventTimeClientSent)));

      // Create signum_event_data object
      ObjectNode signumEventData = rootNode.putObject("signum_event_data");
      signumEventData.put("action", row.action);
      signumEventData.put("activity_type", row.activityType);
      signumEventData.put("deltaOfChanges", row.deltaOfChanges);
      signumEventData.put("entity_id", row.entityId);
      signumEventData.put("id", row.id);
      signumEventData.put("orderId", row.orderId);
      signumEventData.put("status", row.status);
      signumEventData.put("statusDetail", row.statusDetail);
      signumEventData.put("userId", row.userId);
      signumEventData.put("userName", row.userName);
      signumEventData.put("userRole", row.userRole);

      return rootNode;
    }

    @ProcessElement
    public void processElement(DoFn<BigQueryApplicationActivity, Mutation>.ProcessContext c) throws Exception {
      BigQueryApplicationActivity row = c.element();

      String constructedRowKey = this.constructRowKey(
          row.entityId,
          ACTIVITY_TYPE_MAP.get(row.activityType),
          row.eventIdentityId,
          row.eventId,
          convertStringToOffsetDateTime(row.eventTimeClientSentUtc)
      );

      System.out.println(String.format("%s Rowkey: %s", row.entityId, constructedRowKey));
  
      JsonNode rawActivity = createRawActivityFromBqRow(row);
      JsonNode eventPayload = JacksonUtil.removeFieldsFromJsonNode(rawActivity, SIGNUM_EVENT_FIELD_PREFIX);
      JsonNode signumPayload = JacksonUtil.retainFieldsFromJsonNode(rawActivity, SIGNUM_EVENT_FIELD_PREFIX);
      
      Put p = new Put(constructedRowKey.getBytes());
      
      p.addColumn(Core.FAMILY.getBytes(), Core.COLUMN_QUALIFIER_ID.getBytes(), row.id.getBytes());
      p.addColumn(Core.FAMILY.getBytes(), Core.COLUMN_QUALIFIER_ENTITY_ID.getBytes(), row.entityId.getBytes());
      p.addColumn(Core.FAMILY.getBytes(), Core.COLUMN_QUALIFIER_ENTITY_TYPE.getBytes(), ENTITY_TYPE.getBytes());
      p.addColumn(Core.FAMILY.getBytes(), Core.COLUMN_QUALIFIER_ACTIVITY_TYPE.getBytes(), row.activityType.getBytes());
      p.addColumn(Core.FAMILY.getBytes(), Core.COLUMN_QUALIFIER_ACTIVITY_TYPE_ID.getBytes(), ACTIVITY_TYPE_MAP.get(row.activityType).getBytes());
      p.addColumn(Core.FAMILY.getBytes(), Core.COLUMN_QUALIFIER_EVENT_IDENTITY_ID.getBytes(), row.eventIdentityId.getBytes());
      
      OffsetDateTime occuredAt = convertStringToOffsetDateTime(row.eventTimeClientSentUtc);
      OffsetDateTime createdAt = convertStringToOffsetDateTime(row.eventTimeClientSentUtc);
      OffsetDateTime updatedAt = convertStringToOffsetDateTime(row.eventTimeClientSentUtc);
      
      p.addColumn(
          Core.FAMILY.getBytes(), Core.COLUMN_QUALIFIER_OCCURRED_AT.getBytes(), dateTimeBtFormatter.format(occuredAt).getBytes());
      p.addColumn(
          Core.FAMILY.getBytes(), Core.COLUMN_QUALIFIER_CREATED_AT.getBytes(), dateTimeBtFormatter.format(createdAt).getBytes());
      p.addColumn(
          Core.FAMILY.getBytes(), Core.COLUMN_QUALIFIER_UPDATED_AT.getBytes(), dateTimeBtFormatter.format(updatedAt).getBytes());
      
      if (row.eventId != null) {
        p.addColumn(Core.FAMILY.getBytes(), Core.COLUMN_QUALIFIER_EVENT_ID.getBytes(), row.eventId.getBytes());
      }
  
      if (row.eventSessionId != null) {
        p.addColumn(Core.FAMILY.getBytes(), Core.COLUMN_QUALIFIER_EVENT_SESSION_ID.getBytes(), row.eventSessionId.getBytes());
      }
      
      p.addColumn(ApplicationActivity.Activity.FAMILY.getBytes(), ApplicationActivity.Activity.COLUMN_QUALIFIER_JSON.getBytes(), eventPayload.toString().getBytes());
      p.addColumn(ApplicationActivity.Signum.FAMILY.getBytes(), ApplicationActivity.Signum.COLUMN_QUALIFIER_JSON.getBytes(), signumPayload.toString().getBytes());
      
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
    
    System.out.println(String.format("Test %s", options.getBqTable()));

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
            MapElements.into(TypeDescriptor.of(
                BigQueryApplicationActivity.class)).via(
                BigQueryApplicationActivity::fromTableRow)
        )
        .apply(ParDo.of(MUTATION_TRANSFORM))
        .apply(CloudBigtableIO.writeToTable(config));

    p.run().waitUntilFinish();

  }
}
