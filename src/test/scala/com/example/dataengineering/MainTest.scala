package com.example.dataengineering

import org.scalatest.funsuite.AnyFunSuite

class MainTest extends AnyFunSuite {
  test("main object is loadable") {
    assert(Main.getClass.getSimpleName.nonEmpty)
  }
}
