# suitepad-proxyserver

## Note: Because the exported components in this app uses a custom permission defined in the webview app,for security reasons, the [webview app](https://github.com/sunragav/suitepad-weview) should be installed first.

Proxy server binds with the [Webview](https://github.com/sunragav/suitepad-weview) activity via a service connection and communication between them happens via Messenger backed by handler in both the ends.

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

# NOTE: THE RELEASE SIGNING KEY HAS BEEN ADDED JUST FOR THE SAKE OF COMPLETION AND DEMONSTRATION. BECAUSE PROGAURD RULES ARE APPLIED ONLY ON THE RELEASE FLAVOR. THE SIGNING KEY SHOULD BE HIDDEN AND KEPT SECRET FROM OTHERS IN A SECURED PLACE AND ACCESSED VIA CI/CD PROCESS. EACH APP IS SIGNED WITH A DIFFERENT SINGING KEY AS PER THE TASK SPEC.

## Custom Progaurd rule
Note the SuitepadRemoteData has been kept as it is a remote data and the GSON library depends on it to map the incoming json from the server response.Without the following rule this class file would have been removed from the final classes.dex, by progaurd.
```
-keepnames class com.sunragav.suitepad.data.remotedata.models.SuitepadRemoteData { <fields>; }
```
### Following image demonstrates the SuitepadRemoteData and its members have been kept without obfuscation as it is. Without the above rule this file will not be there in the classes.dex

![SuitepadRemoteData and its members kept](https://i.imgur.com/C4TPp3e.jpg)

# Before optimization_shrinking vs After optimization

Before optimization (shrinking+progaurd) the APK size is 3.1 MB and download size is 2.8 MB

![without progaurd](https://i.imgur.com/PjNqV55.jpg)

After optimization the size is 1.4 MB and the download size in 1 MB

![after progaurd](https://i.imgur.com/AWQGJAK.jpg)

## Size comparison

![size comparison](https://i.imgur.com/rs3xX40.jpg)
