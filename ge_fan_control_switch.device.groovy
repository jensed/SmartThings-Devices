metadata {
	// Automatically generated. Make future change here.
	definition (name: "My GE Fan Control Switch", namespace: "jscgs350", author: "SmartThings") {
		capability "Switch Level"
		capability "Actuator"
		capability "Indicator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        
        command "lowSpeed"
        command "medSpeed"
        command "highSpeed"

		attribute "currentSpeed", "string"

		fingerprint inClusters: "0x26"
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff"
//			state "turningOn", label:'${name}', icon:"st.switches.switch.on", backgroundColor:"#79b821"
//			state "turningOff", label:'${name}', icon:"st.switches.switch.off", backgroundColor:"#ffffff"
		}
		standardTile("indicator", "device.indicatorStatus", inactiveLabel: false, decoration: "flat") {
			state "when off", action:"indicator.indicatorWhenOn", icon:"st.indicators.lit-when-off"
			state "when on", action:"indicator.indicatorNever", icon:"st.indicators.lit-when-on"
			state "never", action:"indicator.indicatorWhenOff", icon:"st.indicators.never-lit"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}
        valueTile("currentSpeed", "device.currentSpeed", inactiveLabel: false, decoration: "flat") {
            state ("default", label:'')
        }

//Speed control row
        standardTile("lowSpeed", "device.level", inactiveLabel: false, decoration: "flat") {
            state "lowSpeed", label:'LOW', action:"lowSpeed", icon:"st.Home.home30"
        }
        standardTile("medSpeed", "device.level", inactiveLabel: false, decoration: "flat") {
            state "medSpeed", label:'MED', action:"medSpeed", icon:"st.Home.home30"
        }
        standardTile("highSpeed", "device.level", inactiveLabel: false, decoration: "flat") {
            state "highSpeed", label:'HIGH', action:"highSpeed", icon:"st.Home.home30"
        }

		main(["switch"])
		details(["switch", "currentSpeed", "indicator", "lowSpeed", "medSpeed", "highSpeed", "refresh"])
	}
}

def parse(String description) {
	def item1 = [
		canBeCurrentState: false,
		linkText: getLinkText(device),
		isStateChange: false,
		displayed: false,
		descriptionText: description,
		value:  description
	]
	def result
	def cmd = zwave.parse(description, [0x20: 1, 0x26: 1, 0x70: 1])
	if (cmd) {
		result = createEvent(cmd, item1)
	}
	else {
		item1.displayed = displayed(description, item1.isStateChange)
		result = [item1]
	}
	log.debug "Parse returned ${result?.descriptionText}"
	result
}

def createEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, Map item1) {
	def result = doCreateEvent(cmd, item1)
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "physical"
	}
	result
}

def createEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd, Map item1) {
	def result = doCreateEvent(cmd, item1)
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "physical"
	}
	result
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStartLevelChange cmd, Map item1) {
	[]
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd, Map item1) {
	[response(zwave.basicV1.basicGet())]
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd, Map item1) {
	def result = doCreateEvent(cmd, item1)
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "physical"
	}
	result
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd, Map item1) {
	def result = doCreateEvent(cmd, item1)
	result[0].descriptionText = "${item1.linkText} is ${item1.value}"
	result[0].handlerName = cmd.value ? "statusOn" : "statusOff"
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "digital"
	}
	result
}

def doCreateEvent(physicalgraph.zwave.Command cmd, Map item1) {
	def result = [item1]

	item1.name = "switch"
	item1.value = cmd.value ? "on" : "off"
	item1.handlerName = item1.value
	item1.descriptionText = "${item1.linkText} was turned ${item1.value}"
	item1.canBeCurrentState = true
	item1.isStateChange = isStateChange(device, item1.name, item1.value)
	item1.displayed = item1.isStateChange

	if (cmd.value >= 5) {
		def item2 = new LinkedHashMap(item1)
		item2.name = "level"
		item2.value = cmd.value as String
		item2.unit = "%"
		item2.descriptionText = "${item1.linkText} dimmed ${item2.value} %"
		item2.canBeCurrentState = true
		item2.isStateChange = isStateChange(device, item2.name, item2.value)
		item2.displayed = false
        if (item2.value == "30") {
        	sendEvent(name: "currentSpeed", value: "LOW" as String)
        }
        if (item2.value == "62") {
        	sendEvent(name: "currentSpeed", value: "MEDIUM" as String)
        }
        if (item2.value == "99") {
        	sendEvent(name: "currentSpeed", value: "HIGH" as String)
        }        
		result << item2
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	def value = "when off"
	if (cmd.configurationValue[0] == 1) {value = "when on"}
	if (cmd.configurationValue[0] == 2) {value = "never"}
	[name: "indicatorStatus", value: value, display: false]
}

def createEvent(physicalgraph.zwave.Command cmd,  Map map) {
	// Handles any Z-Wave commands we aren't interested in
	log.debug "UNHANDLED COMMAND $cmd"
}

def on() {
	log.info "on"
	delayBetween([zwave.basicV1.basicSet(value: 0xFF).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def off() {
	delayBetween ([zwave.basicV1.basicSet(value: 0x00).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def setLevel(value) {
    def level = Math.min(value as Integer, 99)
	delayBetween ([zwave.basicV1.basicSet(value: level).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def setLevel(value, duration) {
    def level = Math.min(value as Integer, 99)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format()
}

def lowSpeed() {
	log.debug "Low speed settings"
    def dispSpeed = "LOW"
    delayBetween ([zwave.basicV1.basicSet(value: 30).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def medSpeed() {
	log.debug "Medium speed settings"
    def dispSpeed = "MEDIUM"
    delayBetween ([zwave.basicV1.basicSet(value: 62).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def highSpeed() {
	log.debug "High speed settings"
    def dispSpeed = "HIGH"
    delayBetween ([zwave.basicV1.basicSet(value: 99).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def poll() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def refresh() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def indicatorWhenOn() {
	sendEvent(name: "indicatorStatus", value: "when on", display: false)
	zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1).format()
}

def indicatorWhenOff() {
	sendEvent(name: "indicatorStatus", value: "when off", display: false)
	zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 3, size: 1).format()
}

def indicatorNever() {
	sendEvent(name: "indicatorStatus", value: "never", display: false)
	zwave.configurationV1.configurationSet(configurationValue: [2], parameterNumber: 3, size: 1).format()
}

def invertSwitch(invert=true) {
	if (invert) {
		zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 4, size: 1).format()
	}
	else {
		zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 4, size: 1).format()
	}
}
