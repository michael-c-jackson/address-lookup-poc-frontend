/*
 * Copyright 2026 HM Revenue & Customs
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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import models.{AddressSearch, SearchType}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.WireMockSupport
import org.scalatest.time.{Seconds, Span}

class AddressLookupConnectorSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with WireMockSupport
    with GuiceOneAppPerSuite {

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(
        "microservice.services.address-lookup.protocol" -> "http",
        "microservice.services.address-lookup.host" -> wireMockHost,
        "microservice.services.address-lookup.port" -> wireMockPort
      )
      .build()

  private lazy val connector = app.injector.instanceOf[AddressLookupConnector]
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  "AddressLookupConnector" - {
    Seq(
      (SearchType.Postcode, "AA1 1ZZ", "/lookup", """{"postcode":"AA1 1ZZ"}"""),
      (SearchType.PostTown, "TESTTOWN", "/lookup/by-post-town", """{"posttown":"TESTTOWN"}"""),
      (SearchType.Uprn, "100002", "/lookup/by-uprn", """{"uprn":"100002"}""")
    ).foreach { case (searchType, query, path, body) =>
      s"must search by $searchType using the exact endpoint, JSON and headers" in {
        stubFor(
          post(urlEqualTo(path))
            .withHeader("User-Agent", equalTo("address-lookup-poc-frontend"))
            .withHeader("Accept-Language", equalTo("cy, en"))
            .withRequestBody(equalToJson(body))
            .willReturn(
              aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(
                  """[{"id":"GB100002","uprn":100002,"organisation":"Example Ltd","address":{"lines":["1 Test Street"],"town":"Testtown","postcode":"AA1 1ZZ"},"language":"en"}]"""
                )
            )
        )

        val results = connector
          .search(AddressSearch(searchType, query), Some("cy, en"))
          .futureValue(timeout(Span(5, Seconds)))

        results.map(_.id) mustEqual Seq("GB100002")
        results.head.address.postcode mustEqual "AA1 1ZZ"
        verify(
          1,
          postRequestedFor(urlEqualTo(path))
            .withHeader("User-Agent", equalTo("address-lookup-poc-frontend"))
            .withHeader("Accept-Language", equalTo("cy, en"))
            .withRequestBody(equalToJson(body))
        )
      }
    }
  }
}
