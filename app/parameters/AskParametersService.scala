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

package parameters

import calculator.DateUtils.{ financialYearEnd, _ }
import calculator.{ FYConfig, FlatRateConfig, MarginalReliefConfig }
import cats.data.ValidatedNel
import cats.syntax.apply._
import com.google.inject.{ ImplementedBy, Inject, Singleton }
import config.AppConfig

import java.time.LocalDate

@ImplementedBy(classOf[AskParametersServiceImpl])
trait AskParametersService {

  type ValidationResult[A] = ValidatedNel[ParameterError, A]

  def associatedCompaniesParameters(
    accountingPeriodStart: LocalDate,
    accountingPeriodEnd: LocalDate,
    profit: Double,
    exemptDistributions: Option[Double]
  ): ValidationResult[AssociatedCompaniesParameter]
}

@Singleton
class AskParametersServiceImpl @Inject() (appConfig: AppConfig) extends AskParametersService {

  override def associatedCompaniesParameters(
    accountingPeriodStart: LocalDate,
    accountingPeriodEnd: LocalDate,
    profit: Double,
    exemptDistributions: Option[Double]
  ): ValidationResult[AssociatedCompaniesParameter] = {

    def findConfig: Int => ValidationResult[FYConfig] = appConfig.calculatorConfig.findFYConfig(_)(ConfigMissingError)

    val fyEndForAccountingPeriodStart: LocalDate = financialYearEnd(accountingPeriodStart)
    if (fyEndForAccountingPeriodStart.isEqualOrAfter(accountingPeriodEnd)) {
      val fy = fyEndForAccountingPeriodStart.minusYears(1).getYear
      val maybeFYConfig = findConfig(fy)
      maybeFYConfig.map {
        case _: FlatRateConfig =>
          DontAsk
        case _: MarginalReliefConfig =>
          AskFull
      }
    } else {
      val fy1 = fyEndForAccountingPeriodStart.minusYears(1).getYear
      val fy2 = fyEndForAccountingPeriodStart.getYear
      val maybeFY1Config = findConfig(fy1)
      val maybeFY2Config = findConfig(fy2)

      (maybeFY1Config, maybeFY2Config).mapN {
        case (_: FlatRateConfig, _: FlatRateConfig) =>
          DontAsk
        case (c1: MarginalReliefConfig, c2: MarginalReliefConfig) =>
          if (c1.upperThreshold == c2.upperThreshold && c1.lowerThreshold == c2.lowerThreshold)
            AskFull
          else
            AskBothParts(
              Period(accountingPeriodStart, fyEndForAccountingPeriodStart),
              Period(fyEndForAccountingPeriodStart.plusDays(1), accountingPeriodEnd)
            )
        case (_: FlatRateConfig, _: MarginalReliefConfig) =>
          AskOnePart(Period(fyEndForAccountingPeriodStart.plusDays(1), accountingPeriodEnd))
        case (_: MarginalReliefConfig, _: FlatRateConfig) =>
          AskOnePart(Period(accountingPeriodStart, fyEndForAccountingPeriodStart))
      }
    }
  }
}
