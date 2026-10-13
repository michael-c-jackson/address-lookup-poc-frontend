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

package controllers

import base.SpecBase
import connectors.AddressLookupClient
import models.{Address, AddressResult, AddressSearch, SearchType}
import play.api.http.HeaderNames
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase {

  private final class StubAddressLookupClient(result: Future[Seq[AddressResult]]) extends AddressLookupClient {
    var receivedSearch: Option[AddressSearch] = None
    var receivedLanguage: Option[String] = None

    override def search(
      search: AddressSearch,
      acceptLanguage: Option[String]
    )(implicit hc: HeaderCarrier): Future[Seq[AddressResult]] = {
      receivedSearch = Some(search)
      receivedLanguage = acceptLanguage
      result
    }
  }

  private val address = AddressResult(
    id = "GB100002",
    uprn = Some(100002),
    organisation = Some("Example Ltd"),
    address = Address(Seq("1 Test Street"), "Testtown", "AA1 1ZZ"),
    poBox = None
  )

  "Index Controller" - {
    "must return addresses from a valid search" in {
      val client = new StubAddressLookupClient(Future.successful(Seq(address)))
      val application = applicationBuilder().overrides(bind[AddressLookupClient].toInstance(client)).build()

      running(application) {
        val request = FakeRequest(POST, routes.IndexController.onSubmit().url)
          .withHeaders(HeaderNames.ACCEPT_LANGUAGE -> "cy, en")
          .withFormUrlEncodedBody("searchType" -> "postcode", "query" -> "  aa1 \t 1zz ")
        val result = route(application, request).value

        status(result) mustEqual OK
        client.receivedSearch mustEqual Some(AddressSearch(SearchType.Postcode, "AA1 1ZZ"))
        client.receivedLanguage mustEqual Some("cy, en")
        contentAsString(result) must include("1 Test Street")
      }
    }

    "must reject an invalid search value without calling address lookup" in {
      val client = new StubAddressLookupClient(Future.successful(Seq(address)))
      val application = applicationBuilder().overrides(bind[AddressLookupClient].toInstance(client)).build()

      running(application) {
        val request = FakeRequest(POST, routes.IndexController.onSubmit().url)
          .withFormUrlEncodedBody("searchType" -> "uprn", "query" -> "invalid")
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        client.receivedSearch mustEqual None
        contentAsString(result) must include("Enter a valid value for the selected search type.")
      }
    }

    "must show a safe error when address lookup is unavailable" in {
      val client = new StubAddressLookupClient(Future.failed(new RuntimeException("downstream unavailable")))
      val application = applicationBuilder().overrides(bind[AddressLookupClient].toInstance(client)).build()

      running(application) {
        val request = FakeRequest(POST, routes.IndexController.onSubmit().url)
          .withFormUrlEncodedBody("searchType" -> "postTown", "query" -> "Aberystwyth")
        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        contentAsString(result) must include("We cannot look up an address right now. Try again later.")
      }
    }
  }
}
