/*
 * Volvo Car Device - Hubitat Driver
 *
 * Description: Represents a Volvo vehicle and its sensors/controls.
 */

String appName() { return 'Volvo Cars' }
String appVersion() { return '0.3.0' }
String nameSpace() { return 'brianschmitt' }

metadata {
    definition(
        name: "${appName()} Device",
        namespace: nameSpace(),
        author: 'Brian Schmitt',
        description: "Vehicle device for ${appName()} ",
        category: 'Vehicle',
        version: appVersion(),
        importUrl:
            'https://raw.githubusercontent.com/brianschmitt/hubitat-volvo-cars/main/drivers/volvo-cars-driver.groovy',
        documentationLink:'', // TODO
        communityLink: '', // TODO
    ) {
        capability 'Refresh' // refresh()
        capability 'Lock' // lock(), unlock() lock
        capability 'Battery' // battery
        capability 'Switch' // off() on() switch
        capability 'Presence Sensor' // presence
        capability 'Alarm' // both() siren() strobe() off() alarm
        // capability 'Configuration' // configure()
        capability 'Sensor'
        capability 'Actuator'
        // capability 'EnergyMeter' // energy
        // capability 'PowerMeter' // power

        attribute 'connectionStatus', 'ENUM', ['UNSPECIFIED', 'AVAILABLE', 'UNAVAILABLE']

        attribute 'coolantLevel', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'TOO_LOW']
        attribute 'oilLevel', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'SERVICE_REQUIRED', 'TOO_LOW', 'TOO_HIGH']

        attribute('serviceStatus', 'ENUM',
                    ['UNSPECIFIED', 'NO_WARNING', 'UNKNOWN_WARNING', 'REGULAR_MAINTENANCE_ALMOST_TIME_FOR_SERVICE',
                    'ENGINE_HOURS_ALMOST_TIME_FOR_SERVICE', 'DISTANCE_DRIVEN_ALMOST_TIME_FOR_SERVICE',
                    'REGULAR_MAINTENANCE_TIME_FOR_SERVICE', 'ENGINE_HOURS_TIME_FOR_SERVICE',
                    'DISTANCE_DRIVEN_TIME_FOR_SERVICE', 'REGULAR_MAINTENANCE_OVERDUE_FOR_SERVICE',
                    'ENGINE_HOURS_OVERDUE_FOR_SERVICE', 'DISTANCE_DRIVEN_OVERDUE_FOR_SERVICE'])
        attribute 'serviceTrigger', 'ENUM', ['CALENDAR_TIME', 'DISTANCE', 'ENGINE_HOURS', 'UNSPECIFIED', 'UNKNOWN']
        attribute 'engineTimeToService', 'NUMBER'
        attribute 'distanceToService', 'NUMBER'
        attribute 'timeToService', 'NUMBER'

        attribute 'brakeFluid', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'TOO_LOW']

        attribute 'windowFrontLeft', 'ENUM', ['UNSPECIFIED', 'OPEN', 'CLOSED', 'AJAR']
        attribute 'windowFrontRight', 'ENUM', ['UNSPECIFIED', 'OPEN', 'CLOSED', 'AJAR']
        attribute 'windowRearLeft', 'ENUM', ['UNSPECIFIED', 'OPEN', 'CLOSED', 'AJAR']
        attribute 'windowRearRight', 'ENUM', ['UNSPECIFIED', 'OPEN', 'CLOSED', 'AJAR']
        attribute 'sunroofStatus', 'ENUM', ['UNSPECIFIED', 'OPEN', 'CLOSED', 'AJAR']

        attribute 'lockStatus', 'ENUM', ['UNSPECIFIED', 'UNLOCKED', 'LOCKED']
        attribute 'doorFrontLeft', 'ENUM', ['UNSPECIFIED', 'OPEN', 'CLOSED', 'AJAR']
        attribute 'doorFrontRight', 'ENUM', ['UNSPECIFIED', 'OPEN', 'CLOSED', 'AJAR']
        attribute 'doorRearLeft', 'ENUM', ['UNSPECIFIED', 'OPEN', 'CLOSED', 'AJAR']
        attribute 'doorRearRight', 'ENUM', ['UNSPECIFIED', 'OPEN', 'CLOSED', 'AJAR']
        attribute 'tailgateStatus', 'ENUM', ['UNSPECIFIED', 'OPEN', 'CLOSED', 'AJAR']
        attribute 'hoodStatus', 'ENUM', ['UNSPECIFIED', 'OPEN', 'CLOSED', 'AJAR']
        attribute 'tankLidStatus', 'ENUM', ['UNSPECIFIED', 'OPEN', 'CLOSED', 'AJAR']

        attribute 'fuelLevel', 'NUMBER'

        attribute 'odometer', 'NUMBER'

        attribute 'latitude', 'NUMBER'
        attribute 'longitude', 'NUMBER'
        attribute 'heading', 'NUMBER'
        attribute 'locationTimestamp', 'STRING'

        attribute 'distanceToEmptyBattery', 'NUMBER'
        attribute 'chargingStatus', 'ENUM', ['CHARGING', 'IDLE', 'DONE', 'FAULT', 'SCHEDULED', 'UNSPECIFIED']
        attribute('chargingConnectionStatus',
                    'ENUM',
                    ['CONNECTED_AC', 'CONNECTED_DC', 'DISCONNECTED', 'FAULT', 'UNSPECIFIED'])
        attribute 'targetBatteryChargeLevel', 'NUMBER'
        attribute 'chargingCurrentLimit', 'NUMBER'
        attribute 'estimatedChargingTime', 'NUMBER'

        attribute 'averageFuelConsumption', 'NUMBER'
        attribute 'averageEnergyConsumption', 'NUMBER'
        attribute 'averageFuelAutomatic', 'NUMBER'
        attribute 'averageEnergyAutomatic', 'NUMBER'
        attribute 'distanceToEmptyTank', 'NUMBER'
        attribute 'distanceToEmptyBattery', 'NUMBER'
        attribute 'averageSpeed', 'NUMBER'
        attribute 'averageSpeedAutomatic', 'NUMBER'
        attribute 'tripMeterManual', 'NUMBER'
        attribute 'tripMeterAutomatic', 'NUMBER'

        attribute('tyreFrontLeft',
                    'ENUM',
                    ['UNSPECIFIED', 'NO_WARNING', 'VERY_LOW_PRESSURE', 'LOW_PRESSURE', 'HIGH_PRESSURE'])
        attribute('tyreFrontRight',
                    'ENUM',
                    ['UNSPECIFIED', 'NO_WARNING', 'VERY_LOW_PRESSURE', 'LOW_PRESSURE', 'HIGH_PRESSURE'])
        attribute('tyreRearLeft',
                    'ENUM',
                    ['UNSPECIFIED', 'NO_WARNING', 'VERY_LOW_PRESSURE', 'LOW_PRESSURE', 'HIGH_PRESSURE'])
        attribute('tyreRearRight',
                    'ENUM',
                    ['UNSPECIFIED', 'NO_WARNING', 'VERY_LOW_PRESSURE', 'LOW_PRESSURE', 'HIGH_PRESSURE'])

        attribute 'vin', 'STRING'
        attribute 'modelYear', 'NUMBER'
        attribute 'gearbox', 'ENUM', ['AUTOMATIC', 'MANUAL']
        attribute 'fuelType', 'ENUM', ['DIESEL', 'PETROL', 'PETROL/ELECTRIC', 'ELECTRIC', 'NONE']
        attribute 'externalColour', 'STRING'
        attribute 'batteryCapacityKWH', 'NUMBER'
        attribute 'model', 'STRING'
        attribute 'upholstery', 'STRING'
        attribute 'steering', 'STRING'

        attribute 'warningBrakeLightLeft', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningBrakeLightCenter', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningBrakeLightRight', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningFogLightFront', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningFogLightRear', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningPositionLightFrontLeft', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningPositionLightFrontRight', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningPositionLightRearLeft', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningPositionLightRearRight', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningHighBeamLeft', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningHighBeamRight', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningLowBeamLeft', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningLowBeamRight', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningDaytimeRunningLightLeft', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningDaytimeRunningLightRight', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningTurnIndicationFrontLeft', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningTurnIndicationFrontRight', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningTurnIndicationRearLeft', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningTurnIndicationRearRight', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningRegistrationPlateLight', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningSideMarkLights', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningHazardLights', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']
        attribute 'warningReverseLights', 'ENUM', ['UNSPECIFIED', 'NO_WARNING', 'FAILURE']

        attribute 'apiTimestamp', 'STRING'
        attribute 'commsError', 'STRING'

        attribute 'dashboard', 'STRING'
    }

    preferences {
        input name: 'useImperial', type: 'bool', title: 'Use Imperial Measurements', defaultValue: false
        input name: 'engineStartDuration',
                    type: 'enum',
                    title: 'Remote Start Duration',
                    defaultValue: 10,
                    options: [5: '5 minutes', 10: '10 minutes', 15: '15 minutes']
        input name: 'logEnable', type: 'bool', title: 'Enable debug logging', defaultValue: false
    }
}

