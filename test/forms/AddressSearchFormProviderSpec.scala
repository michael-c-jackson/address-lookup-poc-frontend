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

package forms

import base.SpecBase
import models.{AddressSearch, SearchType}

class AddressSearchFormProviderSpec extends SpecBase {

  private val form = new AddressSearchFormProvider()()

  "AddressSearchFormProvider" - {
    "must bind each supported search type" in {
      Seq(
        "postcode" -> SearchType.Postcode,
        "postTown" -> SearchType.PostTown,
        "uprn" -> SearchType.Uprn
      ).foreach { case (searchType, expectedType) =>
        val result = form.bind(Map("searchType" -> searchType, "query" -> " value "))

        result.value mustEqual Some(AddressSearch(expectedType, "value"))
      }
    }

    "must reject a missing search type" in {
      val result = form.bind(Map("query" -> "AA1 1ZZ"))

      result.errors("searchType").map(_.message) must contain("addressSearch.searchType.error.required")
    }

    "must reject a missing search value" in {
      val result = form.bind(Map("searchType" -> "postcode"))

      result.errors("query").map(_.message) must contain("addressSearch.query.error.required")
    }
  }
}
