# suitepad-proxyserver
Proxy server binds with the Suitepad Webview activity via a service connection and communication between them happens via Messenger backed by handler in both the ends.

Soon afeter the binding is successful with the Webview Activity, it starts the FileProviderActivity.
The FileProviderActivity returns the Uri's of the sample.json and sample.html with READ and WRITE permission.
It later uses these Uri's to read/write using the contenresolver. 

After receiving these Uri's it starts the http server at port 8091 in a secured manner using the localhost.bks keystore. This is a BKS-V1 bouncy castle keystore supported by Android platform. It has been generated using the KeyStore Explorer.

<Img src="https://i.imgur.com/snDY8eq.jpg"/>

The http server uses this key to open a secured connection.

```kotlin
val keyStoreStream = resources.openRawResource(R.raw.localhost)
                val pass = KeyStoreHelper().getPass().toCharArray()
                val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
                keyStore.load(keyStoreStream, pass)
                val keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
                keyManagerFactory.init(keyStore, pass)
                server = SuitePadHttpServer(
                    port = PORT,
                    keyStore = keyStore,
                    keyStoreManagerFactory = keyManagerFactory,
                    dataSource = repository,
                    htmlUri = clipData.getItemAt(0).uri,
                    jsonUri = clipData.getItemAt(1).uri
                )
```


When the service is run for the very first time, the sample.html and the sample.json are fetched from a fake service class named FakeSuitePadService, where the responses are hardcoded, and then written to the respective html and json Uri's provided by the FileProvider earlier.

Note that to hide the password of the keystore from getting discovered by a decompiler, a native c++ library is used. In the above code snippet the KeyStoreHelper object is used to fetch the passowrd from the native layer.

### Native layer cpp code:
```cpp
#include <string>
#include <jni.h>

extern "C" JNIEXPORT jstring JNICALL
Java_com_sunragav_suitepad_nativelib_KeyStoreHelper_getPass(JNIEnv *env, jobject) {
    std::string pass = "suitepad";
    return env->NewStringUTF(pass.c_str());
}

```

### Kotlin layer that consumes the native code
```kotlin
package com.sunragav.suitepad.nativelib

class KeyStoreHelper {


    @Throws(IllegalArgumentException::class)
    external fun getPass(): String

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
}
```

It then intimates the webview app that the http server has been started. It does this by sending a message to the webview activity using the messenger and the handler they have already exchanged between each other.
The webview is loaded with url https://localhost:8091/sample.html. Because the sample.html has an AJAX request embedded in the html js script,
it triggers one more GET request requesting a sample.json file. Because this http server in this service, already has these Uri's handy
from the FileProvider app, it uses the content resolver to reoslve the contents of these files and returns them back as a plain text back.
To install this app from android studio we need to edit the run configurations and choose Nothing in the launch options as shown below:

![Imgur](https://i.imgur.com/3RSH8yL.jpg)

## Note: Because this app uses a permission defined in the webview app, the webview app should be installed first.

