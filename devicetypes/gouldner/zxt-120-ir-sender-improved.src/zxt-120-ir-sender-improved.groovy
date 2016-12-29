/**
 *  ZXT-120 IR Sender Unit from Remotec
 *
 * TODO 
 *  TEST OVERRIDE TEMPS, ROUNDING, AND LIMITS
 *  TEST AUTO, DRY
 *
 *  Author: ERS from Ronald Gouldner (based on b.dahlem@gmail.com version)
 *  Date: 2016-12-27
 *  Code: https://github.com/gouldner/ST-Devices/src/ZXT-120
 *
 * Copyright (C) 2016
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

def devVer() { return "1.0.0"}

preferences {
	input("remoteCode", "number", title: "Remote Code", description: "The number of the remote to emulate - Press configure to complete")
	input("tempOffset", "enum", title: "Temp correction offset (degrees C)?", options: ["-5","-4","-3","-2","-1","0","1","2","3","4","5"])
	input("shortName", "string", title: "Short Name for Home Page Temp Icon", description: "Short Name:")
}

metadata {
	definition (name: "ZXT-120 IR Sender", namespace: "gouldner", author: "Ronald Gouldner") {
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Thermostat"
		capability "Configuration"
		capability "Polling"
		capability "Sensor"
		capability "Battery"
		capability "Switch"
		capability "Health Check"

		// Commands that this device-type exposes for controlling the ZXT-120 directly
		command "fanLow"
		command "fanMed"
		command "fanHigh"
		command "switchFanMode"

		command "switchFanOscillate"
		command "setRemoteCode"
		command "swingModeOn"
		command "swingModeOff"

		//commands for thermostat interface

		command "eco"
		command "dry"
		command "setDrySetpoint", ["number"]
		command "setAutoSetpoint", ["number"]
		command "autoChangeover"

		command "levelUpDown"
		command "levelUp"
		command "levelDown"

		attribute "swingMode", "STRING"
		attribute "lastPoll", "STRING"
		attribute "currentConfigCode", "STRING"
		attribute "currentTempOffset", "STRING"
		attribute "currentemitterPower", "STRING"
		attribute "currentsurroundIR", "STRING"
		attribute "drySetpoint", "STRING"
		attribute "autoSetpoint", "STRING"

		attribute "lastTriedMode", "STRING"
		attribute "supportedModes", "STRING"
		attribute "lastTriedFanMode", "STRING"
		attribute "supportedFanModes", "STRING"

		// Z-Wave description of the ZXT-120 device
		fingerprint deviceId: "0x0806"
		fingerprint inClusters: "0x20,0x27,0x31,0x40,0x43,0x44,0x70,0x72,0x80,0x86"
	}

	// simulator metadata - for testing in the simulator
	simulator {
		// Not sure if these are correct
		status "off"			: "command: 4003, payload: 00"
		status "heat"			: "command: 4003, payload: 01"
		status "cool"			: "command: 4003, payload: 02"
		status "auto"			: "command: 4003, payload: 03"
		status "emergencyHeat"		: "command: 4003, payload: 04"

		status "fanAuto"		: "command: 4403, payload: 00"
		status "fanOn"			: "command: 4403, payload: 01"
		status "fanCirculate"		: "command: 4403, payload: 06"

		status "heat 60"	: "command: 4303, payload: 01 01 3C"
		status "heat 68"	: "command: 4303, payload: 01 01 44"
		status "heat 72"	: "command: 4303, payload: 01 01 48"

		status "cool 72"	: "command: 4303, payload: 02 01 48"
		status "cool 76"	: "command: 4303, payload: 02 01 4C"
		status "cool 80"	: "command: 4303, payload: 02 01 50"

		status "temp 58"	: "command: 3105, payload: 01 22 02 44"
		status "temp 62"	: "command: 3105, payload: 01 22 02 6C"
		status "temp 70"	: "command: 3105, payload: 01 22 02 BC"
		status "temp 74"	: "command: 3105, payload: 01 22 02 E4"
		status "temp 78"	: "command: 3105, payload: 01 22 03 0C"
		status "temp 82"	: "command: 3105, payload: 01 22 03 34"

		// reply messages
		reply "2502": "command: 2503, payload: FF"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"temperature", type:"thermostat", width:6, height:4) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'${currentValue}°')
			}
			tileAttribute("device.temperature", key: "VALUE_CONTROL") {
				attributeState("default", action: "levelUpDown")
				attributeState("VALUE_UP", action: "levelUp")
				attributeState("VALUE_DOWN", action: "levelDown")
			}
/*
			tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
				attributeState("default", label:'${currentValue}%', unit:"%")
			}
*/
			tileAttribute("device.thermostatFanMode", key: "SECONDARY_CONTROL") {
				attributeState("default", label:'Fan ${currentValue}')
			}
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("idle", backgroundColor:"#44B621")
				attributeState("heating", backgroundColor:"#FFA81E")
				attributeState("cooling", backgroundColor:"#2ABBF0")

				attributeState("fan only", backgroundColor:"#145D78")
				attributeState("pending heat", backgroundColor:"#B27515")
				attributeState("pending cool", backgroundColor:"#197090")
				attributeState("vent economizer", backgroundColor:"#8000FF")			 

			}
			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState("off", label:'${name}')
				attributeState("heat", label:'${name}')
				attributeState("cool", label:'${name}')
				attributeState("auto", label:'${name}')
				attributeState("dry", label:'${name}')
			}
			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
				attributeState("default", label:'${currentValue}')
			}
			tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
				attributeState("default", label:'${currentValue}')
			}
		}

		valueTile("temp2", "device.temperature", width: 2, height: 2, canChangeIcon: true) {
			state("default", label:'${currentValue}°', icon:"st.alarm.temperature.normal",
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
			)
		}

		standardTile("thermostatMode", "device.thermostatMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat", canChangeIcon: true, canChangeBackground: true) {
			state "off", icon:"st.thermostat.heating-cooling-off", label: ' '
			state "heat", icon:"st.thermostat.heat", label: ' '
			state "cool", icon:"st.thermostat.cool", label: ' '
			state "auto", icon:"st.thermostat.auto", label: ' '
			state "dry", icon:"st.Weather.weather12", label: ' '
			state "resume", icon:"st.Weather.weather12", label: ' '
			state "autoChangeover", icon:"st.thermostat.auto", label: ' '
		}

		valueTile("battery", "device.battery", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "battery", label:'${currentValue}% battery' 
		}

		standardTile("off", "device.thermostatMode", width: 2, height: 2, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) {
			state "resume", action:"off", backgroundColor:"#92C081", icon: "st.thermostat.heating-cooling-off", label: 'Turn Off'
			state "heat", action:"off", backgroundColor:"#92C081", icon: "st.thermostat.heating-cooling-off", label: 'Turn Off'
			state "cool", action:"off", backgroundColor:"#92C081", icon: "st.thermostat.heating-cooling-off", label: 'Turn Off'
			state "auto", action:"off", backgroundColor:"#92C081", icon: "st.thermostat.heating-cooling-off", label: 'Turn Off'
			state "off", action:"on", backgroundColor:"#92C081", icon: "st.thermostat.heating-cooling-off", label: 'Turn On'
		}

		standardTile("cool", "device.thermostatMode", inactiveLabel: false) {
			state "cool", action:"cool", label:'${name}', backgroundColor:"#4A7BDE", icon: "st.thermostat.cool"
		}

		standardTile("heat", "device.thermostatMode", inactiveLabel: false) {
			state "heat", action:"heat", label:'${name}', backgroundColor:"#C15B47", icon: "st.thermostat.heat"
		}

		standardTile("auto", "device.thermostatMode", inactiveLabel: false) {
			state "auto", action:"auto", label:'${name}', backgroundColor:"#b266b2", icon: "st.thermostat.auto"
		}

		standardTile("dry", "device.thermostatMode", inactiveLabel: false) {
			state "dry", action:"dry", label:'${name}', backgroundColor:"#DBD099", icon: "st.Weather.weather12"
		}

		standardTile("autoChangeover", "device.thermostatMode", inactiveLabel: false) {
			state "autoChangeover", action:"autoChangeover", label:'${name}', backgroundColor:"#b266b2", icon: "st.thermostat.auto"
		}

		standardTile("fanMode", "device.thermostatFanMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat", canChangeIcon: true, canChangeBackground: true) {
			state "auto", icon:"st.Appliances.appliances11", label: 'Fan Auto'
			state "on", icon:"st.Appliances.appliances11", label: 'Fan Low'
			state "fanAuto", icon:"st.Appliances.appliances11", label: 'Fan Auto'
			state "fanLow", icon:"st.Appliances.appliances11", label: 'Fan Low'
			state "fanMedium", icon:"st.Appliances.appliances11", label: 'Fan Med'
			state "fanHigh", icon:"st.Appliances.appliances11", label: 'Fan High'
		}

		standardTile("fanModeLow", "device.thermostatFanMode", inactiveLabel: false /* , decoration: "flat" */) {
			state "fanLow", action:"fanLow", icon:"st.Appliances.appliances11", label: 'Fan Low'
		}

		standardTile("fanModeMed", "device.thermostatFanMode", inactiveLabel: false /*, decoration: "flat" */) {
			state "fanMedium", action:"fanMed", icon:"st.Appliances.appliances11", label: 'Fan Med'
		}

		standardTile("fanModeHigh", "device.thermostatFanMode", inactiveLabel: false /*, decoration: "flat" */) {
			state "fanHigh", action:"fanHigh", icon:"st.Appliances.appliances11", label: 'Fan High'
		}

		standardTile("fanModeAuto", "device.thermostatFanMode", inactiveLabel: false /*, decoration: "flat" */) {
			state "fanAuto", action:"fanAuto", icon:"st.Appliances.appliances11", label: 'Fan Auto'
		}

		standardTile("swingMode", "device.swingMode", width: 2, height: 2, inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) {
			state "auto", action:"swingModeOff", icon:"st.secondary.refresh-icon", label: 'Swing Auto'
			state "off", action:"swingModeOn", icon:"st.secondary.refresh-icon", label: 'Swing Off'
		}

		standardTile("swingModeOn", "device.swingMode", inactiveLabel: false /*, decoration: "flat" */) {
			state "on", action:"swingModeOn", icon:"st.secondary.refresh-icon", label: 'Swing Auto'
		}

		standardTile("swingModeOff", "device.swingMode", inactiveLabel: false /*, decoration: "flat" */) {
			state "off", action:"swingModeOff", icon:"st.secondary.refresh-icon", label: 'Swing Off'
		}

		valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpoint", label:'${currentValue}° heat', backgroundColor:"#ffffff"
		}
		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 2, width: 4, inactiveLabel: false ) {
			state "setHeatingSetpoint", action:"thermostat.setHeatingSetpoint", backgroundColor: "#d04e00"
		}

		valueTile("coolingSetpoint", "device.coolingSetpoint", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "coolingSetpoint", label:'${currentValue}° cool', backgroundColor:"#ffffff"
		}
		controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 2, width: 4, inactiveLabel: false) {
			state "setCoolingSetpoint", action:"thermostat.setCoolingSetpoint", backgroundColor: "#1e9cbb"
		}

		valueTile("drySetpoint", "device.drySetpoint", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "drySetpoint", label:'${currentValue}° dry', backgroundColor:"#ffffff"
		}
		controlTile("drySliderControl", "device.drySetpoint", "slider", height: 2, width: 4, inactiveLabel: false) {
			state "setDrySetpoint", action:"setDrySetpoint", backgroundColor: "#1e9cbb"
		}

		valueTile("autoSetpoint", "device.autoSetpoint", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "autoSetpoint", label:'${currentValue}° auto', backgroundColor:"#ffffff"
		}
		controlTile("autoSliderControl", "device.autoSetpoint", "slider", height: 2, width: 4, inactiveLabel: false) {
			state "setAutoSetpoint", action:"setAutoSetpoint", backgroundColor: "#1e9cbb"
		}

		valueTile("lastPoll", "device.lastPoll", height:2, width:2, inactiveLabel: false, decoration: "flat") {
			state "lastPoll", label:'${currentValue}' 
		}

		valueTile("currentConfigCode", "device.currentConfigCode", height:2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "currentConfigCode", label:'IR Config Code ${currentValue}'
		}

		valueTile("currentTempOffset", "device.currentTempOffset", height:2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "currentTempOffset", label:'Temp Offset ${currentValue}'
		}

		standardTile("configure", "device.configure", height:2, width:2, inactiveLabel: false) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

		standardTile("refresh", "device.thermostatMode", height:2, width:2, inactiveLabel: false) {
			state "default", action:"polling.poll", icon:"st.secondary.refresh"
		}

		main "temp2"
	
		details(["temperature",
			"thermostatMode", "fanMode", "swingMode",
			"heatingSetpoint", "heatSliderControl",
			"coolingSetpoint", "coolSliderControl", 
			"drySetpoint", "drySliderControl",
			"autoSetpoint", "autoSliderControl",
			"off", "cool", "heat", "auto", "dry", /* "autoChangeover", */
				"fanModeAuto", "fanModeLow", "fanModeMed", "fanModeHigh",
			/* "swingModeOn", "swingModeOff", */
			"battery", "lastPoll",
			"currentConfigCode", "currentTempOffset",
			"configure", "refresh"
		])

       

	}
}

