package com.example.dataengineering

import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.SparkSession
import com.example.dataengineering.config.IcebergConfig
import pureconfig.ConfigSource

import org.apache.spark.sql.types.{StructType, StructField, StringType, IntegerType, DateType}

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

      val schema = StructType(Seq(
        StructField("customer_id", StringType, nullable = true),
        StructField("name", StringType, nullable = true),
        StructField("age", IntegerType, nullable = false),
        StructField("event_date", DateType, nullable = true)
      ))

      val df = spark.createDataFrame(
        spark.sparkContext.parallelize(Seq(
          org.apache.spark.sql.Row("C001", "Jhonier", 25, java.sql.Date.valueOf("2026-08-22")),
          org.apache.spark.sql.Row("C002", "Alice", 30, java.sql.Date.valueOf("2026-08-22")),
          org.apache.spark.sql.Row("C003", "Bob", 35, java.sql.Date.valueOf("2026-08-23"))
        )),
        schema
      )

      // Write creates the table (first write with Overwrite mode)
      implicit val implicitSpark: SparkSession = spark
      com.example.dataengineering.iceberg.IcebergWriter().write(df, config)

      // Verify table exists and has data - use the full catalog.table path
      val readDF = spark.read.format("iceberg").load(s"iceberg_local.$tableName")
      assert(readDF.count() === 3)

      // Verify schema matches input (types, column order - Iceberg may adjust nullability)
      val readFields = readDF.schema.map(f => (f.name, f.dataType))
      val inputFields = df.schema.map(f => (f.name, f.dataType))
      assert(readFields === inputFields)

      // Append new records
      val newData = spark.createDataFrame(
        spark.sparkContext.parallelize(Seq(
          org.apache.spark.sql.Row("C004", "David", 40, java.sql.Date.valueOf("2026-08-24"))
        )),
        schema
      )

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