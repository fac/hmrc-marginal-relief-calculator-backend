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

import calculator.{ FYConfig, FlatRateConfig, MarginalReliefConfig }
import org.mockito.ArgumentMatchersSugar
import org.mockito.scalatest.IdiomaticMockito
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{ contentAsString, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty }

class ConfigControllerSpec
    extends AnyFreeSpec with Matchers with IdiomaticMockito with ArgumentMatchersSugar with GuiceOneAppPerSuite {
  "GET /config should return json data of config" in {
    val request = FakeRequest("GET", routes.ConfigController.config(2023).url)
    val result = route(app, request).get

    status(result) mustEqual 200
    Json.parse(contentAsString(result)).validate[FYConfig].get mustEqual MarginalReliefConfig(2023, 50000, 250000, 0.19,
      0.25, 0.015)
  }

  "GET /config should fallback to nearest year if requested year is unavailable" in {
    val request = FakeRequest("GET", routes.ConfigController.config(3000).url)
    val result = route(app, request).get

    status(result) mustEqual 200
    Json.parse(contentAsString(result)).validate[FYConfig].get mustEqual FlatRateConfig(2025, 0.19)
  }

  "GET /config should return error json if can't find value" in {
    val request = FakeRequest("GET", routes.ConfigController.config(1000).url)
    val result = route(app, request).get

    status(result) mustEqual 200
    Json.parse(contentAsString(result)) mustEqual Json.parse(
      s"""{ "error": "Configuration for year ${1000} is missing." }"""
    )
  }
}