void installed() {
    logDebug 'Device Installed'
    initialize()
}

void updated() {
    logDebug 'Device Updated'
    initialize()
}

void configure() {
    logDebug 'Device Configured'
    initialize()
    refresh()
}

void initialize() {
    state.clear()
    if (logEnable) { runIn(1800, 'debugLogOff') }
    sendEvent(name: 'commsError', value: 'OK')
}

void refresh() {
    logDebug 'Refresh requested'
    parent?.refreshVehicleData([vin: device.deviceNetworkId])
}

void lock() {
    if (state['COMMAND_LOCK']) {
        lockDoors()
    } else if (state['COMMAND_LOCK_DOORS_REDUCED_GUARD']) {
        // TODO - For android based cars?
        // TODO - Verify if state command is correct...
        lockDoorsReducedGuard()
    } else {
        logWarn 'Vehicle does not support remote lock'
    }
}

void unlock() {
    logInfo 'Unlock requested'
    parent?.sendCommand(device.deviceNetworkId, 'unlock')
    runIn(15, refresh)
}

void on() {
    if (state['COMMAND_ENGINE_START']) {
        startEngine()
    } else if (state['COMMAND_CLIMATIZATION_START']) {
        startClimatization()
    } else {
        logWarn 'Vehicle does not support remote start'
    }
}

