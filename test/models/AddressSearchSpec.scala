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

package models

import base.SpecBase

class AddressSearchSpec extends SpecBase {

  "AddressSearch" - {
    Seq(
      " aa1 1zz " -> "AA1 1ZZ",
      "gir0aa" -> "GIR0AA",
      "GIR 0AA" -> "GIR 0AA",
      "SW1A  1AA" -> "SW1A 1AA",
      "sw1a \t\t1aa" -> "SW1A 1AA"
    ).foreach { case (input, expected) =>
      s"must accept and normalise the backend-supported postcode ${input.replace("\t", "\\t")}" in {
        val search = AddressSearch(SearchType.Postcode, input).normalised

        search.query mustEqual expected
        search.searchType.isValidQuery(search.query) mustEqual true
      }
    }

    "must still reject malformed postcode boundaries and internal line breaks" in {
      Seq("", "GIR", "GIR 00A", "GIRA 0AA", "AA123 1AA", "SW1A 1A", "SW 1A 1AA", "SW1A\n1AA").foreach { input =>
        val search = AddressSearch(SearchType.Postcode, input).normalised

        withClue(s"postcode: $input") {
          search.searchType.isValidQuery(search.query) mustEqual false
        }
      }
    }

    "must not turn whitespace inside a UPRN into a valid identifier" in {
      val search = AddressSearch(SearchType.Uprn, " 100\t002 ").normalised

      search.searchType.isValidQuery(search.query) mustEqual false
    }
  }
}
