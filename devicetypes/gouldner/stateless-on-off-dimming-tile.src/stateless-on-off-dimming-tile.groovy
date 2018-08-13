/**
 *  Stateless On/Off Dimming Tile
 *
 *  Author: Ronald Gouldner
 *
 *  Date: 2018-08-13
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Stateless On/Off Dimming Tile", namespace: "gouldner", author: "Ronald Gouldner") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
        capability "Switch Level"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
    
         multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
           tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                  attributeState "offReady", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#bfbfbf", nextState: "onReady"
			      attributeState "onReady", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "offReady"
			      attributeState "off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#bfbfbf"
			      attributeState "on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
               }
               tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                   attributeState "level", action:"switch level.setLevel"
               }
        }

		standardTile("button", "device.switch", width: 4, height: 4, canChangeIcon: true) {
			state "offReady", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#bfbfbf", nextState: "onReady"
			state "onReady", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "offReady"
			state "off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#bfbfbf"
			state "on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
        standardTile("buttonOn", "device.switchOn", width: 2, height: 2,) {
			state "on", label: 'On', action: "switch.on", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
        standardTile("buttonOff", "device.switchOff", width: 2, height: 2,) {
			state "off", label: 'Off', action: "switch.off", icon: "st.switches.switch.off", backgroundColor: "#bfbfbf"
		}
        valueTile("level", "device.level", inactiveLabel: false, decoration: "flat", width: 2, height: 4) {
            state "level", label:'${currentValue} %', unit:"%", backgroundColor:"#ffffff"
        }
        controlTile("levelSliderControl", "device.level", "slider", width: 2, height: 4, inactiveLabel: false, range:"(01..100)") {
            state "level", action:"switch level.setLevel"
        }

		main "switch"
		details "button","levelSliderControl","buttonOn","buttonOff"
	}
}

def parse(String description) {
}

def on() {
    log.debug "Stateless On/Off Button Tile Virtual Switch ${device.name} turned on"
    sendEvent(name: "switch", value: "on")
    sendEvent(name: "switch", value: "onReady")
}

def off() {
    log.debug "Stateless On/Off Button Tile Virtual Switch ${device.name} turned off"
    sendEvent(name: "switch", value: "off")
    sendEvent(name: "switch", value: "offReady")
}

def setLevel(valueaux) {
  def level = Math.max(Math.min(valueaux, 99), 0)
  if (level > 0) {
      sendEvent(name: "switch", value: "on")
  } else {
      sendEvent(name: "switch", value: "off")
  }
  sendEvent(name: "level", value: level, unit: "%")
}