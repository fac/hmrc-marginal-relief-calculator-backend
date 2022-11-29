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

package controllers

import parameters.{ AskParametersService, ConfigMissingError }
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, ControllerComponents }
import uk.gov.hmrc.http.UnprocessableEntityException
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.LocalDate
import javax.inject.{ Inject, Singleton }
import scala.concurrent.Future

@Singleton()
class AskParametersController @Inject() (parametersService: AskParametersService, cc: ControllerComponents)
    extends BackendController(cc) {

  def associatedCompanies(
    accountingPeriodStart: LocalDate,
    accountingPeriodEnd: LocalDate
  ): Action[AnyContent] = Action.async { _ =>
    parametersService
      .associatedCompaniesParameters(accountingPeriodStart, accountingPeriodEnd)
      .fold(
        errors =>
          throw new UnprocessableEntityException(
            "Failed to determined associated company parameters for given data: " + errors
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
