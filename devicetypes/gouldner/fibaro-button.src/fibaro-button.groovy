/**
 *  Copyright 2016 Ronald Gouldner
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Note:  The base of this code was taken from AdamV's version however I have overhauled quite a bit.
 *  Version 0.9.0
 *  Author: AdamV
 *  
 *  Version: 1.0 
 *  Author: Ronald Gouldner
 * 
 */
 
metadata {
    definition (name: "Fibaro Button", namespace: "gouldner", author: "Ronald Gouldner") {
        capability "Actuator"
        capability "Button"
        capability "Holdable Button"
        capability "Battery"
        capability "Configuration" 
        capability "Refresh" 
        command "describeAttributes"
        
        attribute "numberOfButtons", "number"
        attribute "buttonClicks", "enum", ["one click", "two clicks", "three clicks", "four clicks", "five clicks", "hold start", "hold release"]
        attribute "holdLevel", "number"
        attribute "battery", "number"

        // http://docs.smartthings.com/en/latest/device-type-developers-guide/definition-metadata.html#fingerprinting
        // fingerprint mfg:0072, prod:0501, model:0F0F 
        fingerprint deviceId: "0x1801", inClusters: "0x5E, 0x86, 0x72, 0x5B, 0x5A, 0x59, 0x85, 0x73, 0x84, 0x80, 0x71, 0x56, 0x70, 0x8E, 0x7A, 0x98", outClusters: "0x26, 0x9C"
    }

    simulator {
        status "button 1 pushed":  "command: 9881, payload: 00 5B 03 DE 00 01"  
        // need to redo simulator commands
    }

    tiles (scale: 2) {      
        multiAttributeTile(name:"button", type:"generic", width:6, height:4) {
            tileAttribute("device.button", key: "PRIMARY_CONTROL"){
                //attributeState "default", label:'Fibaro Button', backgroundColor:"#44b621", icon:"st.Home.home30"
                attributeState "default", label:'Fibaro Button', backgroundColor:"#44b621", icon:"st.unknown.zwave.remote-controller"
                //attributeState "default", label:'Fibaro Button', backgroundColor:"#44b621", icon:"http://www.freeiconspng.com/uploads/push-button-icon-png-2.png"
                attributeState "held", label: "Held", backgroundColor: "#44b621", icon:"st.unknown.zwave.remote-controller"
                attributeState "released", label: "Released", backgroundColor: "#44b621", icon:"st.unknown.zwave.remote-controller"
                attributeState "1", label: "Button 1", backgroundColor:"#44b621", icon:"st.unknown.zwave.remote-controller"
                attributeState "2", label: "Button 2", backgroundColor:"#44b621", icon:"st.unknown.zwave.remote-controller"
                attributeState "3", label: "Button 3", backgroundColor:"#44b621", icon:"st.unknown.zwave.remote-controller"
                attributeState "4", label: "Button 4", backgroundColor:"#44b621", icon:"st.unknown.zwave.remote-controller"
                attributeState "5", label: "Button 5", backgroundColor:"#44b621", icon:"st.unknown.zwave.remote-controller"
            }
            tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
                attributeState "battery", label:'${currentValue} %'
            }   
        }
        valueTile("configure", "device.button", width: 2, height: 2, decoration: "flat") {
            state "default", backgroundColor: "#ffffff", action: "configure", icon:"st.secondary.configure"
        }
        
        standardTile("version", "device.version", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "version", label: 'v1.1'
        }

        
        main "button"
        details(["button","configure","version"])
    }
}



def parse(String description) {
    def results = []
    //log.debug("RAW command: $description")
    if (description.startsWith("Err")) {
        log.debug("An error has occurred")
    } else {      
        def cmd = zwave.parse(description.replace("98C1", "9881"))
        //log.debug "Parsed Command: $cmd"
        if (cmd) {
            results = zwaveEvent(cmd)
        }
        if ( !state.numberOfButtons ) {
            state.numberOfButtons = "5"
            createEvent(name: "numberOfButtons", value: "5", displayed: false)
        }
    }
}
  
