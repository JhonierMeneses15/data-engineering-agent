package com.example.dataengineering

import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.SparkSession
import com.example.dataengineering.config.IcebergConfig
import pureconfig.ConfigSource

class IcebergIntegrationTest extends AnyFunSuite {

  test("integration: create Iceberg table and append records") {
    val testWarehouse = s"file:///tmp/iceberg-warehouse-integration-${System.currentTimeMillis()}"
    val spark = SparkSession
      .builder()
      .appName("IcebergIntegrationTest")
      .master("local[2]")
      .config("spark.ui.enabled", "false")
      .config("spark.sql.extensions", "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions")
      .config("spark.sql.catalog.iceberg_local", "org.apache.iceberg.spark.SparkCatalog")
      .config("spark.sql.catalog.iceberg_local.type", "hadoop")
      .config("spark.sql.catalog.iceberg_local.warehouse", testWarehouse)
      .getOrCreate()

    try {
      val config = ConfigSource.resources("iceberg-config.conf").at("iceberg").loadOrThrow[IcebergConfig]
      val warehousePath = config.catalog.warehouse
      val tableName = config.table.identifier

      import spark.implicits._

      val df = Seq(
        ("C001", "Jhonier", 25, java.sql.Date.valueOf("2026-08-22")),
        ("C002", "Alice", 30, java.sql.Date.valueOf("2026-08-22")),
        ("C003", "Bob", 35, java.sql.Date.valueOf("2026-08-23"))
      ).toDF("customer_id", "name", "age", "event_date")

      // Write creates the table (first write with Overwrite mode)
      implicit val implicitSpark: SparkSession = spark
      com.example.dataengineering.iceberg.IcebergWriter().write(df, config)

      // Verify table exists and has data - use the full catalog.table path
      val readDF = spark.read.format("iceberg").load(s"iceberg_local.$tableName")
      assert(readDF.count() === 3)

      // Verify partitioning by event_date
      val schemaCols = readDF.schema.map(_.name)
      assert(schemaCols.contains("event_date"))

      // Append new records
      val newData = Seq(
        ("C004", "David", 40, java.sql.Date.valueOf("2026-08-24"))
      ).toDF("customer_id", "name", "age", "event_date")

      com.example.dataengineering.iceberg.IcebergWriter().write(newData, config)

      // Verify appended data
      val readAfterAppend = spark.read.format("iceberg").load(s"iceberg_local.$tableName")
      assert(readAfterAppend.count() === 4)

      // Verify the new record is present
      val david = readAfterAppend.filter("customer_id = 'C004'")
      assert(david.count() === 1)
      assert(david.collect()(0)(3) === java.sql.Date.valueOf("2026-08-24"))

    } finally {
      spark.stop()
    }
  }
}