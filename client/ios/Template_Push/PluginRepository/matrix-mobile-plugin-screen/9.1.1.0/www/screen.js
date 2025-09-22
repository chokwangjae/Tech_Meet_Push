var exec = require('matrixMobile/exec');
    
module.exports = {
    startWebView: function(callback, param) {
        exec(callback,"MatrixMobileScreenPlugin","startWebView",[param]);
    },
    closeSubWebView: function(callback) {
        var tag = $h.WebViewTAG
        var options = {"webViewTag": tag};
        exec(callback,"MatrixMobileScreenPlugin","closeSubWebView",[options]);
    },
    screenCapture: function(callback, param) {
        exec(callback,"MatrixMobileScreenPlugin","screenCapture",[param]);
    },
    setupBrightness: function(callback, param){
        exec(callback,"MatrixMobileScreenPlugin","setupBrightness",[param]);
    }
};
