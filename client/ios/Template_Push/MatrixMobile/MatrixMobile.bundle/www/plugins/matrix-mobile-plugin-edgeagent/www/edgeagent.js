matrixMobile.define("matrix-mobile-plugin-edgeagent.edgeagent", function(require, exports, module) {
    
    var exec = require('matrixMobile/exec');
    
    function MatrixMobileEdgeAgentPlugin() {
        console.log("MatrixMobileEdgeAgentPlugin.js: is created");
    }
    
    MatrixMobileEdgeAgentPlugin.prototype.echo = function(callback, param) {
        exec(callback, "MatrixMobileEdgeAgentPlugin", "echo", [param]);
    }
    MatrixMobileEdgeAgentPlugin.prototype.userLogIn = function(callback, param) {
        exec(callback, "MatrixMobileEdgeAgentPlugin","userLogIn",[param]);
    }
    MatrixMobileEdgeAgentPlugin.prototype.userLogOut = function(callback, param) {
        exec(callback, "MatrixMobileEdgeAgentPlugin","userLogOut",[param]);
    }
    MatrixMobileEdgeAgentPlugin.prototype.getMyNewNotification = function(callback, param) {
        exec((r)=>{
            try {
                if(typeof r.data == "string") {
                    r.data = JSON.parse(r.data);
                }
                callback(r);
            } catch(e) {
                callback({"error":e});
            }
        }, "MatrixMobileEdgeAgentPlugin","getMyNewNotification",[param]);
    }
    MatrixMobileEdgeAgentPlugin.prototype.getAllNotification = function(callback, param) {
        exec((r)=>{
            try {
                if(typeof r.data == "string") {
                    r.data = JSON.parse(r.data);
                }
                callback(r);
            } catch(e) {
                callback({"error":e});
            }
        }, "MatrixMobileEdgeAgentPlugin","getAllNotification",[param]);
    }
    MatrixMobileEdgeAgentPlugin.prototype.setMyNotificationRead = function(callback, param) {
        exec(callback, "MatrixMobileEdgeAgentPlugin","setMyNotificationRead",[param]);
    }
    MatrixMobileEdgeAgentPlugin.prototype.getUserList = function(callback, param) {
        exec((r)=>{
            try {
                if(typeof r.data == "string") {
                    r.data = JSON.parse(r.data);
                }
                callback(r);
            } catch(e) {
                callback({"error":e});
            }
        }, "MatrixMobileEdgeAgentPlugin","getUserList", [param]);
    }
    MatrixMobileEdgeAgentPlugin.prototype.getDeviceList = function(callback, param) {
        exec((r)=>{
            try {
                if(typeof r.data == "string") {
                    r.data = JSON.parse(r.data);
                }
                callback(r);
            } catch(e) {
                callback({"error":e});
            }
        },"MatrixMobileEdgeAgentPlugin","getDeviceList", [param]);
    }
    MatrixMobileEdgeAgentPlugin.prototype.sendMessage = function(callback, param) {
        exec((r)=>{
            try {
                if(typeof r.data == "string") {
                    r.data = JSON.parse(r.data);
                }
                callback(r);
            } catch(e) {
                callback({"error":e});
            }
        },"MatrixMobileEdgeAgentPlugin","sendMessage", [param]);
    }
    MatrixMobileEdgeAgentPlugin.prototype.sendDeviceCommand = function(callback, param) {
        exec((r)=>{
            try {
                if(typeof r.data == "string") {
                    r.data = JSON.parse(r.data);
                }
                callback(r);
            } catch(e) {
                callback({"error":e});
            }
        },"MatrixMobileEdgeAgentPlugin","sendDeviceCommand", [param]);
    }
    MatrixMobileEdgeAgentPlugin.prototype.deviceEventListener = function(callback, param) {
        exec((r)=>{
            try {
                if(typeof r.data == "string") {
                    r.data = JSON.parse(r.data);
                }
                callback(r);
            } catch(e) {
                callback({"error":e});
            }
        },"MatrixMobileEdgeAgentPlugin","deviceEventListener", [param]);
    }
    
    module.exports = new MatrixMobileEdgeAgentPlugin();
});