def initialize() {
	log.trace "initialize()"
}

def installed() {
	log.trace "installed()"
	def tempscale = getTemperatureScale()
	if(!tz || !(tempscale == "F" || tempscale == "C")) {
		log.warn "Timezone (${tz}) or Temperature Scale (${tempscale}) not set"
	}
}

def ping() {
	log.trace "ping()"
/*
	device.updateDataValue("supportedFanModes", "")
	device.updateDataValue("supportedModes", "")
	device.updateDataValue("swingMode", "")
	device.updateDataValue("lastTriedFanMode", "")
*/
	poll()
}

def configure() {
	log.trace "configure()"
/*  Smartthings will run the method pollLite on this schedule, however this method will not send zwave commands...
	unschedule()
	def random = new Random()
	def random_int = random.nextInt(60)
	def random_dint = random.nextInt(3)
	schedule("${random_int} ${random_dint}/3 * * * ?", pollLite)
	log.info "POLL scheduled (${random_int} ${random_dint}/3 * * * ?)"
*/
	sendEvent(name: "checkInterval", value: 60 * 15, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID], displayed: false)
	state.swVersion = devVer()
	delayBetween([
		zwave.manufacturerSpecificV1.manufacturerSpecificGet().format(),
		setRemoteCode(),					// update the device's remote code to ensure it provides proper mode info
		setTempOffset(),
		zwave.thermostatModeV2.thermostatModeSupportedGet().format(),			// Request the device's supported modes
		zwave.thermostatFanModeV2.thermostatFanModeSupportedGet().format(),				// Request the device's supported fan modes
		zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()	// Assign the device to ZWave group 1
	], 1300)
}


