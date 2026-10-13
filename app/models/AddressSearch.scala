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

import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

import java.util.Locale
import java.util.regex.Pattern

sealed trait SearchType {
  def endpoint: String

  def isValidQuery(query: String): Boolean
}

object SearchType extends Enumerable.Implicits {

  case object Postcode extends WithName("postcode") with SearchType {
    override val endpoint: String = "/lookup"
    private val queryPattern = Pattern.compile("(?:GIR|[A-Z]{1,2}[0-9][0-9A-Z]?) ?[0-9][A-Z]{2}")

    override def isValidQuery(query: String): Boolean =
      queryPattern.matcher(query).matches()
  }

  case object PostTown extends WithName("postTown") with SearchType {
    override val endpoint: String = "/lookup/by-post-town"

    override def isValidQuery(query: String): Boolean =
      query.nonEmpty
  }

  case object Uprn extends WithName("uprn") with SearchType {
    override val endpoint: String = "/lookup/by-uprn"

    override def isValidQuery(query: String): Boolean =
      query.matches("^[0-9]{1,12}$")
  }

  val values: Seq[SearchType] = Seq(Postcode, PostTown, Uprn)

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map { case (value, index) =>
    RadioItem(
      content = Text(messages(s"addressSearch.searchType.${value.toString}")),
      value = Some(value.toString),
      id = Some(s"searchType_$index")
    )
  }

  implicit val enumerable: Enumerable[SearchType] =
    Enumerable(values.map(value => value.toString -> value): _*)
}

final case class AddressSearch(searchType: SearchType, query: String) {

  def normalised: AddressSearch = {
    val trimmedQuery = query.trim.toUpperCase(Locale.ROOT)
    val normalisedQuery = searchType match {
      case SearchType.Postcode => AddressSearch.postcodeWhitespace.matcher(trimmedQuery).replaceAll(" ")
      case _                   => trimmedQuery
    }
    copy(query = normalisedQuery)
  }

  def payload: JsObject =
    searchType match {
      case SearchType.Postcode => Json.obj("postcode" -> query)
      case SearchType.PostTown => Json.obj("posttown" -> query)
      case SearchType.Uprn     => Json.obj("uprn" -> query)
    }
}

object AddressSearch {
  private val postcodeWhitespace = Pattern.compile("[ \\t]+")
}
