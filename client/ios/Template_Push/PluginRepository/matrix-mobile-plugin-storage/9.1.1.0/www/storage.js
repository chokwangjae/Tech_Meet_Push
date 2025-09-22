var exec = require('matrixMobile/exec');
    
module.exports = {
    getPreference: function(callback, param){
        exec(callback,"MatrixMobileStoragePlugin","getPreference",[param]);
    },
    setPreference: function(callback, param){
        exec(callback,"MatrixMobileStoragePlugin","setPreference",[param]);
    },
    removePreference: function(callback, param){
        exec(callback,"MatrixMobileStoragePlugin","removePreference",[param]);
    },
    shareData: function(callback, param){
        exec(callback,"MatrixMobileStoragePlugin","shareData",[param]);
    },
    reset: function(callback, param) {
        exec(callback,"MatrixMobileStoragePlugin","appReset",[param]);
    }
};
