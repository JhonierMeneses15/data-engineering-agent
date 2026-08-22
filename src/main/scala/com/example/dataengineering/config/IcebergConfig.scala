package com.example.dataengineering.config

import pureconfig.generic.auto._
import pureconfig.generic.semiauto._
import pureconfig.ConfigReader

/** HOCON-configurable Iceberg configuration case classes. */
final case class IcebergCatalogConfig(
  name: String = "iceberg_local",
  `type`: String = "hadoop",
  warehouse: String = "file:///tmp/iceberg-warehouse",
  properties: Map[String, String] = Map.empty
)

final case class IcebergTableConfig(
  identifier: String = "default.customers",
  `partition-field`: String = "event_date"
)

final case class IcebergWriteConfig(
  format: String = "parquet",
  `target-file-size-bytes`: Long = 134217728L
)

final case class IcebergConfig(
  catalog: IcebergCatalogConfig,
  table: IcebergTableConfig,
  write: IcebergWriteConfig
)

object IcebergConfig {
  implicit val catalogConfigReader: ConfigReader[IcebergCatalogConfig] = deriveReader[IcebergCatalogConfig]
  implicit val tableConfigReader: ConfigReader[IcebergTableConfig] = deriveReader[IcebergTableConfig]
  implicit val writeConfigReader: ConfigReader[IcebergWriteConfig] = deriveReader[IcebergWriteConfig]
  implicit val icebergConfigReader: ConfigReader[IcebergConfig] = deriveReader[IcebergConfig]
}