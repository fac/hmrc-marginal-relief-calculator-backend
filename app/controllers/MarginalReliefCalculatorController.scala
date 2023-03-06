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

package controllers

import calculator.{ ConfigMissingError, MarginalReliefCalculator }
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, ControllerComponents }
import uk.gov.hmrc.http.UnprocessableEntityException
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.LocalDate
import javax.inject.{ Inject, Singleton }
import scala.concurrent.Future

@Singleton()
class MarginalReliefCalculatorController @Inject() (
  cc: ControllerComponents,
  marginalReliefCalculator: MarginalReliefCalculator
) extends BackendController(cc) {

  def calculate(
    accountingPeriodStart: LocalDate,
    accountingPeriodEnd: LocalDate,
    profit: Double,
    exemptDistributions: Option[Double],
    associatedCompanies: Option[Int],
    associatedCompaniesFY1: Option[Int],
    associatedCompaniesFY2: Option[Int]
  ): Action[AnyContent] = Action.async { _ =>
    marginalReliefCalculator
      .compute(
        accountingPeriodStart,
        accountingPeriodEnd,
        BigDecimal(profit),
        BigDecimal(exemptDistributions.getOrElse(0.0)),
        associatedCompanies,
        associatedCompaniesFY1,
        associatedCompaniesFY2
      )
      .fold(
        errors =>
          throw new UnprocessableEntityException(
            "Failed to calculate marginal relief: " + errors
              .map { case ConfigMissingError(year) =>
                throw new UnprocessableEntityException(
                  s"Configuration missing for financial year: $year"
                )
              }
              .toList
              .mkString(", ")
          ),
        success =>
          Future.successful(
            Ok(
              Json.toJson(success)
            )
          )
      )
  }

}
