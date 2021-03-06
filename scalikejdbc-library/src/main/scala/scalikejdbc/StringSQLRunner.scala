/*
 * Copyright 2012 Kazuhiro Sera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package scalikejdbc

/**
 * String SQL Runner
 *
 * Basic Usage:
 *
 * {{{
 * import scalikejdbc.StringSQLRunner._
 *
 * val result: List[Map[String, Any]] = "insert into users values (1, 'Alice')".run()
 *
 * val users: List[Map[String, Any]] = "select * from users".run()
 * }}}
 *
 * @param sql SQL value
 */
case class StringSQLRunner(sql: String) {

  /**
   * Runs all SQL and returns result as List[Map[String, Any]]
   * @param session DB Session
   * @return results as List[Map]
   */
  def run()(implicit session: DBSession = AutoSession): List[Map[String, Any]] = try {
    SQL(sql).map(_.toMap()).list.apply()
  } catch {
    case e: java.sql.SQLException =>
      val result = SQL(sql).execute.apply()
      List(Map("RESULT" -> result))
  }

  /**
   * Casts value to expected type value
   *
   * TODO: ClassManifest will be deprecated since Scala 2.10.0
   *
   * @param v value
   * @param manifest manifest of expected type
   * @tparam A expected type
   * @return casted value
   */
  private[scalikejdbc] def cast[A](v: Any)(implicit manifest: ClassManifest[A]): A = {
    if (manifest == classManifest[Int]) {
      v match {
        case null => 0
        case bigDecimal: java.math.BigDecimal => bigDecimal.intValue
        case bigDecimal: scala.math.BigDecimal => bigDecimal.toInt
        case int: java.lang.Integer => java.lang.Integer.parseInt(int.toString)
        case short: java.lang.Short => java.lang.Integer.parseInt(short.toString)
        case x => x
      }
    } else if (manifest == classManifest[Long]) {
      v match {
        case null => 0L
        case bigDecimal: java.math.BigDecimal => bigDecimal.longValue
        case bigDecimal: scala.math.BigDecimal => bigDecimal.toLong
        case int: java.lang.Integer => java.lang.Long.parseLong(int.toString)
        case short: java.lang.Short => java.lang.Long.parseLong(short.toString)
        case x => x
      }
    } else if (manifest == classManifest[String]) {
      v match {
        case null => null
        case v => String.valueOf(v)
      }
    } else {
      v
    }
  }.asInstanceOf[A]

  /**
   * Returns SQL results as List[A]
   *
   * @tparam A value type
   * @return results as List[A]
   */
  def asList[A](implicit manifest: ClassManifest[A]): List[A] = run().map(m => cast[A](m.apply(m.keys.head)))

  /**
   * Returns SQL result as single value optionally
   *
   * @tparam A value type
   * @return a single result as A optionally
   */
  def asOption[A](implicit manifest: ClassManifest[A]): Option[A] = asList[A].headOption

  /**
   * Returns SQL result as single value
   *
   * @tparam A value type
   * @return a single result as A
   */
  def as[A](implicit manifest: ClassManifest[A]): A = asOption[A].get

}

object StringSQLRunner {

  /**
   * Converts String to SQLRunner implicitly
   *
   * @param sql SQL string
   * @return SQLRunner
   */
  implicit def stringToSQLRunner(sql: String) = StringSQLRunner(sql)

}
