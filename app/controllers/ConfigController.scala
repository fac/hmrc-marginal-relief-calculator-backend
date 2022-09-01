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

import calculator.ConfigMissingError
import cats.data.Validated
import config.AppConfig
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject

class ConfigController @Inject() (cc: ControllerComponents, appConfig: AppConfig) extends BackendController(cc) {
  def config(year:Int): Action[AnyContent] = Action {
    Ok(appConfig.findFYConfig(year)(ConfigMissingError) match {
      case Validated.Valid(a) => Json.toJson(a)
      case Validated.Invalid(e) => Json.parse(s"""{ "error": "Configuration for year ${e.head.year} is missing." }""")
    })
  }
}