void off() {
    if (state['COMMAND_ENGINE_STOP']) {
        stopEngine()
    } else if (state['COMMAND_CLIMATIZATION_STOP']) {
        stopClimatization()
    } else {
        logWarn 'Vehicle does not support remote stop'
    }
}

void startEngine() {
    logInfo 'Start Engine requested'
    parent?.sendCommand(device.deviceNetworkId, 'engine-start', [runtimeMinutes: engineStartDuration])
    runIn(15, refresh)
}

void stopEngine() {
    logInfo 'Stop Engine requested'
    parent?.sendCommand(device.deviceNetworkId, 'engine-stop')
    runIn(15, refresh)
}

void startClimatization() {
    logInfo 'Start Climatization requested'
    parent?.sendCommand(device.deviceNetworkId, 'climatization-start')
    runIn(15, refresh)
}

void stopClimatization() {
    logInfo 'Stop Climatization requested'
    parent?.sendCommand(device.deviceNetworkId, 'climatization-stop')
    runIn(15, refresh)
}

void both() {
    logInfo 'Honk & Flash requested'
    parent?.sendCommand(device.deviceNetworkId, 'honk-flash')
}

void siren() {
    logInfo 'Honk requested'
    parent?.sendCommand(device.deviceNetworkId, 'honk')
}

void strobe() {
    logInfo 'Flash requested'
    parent?.sendCommand(device.deviceNetworkId, 'flash')
}

void lockDoors() {
    logInfo 'Lock doors requested'
    parent?.sendCommand(device.deviceNetworkId, 'lock')
    runIn(15, refresh)
}

void lockDoorsReducedGuard() {
    logInfo 'Lock doors with reduced guard requested'
    parent?.sendCommand(device.deviceNetworkId, 'lock-guard')
    runIn(15, refresh)
}

void debugLogOff() {
    device.updateSetting('logEnable', [value:'false', type:'bool'])
}