def describeAttributes(payload) {
    payload.attributes = [
        [ name: "holdLevel", type: "number", range:"1..100", capability: "button" ],
        [ name: "buttonClicks", type: "enum",  options: ["one click", "two clicks", "three clicks", "four clicks", "five clicks", "hold start", "hold release"], momentary: true, capability: "button" ],
    ]
    return null
}         

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand()
        log.debug("UnsecuredCommand: $encapsulatedCommand")
    // can specify command class versions here like in zwave.parse
    if (encapsulatedCommand) {
        log.debug("UnsecuredCommand: $encapsulatedCommand")
        return zwaveEvent(encapsulatedCommand)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    log.debug("Button Woke Up!")
    def event = createEvent(descriptionText: "${device.displayName} woke up", displayed: false)
    def cmds = []
    // request battery 
    cmds << zwave.batteryV1.batteryGet().format()
    // wait for response
    cmds << "delay 2400"
    // let wakeup go back to sleep
    cmds << zwave.wakeUpV1.wakeUpNoMoreInformation().format()
    [event, response(cmds)]
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    log.debug "VersionReport cmd:$cmd"
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
    log.debug("Crc16Encap: $cmd")
    // log.debug( "Data: $cmd.data")
    // log.debug( "Payload: $cmd.payload")
    // log.debug( "command: $cmd.command")
    // log.debug( "commandclass: $cmd.commandClass")
    def versions = [0x31: 3, 0x30: 2, 0x84: 2, 0x9C: 1, 0x70: 2]
    // def encapsulatedCommand = cmd.encapsulatedCommand(versions)
    def version = versions[cmd.commandClass as Integer]
    def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
    def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
    if (!encapsulatedCommand) {
        log.debug "Could not extract command from $cmd"
    } else {
        zwaveEvent(encapsulatedCommand)
        // log.debug("UnsecuredCommand: $encapsulatedCommand")
    }
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    log.debug( "CentralSceneNotification: $cmd")
    // log.debug( "keyAttributes: $cmd.keyAttributes")
    // log.debug( "sceneNumber: $cmd.sceneNumber")
    // log.debug( "sequenceNumber: $cmd.sequenceNumber")
    // log.debug( "payload: $cmd.payload")
    
    if ( cmd.keyAttributes == 0 ) {
        Integer button = 1
        sendEvent(name: "buttonClicks", value: "one click", descriptionText: "$device.displayName button was clicked once", isStateChange: true)
        sendEvent(name: "button", value: "1", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
        log.debug( "Button was pushed once" )
    }
    if ( cmd.keyAttributes == 3 ) {
        Integer button = 2
        sendEvent(name: "buttonClicks", value: "two clicks", descriptionText: "$device.displayName button was pushed twice", isStateChange: true)
        sendEvent(name: "button", value: "2", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
        log.debug( "Button was pushed twice" )
    }
    if ( cmd.keyAttributes == 4 ) {
        Integer button = 3
        sendEvent(name: "buttonClicks", value: "three clicks", descriptionText: "$device.displayName button was pushed three times", isStateChange: true)
        sendEvent(name: "button", value: "3", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
        log.debug( "Button was pushed three times" )
    }
    if ( cmd.keyAttributes == 5 ) {
        Integer button = 4
        sendEvent(name: "buttonClicks", value: "four clicks", descriptionText: "$device.displayName button was pushed four times", isStateChange: true)
        sendEvent(name: "button", value: "4", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
        log.debug( "Button was pushed four times" )
    }
    if ( cmd.keyAttributes == 6 ) {
        Integer button = 5
        sendEvent(name: "buttonClicks", value: "five clicks", descriptionText: "$device.displayName button was pushed FIVE times", isStateChange: true)
        sendEvent(name: "button", value: "5", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
        log.debug( "Button was pushed five times" )
    }
    if ( cmd.keyAttributes == 2 ) {
        // Fixed Errors by added data for button number and commented out button clicks and button events
        // 
        Integer button = 1
        sendEvent(name: "buttonClicks", value: "hold start", descriptionText: "$device.displayName button is released", isStateChange: true)
        sendEvent(name: "button", value: "held", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was held", isStateChange: true)
        log.debug( "Button held" )
    }
    if ( cmd.keyAttributes == 1 ) {
        sendEvent(name: "buttonClicks", value: "hold release", descriptionText: "$device.displayName button is released", isStateChange: true)
        sendEvent(name: "button", value: "released", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was held", isStateChange: true)
        log.debug( "Button released" )
    } 
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    log.debug ("Battery Report cmd=$cmd")
    //def map = [ name: "battery", unit: "%" ]
    if (cmd.batteryLevel == 0xFF) {  // Special value for low battery alert
        //map.value = 1
        device.battery = 1
        //map.descriptionText = "${device.displayName} has a low battery"
        //map.isStateChange = true
    } else {
        //map.value = cmd.batteryLevel
        device.battery = cmd.batteryLevel
        log.debug ("Battery: $cmd.batteryLevel")
    }
    // Store time of last battery update so we don't ask every wakeup, see WakeUpNotification handler
    state.lastbatt = new Date().time
    //createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    log.debug( "parameter: $cmd.parameterNumber, values: $cmd.configurationValue, size: $cmd.size")
}

def zwaveEvent(physicalgraph.zwave.commands.powerlevelv1.PowerlevelReport cmd) {
    log.debug "Power Level Report cmd=$cmd"
}

def configure() {
    log.debug ("configure: requesting battery and version info 4")
    def commands = []
    commands <<	response(zwave.batteryV1.batteryGet().format())
    commands << zwave.versionV1.versionGet().format()
    commands << zwave.powerlevelV1.powerlevelGet().format()
    delayBetween(commands, 2300)
}