def parse(String description)
{
	//log.info "Parsing Description=$description"

	// BatteryV1, ConfigurationV1, ThermostatModeV2, ThermostatOperatingStateV1,  ThermostatSetpointV2,  ThermostatFanModeV2, SensorMultilevelV1, SWITCHALLV1
	//def map = createEvent(zwaveEvent(zwave.parse(description, [0x80:1, 0x70:1, 0x40:2, 0x42:1, 0x43:2, 0x44:2, 0x31:2, 0x27:1 ])))

	def myzwave = zwave.parse(description, [0x80:1, 0x70:1, 0x40:2, 0x42:1, 0x43:2, 0x44:2, 0x31:2, 0x27:1 ])
	//log.trace "myzwave is ${myzwave}"
	def map = createEvent(zwaveEvent(myzwave))

	if(!map) {
		log.warn "parse called generating null map....why is this possible ? description=$description"
		return null
	}

	//log.debug "Parse map=$map"

	def result = [map]

	if(map && map.name in ["heatingSetpoint","coolingSetpoint","thermostatMode"]) {
		def map2 = [
			name: "thermostatSetpoint",
			unit: getTemperatureScale()
		]
		def map3 = [
			name: "thermostatOperatingState",
		]
		if(map.name == "thermostatMode") {
			updateState("lastTriedMode", map.value)
			if(map.value == "cool") {
				map2.value = device.latestValue("coolingSetpoint")
				log.info "latest cooling setpoint = ${map2.value}"
				map3.value = "cooling"
			}
			else if(map.value == "heat") {
				map2.value = device.latestValue("heatingSetpoint")
				log.info "latest heating setpoint = ${map2.value}"
				map3.value = "heating"
			}
			else if(map.value == "dry") {
				map2.value = device.latestValue("drySetpoint")
				log.info "latest dry setpoint = ${map2.value}"
				map3.value = "cooling"
			}
			else if(map.value == "auto") {
				map2.value = device.latestValue("autoSetpoint")
				log.info "latest auto setpoint = ${map2.value}"
				map3.value = "heating"
			}
			else if(map.value == "off") {
				map3.value = "idle"
			}
		}
		else {
			def mode = device.latestValue("thermostatMode")
			//log.info "THERMOSTAT, latest mode = ${mode}"
			if(    (map.name == "heatingSetpoint" && mode == "heat") ||
				(map.name == "coolingSetpoint" && mode == "cool") ||
				(map.name == "drySetpoint" && mode == "dry") ||
				(map.name == "autoSetpoint" && mode == "auto") ) {
				map2.value = map.value
				map2.unit = map.unit
			}
		}
		if(map2?.value != null) {
			//log.debug "THERMOSTAT, adding setpoint event: $map2"
			result << createEvent(map2)
		}
		if(map3?.value != null) {
			//log.debug "THERMOSTAT, adding operating state event: $map3"
			result << createEvent(map3)
		}
	} else if(map.name == "thermostatFanMode" && map.isStateChange) {
		updateState("lastTriedFanMode", map.value)
	}

	//log.debug "Parse returned $result"
	result
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd)
{
	//log.debug "ThermostatSetpointReport...cmd=$cmd"
	def cmdScale = cmd.scale == 1 ? "F" : "C"
	def map = [:]
	map.value = convertTemperatureIfNeeded(cmd.scaledValue, cmdScale, cmd.precision)
	map.unit = getTemperatureScale()
	map.displayed = false

	switch (cmd.setpointType) {
		case physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointSet.SETPOINT_TYPE_HEATING_1:
			map.name = "heatingSetpoint"
			break;
		case physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointSet.SETPOINT_TYPE_COOLING_1:
			map.name = "coolingSetpoint"
			break;
		case physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointSet.SETPOINT_TYPE_DRY_AIR:
			map.name = "drySetpoint"
			break;
		case physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointSet.SETPOINT_TYPE_AUTO_CHANGEOVER:
			map.name = "autoSetpoint"
			break;
		default:
			log.debug "Thermostat Setpoint Report for setpointType ${cmd.setpointType} = ${map.value} ${map.unit}"
			return [:]
	}
	// So we can respond with same format
	state.size = cmd.size
	state.scale = cmd.scale
	state.precision = cmd.precision
	//log.info "Thermostat Setpoint Report for ${map.name} = ${map.value} ${map.unit}"
	map
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd)
{
	//log.debug "SensorMultilevelReport...cmd=$cmd"
	def map = [:]
	switch (cmd.sensorType) {
	case 1:
		map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
		map.unit = getTemperatureScale()
		map.name = "temperature"
		log.info "SensorMultilevelReport temperature map.value=${map.value} ${map.unit}"
		break;
	default:
		log.warn "Unknown sensorType ${cmd.sensorType} from device"   // 5 is humidity in V2 and later
		break;
	}
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport cmd) {
	def map = [:]
	//log.debug "FanModeReport $cmd"

	switch (cmd.fanMode) {
		case physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport.FAN_MODE_AUTO_LOW:
		case physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport.FAN_MODE_AUTO_HIGH:
		case physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport.FAN_MODE_AUTO_MEDIUM:
			map.value = "auto"
			break
		case physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport.FAN_MODE_LOW:
		case physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport.FAN_MODE_HIGH:
		case physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport.FAN_MODE_MEDIUM:
			map.value = "on"
			break
/*
		case physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_CIRCULATION:
			map.value = "circulate"
			break
*/
	}
	map.name = "thermostatFanMode"
	map.displayed = false
	//log.info "FanModeReport ${map.value}"
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
	def map = [:]
	//log.debug "ThermostatModeReport $cmd"

	switch (cmd.mode) {
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_OFF:
			map.value = "off"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_HEAT:
			map.value = "heat"
			break
/*
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUXILIARY_HEAT:
			map.value = "emergencyHeat"
			break
*/
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_COOL:
			map.value = "cool"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUTO:
			map.value = "auto"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_RESUME:
			map.value = "resume"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_FAN_ONLY:
			map.value = "fanonly"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_DRY_AIR:
			map.value = "dry"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUTO_CHANGEOVER:
			map.value = "autoChangeover"
			break
	}
	map.name = "thermostatMode"
	log.info "Thermostat Mode reported : ${map.value.toString().capitalize()}"
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSupportedReport cmd) {
	def map = [:]
	//log.debug "thermostatModeSupported  $cmd"

	def supportedModes = ""
	if(cmd.off) { supportedModes += "off " }
	if(cmd.heat) { supportedModes += "heat " }
	//if(cmd.auxiliaryemergencyHeat) { supportedModes += "emergencyHeat " }
	if(cmd.cool) { supportedModes += "cool " }
	if(cmd.auto) { supportedModes += "auto " }
	if(cmd.dryAir) { supportedModes += "dry " }
	//if(cmd.autoChangeover) { supportedModes += "autoChangeover " }

	log.info "Supported Modes: ${supportedModes}"
	updateState("supportedModes", supportedModes)

	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeSupportedReport cmd) {
	def map = [:]
	//log.debug "fanModeSupported  $cmd"

	def supportedFanModes = ""
	if(cmd.auto) { supportedFanModes += "fanAuto " }
	if(cmd.low) { supportedFanModes += "fanLow " }
	if(cmd.medium) { supportedFanModes += "fanMedium " }
	if(cmd.high) { supportedFanModes += "fanHigh " }

	log.info "Supported Fan Modes: ${supportedFanModes}"
	updateState("supportedFanModes", supportedFanModes)

	map
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [:]
	map.name = "battery"
	map.value = cmd.batteryLevel > 0 ? cmd.batteryLevel.toString() : 1
	map.unit = "%"
	map.displayed = false
	//log.info "Battery Level Reported=$map.value"
	map
}

