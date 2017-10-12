/**
 *  Copyright 2015 SmartThings
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
 */
metadata {
	definition (name: "RRG Aeon Smart Meter 6 - Beta", namespace: "gouldner", author: "gouldner") {
		capability "Energy Meter"
		capability "Actuator"
		capability "Switch"
		capability "Power Meter"
		capability "Polling"
		capability "Refresh"
		capability "Configuration"
		capability "Sensor"

		command "reset"
        
        command "testColor"
        command "setNightLightColor"
        command "setNightBrightness"
        command "modeMomentary"
        command "modeNightlight"
        command "energyMode"
        
        attribute "testColorVal", "String"
        attribute "ledMode", "String"
        attribute "nightBright", "Number"

		fingerprint deviceId:"0x1001", inClusters:"0x5E 0x25 0x70 0x27 0x32 0x81 0x85 0x59 0x72 0x86 0x7A 0x73 0xEF 0x5A 0x82"
        
	}

	// simulator metadata
	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"

		for (int i = 0; i <= 10000; i += 1000) {
			status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
		}

		// reply messages
		reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
		reply "200100,delay 100,2502": "command: 2503, payload: 00"

	}

	// tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
		valueTile("power", "device.power") {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy") {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat") {
			state "default", label:'reset KWH', action:"reset", icon:"st.secondary.refresh-icon"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        // Configure button.  Syncronize the device capabilities that the UI provides
        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
            state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
        }
        // Test Color Selector
        //controlTile("testrgbSelector", "device.testColorVal", "color", height: 3, width: 3, inactiveLabel: false) {
		//    state "color", label:"Test Color", action:"testColor"
        //}
        
        // NightLight Color Selector
        controlTile("nightlightColor", "device.nightlightRGB", "color", height: 3, width: 3, inactiveLabel: false) {
		    state "nightLightColor", label:"Night Light Color", action:"setNightLightColor"
        }
        standardTile("ledMode", "ledMode", inactiveLabel: false, decoration: "flat") {
            state "EnergyMode", label:'Energy', action:"modeMomentary"
            state "Momentary", label:'Moment', action:"modeNightlight"
            state "NightLight", label:'Night', action:"energyMode"
        }
        valueTile("nightLightBrightness", "nightBright", inactiveLabel: false, decoration: "flat") {
            state "nightBright", label:'${currentValue}', unit:"%", backgroundColor:"#ffffff"
        }
        controlTile("nightBrightSliderControl", "device.nightBright", "slider", height: 1, width: 1, inactiveLabel: false) {
            state "nightBright", action:"setNightBrightness", backgroundColor: "#1e9cbb"
        }


		main(["switch","power","energy"])
		details(["switch","ledMode","power","energy","configure", "refresh","reset"
                ,"nightLightBrightness", "nightBrightSliderControl", "nightlightColor"
                ])
	}
}

def updated() {
	try {
		if (!state.MSR) {
			response(zwave.manufacturerSpecificV2.manufacturerSpecificGet().format())
		}
	} catch (e) { log.debug e }
}

def parse(String description) {
    log.debug "parse called description:$description"
	def result = null
	if(description == "updated") return
	def cmd = zwave.parse(description, [0x20: 1, 0x32: 1, 0x72: 2])
    log.debug "Command Parsed cmd:$cmd"
	if (cmd) {
		result = zwaveEvent(cmd)
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
    log.debug "zwave event MeterReport"
	if (cmd.scale == 0) {
		createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
	} else if (cmd.scale == 1) {
		createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
	} else if (cmd.scale == 2) {
		createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
    log.debug "zwave event BasicReport"
	def evt = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "physical")
	if (evt.isStateChange) {
		[evt, response(["delay 3000", zwave.meterV2.meterGet(scale: 2).format()])]
	} else {
		evt
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
    log.debug "zwave event SwitchBinaryReport"
	createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    log.debug "cmd:$cmd"
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    log.debug "zwave event ManufacturerSpecificReport"
	def result = []
    log.debug "cmd=$cmd"
	def msr = String.format("Manufacture Id:%04X, Product Type Id:%04X, Product Id:%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
	updateDataValue("MSR", msr)

	// retypeBasedOnMSR()

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)

	if (msr.startsWith("0086") && !state.aeonconfig) {  // Aeon Labs meter
		state.aeonconfig = 1
		result << response(delayBetween([
			zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 4).format(),   // report power in watts
			zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: 300).format(), // every 5 min
			zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 8).format(),   // report energy in kWh
			zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 300).format(), // every 5 min
			zwave.configurationV1.configurationSet(parameterNumber: 103, size: 4, scaledConfigurationValue: 0).format(),    // no third report
			//zwave.configurationV1.configurationSet(parameterNumber: 113, size: 4, scaledConfigurationValue: 300).format(), // every 5 min
            zwave.meterV2.meterGet(scale: 0).format(),
			zwave.meterV2.meterGet(scale: 2).format(),
		]))
	} else {
		result << response(delayBetween([
			zwave.meterV2.meterGet(scale: 0).format(),
			zwave.meterV2.meterGet(scale: 2).format(),
		]))
	}

	result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "$device.displayName: Unhandled: $cmd"
	[:]
}

