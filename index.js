
import { NativeModules, Platform } from 'react-native';

const { RNEpsonEposPrinter } = NativeModules;

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
            return Print.printTest(ipOrMac);
        }
    }
};