def getCommandParameters() { [
	"remoteCode": 27,
	"tempOffsetParam": 37,
	"oscillateSetting": 33,
	"emitterPowerSetting": 28,
	"surroundIRSetting": 32
]}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	def map = [:]
	//log.debug "ConfigurationReport $cmd"

	map.displayed = false
	switch (cmd.parameterNumber) {
		// remote code
		case commandParameters["remoteCode"]:
			map.name = "currentConfigCode"
			def short remoteCodeLow = cmd.configurationValue[1]
			def short remoteCodeHigh = cmd.configurationValue[0]
			map.value = (remoteCodeHigh << 8) + remoteCodeLow
			//log.info "reported currentConfigCode=$map.value"
			break

		case commandParameters["tempOffsetParam"]:
			map.name = "currentTempOffset"
			def short offset = cmd.configurationValue[0]
			if(offset >= 0xFB) {		 // Hex FB-FF represent negative offsets FF=-1 - FB=-5
				offset = offset - 256
			}
			map.value = offset
			//log.info "reported offset=$map.value C"
			break

		case commandParameters["emitterPowerSetting"]:
			def power = (cmd.configurationValue[0] == 0) ? "normal" : "high"
			map.name = "currentemitterPower"
			map.value = power
			//log.info "reported power ${cmd.configurationValue[0]}  ${power}"
			break

		case commandParameters["surroundIRSetting"]:
			def surround = (cmd.configurationValue[0] == 0) ? "disabled" : "enabled"
			map.name = "currentsurroundIR"
			map.value = surround
			//log.info "reported surround ${cmd.configurationValue[0]}  ${surround}"
			break

		case commandParameters["oscillateSetting"]:
			def oscillateMode = (cmd.configurationValue[0] == 0) ? "off" : "auto"  // THIS IS OFF, AUTO (default)
			map.name = "swingMode"
			map.value = oscillateMode
			//log.info "reported swing mode = ${oscillateMode}"
			state.swingMode = oscillateMode
			break
		default:
			log.warn "Unknown configuration report ${cmd.parameterNumber}"
			break;
	}

	map
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	//log.debug "Zwave event received: $cmd"
	def map = [:]
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	updateDataValue("manufacturer", cmd.manufacturerName)
	//createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
	map
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	def map = [:]
	log.debug "Zwave event received: $cmd"
	map
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	def map = [:]
	log.warn "Unexpected zwave command $cmd"
	map
}

