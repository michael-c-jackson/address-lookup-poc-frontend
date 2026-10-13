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

import config.FrontendAppConfig
import models.{AddressResult, AddressSearch}
import play.api.http.HeaderNames
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2

import java.net.URI
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait AddressLookupClient {
  def search(search: AddressSearch, acceptLanguage: Option[String])(implicit hc: HeaderCarrier): Future[Seq[AddressResult]]
}

@Singleton
class AddressLookupConnector @Inject() (
  http: HttpClientV2,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends AddressLookupClient {

  private val baseUri = URI.create(appConfig.addressLookupBaseUrl)

  override def search(
    search: AddressSearch,
    acceptLanguage: Option[String]
  )(implicit hc: HeaderCarrier): Future[Seq[AddressResult]] = {
    val headers = Seq(HeaderNames.USER_AGENT -> appConfig.appName) ++
      acceptLanguage.map(HeaderNames.ACCEPT_LANGUAGE -> _)

    http
      .post(baseUri.resolve(search.searchType.endpoint).toURL)
      .setHeader(headers: _*)
      .withBody(search.payload)
      .execute[Seq[AddressResult]]
  }
}
