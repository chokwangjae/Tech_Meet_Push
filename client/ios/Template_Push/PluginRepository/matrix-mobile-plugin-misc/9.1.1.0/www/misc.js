var exec = require('matrixMobile/exec');
    
module.exports = {
    openBrowser: function(callback, param) {
        exec(callback,"MatrixMobileMiscPlugin","openBrowser",[param]);
    },
    settingStatus: function(callback, data){
        exec(callback,"MatrixMobileMiscPlugin","settingStatus",[data]);
    },
    sendCall: function(callback, param){
        exec(callback,"MatrixMobileMiscPlugin","sendCall",[param]);
    },
    sendSMS: function(callback, param){
        exec(callback,"MatrixMobileMiscPlugin","sendSMS",[param]);
    },
    getContacts: function(callback){
        exec(callback,"MatrixMobileMiscPlugin","getContacts",[]);
    },
    getServerIp: function(callback){
        exec(callback,"MatrixMobileMiscPlugin","getServerIp",[]);
    }
};
