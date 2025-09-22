var exec = require('matrixMobile/exec');
    
module.exports = {
    startSpeechToText: function(callback, param){
        exec(callback,"MatrixMobileSpeechPlugin","startSpeechToText",[param]);
    },
    stopSpeechToText: function(callback, param){
        exec(callback,"MatrixMobileSpeechPlugin","stopSpeechToText",[param]);
    },
    speakTTS: function(callback, param){
        exec(callback,"MatrixMobileSpeechPlugin","speakTTS",[param]);
    },
    stopTTS: function(callback, param){
        exec(callback,"MatrixMobileSpeechPlugin","stopTTS",[param]);
    }
};
