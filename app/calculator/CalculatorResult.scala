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

package calculator

import julienrf.json.derived
import play.api.libs.json.{ OFormat, __ }

object ThresholdAdjustmentRatioValues {
  def apply(values: (BigDecimal, Int)): ThresholdAdjustmentRatioValues =
    ThresholdAdjustmentRatioValues(values._1.toInt, values._2)
  implicit val format: OFormat[ThresholdAdjustmentRatioValues] = derived.oformat[ThresholdAdjustmentRatioValues]()
}
case class ThresholdAdjustmentRatioValues(numerator: Int, denominator: Int)

sealed trait TaxDetails
case class FlatRate(
  year: Int,
  corporationTax: Double,
  taxRate: Double,
  adjustedProfit: Double,
  adjustedDistributions: Double,
  adjustedAugmentedProfit: Double,
  days: Int
) extends TaxDetails
case class MarginalRate(
  year: Int,
  corporationTaxBeforeMR: Double,
  taxRateBeforeMR: Double,
  corporationTax: Double,
  taxRate: Double,
  marginalRelief: Double,
  adjustedProfit: Double,
  adjustedDistributions: Double,
  adjustedAugmentedProfit: Double,
  adjustedLowerThreshold: Double,
  adjustedUpperThreshold: Double,
  days: Int,
  thresholdAdjustmentRatioValues: ThresholdAdjustmentRatioValues
) extends TaxDetails

object TaxDetails {
  implicit val format: OFormat[TaxDetails] =
    derived.flat.oformat[TaxDetails]((__ \ "type").format[String])
}

sealed trait CalculatorResult {
  def effectiveTaxRate: Double
}
object CalculatorResult {
  implicit val format: OFormat[CalculatorResult] =
    derived.flat.oformat[CalculatorResult]((__ \ "type").format[String])
}

case class SingleResult(
  details: TaxDetails,
  effectiveTaxRate: Double
) extends CalculatorResult

case class DualResult(
  year1: TaxDetails,
  year2: TaxDetails,
  effectiveTaxRate: Double
) extends CalculatorResult
