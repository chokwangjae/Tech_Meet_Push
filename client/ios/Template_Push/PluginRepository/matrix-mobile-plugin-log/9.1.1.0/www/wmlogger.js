var exec = require('matrixMobile/exec');
    
module.exports = {
    getLog: function(callback, param){
        exec(callback,"MatrixMobileLoggerPlugin","getLog",[param]);
    },
    clearLog: function(callback){
        exec(callback,"MatrixMobileLoggerPlugin","clearLog",[]);
    },
    getLogUpload: function(callback, param){
        exec(callback,"MatrixMobileLoggerPlugin","getLogUpload",[param]);
    },
    getLogMail: function(callback, param){
        exec(callback,"MatrixMobileLoggerPlugin","getLogMail",[param]);
    },
    logPath: function(callback, param){
        exec(callback,"MatrixMobileLoggerPlugin","logPath",[param]);
    }
};