def pollLite() {
	log.info "PollLite.."
	refresh()
} 

def refresh() {
	def commands = []
	unschedule()

	commands <<	zwave.sensorMultilevelV2.sensorMultilevelGet().format()		// current temperature
	commands <<	zwave.thermostatModeV2.thermostatModeGet().format()		// thermostat mode
	commands <<	zwave.thermostatFanModeV2.thermostatFanModeGet().format()	// fan speed
	delayBetween(commands, standardDelay)
}

def poll() {
	def now=new Date()
	def tz = location.timeZone
	def tempscale = getTemperatureScale()
	if(!tz || !(tempscale == "F" || tempscale == "C")) {
		log.warn "Timezone (${tz}) or Temperature Scale (${tempscale}) not set"
		return
	}
	def nowString = now.format("MMM/dd HH:mm",tz)
	sendEvent("name":"lastPoll", "value":nowString)
	log.info "Polling now $nowString"

	def commands = []

	commands <<	zwave.sensorMultilevelV2.sensorMultilevelGet().format()		// current temperature
	commands <<	zwave.batteryV1.batteryGet().format()				// current battery level
	commands <<	zwave.thermostatModeV2.thermostatModeGet().format()		// thermostat mode
	commands <<	zwave.thermostatFanModeV2.thermostatFanModeGet().format()	// fan speed
	commands <<	zwave.configurationV1.configurationGet(parameterNumber: commandParameters["remoteCode"]).format()	// remote code
	commands <<	zwave.configurationV1.configurationGet(parameterNumber: commandParameters["tempOffsetParam"]).format()	// temp offset
	commands <<	zwave.configurationV1.configurationGet(parameterNumber: commandParameters["oscillateSetting"]).format()	// oscillate setting
	commands <<	zwave.configurationV1.configurationGet(parameterNumber: commandParameters["emitterPowerSetting"]).format()	// emitter setting
	commands <<	zwave.configurationV1.configurationGet(parameterNumber: commandParameters["surroundIRSetting"]).format()	// surround IR setting

	// add requests for each thermostat setpoint available on the device
	def supportedModes = getDataByName("supportedModes")
	for (setpoint in setpointMap) {
		commands << [zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: setpoint.value).format()]
	}

	delayBetween(commands, standardDelay)
}

def getSetpointMap() { [
	"heatingSetpoint": physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointSet.SETPOINT_TYPE_HEATING_1,
	"coolingSetpoint": physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointSet.SETPOINT_TYPE_COOLING_1,
	"drySetpoint": physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointSet.SETPOINT_TYPE_DRY_AIR,
	"autoSetpoint": physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointSet.SETPOINT_TYPE_AUTO_CHANGEOVER
]}

def setThermostatMode(String value) {
	def commands = []
	def degrees = 0

	log.debug "setting thermostat mode $value"

	commands << zwave.thermostatModeV2.thermostatModeSet(mode: modeMap[value]).format()
	commands << zwave.thermostatModeV2.thermostatModeGet().format()

	if(value == "cool") {
		degrees = device.currentValue("coolingSetpoint")
		commands << setCoolingSetpoint(degrees, true)
	} else if(value == "heat") {
		degrees = device.currentValue("heatingSetpoint")
		commands << setHeatingSetpoint(degrees, true)
	} else if(value == "dry" || value == "off" || value == "resume" || value == "auto") {
		log.debug("Dry, Resume or Off no need to send temp")
	} else {
		log.warn("Unknown thermostat mode set:$value")
	}

	delayBetween(commands, standardDelay)
}

