
package org.deeplearning4j.ui.views.html.samediff

import play.twirl.api._
import play.twirl.api.TemplateMagic._


     object SameDiffUI_Scope0 {
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import play.api.mvc._
import play.api.data._

class SameDiffUI extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/():play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*1.4*/("""
"""),format.raw/*2.1*/("""<!DOCTYPE html>

<!--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ~ Copyright (c) 2015-2019 Skymind, Inc.
  ~
  ~ This program and the accompanying materials are made available under the
  ~ terms of the Apache License, Version 2.0 which is available at
  ~ https://www.apache.org/licenses/LICENSE-2.0.
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  ~ WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  ~ License for the specific language governing permissions and limitations
  ~ under the License.
  ~
  ~ SPDX-License-Identifier: Apache-2.0
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-->

<html lang="en">
    <head>

        <meta charset="utf-8">
        <title>SameDiff Graph Visualization</title>
            <!-- start: Mobile Specific -->
        <meta name="viewport" content="width=device-width, initial-scale=1">
            <!-- end: Mobile Specific -->

        <link id="bootstrap-style" href="/assets/webjars/bootstrap/2.3.1/css/bootstrap.min.css" rel="stylesheet">

            <!-- The HTML5 shim, for IE6-8 support of HTML5 elements -->
            <!--[if lt IE 9]>
	  	<script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
		<link id="ie-style" href="/assets/css/ie.css" rel="stylesheet"/>
	<![endif]-->

            <!--[if IE 9]>
		<link id="ie9style" href="/assets/css/ie9.css" rel="stylesheet"/>
	<![endif]-->
    </head>

    <body>
            <!-- Start JavaScript-->
        <script src="/assets/webjars/jquery/2.2.0/jquery.min.js"></script>
        <script src="/assets/webjars/jquery-ui/1.10.2/ui/minified/jquery-ui.min.js"></script>
        <script src="/assets/webjars/bootstrap/2.3.1/js/bootstrap.min.js"></script>
        <script src="/assets/webjars/jquery-cookie/1.4.1-1/jquery.cookie.js"></script>
        <script src="/assets/webjars/dagre/0.8.4/dist/dagre.min.js"></script>
        <script src="/assets/webjars/cytoscape/3.2.5/dist/cytoscape.min.js"></script>
        <script src="/assets/webjars/cytoscape-dagre/2.1.0/cytoscape-dagre.js"></script>
        <script src="/assets/js/samediff/generated/uigraphevents_generated.js"></script>
        <script src="/assets/js/samediff/generated/uigraphstatic_generated.js"></script>
        <script src="/assets/js/samediff/generated/array_generated.js"></script>
        <script src="/assets/js/samediff/generated/utils_generated.js"></script>
        <script src="/assets/js/samediff/generated/variable_generated.js"></script>

        <script src="/assets/js/samediff/samediff-ui.js"></script>

        <div class="container-fluid-full">
            <div class="row-fluid">
                <input type="file" id="file" name="file" />
                <output id="list"></output>
                <div id="graphdiv" style="width: 1024px; height: 800px;">

                </div>
            </div>
        </div>


            <!-- Execute once on page load -->
        <script>
                document.getElementById('file').addEventListener('change', fileSelect, false);
                $(document).ready(function () """),format.raw/*73.47*/("""{"""),format.raw/*73.48*/("""
                    """),format.raw/*74.21*/("""renderSameDiffGraph();
                """),format.raw/*75.17*/("""}"""),format.raw/*75.18*/(""");
        </script>
    </body>
</html>
"""))
      }
    }
  }

  def render(): play.twirl.api.HtmlFormat.Appendable = apply()

  def f:(() => play.twirl.api.HtmlFormat.Appendable) = () => apply()

  def ref: this.type = this

}


}

/**/
object SameDiffUI extends SameDiffUI_Scope0.SameDiffUI
              /*
                  -- GENERATED --
                  DATE: Wed Jan 23 22:32:44 AEDT 2019
                  SOURCE: c:/DL4J/Git/deeplearning4j/deeplearning4j/deeplearning4j-ui-parent/deeplearning4j-play/src/main/views/org/deeplearning4j/ui/views/samediff/SameDiffUI.scala.html
                  HASH: ee379b11d7b482987da65e6f51c4e47570890703
                  MATRIX: 561->1|657->3|685->5|4010->3302|4039->3303|4089->3325|4157->3365|4186->3366
                  LINES: 20->1|25->1|26->2|97->73|97->73|98->74|99->75|99->75
                  -- GENERATED --
              */
          