
import { NativeModules } from 'react-native';

const { RNEpsonEposPrinter } = NativeModules;

export default {
    isAvailable() {
        return RNEpsonEposPrinter.isAvailable();

    }
};
