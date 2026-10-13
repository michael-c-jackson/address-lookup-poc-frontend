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

import connectors.AddressLookupClient
import controllers.actions.IdentifierAction
import forms.AddressSearchFormProvider
import javax.inject.Inject
import play.api.http.HeaderNames
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.IndexView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class IndexController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  formProvider: AddressSearchFormProvider,
  addressLookupClient: AddressLookupClient,
  view: IndexView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(): Action[AnyContent] = identify { implicit request =>
    Ok(view(form, None, lookupFailed = false))
  }

  def onSubmit(): Action[AnyContent] = identify.async { implicit request =>
    form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors, None, lookupFailed = false))),
      submittedSearch => {
        val search = submittedSearch.normalised

        if (!search.searchType.isValidQuery(search.query)) {
          Future.successful(
            BadRequest(
              view(
                form.fill(search).withError("query", "addressSearch.query.error.invalid"),
                None,
                lookupFailed = false
              )
            )
          )
        } else {
          implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

          addressLookupClient
            .search(search, request.headers.get(HeaderNames.ACCEPT_LANGUAGE))
            .map(results => Ok(view(form.fill(search), Some(results), lookupFailed = false)))
            .recover { case NonFatal(_) =>
              InternalServerError(view(form.fill(search), None, lookupFailed = true))
            }
        }
      }
    )
  }
}
