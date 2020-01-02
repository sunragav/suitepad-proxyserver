package com.sunragav.suitepad.data.remotedata.api

import com.sunragav.suitepad.data.remotedata.models.SuitepadRemoteData
import io.reactivex.Single
import java.util.concurrent.TimeUnit

class FakeSuitePadService : SuitePadService {
    companion object {
        const val FAKE_DELAY = 500L
    }

    override fun getSampleJson(): Single<List<SuitepadRemoteData>> {
        return Single.just(
            listOf(
                SuitepadRemoteData(
                    id = "58ab140932dfbcc4253b5236",
                    name = "consectetur",
                    price = 1200,
                    type = "main course"
                ),
                SuitepadRemoteData(
                    id = "58ab140904117a99a73565e4",
                    name = "adipisicing",
                    price = 1400,
                    type = "drink"
                ),
                SuitepadRemoteData(
                    id = "58ab140950d5905bd0d4752a",
                    name = "commodo",
                    price = 500,
                    type = "main course"
                ),
                SuitepadRemoteData(
                    id = "58ab14097e1bf08ae9af7829",
                    name = "labore",
                    price = 1800,
                    type = "drink"
                ),
                SuitepadRemoteData(
                    id = "58ab140961c812ff8022b757",
                    name = "occaecat",
                    price = 1400,
                    type = "appetizer"
                ),
                SuitepadRemoteData(
                    id = "58ab1409b0148f92565506d0",
                    name = "incididunt",
                    price = 1300,
                    type = "drink"
                ),
                SuitepadRemoteData(
                    id = "58ab1409a82cddf441e296c7",
                    name = "ipsum",
                    price = 1500,
                    type = "main course"
                ),
                SuitepadRemoteData(
                    id = "58ab140931b3af85a6a11b10",
                    name = "consectetur",
                    price = 400,
                    type = "drink"
                ),
                SuitepadRemoteData(
                    id = "58ab1409248dc6f777c816ce",
                    name = "ut",
                    price = 2500,
                    type = "drink"
                ),
                SuitepadRemoteData(
                    id = "58ab14097fff45868acc9a94",
                    name = "proident",
                    price = 1300,
                    type = "drink"
                ),
                SuitepadRemoteData(
                    id = "58ab14098a4ea9b9491121fa",
                    name = "in",
                    price = 3700,
                    type = "appetizer"
                )

            )
        ).delay(FAKE_DELAY, TimeUnit.MILLISECONDS)
    }

    override fun getSampleHtml(): Single<String> {
        return Single.just(
            """
         <!DOCTYPE html>
            <html lang="en">
            <head>
            <meta charset="UTF-8">
            <title>Suitepad Menu UI Webview Application - HTML for WebView</title>
            </head>
            <body>
            <table>
            <thead>
            <tr>
            <th>type</th>
            <th>name</th>
            <th>price</th>
            </tr>
            </thead>
            <tbody id="target">
            </tbody>
            </table>
            
            <script>
            var target = document.getElementById('target'),
            xhReq = new XMLHttpRequest(),
            serverResponse,
            jsonResponse,
            i,j,
            fields =  ['type', 'name', 'price'];
            xhReq.open("GET", "http://someremoteurl.com/sample.json", false);
            xhReq.send(null);
    
            serverResponse = xhReq.responseText;
        
            jsonResponse = JSON.parse(serverResponse);
            
            for(i in jsonResponse) {
        
                var row = document.createElement('tr');
                for(j in fields) {
        
                    var column = document.createElement('td'),
                        columnText = document.createTextNode(jsonResponse[i][fields[j]]);
        
                    column.appendChild(columnText);
                    row.appendChild(column);
                
                }
                target.appendChild(row);
            }
            
        
        
          </script>
          </body>
          </html>   
          """.trimIndent()
        ).delay(FAKE_DELAY, TimeUnit.MILLISECONDS)
    }

}