def getModeMap() { [
	"off": physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSet.MODE_OFF,
	"heat": physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSet.MODE_HEAT,
	"cool": physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSet.MODE_COOL,
	"auto": physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSet.MODE_AUTO,
	"resume": physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSet.MODE_RESUME,
	"dry": physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSet.MODE_DRY_AIR,
	"autoChangeover": physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSet.MODE_AUTO_CHANGEOVER
]}

def setHeatingSetpoint(degrees) {
	setHeatingSetpoint(degrees.toDouble())
}

def setHeatingSetpoint(Double degrees, nocheck = false, Integer delay = standardDelay) {
	log.trace "setHeatingSetpoint($degrees, $delay)"

	def commands = []
	def hvacMode = device.latestValue("thermostatMode")
	if(nocheck || hvacMode in ["heat"]) {
		def convertedDegrees = checkValidTemp(degrees)

		def deviceScale = state?.scale != null ? state.scale : 1
		def deviceScaleString = deviceScale == 1 ? "F" : "C"
		log.debug "setHeatingSetpoint({$convertedDegrees} ${deviceScaleString})"

		def p = (state.precision == null) ? 1 : state.precision
		commands << zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 1, scale: deviceScale, precision: p, scaledValue: convertedDegrees).format()
	} else { log.warn "cannot change setpoint due to hvacMode: ${hvacMode}" }
	commands << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format()
	delayBetween(commands, delay)
}

def setCoolingSetpoint(degrees) {
	setCoolingSetpoint(degrees.toDouble())
}

def setCoolingSetpoint(Double degrees, nocheck = false, Integer delay = standardDelay) {
	log.trace "setCoolingSetpoint($degrees, $delay)"

	def commands = []
	def hvacMode = device.latestValue("thermostatMode")
	if(nocheck || hvacMode in ["cool"]) {
		def convertedDegrees = checkValidTemp(degrees)

		def deviceScale = state?.scale != null ? state.scale : 1
		def deviceScaleString = deviceScale == 1 ? "F" : "C"
		log.debug "setCoolingSetpoint({$convertedDegrees} ${deviceScaleString})"

		def p = (state.precision == null) ? 1 : state.precision
		commands << zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 2, scale: deviceScale, precision: p, scaledValue: convertedDegrees).format()
	} else { log.warn "cannot change setpoint due to hvacMode: ${hvacMode}" }
	commands << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 2).format()
	delayBetween(commands, delay)
}

def setDrySetpoint(degrees) {
	setDrySetpoint(degrees.toDouble())
}

def setDrySetpoint(Double degrees, nocheck = false, Integer delay = standardDelay) {
	log.trace "setDrySetpoint($degrees, $delay)"

	def commands = []
	def hvacMode = device.latestValue("thermostatMode")
	if(nocheck || hvacMode in ["dry"]) {
		def convertedDegrees = checkValidTemp(degrees)

		def deviceScale = state?.scale != null ? state.scale : 1
		def deviceScaleString = deviceScale == 1 ? "F" : "C"
		log.debug "setDrySetpoint({$convertedDegrees} ${deviceScaleString})"

		def p = (state.precision == null) ? 1 : state.precision
		commands << zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 8, scale: deviceScale, precision: p, scaledValue: convertedDegrees).format()
	} else { log.warn "cannot change setpoint due to hvacMode: ${hvacMode}" }
	commands << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 8).format()
	delayBetween(commands, delay)
}

def setAutoSetpoint(degrees) {
	setAutoSetpoint(degrees.toDouble())
}

def setAutoSetpoint(Double degrees, nocheck = false, Integer delay = standardDelay) {
	log.trace "setAutoSetpoint($degrees, $delay)"

	def commands = []
	def hvacMode = device.latestValue("thermostatMode")
	if(nocheck || hvacMode in ["auto"]) {
		def convertedDegrees = checkValidTemp(degrees)

		def deviceScale = state?.scale != null ? state.scale : 1
		def deviceScaleString = deviceScale == 1 ? "F" : "C"
		log.debug "setAutoSetpoint({$convertedDegrees} ${deviceScaleString})"

		def p = (state.precision == null) ? 1 : state.precision
		commands << zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 10, scale: deviceScale, precision: p, scaledValue: convertedDegrees).format()
	} else { log.warn "cannot change setpoint due to hvacMode: ${hvacMode}" }
	commands << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 10).format()
	delayBetween(commands, delay)
}

def checkValidTemp(degrees) {
	def deviceScale = state?.scale != null ? state.scale : 1
	def deviceScaleString = deviceScale == 1 ? "F" : "C"
	def locationScale = getTemperatureScale()

	def convertedDegrees = degrees
	if(locationScale == "C" && deviceScaleString == "F") {
		convertedDegrees = celsiusToFahrenheit(degrees)
	} else if(locationScale == "F" && deviceScaleString == "C") {
		convertedDegrees = fahrenheitToCelsius(degrees)
	}

	def override = false
	def overrideDegrees = convertedDegrees
	if(deviceScaleString == "F") {
		overrideDegrees = Math.ceil(convertedDegrees)
		// ZXT-120 lowest settings is 67
		if(overrideDegrees < 67) {
			overrideDegrees = 67
			override = true
		}
		// ZXT-120 highest setting is 84
		if(overrideDegrees > 84) {
			overrideDegrees = 84
			override = true
		}

	} else if(deviceScaleString == "C") {
		// ZXT-120 lowest settings is 19 C
		if(overrideDegrees < 19) {
			overrideDegrees = 19
			override = true
		}
		// ZXT-120 highest setting is 28 C
		if(overrideDegrees > 28) {
			overrideDegrees = 28
			override = true
		}
	} else { log.error "checkValidTemp: unknown device scale" }

	if(override) {
		log.warn "overriding temp ${convertedDegrees} to ${overrideDegrees}"
	}
	convertedDegrees = overrideDegrees
	return convertedDegrees
}

