package com.example.dataengineering.iceberg

import org.apache.spark.sql.{DataFrame, SparkSession}
import com.example.dataengineering.config.IcebergConfig

trait IcebergWriter {
  def write(df: DataFrame, config: IcebergConfig)(implicit spark: SparkSession): Unit
}

object IcebergWriter {
  def apply(): IcebergWriter = new IcebergWriterImpl()
}

private class IcebergWriterImpl extends IcebergWriter {

  override def write(df: DataFrame, config: IcebergConfig)(implicit spark: SparkSession): Unit = {
    val fullTableIdentifier = s"${config.catalog.name}.${config.table.identifier}"

    if (spark.catalog.tableExists(fullTableIdentifier)) {
      df.writeTo(fullTableIdentifier).append()
    } else {
      df.writeTo(fullTableIdentifier)
        .using("iceberg")
        .create()
    }
  }
}
