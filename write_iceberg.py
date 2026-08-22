import sys
import os

content = """package com.example.dataengineering.iceberg

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.col
import com.example.dataengineering.config.IcebergConfig

/**
 * Trait defining the Iceberg writer interface.
 */
trait IcebergWriter {
  def write(df: DataFrame, config: IcebergConfig)(implicit spark: SparkSession): Unit
}

/**
 * Implementation of IcebergWriter using Spark's Iceberg V2 API (writeTo).
 * Uses createOrReplace for first write, append for subsequent writes.
 */
object IcebergWriter {
  def apply(): IcebergWriter = new IcebergWriterImpl()
}

private class IcebergWriterImpl extends IcebergWriter {

  override def write(df: DataFrame, config: IcebergConfig)(implicit spark: SparkSession): Unit = {
    val fullTableIdentifier = s"${config.catalog.name}.${config.table.identifier}"
    val partitionField = config.table.`partition-field`
    val targetFileSize = config.write.`target-file-size-bytes`

    // Check if table exists to decide between create and append
    val tableExists = spark.catalog.tableExists(s"${config.catalog.name}.${config.table.identifier}")

    val writer = df.writeTo(s"${config.catalog.name}.${config.table.identifier}")
      .using("iceberg")
      .option("write.format.default", config.write.format)
      .option("write.target-file-size-bytes", config.write.`target-file-size-bytes`.toString)
      .partitionedBy(col(config.table.`partition-field`))

    if (tableExists) {
      writer.append()
    } else {
      writer.createOrReplace()
    }
  }
}"""

with open(r'C:\Users\jhoni\Documents\repos\data-engineering-agent\src\main\scala\com\example\dataengineering\IcebergWriter.scala', 'w', encoding='utf-8') as f:
    f.write(content)
print('File written successfully')