# YardSale

This example application illustrates how to use the ZXing library to scan a QR Code, fetch JSON data from a server and convert it into a Java object using Gson, and perform an in-app payment as part of a yard sale workflow. It is highly recommended that you review highlights from the [Android Fundamentals](http://developer.android.com/guide/topics/fundamentals.html) document before attempting to adapt this source code.

In a little more detail:

* You assemble and manage an inventory for your yard sale in a web application. (Note: The YardSale project does not include a web application for managing inventory and instead opts to use a static file to simulate a server response. See the Inquire or Donate projects for extensive examples of implementing Python-powered GAE projects that are easily adaptable for the purpose of managing an inventory.) The web application serves up items using a simple JSON format that provides pertinent item details accessible via a simple URL such as http://example.com/yardsale/123.json to access an item with id 123
* You use Google's Chart APIs to create QR codes for each of the item URLs so that you can print out labels and attach them to items in the same way that ordinary store fronts operate. For example, the sample data.json file provided with this project is accessible as a QR Code via [http://chart.apis.google.com/chart?cht=qr&chs=300x300&chl=http%3A//github.com/zaffra/YardSale/raw/master/item.json&chld=H|0](http://chart.apis.google.com/chart?cht=qr&chs=300x300&chl=http%3A//github.com/zaffra/YardSale/raw/master/item.json&chld=H|0) -- see the [official docs](http://code.google.com/apis/chart/docs/gallery/qr_codes.html) for more details on the Google Chart APIs.
* Potential buyers scan the QR Codes with their Android capable smartphone to view details and potentially purchase the item using PayPal

# Building and running the project = 

There are only a couple of dependencies that you'll need to add to your proejct's build path in Eclipse to build the project:

* [PayPal's Android MPL](https://www.x.com/community/ppx/sdks) - Used to perform in-app purchases with PayPal
* [Gson](http://code.google.com/p/google-gson/) - Used to convert JSON data from remote servers to Java objects
* [ZXing](http://code.google.com/p/zxing/) - Provides barcode scanner functionality that your app can use via an Android Intent. You actually don't have to add this dependency, because it is implemented to install the Barcode Scanner app on-demand so that it can use ZXing via Android's Intent mechanisms. However, it would be trivial to modify the app and package ZXing if you'd prefer to packageit. 

The application code makes no particular assumption about specific hardware or screen real estate and was tested on a Motorola Xoom using Android 3.0.1. Note that it *must* be run on physical hardware to invoke scanning functionality since the Android emulator does not support camera functionality. You could, however, minimally modify the source code to run on the emulator by simulating that a scan takes place in the Scanner.java source file.

# Extending the app

Fun ways to extend this app to a realworld point-of-sale system and compete in PayPal's Android [Developer Challenge](https://www.x.com/community/ppx/devchallenge) might include:

* Implementing a server app to log inventory as opposed to using the mock JSON data source. You might try writing a separate Android client using ZXing to scan barcodes on items and pass them to a server application as part of a web services workflow as opposed to manually typing them in. Amazon's [ItemLookup operation](http://docs.amazonwebservices.com/AWSEcommerceService/4-0/ApiReference/ItemLookupOperation.html) may be useful in resolving ISBNs to relevant details that you'd want to store and provide reasonable suggestions for item prices
* Assuming you implement a web server, try using IPNs to remove items from your inventory when items are purchased to keep a realtime inventory that you don't have to update when items are sold.

Provided by: [Zaffra, LLC](http://zaffra.com)
