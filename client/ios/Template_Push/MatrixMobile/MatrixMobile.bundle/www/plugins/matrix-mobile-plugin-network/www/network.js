matrixMobile.define("matrix-mobile-plugin-network.network", function(require, exports, module) {
var exec = require('matrixMobile/exec');
var matrixMobile = require('matrixMobile');
var channel = require('matrixMobile/channel');
var utils = require('matrixMobile/utils');

if (matrixMobile.platformId !== 'browser' && typeof navigator !== 'undefined') {
    utils.defineGetter(navigator, 'onLine', function () {
        return this.connection.type !== 'none';
    });
}

function NetworkConnection () {
    this.type = 'unknown';
}

/**
 * Get connection info
 *
 * @param {Function} successCallback The function to call when the Connection data is available
 * @param {Function} errorCallback The function to call when there is an error getting the Connection data. (OPTIONAL)
 */
NetworkConnection.prototype.getInfo = function (callback) {
    exec(callback, 'MatrixMobileNetworkPlugin', 'getConnectionInfo', []);
};

var me = new NetworkConnection();
var timerId = null;
var timeout = 500;

channel.createSticky('onMatrixMobileConnectionReady');
channel.waitForInitialization('onMatrixMobileConnectionReady');

channel.onJSReady.subscribe(function () {
    me.getInfo(function (info) {
        me.type = info.data;
        if (info === 'none') {
            // set a timer if still offline at the end of timer send the offline event
            timerId = setTimeout(function () {
                matrixMobile.fireDocumentEvent('offline');
                timerId = null;
            }, timeout);
        } else {
            // If there is a current offline event pending clear it
            if (timerId !== null) {
                clearTimeout(timerId);
                timerId = null;
            }
            matrixMobile.fireDocumentEvent('online');
        }

        // should only fire this once
        if (channel.onMatrixMobileConnectionReady.state !== 2) {
            channel.onMatrixMobileConnectionReady.fire();
        }
    },
    function (e) {
        // If we can't get the network info we should still tell Cordova
        // to fire the deviceready event.
        if (channel.onMatrixMobileConnectionReady.state !== 2) {
            channel.onMatrixMobileConnectionReady.fire();
        }
        console.log('Error initializing Network Connection: ' + e);
    });
});

module.exports = me;

});