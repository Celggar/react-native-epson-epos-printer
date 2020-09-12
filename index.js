
import { NativeModules, Platform } from 'react-native';

const { RNEpsonEposPrinter } = NativeModules;

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

export default {
    isAvailable() {
        return RNEpsonEposPrinter.isAvailable();
    },
    printTest(ipOrMac) {
        if (Platform.OS === 'ios') {
           return new Promise((resolve, reject) => {
                reject('No disponible para iOS aún');
            });
        } else {
            return RNEpsonEposPrinter.printTest(ipOrMac);
        }
    },
    print(qty, ipOrMac, dataToPrint) {
        return RNEpsonEposPrinter.print(qty, ipOrMac, dataToPrint);
    }
};

export {Command, BarcodeType};