def levelUp() {
	levelUpDown(1)
}

def levelDown() {
	levelUpDown(-1)
}

def levelUpDown(tempVal) {
	//LogAction("levelUpDown()...($tempVal | $chgType)", "trace")
	def hvacMode = device.latestValue("thermostatMode")

	def cmds = []
	if(hvacMode in ["heat", "cool", "auto", "dry"]) {
	// From RBOY https://community.smartthings.com/t/multiattributetile-value-control/41651/23
	// Determine OS intended behaviors based on value behaviors (urrgghhh.....ST!)
		def upLevel

		if(!state?.lastLevelUpDown) { state.lastLevelUpDown = 0 } // If it isn't defined lets baseline it

		if((state.lastLevelUpDown == 1) && (tempVal == 1)) { upLevel = true } //Last time it was 1 and again it's 1 its increase

		else if((state.lastLevelUpDown == 0) && (tempVal == 0)) { upLevel = false } //Last time it was 0 and again it's 0 then it's decrease

		else if((state.lastLevelUpDown == -1) && (tempVal == -1)) { upLevel = false } //Last time it was -1 and again it's -1 then it's decrease

		else if((tempVal - state.lastLevelUpDown) > 0) { upLevel = true } //If it's increasing then it's up

		else if((tempVal - state.lastLevelUpDown) < 0) { upLevel = false } //If it's decreasing then it's down

		else { log.error "UNDEFINED STATE, CONTACT DEVELOPER. Last level $state.lastLevelUpDown, Current level, $value" }

		state.lastLevelUpDown = tempVal // Save it

		def targetVal = 0.0
		def curThermSetpoint = device.latestValue("thermostatSetpoint")

		switch (hvacMode) {
			case "heat":
				def curHeatSetpoint = device.currentValue("heatingSetpoint")
				targetVal = curHeatSetpoint ?: 0.0
				break
			case "cool":
				def curCoolSetpoint = device.currentValue("coolingSetpoint")
				targetVal = curCoolSetpoint ?: 0.0
				break
			case "dry":
				def curDrySetpoint = device.currentValue("drySetpoint")
				targetVal = curDrySetpoint ?: 0.0
				break
			case "auto":
				def curAutoSetpoint = device.currentValue("autoSetpoint")
				targetVal = curAutoSetpoint ?: 0.0
				break
			default:
				log.warn "Change in Unsupported Mode Received: ($hvacMode}!!!"
				return []
				break
		}

		if(targetVal == 0.0) { log.warn "No targetVal"; return }

		//def tempUnit = device.currentValue('temperatureUnit')

		if(upLevel) {
			//LogAction("Increasing by 1 increment")
			targetVal = targetVal.toDouble() + 1.0
/*
			if(tempUnit == "C" ) {
				targetVal = targetVal.toDouble() + 0.5
				if(targetVal < 9.0) { targetVal = 9.0 }
				if(targetVal > 32.0 ) { targetVal = 32.0 }
			} else {
				targetVal = targetVal.toDouble() + 1.0
				if(targetVal < 50.0) { targetVal = 50 }
				if(targetVal > 90.0) { targetVal = 90 }
			}
*/
		} else {
			//LogAction("Reducing by 1 increment")
			targetVal = targetVal.toDouble() - 1.0
/*
			if(tempUnit == "C" ) {
				targetVal = targetVal.toDouble() - 0.5
				if(targetVal < 9.0) { targetVal = 9.0 }
				if(targetVal > 32.0 ) { targetVal = 32.0 }
			} else {
				targetVal = targetVal.toDouble() - 1.0
				if(targetVal < 50.0) { targetVal = 50 }
				if(targetVal > 90.0) { targetVal = 90 }
			}
*/
		}

		if(targetVal != curThermSetpoint) {
			log.info "Sending changeSetpoint(Temp: ${targetVal})"
			switch (hvacMode) {
				case "heat":
					cmds << setHeatingSetpoint(targetVal)
					break
				case "cool":
					cmds << setCoolingSetpoint(targetVal)
					break
				case "dry":
					cmds << setDrySetpoint(targetVal)
					break
				case "auto":
					cmds << setAutoSetpoint(targetVal)
					break
				default:
					log.warn "Unsupported Mode Received: ($hvacMode}!!!"
					break
			}
		}
	} else { log.warn "levelUpDown: Cannot adjust temperature due to hvacMode ${hvacMode}" }
	if(cmds) { delayBetween(cmds, standardDelay) }
}

def setRemoteCode() {
	def remoteCodeVal = remoteCode.toInteger()
							// Divide the remote code into a 2 byte value
	def short remoteCodeLow = remoteCodeVal & 0xFF
	def short remoteCodeHigh = (remoteCodeVal >> 8) & 0xFF
	def remoteBytes = [remoteCodeHigh, remoteCodeLow]
	log.debug "New Remote Code: ${remoteBytes}"

	delayBetween ([
		zwave.configurationV1.configurationSet(configurationValue: remoteBytes, parameterNumber: commandParameters["remoteCode"], size: 2).format(),
		zwave.configurationV1.configurationGet(parameterNumber: commandParameters["remoteCode"]).format()
	], standardDelay)
}

def setTempOffset() {
	def tempOffsetVal = tempOffset == null ? 0 : tempOffset.toInteger()
	if(tempOffsetVal < 0) {		 // Convert negative values into hex value for this param -1 = 0xFF -5 = 0xFB
		tempOffsetVal = 256 + tempOffsetVal
	}

	def configArray = [tempOffsetVal]
	log.debug "TempOffset: ${tempOffsetVal}"

	delayBetween ([
		zwave.configurationV1.configurationSet(configurationValue: configArray, parameterNumber: commandParameters["tempOffsetParam"], size: 1).format(),
		zwave.configurationV1.configurationGet(parameterNumber: commandParameters["tempOffsetParam"]).format()
	], standardDelay)
}