void parse(Map data) {
    sendEvent(name: 'commsError', value: 'OK')

    if (data.error) {
        logDebug "Error received from App: ${data.error}"
        sendEvent(name: 'commsError', value: data.error)
        return
    }

    // Process each endpoint's data if present
    data.each { key, rawJson ->
        logDebug "Processing endpoint: ${key}"
        if (rawJson) {
            try {
                def endpointData = rawJson?.data

                if (endpointData) {
                    switch (key) {
                        case 'availability':   parseAvailability(endpointData); break
                        case 'brakes':         parseBrakes(endpointData); break
                        case 'commands':       parseCommands(endpointData); break
                        case 'diagnostics':    parseDiagnostics(endpointData); break
                        case 'doors':          parseDoors(endpointData); break
                        case 'engineStatus':   parseEngineStatus(endpointData); break
                        case 'engineWarnings': parseEngineWarnings(endpointData); break
                        case 'fuel':           parseFuel(endpointData); break
                        case 'odometer':       parseOdometer(endpointData); break
                        case 'statistics':     parseStatistics(endpointData); break
                        case 'tyres':          parseTyres(endpointData); break
                        case 'vehicle':        parseVehicle(endpointData); break
                        case 'warnings':       parseWarnings(endpointData); break
                        case 'windows':        parseWindows(endpointData); break
                        case 'rechargeStatus': parseRechargeStatus(endpointData); break
                        case 'location':       parseLocation(parsedData); break
                        default:
                            logDebug "No specific parser for endpoint: ${key}"
                            break
                    }
                } else if (parsedData?.error) {
                    logWarn "API returned error for ${key}: ${parsedData.error}"
                }
            } catch (e) {
                logError "Error parsing JSON for key ${key}: ${e.message}. Raw data: ${rawJson}"
                sendEvent(name: 'commsError', value: "JSON Parsing Error for ${key}")
            }
        } else if (rawJson?.error) {
            logWarn "Error reported by App for ${key}: ${rawJson.error}"
        }
    }

    updateDashboardAttribute()
    sendEvent(name: 'apiTimestamp', value: new Date().format('yyyy-MM-dd HH:mm:ss z', location.timeZone))
}

private void parseAvailability(Map data) {
    parseField(data, 'availabilityStatus', 'connectionStatus')
}

private void parseBrakes(Map data) {
    parseField(data, 'brakeFluidLevelWarning', 'brakeFluid')
}

private void parseCommands(def data) {
    for (def command : data) {
        if (command?.command) {
            state['COMMAND_' + command.command] = true
        }
    }
}

private void parseDiagnostics(Map data) {
    parseField(data, 'serviceWarning', 'serviceStatus')
    parseField(data, 'serviceTrigger', 'serviceTrigger')
    parseField(data, 'engineHoursToService', 'engineTimeToService')
    parseField(data, 'distanceToServiceKm', 'distanceToService')
    parseField(data, 'timeToService', 'timeToService', { x -> x.value.toInteger() * (x.unit == 'months' ? 1 : 30) })
}

private void parseDoors(Map data) {
    parseField(data, 'centralLock', 'lockStatus', { x -> x.value.replace('UNSPECIFIED', 'unknown') })
    updateLockCapability(data?.centralLock?.value)

    parseField(data, 'frontLeftDoor', 'doorFrontLeft')
    parseField(data, 'frontRightDoor', 'doorFrontRight')
    parseField(data, 'rearLeftDoor', 'doorRearLeft')
    parseField(data, 'rearRightDoor', 'doorRearRight')
    parseField(data, 'hood', 'hoodStatus')
    parseField(data, 'tailgate', 'tailgateStatus')
    parseField(data, 'tankLid', 'tankLidStatus')
}

private void parseEngineStatus(Map data) {
    parseField(data, 'engineStatus', 'switch', { x -> x.value == 'RUNNING' ? 'on' : 'off' })
}

private void parseEngineWarnings(Map data) {
    parseField(data, 'oilLevelWarning', 'oilLevel')
    parseField(data, 'engineCoolantLevelWarning', 'coolantLevel')
}

private void parseFuel(Map data) {
    parseField(data, 'fuelAmount', 'fuelLevel')
    parseField(data, 'batteryChargeLevel', 'battery')
}

private void parseLocation(Map data) {
    if (data?.geometry?.coordinates?.size() >= 2) {
        sendEvent(name: 'longitude', value: data.geometry.coordinates[0])
        sendEvent(name: 'latitude', value: data.geometry.coordinates[1])
    }
    if (data?.properties?.heading) {
        sendEvent(name: 'heading', value: data.properties.heading.toInteger())
    }
    if (data?.properties?.timestamp) {
        sendEvent(name: 'locationTimestamp', value: formatApiTimestamp(data.properties.timestamp))
    }
}

