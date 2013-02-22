package scalikejdbc

import org.scalatest._
import org.scalatest.matchers._
import scala.util.control.Exception._
import java.util.NoSuchElementException

class StringSQLRunnerSpec extends FlatSpec with ShouldMatchers with Settings {

  val tableNamePrefix = "emp_StringSQLRunnerSpec" + System.currentTimeMillis()

  behavior of "StringSQLRunner"

  it should "be available" in {

    val tableName = tableNamePrefix + "_beAvailable"
    ultimately(TestUtils.deleteTable(tableName)) {
      TestUtils.initialize(tableName)

      import scalikejdbc.StringSQLRunner._

      // run insert SQL
      ("insert into " + tableName + " values (3, 'Ben')").run

      // run select SQL
      val result = ("select id,name from " + tableName + " where id = 3").run
      if (result.head.get("ID").isDefined) {
        result.head.get("ID").get should equal(3)
        result.head.get("NAME").get should equal("Ben")
      } else {
        result.head.get("id").get should equal(3)
        result.head.get("name").get should equal("Ben")
      }

      // should be found
      ("select name from " + tableName + " where id = 3").asList[String] should equal(List("Ben"))
      ("select name from " + tableName + " where id = 3").asOption[String] should equal(Some("Ben"))
      ("select name from " + tableName + " where id = 3").as[String] should equal("Ben")

      // should not be found
      ("select name from " + tableName + " where id = 999").asList[String] should equal(List())
      ("select name from " + tableName + " where id = 999").asOption[String] should equal(None)
      try {
        ("select name from " + tableName + " where id = 999").as[String]
        fail("NoSuchElementException is expected")
      } catch {
        case e: NoSuchElementException =>
      }

    }
  }

  it should "cast number values" in {

    val runner = new StringSQLRunner("")
    val expectedInt: Int = 123
    val expectedLong: Long = 123L
    val expectedString: String = "123"

    val javaInteger: java.lang.Integer = java.lang.Integer.parseInt("123")
    val scalaInt: Int = 123
    val javaShort: java.lang.Short = java.lang.Short.parseShort("123")
    val scalaShort: Short = 123
    val javaBigDecimal: java.math.BigDecimal = new java.math.BigDecimal("123")
    val scalaBigDecimal: scala.math.BigDecimal = scala.math.BigDecimal("123")

    runner.cast[Int](javaInteger) should equal(expectedInt)
    runner.cast[Int](scalaInt) should equal(expectedInt)
    runner.cast[Int](javaShort) should equal(expectedInt)
    runner.cast[Int](scalaShort) should equal(expectedInt)
    runner.cast[Int](javaBigDecimal) should equal(expectedInt)
    runner.cast[Int](scalaBigDecimal) should equal(expectedInt)

    runner.cast[Long](javaInteger) should equal(expectedLong)
    runner.cast[Long](scalaInt) should equal(expectedLong)
    runner.cast[Long](javaShort) should equal(expectedLong)
    runner.cast[Long](scalaShort) should equal(expectedLong)
    runner.cast[Long](javaBigDecimal) should equal(expectedLong)
    runner.cast[Long](scalaBigDecimal) should equal(expectedLong)

    runner.cast[String](javaInteger) should equal(expectedString)
    runner.cast[String](scalaInt) should equal(expectedString)
    runner.cast[String](javaShort) should equal(expectedString)
    runner.cast[String](scalaShort) should equal(expectedString)
    runner.cast[String](javaBigDecimal) should equal(expectedString)
    runner.cast[String](scalaBigDecimal) should equal(expectedString)

  }

}