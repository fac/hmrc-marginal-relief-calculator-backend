/*
 * Copyright 2023 HM Revenue & Customs
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

package calculator

import cats.data.ValidatedNel
import cats.implicits.catsSyntaxValidatedId
import play.api.libs.json.{ Json, OFormat }

object CalculatorConfig {
  implicit val format: OFormat[CalculatorConfig] = Json.format[CalculatorConfig]
}
case class CalculatorConfig(private val fyConfigs: Seq[FYConfig]) {
  def findFYConfig[T](year: Int)(error: Int => T): ValidatedNel[T, FYConfig] =
    this.fyConfigs.sortBy(_.year)(Ordering[Int].reverse).find(_.year <= year) match {
      case Some(value) => value.validNel
      case None        => error(year).invalidNel
    }
}