private void parseOdometer(Map data) {
    parseField(data, 'odometer', 'odometer')
}

private void parseRechargeStatus(Map data) {
    parseField(data, 'batteryChargeLevel', 'battery')
    parseField(data, 'electricRange', 'distanceToEmptyBattery')
    parseField(data, 'chargingSystemStatus', 'chargingStatus',
                { x -> x.value?.replace('charging_system_', '') })
    parseField(data, 'chargingConnectionStatus', 'chargingConnectionStatus',
                { x -> x.value?.replace('connection_status_', '') })
    parseField(data, 'targetBatteryChargeLevel', 'targetBatteryChargeLevel')
    parseField(data, 'chargingCurrentLimit', 'chargingCurrentLimit')
    parseField(data, 'estimatedChargingTime', 'estimatedChargingTime')
}

private void parseStatistics(Map data) {
    parseField(data, 'averageFuelConsumption', 'averageFuelConsumption')
    parseField(data, 'averageEnergyConsumption', 'averageEnergyConsumption')
    parseField(data, 'averageFuelAutomatic', 'averageFuelAutomatic')
    parseField(data, 'averageEnergyAutomatic', 'averageEnergyAutomatic')
    parseField(data, 'distanceToEmptyTank', 'distanceToEmptyTank')
    parseField(data, 'distanceToEmptyBattery', 'distanceToEmptyBattery')
    parseField(data, 'averageSpeed', 'averageSpeed')
    parseField(data, 'averageSpeedAutomatic', 'averageSpeedAutomatic')
    parseField(data, 'tripMeterManual', 'tripMeterManual')
    parseField(data, 'tripMeterAutomatic', 'tripMeterAutomatic')
}

private void parseTyres(Map data) {
    parseField(data, 'frontLeft', 'tyreFrontLeft')
    parseField(data, 'frontRight', 'tyreFrontRight')
    parseField(data, 'rearLeft', 'tyreRearLeft')
    parseField(data, 'rearRight', 'tyreRearRight')
}

private void parseVehicle(Map data) {
    parseKeyValue(data, 'vin', 'vin')
    parseKeyValue(data, 'modelYear', 'modelYear')
    parseKeyValue(data, 'gearbox', 'gearbox')
    parseKeyValue(data, 'fuelType', 'fuelType')
    parseKeyValue(data, 'externalColour', 'externalColour')
    parseKeyValue(data, 'batteryCapacityKWH', 'batteryCapacityKWH')
    parseKeyValue(data, 'model', 'model')
    parseKeyValue(data, 'upholstery', 'upholstery')
    parseKeyValue(data, 'steering', 'steering')
}

private void parseWarnings(Map data) {
    parseField(data, 'brakeLightRightWarning', 'warningBrakeLightRight')
    parseField(data, 'brakeLightLeftWarning', 'warningBrakeLightLeft')
    parseField(data, 'brakeLightCenterWarning', 'warningBrakeLightCenter')
    parseField(data, 'fogLightFrontWarning', 'warningFogLightFront')
    parseField(data, 'fogLightRearWarning', 'warningFogLightRear')
    parseField(data, 'positionLightFrontLeftWarning', 'warningPositionLightFrontLeft')
    parseField(data, 'positionLightFrontRightWarning', 'warningPositionLightFrontRight')
    parseField(data, 'positionLightRearLeftWarning', 'warningPositionLightRearLeft')
    parseField(data, 'positionLightRearRightWarning', 'warningPositionLightRearRight')
    parseField(data, 'highBeamLeftWarning', 'warningHighBeamLeft')
    parseField(data, 'highBeamRightWarning', 'warningHighBeamRight')
    parseField(data, 'lowBeamLeftWarning', 'warningLowBeamLeft')
    parseField(data, 'lowBeamRightWarning', 'warningLowBeamRight')
    parseField(data, 'daytimeRunningLightLeftWarning', 'warningDaytimeRunningLightLeft')
    parseField(data, 'daytimeRunningLightRightWarning', 'warningDaytimeRunningLightRight')
    parseField(data, 'turnIndicationFrontLeftWarning', 'warningTurnIndicationFrontLeft')
    parseField(data, 'turnIndicationFrontRightWarning', 'warningTurnIndicationFrontRight')
    parseField(data, 'turnIndicationRearLeftWarning', 'warningTurnIndicationRearLeft')
    parseField(data, 'turnIndicationRearRightWarning', 'warningTurnIndicationRearRight')
    parseField(data, 'registrationPlateLightWarning', 'warningRegistrationPlateLight')
    parseField(data, 'sideMarkLightsWarning', 'warningSideMarkLights')
    parseField(data, 'hazardLightsWarning', 'warningHazardLights')
    parseField(data, 'reverseLightsWarning', 'warningReverseLights')
}

