/**
 *  Lock Door when my Garage left open for N min
 *  Author: Ronald Gouldner
 */
definition(
    name: "Lock Door when my Garage left open for N min",
    namespace: "gouldner",
    author: "Ronald Gouldner",
    description: "Lock Door when my Garage left open for N min",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
    section("When the Garage Door opens...") {
	    input "contact1", "capability.contactSensor", title: "Door?"
    }
    section("Lock Door after...") {
        input "lockDoorAfter", "number", title: "Minutes?"
    }
    section("Lock This Door...") {
	    input "lock1", "capability.lock", title: "Lock?"
    }
}

def installed()
{
	subscribe(contact1, "contact.open", doorOpenHandler)
}

def updated()
{
	unsubscribe()
	subscribe(contact1, "contact.open", doorOpenHandler)
}

def doorOpenHandler(evt) {
    log.debug "$evt.value: $evt, $settings"
    log.debug "${contact1.label ?: contact1.name} was opened, Locking ${lock1.label ?: lock1.name} after $lockDoorAfter min"
    def delay = 60 * lockDoorAfter
    runIn(delay, lockDoor)
}

def lockDoor() {
    log.debug "Locking Door"
    lock1.lock()
}