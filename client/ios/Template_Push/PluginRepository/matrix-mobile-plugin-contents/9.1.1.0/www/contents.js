var exec = require('matrixMobile/exec');

module.exports = {
    imagePicker: function(callback, param){
        if(param.camera){
            navigator.camera.getPicture(function(r){callback([r])},param.cameraoption);
        } else {
            exec(callback,"MatrixMobileContentsPlugin","imagePicker",[param]);
        }
    },
    filePicker: function(callback, param) {
        exec(callback,"MatrixMobileContentsPlugin","filePicker",[param]);
    },
    fileDownload: function(callback, param) {
        exec(callback,"MatrixMobileContentsPlugin","fileDownload",[param]);
    }
};