private void parseWindows(Map data) {
    parseField(data, 'frontLeftWindow', 'windowFrontLeft')
    parseField(data, 'frontRightWindow', 'windowFrontRight')
    parseField(data, 'rearLeftWindow', 'windowRearLeft')
    parseField(data, 'rearRightWindow', 'windowRearRight')
    parseField(data, 'sunroof', 'sunroofStatus')
}

private void parseField(Map data, String sourceKey, String targetAttribute, Closure valueExtractor = { x -> x.value }) {
    def fieldData = data[sourceKey]
    if (fieldData) {
        try {
            def value = valueExtractor(fieldData)
            String unit = fieldData.unit

            if (value instanceof String && value.isNumber()) {
                value = value.toDouble()
            }

            // Imperial conversion
            if (value instanceof Number && unit && useImperial) {
                def conversion = convertToImperial(value, unit)
                value = conversion.value
                unit = conversion.unit
            }

            sendUpdatedEvent(targetAttribute, value, unit)
        } catch (e) {
            logError "Error extracting value for ${sourceKey} from ${fieldData}: ${e.message}"
        }
    }
}

private void parseKeyValue(Map data, String sourceKey, String targetAttribute) {
    def fieldData = data[sourceKey]
    if (fieldData) {
        try {
            sendUpdatedEvent(targetAttribute, fieldData)
        } catch (e) {
            logError "Error extracting value for ${sourceKey} from ${fieldData}: ${e.message}"
        }
    }
}

private def convertToImperial(def value, String unit) {
    if (value instanceof Number) {
        double doubleValue = value.toDouble()

        switch (unit) {
            case 'km':
                def miles = doubleValue * 0.621371
                def roundedMiles = Math.round(miles * 10.0) / 10.0
                return [value: roundedMiles, unit: 'miles']

            case 'l':
                def gallons = doubleValue * 0.264172
                def roundedGallons = Math.round(gallons * 10.0) / 10.0
                return [value: roundedGallons, unit: 'gallons']

            case 'km/h':
                def mph = doubleValue * 0.621371
                def roundedMph = Math.round(mph * 10.0) / 10.0
                return [value: roundedMph, unit: 'mph']

            case 'l/100km':
                if (doubleValue == 0 || doubleValue < 0.0001) {
                    logWarn("Attempted to convert l/100km with value close to zero: ${doubleValue}. Returning 0 mpg.")
                    return [value: 0.0, unit: 'mpg']
                }
                def mpg = 282.481 / doubleValue
                def roundedMpg = Math.round(mpg * 10.0) / 10.0
                return [value: roundedMpg, unit: 'mpg']
        }
    }
    return [value: value, unit: unit]
}

private void sendUpdatedEvent(String targetAttribute, def value, String unit = null) {
    def currentVal = device.currentValue(targetAttribute)
    String storedUnitKey = "${targetAttribute}_unit"
    String currentStoredUnit = state[storedUnitKey]

    if (currentVal.toString() != value.toString() || currentStoredUnit != unit) {
        logDebug "Updating ${targetAttribute} from ${currentVal} to ${value} ${unit ?: ''}"
        sendEvent(name: targetAttribute, value: value, unit: unit)
        if (unit != null) {
            state[storedUnitKey] = unit
        } else {
            state.remove(storedUnitKey)
        }
        logDebug "Updated ${targetAttribute}: ${value} ${unit ?: ''}"
    }
}

