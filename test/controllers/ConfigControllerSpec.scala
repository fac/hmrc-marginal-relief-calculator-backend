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

import calculator.CalculatorConfig
import config.AppConfig
import org.mockito.ArgumentMatchersSugar
import org.mockito.scalatest.IdiomaticMockito
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.Helpers.{ contentAsString, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty }
import play.api.test.FakeRequest

class ConfigControllerSpec
    extends AnyFreeSpec with Matchers with IdiomaticMockito with ArgumentMatchersSugar with GuiceOneAppPerSuite {
  "GET /config should return json data of config" in {
    val appConfig = app.injector.instanceOf(classOf[AppConfig])
    val request = FakeRequest("GET", routes.ConfigController.config().url)

    val result = route(app, request).get

    status(result) mustEqual 200
    Json.parse(contentAsString(result)).validate[CalculatorConfig].get mustEqual appConfig.calculatorConfig
  }
}
