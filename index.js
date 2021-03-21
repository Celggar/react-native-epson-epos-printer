
import { NativeModules, Platform, DeviceEventEmitter } from 'react-native';
import { EventEmitter } from 'events';

const { RNEpsonEposPrinter } = NativeModules;

const RNZeroconf = NativeModules.RNZeroconf;

const Command = {
    /**
     * Default text positioned to the left and add new line at the end.
     * You can write a paragraph making line breaks with "\n" to avoid having to add several text tags
     */
    TEXT: 'text',
    /**
     * Centered text
     */
    CENTERED: 'centered',
    /**
     * Specifies the number of lines to skip.
     * You can also use the text tag with new lines
     */
    LINE: 'line',
    /**
     * Specifies the number of centered dotted lines.
     * You can also use the centered tag with "------------------------------"
     */
    DOTTED: 'dotted',
    /**
     * Write the text to the left without adding a new line.
     * Using new lines can cause unexpected problems, to use a new line better use the text tag
     */
    LEFT: 'left',
    /**
     * write the text to the right without adding a new line.
     *
     * left and right can be used in combination, when combined a new line will be added by default
     * Example:
     * {
     *  "left": "HELLO CELGGAR:",
     *  "right": "$33,001.24",
     *  }
     */
    RIGHT: 'right',
    /**
     * Print a barcode. default (BARCODE_CODE93)
     * @see BarcodeType
     */
    BARCODE: 'barcode',
    /**
     * Need the link to download the image
     */
    IMAGE: 'image',
    /**
     * Converts the string into a Bitmap of QR code,
     * You can also use Array to print two QR code in a line.
     */
    QRCODE: 'qrcode',
};

const BarcodeType = {
    BARCODE_UPC_A: 0,
    BARCODE_UPC_E: 1,
    BARCODE_EAN13: 2,
    BARCODE_JAN13: 3,
    BARCODE_EAN8: 4,
    BARCODE_JAN8: 5,
    BARCODE_CODE39: 6,
    BARCODE_ITF: 7,
    BARCODE_CODABAR: 8,
    BARCODE_CODE93: 9,
    BARCODE_CODE128: 10,
    BARCODE_GS1_128: 11,
    BARCODE_GS1_DATABAR_OMNIDIRECTIONAL: 12,
    BARCODE_GS1_DATABAR_TRUNCATED: 13,
    BARCODE_GS1_DATABAR_LIMITED: 14,
    BARCODE_GS1_DATABAR_EXPANDED: 15,
};

const PrinterSeries = {
    TM_M10: 0,
    TM_M30: 1,
    TM_M30II: 2,
    TM_P20: 3,
    TM_P60: 4,
    TM_P60II: 5,
    TM_P80: 6,
    TM_T20: 7,
    TM_T60: 8,
    TM_T70: 9,
    TM_T81: 10,
    TM_T82: 11,
    TM_T83: 12,
    TM_T83III: 13,
    TM_T88: 14,
    TM_T90: 15,
    TM_T100: 16,
    TM_U220: 17,
    TM_U330: 18,
    TM_L90: 19,
    TM_H6000: 20,
}

const TextLanguage = {
    LANG_EN: 0,
    LANG_JA: 1,
    LANG_ZH_CN: 2,
    LANG_ZH_TW: 3,
    LANG_KO: 4,
    LANG_TH: 5,
    LANG_VI: 6,
    LANG_MULTI: 7,
}

var targetPrinter = null;

class PrinterDiscover extends EventEmitter {
    constructor(props) {
        super(props)
        DeviceEventEmitter.addListener('discoverPrinter', (printer) =>{
            if(printer.Target){
                targetPrinter = printer.Target
            }
            this.emit('discover', printer)
        });
        DeviceEventEmitter.addListener('discoverPrinterError', (error) =>{
            this.emit('error', error)
        });
    }
    async start(){
        await RNEpsonEposPrinter.startDiscover();
    }
    async stop(){
        await RNEpsonEposPrinter.stopDiscovery();
    }
}

export default {
    isAvailable() {
        return RNEpsonEposPrinter.isAvailable();
    },
    setPrinterClass(printerSeries, textLanguage){
        RNEpsonEposPrinter.setPrinterClass(printerSeries, textLanguage);
    },
    printTest(connectionString) {
        if (Platform.OS === 'ios') {
           return new Promise((resolve, reject) => {
                reject('Not available for iOS yet');
            });
        } else {
            if(connectionString.toLowerCase() == 'auto'){
                if(targetPrinter){
                    return RNEpsonEposPrinter.printTest(targetPrinter);
                } else {
                    return new Promise((resolve, reject) => {
                        reject('Did not discover any printer');
                    });
                }
            } else {
                return RNEpsonEposPrinter.printTest(connectionString);
            }
        }
    },
    print(qty, connectionString, dataToPrint) {
        if (Platform.OS === 'ios') {
            return new Promise((resolve, reject) => {
                reject('Not available for iOS yet');
            });
        } else {
            if(connectionString.toLowerCase() == 'auto'){
                if(targetPrinter){
                    return RNEpsonEposPrinter.print(qty, targetPrinter, dataToPrint);
                } else {
                    return new Promise((resolve, reject) => {
                        reject('Did not discover any printer');
                    });
                }
            } else {
                return RNEpsonEposPrinter.print(qty, connectionString, dataToPrint);
            }
        }
    }
};

export {Command, BarcodeType, PrinterSeries, TextLanguage, PrinterDiscover};
