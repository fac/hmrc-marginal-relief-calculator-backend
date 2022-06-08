/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.binders

import play.api.mvc.QueryStringBindable

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

object QueryStringBinders {
  implicit val localDateBindable = new QueryStringBindable[LocalDate] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, LocalDate]] = for {
      dateValues <- params.get(key)
      dateValue  <- dateValues.headOption
    } yield Try(LocalDate.parse(dateValue)).toEither.left.map(_ => s"""Cannot parse parameter $key as Date: For input string: "$dateValue"""")

    // $COVERAGE-OFF$
    override def unbind(key: String, value: LocalDate): String = value.format(DateTimeFormatter.ISO_LOCAL_DATE)
    // $COVERAGE-ON$
  }
}
