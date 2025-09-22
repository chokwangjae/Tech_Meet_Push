matrixMobile.define("matrix-mobile-plugin-app.app", function(require, exports, module) {
var exec = require('matrixMobile/exec');
    
module.exports = {
    version: function(callback) {
        exec(callback,"MatrixMobileAppPlugin","appVersion",[]);
    },
    finish: function(callback, param) {
        exec(callback,"MatrixMobileAppPlugin","appFinish",[param]);
    },
    checkAppInstalled: function(callback, param) {
        exec(callback,"MatrixMobileAppPlugin","checkAppInstalled",[param]);
    }
};

});