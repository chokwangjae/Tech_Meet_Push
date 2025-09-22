matrixMobile.define("matrix-mobile-plugin-device.device", function(require, exports, module) {
var argscheck = require('matrixMobile/argscheck');
var channel = require('matrixMobile/channel');
var utils = require('matrixMobile/utils');
var exec = require('matrixMobile/exec');
var matrixMobile = require('matrixMobile');

channel.createSticky('onMatrixMobileInfoReady');
// Tell cordova channel to wait on the CordovaInfoReady event
channel.waitForInitialization('onMatrixMobileInfoReady');

/**
 * This represents the mobile device, and provides properties for inspecting the model, version, UUID of the
 * phone, etc.
 * @constructor
 */
function Device () {
    this.available = false;
    this.platform = null;
    this.version = null;
    this.uuid = null;
    this.matrixMobile = null;
    this.model = null;
    this.manufacturer = null;
    this.isVirtual = null;
    this.serial = null;
    this.type = null;

    var me = this;

    channel.onJSReady.subscribe(function () {
        me.getInfo(function (info) {
            var buildLabel = matrixMobile.version;
            me.available = true;
            me.platform = info.data.platform;
            me.version = info.data.version;
            me.uuid = info.data.uuid;
            me.matrixMobile = buildLabel;
            me.model = info.data.model;
            me.isVirtual = info.data.isVirtual;
            me.manufacturer = info.data.manufacturer || 'unknown';
            me.appID = info.data.appID;
            me.type = info.data.type;
            channel.onMatrixMobileInfoReady.fire();
        });
    });
}

Device.prototype.getInfo = function (callback) {
    argscheck.checkArgs('f', 'Device.getInfo', arguments);
    exec(callback, 'MatrixMobileDevicePlugin', 'getDeviceInfo',[]);
};

module.exports = new Device();

});