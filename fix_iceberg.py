import os

content = """package com.example.dataengineering.iceberg

import org.apache.spark.sql.{DataFrame, SparkSession, SaveMode}
import org.apache.spark.sql.functions.col
import com.example.dataengineering.config.IcebergConfig

/**
 * Trait defining the Iceberg writer interface.
 */
trait IcebergWriter {
  def write(df: DataFrame, config: IcebergConfig)(implicit spark: SparkSession): Unit
}

/**
 * Implementation of IcebergWriter using Spark's DataFrameWriter API with Iceberg format.
 * Supports create-if-not-exists and append semantics with partitioning by event_date.
 */
object IcebergWriter {
  def apply(): IcebergWriter = new IcebergWriterImpl()
}

private class IcebergWriterImpl extends IcebergWriter {

  override def write(df: DataFrame, config: IcebergConfig)(implicit spark: SparkSession): Unit = {
    val tableIdentifier = config.table.identifier
    val partitionField = config.table.`partition-field`
    val targetFileSize = config.write.`target-file-size-bytes`

    // Check if table exists to decide between create and append
    val tableExists = spark.catalog.tableExists(config.table.identifier)

    val mode = if (tableExists) SaveMode.Append else SaveMode.Overwrite

    // Use DataFrameWriter API with Iceberg format
    df.write
      .format("iceberg")
      .mode(mode)
      .option("write.format.default", config.write.format)
      .option("write.target-file-size-bytes", config.write.`target-file-size-bytes`.toString)
      .partitionBy(config.table.`partition-field`)
      .save(config.table.identifier)
  }
}"""

file_path = r'C:\Users\jhoni\Documents\repos\data-engineering-agent\src\main\scala\com\example\dataengineering\IcebergWriter.scala'

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print('File written successfully')