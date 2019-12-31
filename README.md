# suitepad-proxyserver
Proxy server starts a foreground service with a notification channel, configured with a pending intent, to start the Suitepad Webview activity.
While the onStartCommand is invoked as it is started by the Webview Activity, it starts the FileProviderActivity.
The FileProviderActivity returns the Uri's of the sample.json and sample.html with READ and WRITE permission.
It later uses these Uri's to load the content of them using the contenresolver.
After receiving these Uri's it starts the http server at port 8091. 
It then intimates the webview app that the http server has been started. It does this by sending a broadcast Intent targetted at the webview activity component.
The webview is loaded with url localhost:8091/sample.html. Because the sample.html has an AJAX request embedded in the html js script,
it triggers one more GET request requesting a sample.json file. Because this http server in this service, has already these Uri's handy
from the FileProvider app, it uses the content resolver to reoslve the contents of these files and returns them back as a plain text back.
To install this app from android studio we need to edit the run configurations and choose Nothing in the launch options as shown below:

![Imgur](https://i.imgur.com/3RSH8yL.jpg)
