package com.example.dataengineering

import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.SparkSession
import com.example.dataengineering.config.IcebergConfig
import pureconfig.ConfigSource

class IcebergWriterTest extends AnyFunSuite {

  test("IcebergConfig loads from HOCON config") {
    val config = ConfigSource.resources("iceberg-config.conf").at("iceberg").loadOrThrow[IcebergConfig]
    
    assert(config.catalog.name === "iceberg_local")
    assert(config.catalog.`type` === "hadoop")
    assert(config.catalog.warehouse === "file:///tmp/iceberg-warehouse")
    assert(config.table.identifier === "default.customers")
    assert(config.table.`partition-field` === "event_date")
    assert(config.write.format === "parquet")
    assert(config.write.`target-file-size-bytes` === 134217728L)
  }

  test("IcebergConfig case classes have correct structure") {
    val config = ConfigSource.resources("iceberg-config.conf").at("iceberg").loadOrThrow[IcebergConfig]
    
    assert(config.catalog.name === "iceberg_local")
    assert(config.catalog.`type` === "hadoop")
    assert(config.table.identifier === "default.customers")
    assert(config.table.`partition-field` === "event_date")
    assert(config.write.format === "parquet")
    assert(config.write.`target-file-size-bytes` === 134217728L)
  }
}