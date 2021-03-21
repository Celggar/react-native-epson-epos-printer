# react-native-epson-epos-printer
> React native module to handle Epson ePOS SDK (for now, only Android)

[![npm version](https://img.shields.io/npm/v/react-native-epson-epos-printer.svg?style=flat-square)](https://www.npmjs.com/package/react-native-epson-epos-printer)
[![MIT License](https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square)](https://github.com/Celggar/react-native-epson-epos-printer/blob/master/LICENSE)
[![npm downloads](https://img.shields.io/npm/dm/react-native-epson-epos-printer.svg?style=flat-square)](http://npm-stat.com/charts.html?package=react-native-epson-epos-printer)
[![install size](https://packagephobia.com/badge?p=react-native-epson-epos-printer)](https://packagephobia.com/result?p=react-native-epson-epos-printer)
[![npm version](https://img.shields.io/twitter/follow/celggar.svg?label=Follow%20@celggar)](https://twitter.com/intent/follow?screen_name=celggar)

## 🚀 Installation

Using npm:
```sh
npm i react-native-epson-epos-printer
```

## 📖 Documentation
### Tags:
>The printing order is sequential and will go according to the payload.

### Implementation 

```
import Printer from 'react-native-epson-epos-printer';
```

```
Printer.print(1, '50:57:9C:57:7B:M1', examplePayload)
                .then(console.log)
                .catch(console.log)
```

#### Setting the series of printer and the target language
default (TM_M10, English), it cloud specifies with [Printer Series](#printer-series) and [Text Language](#text-language).
```
Printer.setPrinterClass(printerSeries, textLanguage)
```

#### Controls the device discovery function
 - Update Manifest
```
// file: android/app/src/main/AndroidManifest.xml
...
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
...
```
 - Initialization 
```
import {PrinterDiscover} from 'react-native-epson-epos-printer';
const discover = new PrinterDiscover()
```
 - Start
```
discover.start()
discover.on('discover', console.log)
```
 - Stop
```
discover.stop()
```

#### Different connection method
 - Connect by TCP
```
Printer.print(1, '50:57:9C:57:7B:M1', examplePayload)
Printer.print(1, '192.168.192.168', examplePayload)
```
 - Connect by USB
```
Printer.print(1, 'USB:', examplePayload)
Printer.print(1, 'USB:/dev/udev/*', examplePayload)
```
 - If discovery function has been running, it could auto choose the Printer.
```
Printer.print(1, 'auto', examplePayload)
```
> In the case of USB interface, it is recommended that you obtain permission to access theUSB device in the application in advance.
> In the case of USB interface, it is recommended that you obtain permission
  to access theUSB device in the application in advance.

Enter the following code into the AndroidManifest.xml file.
```
<manifest ...>
    <application>
        <activity ...>
            <intent-filter>
                <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
            </intent-filter>
            <meta-data android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"
              android:resource="@xml/device_filter" />
        </activity>
    </application>
</manifest>
```
Add the res/xml/device_filter.xml in resource file, enter the following code into the device_filter.xml file.
```
<?xml version="1.0" encoding="utf-8"?>  
<resources>  
    <usb-device vendor-id="1208" />
</resources>
```
## 📋 Commands

| Key | Type | Description |
|---|---|---|
|**`image`**|`url`|link to download the image. Alternatively, the `size` tag can be used to indicate the size of the image, by default the size is 187|
|**`line`**|`number`|specifies the number of lines to skip. You can also use the text tag with new lines.|
|**`dotted`**|`number`|specifies the number of centered dotted lines. You can also use the centered tag with "------------------------------".|
|**`centered`**|`string`|centered text.|
|**`text`**|`string`|default text positioned to the left and add new line at the end. You can write a paragraph making line breaks with \n to avoid having to add several text tags.|
|**`left`**|`string`|write the text to the left without adding a new line. Using new lines can cause unexpected problems, to use a new line better use the text tag.|
|**`right`**|`string`|write the text to the right without adding a new line. left and right can be used in combination, when combined a new line will be added by default. Example:`{ "left": "Celggar:", "right": "$33,024.01"}.` |
|**`barcode`**|`string`|print a barcode. default (BARCODE_CODE93). [Barcode Types](#-barcode-types)|
|**`qrcode`**|`string / array`|print one or two QR code in a line.|

### Optional setting

#### text
| Key | Type | Description | Default |
|---|---|---|---|
|**`align`**|`string`|specifies the alignment ("left", "right", "center").|left|
|**`width`**|`number`|specifies the horizontal scaling of characters, integer from 1 to 8.|1|
|**`height`**|`number`|specifies the vertical scaling of characters, integer from 1 to 8.|1|
|**`smooth`**|`boolean`|enables or disables smoothing.|false|
|**`bold`**|`boolean`|enables or disables the bold style.|false|
|**`underscore`**|`boolean`|enables or disables the underscore style.|false|

#### dotted
| Key | Type | Description | Default |
|---|---|---|---|
|**`amount`**|`number`|specifies the number of dot of dotted lines.|32|

#### image
| Key | Type | Description | Default |
|---|---|---|---|
|**`align`**|`string`|specifies the alignment ("left", "right", "center").|center|
|**`size`**|`number`|specifies the size of the image (in pixels).|187|

#### barcode
| Key | Type | Description | Default |
|---|---|---|---|
|**`align`**|`string`|specifies the alignment ("left", "right", "center").|left|
|**`type`**|`number`|specifies the [Barcode Types](#-barcode-types).|9(BARCODE_CODE93)|
|**`width`**|`number`|specifies the width of a single module in dots, integer from 2 to 6.|2|
|**`height`**|`number`|specifies the height of the barcode in dots, integer from 1 to 255.|50|
|**`HRI`**|`string`|human readable interpretation, specifies the HRI position ("HRI_NONE", "HRI_ABOVE", "HRI_BELOW", "HRI_BOTH").|HRI_NONE|

#### qrcode
| Key | Type | Description | Default |
|---|---|---|---|
|**`align`**|`string`|specifies the alignment ("left", "right", "center"). if the type of qrcode input is array, the alignment will be centered.|left|
|**`size`**|`number`|specifies the size of the QR code (in pixels).|187|
|**`shift`**|`number`|only for printing two QR code in a line specifies the size of the space area between both QR code (in pixels).|20|


## 📋 Barcode types
| Barcode | Value |
|---|---|
|**`BARCODE_UPC_A`**|`0`|
|**`BARCODE_UPC_E`**|`1`|
|**`BARCODE_EAN13`**|`2`|
|**`BARCODE_JAN13`**|`3`|
|**`BARCODE_EAN8`**|`4`|
|**`BARCODE_JAN8`**|`5`|
|**`BARCODE_CODE39`**|`6`|
|**`BARCODE_ITF`**|`7`|
|**`BARCODE_CODABAR`**|`8`|
|**`BARCODE_CODE93`**|`9`|
|**`BARCODE_CODE128`**|`10`|
|**`BARCODE_GS1_128`**|`11`|
|**`BARCODE_GS1_DATABAR_OMNIDIRECTIONAL`**|`12`|
|**`BARCODE_GS1_DATABAR_TRUNCATED`**|`13`|
|**`BARCODE_GS1_DATABAR_LIMITED`**|`14`|
|**`BARCODE_GS1_DATABAR_EXPANDED`**|`15`|


## Printer Series
| Series | Value |
|---|---|
|**`TM_M10`**|0|
|**`TM_M30`**|1|
|**`TM_M30II`**|2|
|**`TM_P20`**|3|
|**`TM_P60`**|4|
|**`TM_P60II`**|5|
|**`TM_P80`**|6|
|**`TM_T20`**|7|
|**`TM_T60`**|8|
|**`TM_T70`**|9|
|**`TM_T81`**|10|
|**`TM_T82`**|11|
|**`TM_T83`**|12|
|**`TM_T83III`**|13|
|**`TM_T88`**|14|
|**`TM_T90`**|15|
|**`TM_T100`**|16|
|**`TM_U220`**|17|
|**`TM_U330`**|18|
|**`TM_L90`**|19|
|**`TM_H6000`**|20|

## Text Language
| Language | Value |
|---|---|
|**`English (ANK specification)`**|0|
|**`Japanese`**|1|
|**`Simplified Chinese`**|2|
|**`Traditional Chinese`**|3|
|**`Korean`**|4|
|**`Thai (South Asian specification)`**|5|
|**`Vietnamese (South Asian specification)`**|6|
|**`Multiple languages (UTF-8)`**|7|

## 💻 Example Payload

### sample 1
```
[
  {
    "image": "https://github.githubassets.com/images/modules/logos_page/Octocat.png",
    "align": "center"
  },
  {
    "line": 1
  },
  {
    "centered": "Celggar Company"
  },
  {
    "centered": "RUC 777-136-113133  DV  02"
  },
  {
    "centered": "Capellanía"
  },
  {
    "centered": "Express line 236-5555"
  },
  {
    "line": 2
  },
  {
    "text": "RUC/CIP: C-8-330124"
  },
  {
    "text": "SOCIAL: TODD MULLINS - 8733212"
  },
  {
    "line": 2
  },
  {
    "text": "INVOICE: 0-00000002401\nDOCUMENTO ERP: ACM1PT\nSUCURSAL: Capellanía - Principal\nSELLER: Celggar WEB\n NAME: Celggar"
  },
  {
    "line": 3
  },
  {
    "centered": "FECHA: 11/09/2020    HORA: 8:47"
  },
  {
    "barcode": "0-0000039242",
    "type": 9,
    "align": "center"
  },
  {
    "text": "COMMAND"
  },
  {
    "dotted": 2
  },
  {
    "text": "2.0 x $4.99"
  },
  {
    "text": "C000056297 Abono orgánico humus 40lb $9.98 1.0 x 10.99"
  },
  {
    "text": "C000056298 Rodillo para pintar $9.98 1.0 x 10.99"
  },
  {
    "text": "C000056299 Leche 40lb $9.98 1.0 x 10.99"
  },
  {
    "text": "C000007394 COSTO DE ENVÍO (ACARREO) $7.95"
  },
  {
    "left": "SUBTOTAL:",
    "right": "$109.10"
  },
  {
    "left": "DISCOUNT:",
    "right": "$0.0"
  },
  {
    "dotted": 1
  },
  {
    "left": "SUBTOTAL (again):",
    "right": "$109.10"
  },
  {
    "left": "EXEMPT:",
    "right": "$0.00"
  },
  {
    "left": "GRAVABLE:",
    "right": "$109.10"
  },
  {
    "left": "I.T.B.M.S (7%):",
    "right": "$5.38"
  },
  {
    "left": "TOTAL:",
    "right": "$114.48"
  },
  {
    "left": "PUNTOS C:",
    "right": "$7.83"
  },
  {
    "left": "CAMBIO:",
    "right": "$0"
  },
  {
    "dotted": 2
  },
  {
    "text": "DELIVERY ADDRESS"
  },
  {
    "dotted": 1
  },
  {
    "text": "Panamá, Coclé, Capellanía, Lex Luthor Tower, Apto ACM1PT 01A Calle Justice League - cerca de la cueva de Batman"
  },
  {
    "text": "COMMENTS: NO COMMENTS"
  },
  {
    "text": "EMAIL: lobezno33@jleague.com"
  },
  {
    "text": "PHONE: 236-5555"
  }
]

```

### sample 2
```
[
  {
    "image": "https://github.githubassets.com/images/modules/logos_page/Octocat.png",
    "align": "center",
    "size": 150
  },
  { 
    "text": "2021-JAN",
    "align": "center",
    "width": 2,
    "height": 2,
    "smooth": true,
    "bold": true,
    "underscore": true
  },
  {
    "text": "BK-12345678",
    "align": "center",
    "width": 2,
    "height": 2,
    "smooth": true,
    "bold": true
  },
  {
    "line": 1
  },
  {
    "text": "2021-01-23 12:34:56  type:25",
    "align": "left",
    "width": 1,
    "height": 1,
    "smooth": false,
    "bold": false
  },
  {
    "left":"code：000     total : $888"
  },
  {
    "line": 1
  },
  {
    "barcode":"1234567890",
    "type": 9,
    "align": "center",
    "width": 2,
    "height": 50,
    "HRI": "HRI_ABOVE"
  },
  {
    "qrcode":["left","rigt"],
    "size":200,
    "shift":20
  },
  {
    "dotted":1,
    "amount":40
  },
  {
    "line": 1
  },
  {
    "left":"USB",
    "right":"111 TX"
  },
  {
    "left":"TCP",
    "right":"777 TX"
  },
  {
    "right":"total : $888"
  },
  {
    "dotted":2,
    "amount":40
  },
  {
    "left":"Please keep your invoice and sales details properly, For commodity-related operations, please hold the invoice and sales details."
  }
]
```
