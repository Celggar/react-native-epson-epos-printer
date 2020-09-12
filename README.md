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
Tags:
>The printing order is sequential and will go according to the payload.

Implementation 

```
import Printer from 'react-native-epson-epos-printer';
```

```
Printer.print(1, '50:57:9C:57:7B:M1', examplePayload)
                .then(console.log)
                .catch(console.log)
```

## 📋 Commands

| Key | Type | Description |
|---|---|---|
|**`image`**|`url`|link to download the image.|
|**`line`**|`number`|specifies the number of lines to skip. You can also use the text tag with new lines.|
|**`dotted`**|`number`|specifies the number of centered dotted lines. You can also use the centered tag with "------------------------------".|
|**`centered`**|`string`|centered text.|
|**`text`**|`string`|default text positioned to the left and add new line at the end. You can write a paragraph making line breaks with \n to avoid having to add several text tags.|
|**`left`**|`string`|write the text to the left without adding a new line. Using new lines can cause unexpected problems, to use a new line better use the text tag.|
|**`right`**|`string`|write the text to the right without adding a new line. left and right can be used in combination, when combined a new line will be added by default. Example:`{ "left": "Celggar:", "right": "$33,024.01"}.` |
|**`barcode`**|`string`|print a barcode. default (BARCODE_CODE93). [Barcode Types](#-barcode-types)|


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

## 💻 Example Payload

```
[
  {
    "image": "https://github.githubassets.com/images/modules/logos_page/Octocat.png"
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
    "type": 9
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