def on() {
	[
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format(),
		"delay 3000",
		zwave.meterV2.meterGet(scale: 2).format()
	]
}

def off() {
	[
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format(),
		"delay 3000",
		zwave.meterV2.meterGet(scale: 2).format()
	]
}

def poll() {
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.meterV2.meterGet(scale: 0).format(),
		zwave.meterV2.meterGet(scale: 2).format()
	])
}

def refresh() {
    log.debug "refresh called"
    
	delayBetween([
        //  RRG testing so commenting out what I am not testing
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.meterV2.meterGet(scale: 0).format(),
		zwave.meterV2.meterGet(scale: 2).format(),
        zwave.manufacturerSpecificV2.manufacturerSpecificGet().format(),
        zwave.versionV1.versionGet().format(),
        zwave.switchMultilevelV3.switchMultilevelSupportedGet().format(),
        zwave.switchMultilevelV1.switchMultilevelGet().format() 
	], 3500)
}

def configure() {
        def short red = 0xFF
        def short green = 0x00
        def short blue = 0x00
        def nightLightColorArray = [ red, green, blue ]
        log.debug "configure called, setting nightlight color to $nightLightColorArray"
        delayBetween([
            // Report manual changes
            zwave.configurationV1.configurationSet(configurationValue: [2], parameterNumber: 80, size: 1).format(),
            // Set Night light color RGB
            zwave.configurationV1.configurationSet(configurationValue: nightLightColorArray, parameterNumber: 83, size: 3).format(),
            // Set Night Light Mode 0=Energy Mode, 1=Energy (5sec), 2=Night Light Mode (always one color)
            zwave.configurationV1.configurationSet(configurationValue: [2], parameterNumber: 81, size: 1).format(),
            // Brightness of momentary status G(low)% Y(High)% R(Warn)%
            zwave.configurationV1.configurationSet(configurationValue: [25,25,100], parameterNumber: 84, size: 3).format()
        ])
}

def testColor (colormap) {
    log.debug "testColor called colormap=$colormap"
    
    // Test Colors param 33
    // changes color until plug is unplugged
    delayBetween([zwave.configurationV1.configurationSet(configurationValue: [colormap.red,colormap.green,colormap.blue], parameterNumber: 33, size: 3).format()
                ]) 
}

def setNightLightColor (colormap) {
    log.debug "setNightLightColor called colormap=$colormap"
    
    // Set Night light color RGB
    delayBetween([zwave.configurationV1.configurationSet(configurationValue: [colormap.red,colormap.green,colormap.blue], parameterNumber: 83, size: 3).format()
                ]) 
}

def modeMomentary () {
    log.debug "setting ledMode to Momentary"
    sendEvent("name":"ledMode", "value":"Momentary", "isStateChange":true)
    delayBetween([
            // Set Night Light Mode 0=Energy Mode, 1=Energy (5sec), 2=Night Light Mode (always one color)
            zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 81, size: 1).format(),
        ])
}

def modeNightlight () {
    log.debug "setting ledMode to NightLight"
    sendEvent("name":"ledMode", "value":"NightLight", "isStateChange":true)
    delayBetween([
            // Set Night Light Mode 0=Energy Mode, 1=Energy (5sec), 2=Night Light Mode (always one color)
            zwave.configurationV1.configurationSet(configurationValue: [2], parameterNumber: 81, size: 1).format(),
        ])
}
        
def energyMode () {
    log.debug "setting ledMode to EnergyMode"
    sendEvent("name":"ledMode", "value":"EnergyMode", "isStateChange":true)
    delayBetween([
            // Set Night Light Mode 0=Energy Mode, 1=Energy (5sec), 2=Night Light Mode (always one color)
            zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 81, size: 1).format(),
        ])
}
        
def reset() {
	return [
		zwave.meterV2.meterReset().format(),
		zwave.meterV2.meterGet(scale: 0).format()
	]
}

def setNightBrightness(brightVal) {
    log.debug "setNightBrightness called with value:$brightVal"
    sendEvent("name":"nightBright", "value":brightVal, "isStateChange":true)
    if (brightVal >= 0 && brightVal <=100) {
        log.debug "sending multilevel brightness setting and requesting report"
        delayBetween([
            zwave.switchMultilevelV1.switchMultilevelSet(value: brightVal).format(),
            zwave.switchMultilevelV1.switchMultilevelGet().format(),
            zwave.switchMultilevelV2.switchMultilevelGet().format(),
            zwave.switchMultilevelV3.switchMultilevelGet().format() 
        ], 3500)
    }
}