package com.example.dataengineering

import org.apache.spark.sql.SparkSession

object Main {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .appName("Data Engineering Agent")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    val data = Seq(
      ("Jhonier", 25),
      ("Alice", 30),
      ("Bob", 35)
    )

    val df = data.toDF("name", "age")

    df.show(false)
    println("DataFrame schema:")
    df.printSchema()
    spark.stop()
  }
}