private void updateLockCapability(String lockValue) {
    if (lockValue) {
        String lockState = (lockValue == 'LOCKED') ? 'locked' : 'unlocked'
        if (device.currentValue('lock') != lockState) {
            sendEvent(name: 'lock', value: lockState)
            logDebug "Updated lock capability: ${lockState}"
        }
    }
}

private void logInfo(String msg) { log.info "${device.displayName }: ${msg }" }
private void logDebug(String msg) { if (logEnable) { log.debug "${device.displayName }: ${msg }" } }
private void logWarn(String msg) { log.warn "${device.displayName}: ${msg}" }
private void logError(String msg) { log.error "${device.displayName}: ${msg}" }

private String formatApiTimestamp(String isoTimestamp) {
    if (!isoTimestamp) { return null }
    try {
        def date = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSX", isoTimestamp) ?:
                   Date.parse("yyyy-MM-dd'T'HH:mm:ssX", isoTimestamp)
        def df = new java.text.SimpleDateFormat('yyyy-MM-dd HH:mm:ss z')
        df.setTimeZone(location.timeZone ?: TimeZone.getDefault())
        return df.format(date)
    } catch (e) {
        logWarn "Could not parse timestamp '${isoTimestamp}': ${e.message}"
        return isoTimestamp
    }
}

private void updateDashboardAttribute() {
    def lockStatus = device.currentValue('lock')
    String lockSummary = ''
    def runningStatus = device.currentValue('switch')
    String runningSummary = ''
    def chargingStatus = device.currentValue('chargingStatus')
    String chargingSummary = ''

    def distanceToEmptyBattery = device.currentValue('distanceToEmptyBattery') ?: 0
    def distanceToEmptyTank = device.currentValue('distanceToEmptyTank') ?: 0

    def distanceStatus = distanceToEmptyBattery + distanceToEmptyTank

    // we might want to move this to a common property
    def fuelType = device.currentValue('fuelType') ?: 'UNKNOWN'
    def isElectric = false
    if (fuelType == 'PETROL/ELECTRIC' || fuelType == 'ELECTRIC' || fuelType == 'NONE') {
        isElectric = true
    }

    // Lock Status
    switch (lockStatus) {
        case 'locked':
            lockSummary = '🔒\n'
            break
        case 'unlocked':
            lockSummary = '🔓\n'
            break
        default:
            lockSummary = '❓\n'
            break
    }

    // Running Status
    switch (runningStatus) {
        case 'on':
            runningSummary = '🚗\n'
            break
        case 'off':
            runningSummary = '🛑\n'
            break
        default:
            runningSummary = '❓\n'
            break
    }

    // Charging Status
    switch (chargingStatus) {
        case 'CHARGING':
            chargingSummary = '⚡️ Charging\n'
            break
        case 'IDLE':
            chargingSummary = '🔋 Idle\n'
            break
        case 'DONE':
            chargingSummary = '✅ Done\n'
            break
        case 'FAULT':
            chargingSummary = '❌ Fault\n'
            break
        case 'SCHEDULED':
            chargingSummary = '⏰ Scheduled\n'
            break
        default:
            chargingSummary = '❓ Unknown\n'
            break
    }

    String statusColor = ''
    if (lockStatus == 'locked') {
        statusColor = 'color: #4CAF50;'
    } else {
        statusColor = 'color: #F44336;'
    }

    def html = """
    <style>
        .container { padding: 5px; font-size: 14px; background-color: ${statusColor} }
        .title { font-weight: bold; text-align: center; margin-bottom: 5px; }
        .status { font-weight: bold; text-align: center; ${statusColor} }
        .line { display: flex; justify-content: space-between; margin-bottom: 2px; }
        .label { color: #888; flex-basis: 40%; }
        .value { text-align: right; font-weight: normal; flex-basis: 60%; }
    </style>
    <div class="container">
        <div class="title">Range: ${distanceStatus}</div>
        <div class="status"><span class="label">Doors</span> <span class="value">${lockSummary}</span></div>
        <div class="status"><span class="label">Engine</span> <span class="value">${runningSummary}</span></div>
    """

    if (isElectric) {
        html += """
        <div class="status"><span class="label">Charge</span> <span class="value">${chargingSummary}</span></div>
        """
    }

    html += '''
    </div>
    '''
    sendEvent(name: 'dashboard', value: html, isStateChange: true)
}
