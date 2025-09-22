matrixMobile.define('matrixMobile/plugin_list', function(require, exports, module) {
    module.exports = [
        {
            "clobbers" : [
                          "matrixMobile.plugin.edgeagent"
                          ],
            "pluginId" : "matrix-mobile-plugin-edgeagent",
            "id" : "matrix-mobile-plugin-edgeagent.edgeagent",
            "file" : "plugins/matrix-mobile-plugin-edgeagent/www/edgeagent.js"
        },
        {
            "clobbers" : [
                          "matrixMobile.plugin.app"
                          ],
            "pluginId" : "matrix-mobile-plugin-app",
            "id" : "matrix-mobile-plugin-app.app",
            "file" : "plugins/matrix-mobile-plugin-app/www/app.js"
        },
        {
            "clobbers" : [
                          "device"
                          ],
            "id" : "matrix-mobile-plugin-device.device",
            "file" : "plugins/matrix-mobile-plugin-device/www/device.js",
            "pluginId" : "matrix-mobile-plugin-device"
        },
        {
            "id" : "matrix-mobile-plugin-camera.Camera",
            "file" : "plugins/matrix-mobile-plugin-camera/www/CameraConstants.js",
            "pluginId" : "matrix-mobile-plugin-camera",
            "clobbers" : [
                          "Camera"
                          ]
        },
        {
            "file" : "plugins/matrix-mobile-plugin-camera/www/CameraPopoverOptions.js",
            "clobbers" : [
                          "CameraPopoverOptions"
                          ],
            "id" : "matrix-mobile-plugin-camera.CameraPopoverOptions",
            "pluginId" : "matrix-mobile-plugin-camera"
        },
        {
            "file" : "plugins/matrix-mobile-plugin-camera/www/Camera.js",
            "pluginId" : "matrix-mobile-plugin-camera",
            "id" : "matrix-mobile-plugin-camera.camera",
            "clobbers" : [
                          "navigator.camera"
                          ]
        },
        {
            "pluginId" : "matrix-mobile-plugin-camera",
            "id" : "matrix-mobile-plugin-camera.CameraPopoverHandle",
            "file" : "plugins/matrix-mobile-plugin-camera/www/ios/CameraPopoverHandle.js",
            "clobbers" : [
                          "CameraPopoverHandle"
                          ]
        },
        {
            "id" : "matrix-mobile-plugin-network.Connection",
            "pluginId" : "matrix-mobile-plugin-network",
            "file" : "plugins/matrix-mobile-plugin-network/www/Connection.js",
            "clobbers" : [
                          "Connection"
                          ]
        },
        {
            "file" : "plugins/matrix-mobile-plugin-network/www/network.js",
            "pluginId" : "matrix-mobile-plugin-network",
            "clobbers" : [
                "navigator.connection",
                "navigator.network.connection"
            ],
            "id" : "matrix-mobile-plugin-network.network"
        }
    ];
    module.exports.metadata =
    // TOP OF METADATA
    {
        "matrix-mobile-plugin-camera" : {
            "version": "9.1.1.0",
            "type" : "basic"
        },
        "matrix-mobile-plugin-device" : {
            "version": "9.1.1.0",
            "type" : "basic"
        },
        "matrix-mobile-plugin-app" : {
            "version": "9.1.1.0",
            "type" : "basic"
        },
        "matrix-mobile-plugin-network" : {
            "version": "9.1.1.0",
            "type" : "basic"
        }
    }
    // BOTTOM OF METADATA
});

