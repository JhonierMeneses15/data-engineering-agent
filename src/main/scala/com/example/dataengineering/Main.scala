package com.example.dataengineering

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
import org.apache.spark.SparkConf
import com.example.dataengineering.config.IcebergConfig
import com.example.dataengineering.iceberg.IcebergWriter
import pureconfig.ConfigSource

object Main {

  def main(args: Array[String]): Unit = {
    // Load configuration first
    val config = ConfigSource.resources("iceberg-config.conf").loadOrThrow[IcebergConfig]

    // Configure SparkConf before creating SparkSession to avoid default Hive catalog
    val sparkConf = new SparkConf()
      .setAppName("Data Engineering Agent")
      .setMaster("local[*]")
      .set("spark.ui.enabled", "false")
      .set("spark.sql.extensions", "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions")
      // Configure default catalog to be Iceberg Hadoop catalog (not Hive)
      .set("spark.sql.catalog.iceberg_local", "org.apache.iceberg.spark.SparkCatalog")
      .set("spark.sql.catalog.iceberg_local.type", "hadoop")
      .set("spark.sql.catalog.iceberg_local.warehouse", config.catalog.warehouse)
      .set("spark.sql.defaultCatalog", "iceberg_local")
      .set("spark.sql.extensions", "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions")

    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    try {
      import spark.implicits._

      // Load configuration from HOCON file using PureConfig
      val config = ConfigSource.resources("iceberg-config.conf").loadOrThrow[IcebergConfig]

      // Example customer DataFrame (in production, this would be received as input)
      val customerData = Seq(
        ("C001", "Jhonier", 25, "2026-08-22"),
        ("C002", "Alice", 30, "2026-08-22"),
        ("C003", "Bob", 35, "2026-08-23")
      )

      val df = customerData.toDF("customer_id", "name", "age", "event_date")

      // Preserve the input schema
      val preservedSchema = df.schema

      // Write to Iceberg table using the separated writer
      IcebergWriter().write(df, config)(spark)

      println(s"Iceberg table '${config.table.identifier}' written successfully with event_date partitioning")
      println(s"Warehouse path: ${config.catalog.warehouse}")

    } finally {
      spark.stop()
    }
  }
}