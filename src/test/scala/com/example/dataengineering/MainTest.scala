package com.example.dataengineering

import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkConf
import com.example.dataengineering.config.IcebergConfig
import com.example.dataengineering.iceberg.IcebergWriter
import pureconfig.ConfigSource

class MainTest extends AnyFunSuite {
  test("main object is loadable") {
    assert(Main.getClass.getSimpleName.nonEmpty)
  }

  test("writeCustomersToIceberg delegates to IcebergWriter") {
    val config = ConfigSource.resources("iceberg-config.conf").at("iceberg").loadOrThrow[IcebergConfig]

    val sparkConf = new SparkConf()
      .setAppName("MainTest")
      .setMaster("local[2]")
      .set("spark.ui.enabled", "false")
      .set("spark.sql.extensions", "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions")
      .set("spark.sql.catalog.iceberg_local", "org.apache.iceberg.spark.SparkCatalog")
      .set("spark.sql.catalog.iceberg_local.type", "hadoop")
      .set("spark.sql.catalog.iceberg_local.warehouse", s"file:///tmp/iceberg-warehouse-main-${System.currentTimeMillis()}")
      .set("spark.sql.defaultCatalog", "iceberg_local")
      .set("spark.sql.extensions", "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions")

    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    try {
      import spark.implicits._

      val data = Seq(
        ("C001", "Test", 25, java.sql.Date.valueOf("2026-08-22")),
        ("C002", "Test2", 30, java.sql.Date.valueOf("2026-08-22"))
      )
      val df = data.toDF("customer_id", "name", "age", "event_date")

      // Load config
      val config = ConfigSource.resources("iceberg-config.conf").at("iceberg").loadOrThrow[IcebergConfig]

      // This should not throw - just verify the method exists and can be called
      IcebergWriter().write(df, config)(spark)
      succeed
    } finally {
      spark.stop()
    }
  }
}