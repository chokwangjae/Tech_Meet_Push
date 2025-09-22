var exec = require('matrixMobile/exec');
    
module.exports = {
    toast: function(callback, param) {
        exec(callback,"MatrixMobileViewPlugin","toast",[param]);
    },
    snackBar: function(callback, param){
        exec(callback,"MatrixMobileViewPlugin","snackbar",[param]);
    },
    notification: function(callback, param){
        exec(callback,"MatrixMobileViewPlugin","notification",[param]);
    }
};