def switchFanOscillate() {
	def swingMode = (getDataByName("swingMode") == "off") ? true : false	 // Load the current swingmode and invert it (Off becomes true, On becomes false)
	setFanOscillate(swingMode)
}

def swingModeOn() {
	log.debug "Setting Swing mode AUTO"
	setFanOscillate(true)
}

def swingModeOff() {
	log.debug "Setting Swing mode Off"
	setFanOscillate(false)
}

def setFanOscillate(swingMode) {
	def swingValue = swingMode ? 1 : 0		 // Convert the swing mode requested to 1 for on, 0 for off

	def hvacMode = device.latestValue("thermostatMode")
	if(  !(hvacMode in ["heat","cool","auto","dry"]) ) {
		log.warn "wrong mode ${hvacMode}"
	} else {
		delayBetween ([
			zwave.configurationV1.configurationSet(configurationValue: [swingValue], parameterNumber: commandParameters["oscillateSetting"], size: 1).format(),
			zwave.configurationV1.configurationGet(parameterNumber: commandParameters["oscillateSetting"]).format()
		], standardDelay)
	}
}


def updateState(String name, String value) {
	state[name] = value
	sendEvent(name: "${name}", value: "${value}", displayed: false)
	//device.updateDataValue(name, value)
}

def getDataByName(String name) {
	//state[name] ?: device.getDataValue(name)
	state[name] ?: device.currentState("${name}")?.value
}


def fanModes() {
	["fanAuto", "fanOn", "fanLow", "fanMedium", "fanHigh"]
}

def switchFanMode() {
	def currentMode = device.currentState("thermostatFanMode")?.value
	if(currentMode == "auto") { currentMode = "fanAuto" }
	else if(currentMode == "on") { currentMode = "fanLow" }
	else { currentMode == null }

	def lastTriedMode = getDataByName("lastTriedFanMode") ?: currentMode.value ?: "fanAuto"

	def supportedModes = getDataByName("supportedFanModes") ?: "fanAuto fanLow"
	def modeOrder = fanModes()
	//log.info modeOrder

	def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
	def nextMode = next(lastTriedMode)
	while (!supportedModes?.contains(nextMode) && nextMode != "fanAuto") {
		nextMode = next(nextMode)
	}

	switchToFanMode(nextMode)
}

def switchToFanMode(nextMode) {
	def supportedFanModes = getDataByName("supportedFanModes")
	if(supportedFanModes && !supportedFanModes.tokenize()?.contains(nextMode)) log.warn "thermostat mode '$nextMode' is not supported"

	if(nextMode in fanModes()) {
		updateState("lastTriedFanMode", nextMode)
		return "$nextMode"()
	} else {
		log.debug("no fan mode method '$nextMode'")
	}
}


def setThermostatFanMode(String value) {
	log.info "fan mode " + value + " ${fanModeMap[value]}"
	delayBetween([
		zwave.thermostatFanModeV2.thermostatFanModeSet(fanMode: fanModeMap[value]).format(),
		zwave.thermostatFanModeV2.thermostatFanModeGet().format()
	], standardDelay)
}


def getFanModeMap() { [
	"auto": physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport.FAN_MODE_AUTO_LOW,
	"circulate": physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport.FAN_MODE_AUTO_LOW,
	"on": physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport.FAN_MODE_LOW,

	"fanAuto": physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport.FAN_MODE_AUTO_LOW,
	"fanOn": physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport.FAN_MODE_LOW,
	"fanLow": physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport.FAN_MODE_LOW,
	"fanMedium": physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport.FAN_MODE_MEDIUM,
	"fanHigh": physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport.FAN_MODE_HIGH
]}


def auto() {
	log.debug "${device.name} received AUTO request"
	setThermostatMode("auto")
}

def cool() {
	log.debug "${device.name} received COOL request"
	setThermostatMode("cool")
}

def emergencyHeat() {
	log.warn "emergencyheat() not supported"
	return
	setThermostatMode("emergencyHeat")
}

def heat() {
	log.debug "${device.name} received HEAT request"
	setThermostatMode("heat")
}

def off() {
	log.debug "${device.name} received OFF request"
	setThermostatMode("off")
}

def fanAuto() {
	log.debug "${device.name} received FANAUTO request"
	delayBetween([
		zwave.thermostatFanModeV2.thermostatFanModeSet(fanMode: 0).format(),
		zwave.thermostatFanModeV2.thermostatFanModeGet().format()
	], standardDelay)
}

def fanCirculate() {
	log.warn "fanCirculate() not supported"
	return
	delayBetween([
		zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: 6).format(),
		zwave.thermostatFanModeV3.thermostatFanModeGet().format()
	], standardDelay)
}

def fanOn() {
	log.debug "${device.name} received FANON request"
	delayBetween([
		zwave.thermostatFanModeV2.thermostatFanModeSet(fanMode: 1).format(),
		zwave.thermostatFanModeV2.thermostatFanModeGet().format()
	], standardDelay)
}

def on() {
	log.debug "${device.name} received on request"
	setThermostatMode("resume")
}


private getStandardDelay() {
	1000
}


def eco() {
	log.debug "${device.name} received ECO request"
	setThermostatMode("off")
}

def dry() {
	log.debug "${device.name} received DRY request"
	setThermostatMode("dry")
}

def autoChangeover() {
	log.debug "${device.name} received AUTOCHANGEOVER request"
	setThermostatMode("autoChangeover")
}

def fanLow() {
	log.debug "setting fan mode low"
	setThermostatFanMode("fanLow")
}

def fanMed() {
	log.debug "setting fan mode med"
	setThermostatFanMode("fanMedium")
}

def fanHigh() {
	log.debug "setting fan mode high"
	setThermostatFanMode("fanHigh")
}
