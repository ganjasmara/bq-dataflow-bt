package com.google.cloud.bigtable.dataflow.example;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class TestDateTImeFormatter {
  
  public static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
      .append(DateTimeFormatter.ISO_LOCAL_DATE)
      .appendLiteral(" ")
      .append(DateTimeFormatter.ISO_LOCAL_TIME)
      .appendPattern("[SSS SSSSSS]")
      .appendPattern("X")
      .toFormatter();
  
  public static OffsetDateTime convertStringToOffsetDateTime(String stringDateTime) {
    return ZonedDateTime.parse(stringDateTime, formatter).withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
  }
  
  public static void main(String[] args) {
    String test1 = "2022-01-03 06:23:52.521+00";
    
    OffsetDateTime hehe = convertStringToOffsetDateTime(test1);
    System.out.println(Long.MAX_VALUE - hehe.toInstant().toEpochMilli());
  
    String test2 = "2022-01-03 00:31:52.000123+00";
  
    OffsetDateTime hoho = convertStringToOffsetDateTime(test2);
    System.out.println(Long.MAX_VALUE - hoho.toInstant().toEpochMilli());
  
    String test3 = "2022-01-03T06:23:52.521Z";
  
    OffsetDateTime hihi = ZonedDateTime.parse(test3, DateTimeFormatter.ISO_DATE_TIME).withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
    System.out.println(Long.MAX_VALUE - hihi.toInstant().toEpochMilli());
  }
}
