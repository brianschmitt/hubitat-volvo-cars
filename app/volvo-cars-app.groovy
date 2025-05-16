/*
 * Volvo Cars - Hubitat App
 *
 * Description: Manages connection and communication with Volvo Cars API.
 * Handles authentication, token management, and device creation.
 */
import groovy.json.JsonSlurper

String appName() { return 'Volvo Cars' }
String appVersion() { return '0.1.0' }
String nameSpace() { return 'brianschmitt' }

definition(
    name: appName(),
    namespace: nameSpace(),
    author: 'Brian Schmitt',
    description: 'Connects Hubitat to Volvo Cars API',
    category: 'Vehicle',
    iconUrl: '',
    iconX2Url: '',
    version: appVersion(),
    installOnOpen: true,
    singleInstance: true,
    documentationLink: 'https://community.hubitat.com/t/release-volvo-cars-integration-official-api-for-hubitat/153428',
    importUrl: 'https://raw.githubusercontent.com/brianschmitt/hubitat-volvo-cars/main/app/volvo-cars-app.groovy',
)

preferences {
    page(name: 'mainPage')
    page(name: 'authPage')
    page(name: 'otpPage')
    page(name: 'addVehiclePage')
    page(name: 'removeVehiclePage')
}

String authApiUrl() { return 'https://volvoid.eu.volvocars.com/as/authorization.oauth2' }
String tokenUrl() { return 'https://volvoid.eu.volvocars.com/as/token.oauth2' }
String apiBaseUrl() { return 'https://api.volvocars.com' }

void installed() {
    logInfo("Installing ${appName()} v${appVersion()}")
    initialize()
}

void updated() {
    logInfo("Updating ${appName()} v${appVersion()}")
    unschedule()
    initialize()
}

void uninstalled() {
    logInfo("Uninstalling ${appName()}")
    clearTokens()
    getAllChildDevices().each { x -> deleteChildDevice(x.deviceNetworkId) }
}

void initialize() {
    app.updateSetting('refreshIntervalMinutes', [type:'number', value: (settings.refreshIntervalMinutes ?: '10')])

    scheduleDeviceRefresh()
    scheduleTokenRefresh()

    app.updateSetting('logEnable', [type:'bool', value: (settings.logEnable ?: false)])
    if (settings.logEnable) { runIn(1800, 'debugLogOff') }
}

def mainPage() {
    dynamicPage(name: 'mainPage', install: true, uninstall: true) {
        section {
            paragraph "Status: <b>${atomicState.accessToken ? 'Authenticated' : 'Not Authenticated'}</b>"
            href 'authPage',
                title: 'Manage Credentials & API Key',
                description: 'Enter or update your credentials and VCC API Key.'
        }
        section('Connected Vehicles') {
            href 'addVehiclePage', title: 'Manage Volvo Vehicles', description: 'Add or remove a vehicle using its VIN.'
        }
        section('Settings') {
            input 'refreshIntervalMinutes',
                'enum',
                title: 'Vehicle Data Refresh Interval (minutes)',
                defaultValue: 10,
                submitOnChange: true,
                options: ['1': '1 minute', '5': '5 minutes', '10': '10 minutes', '15': '15 minutes', '30': '30 minutes']
            input 'logEnable',
                'bool',
                title: 'Enable debug logging',
                defaultValue: false,
                submitOnChange: true
        }
    }
}

def authPage() {
    String nextPage = atomicState.accessToken ? 'mainPage' : 'otpPage'
    dynamicPage(name: 'authPage', title: 'Volvo Credentials & API Key', uninstall: false, nextPage: nextPage) {
        if (atomicState.accessToken) {
            section('Current Token') {
                paragraph "Token expires: ${formatExpiry(atomicState.tokenExpiresAt)}"
                input 'refreshTokenNow', 'button', title: 'Refresh Token Now'
            }
            section('Clear Authentication') {
                paragraph 'Revoke authentication data and tokens. Warning: This will stop all vehicle data updates.'
                input 'clearAuthentication', 'button', title: 'Revoke Authentication'
            }
        } else {
            section('User Authentication') {
                input('username',
                    'email',
                    title: 'Volvo Username (Email)',
                    required: true,
                    defaultValue: settings.username,
                    submitOnChange: true)
                input('password',
                    'password',
                    title: 'Volvo Password',
                    required: true,
                    submitOnChange: true)
                paragraph(
                    "<a href='https://developer.volvocars.com/account/#your-api-applications', target='_blank'>"
                    + 'Link to Volvo for Api Key</a>')
                input('vccApiKey',
                    'text',
                    title: 'Volvo VCC API Key',
                    required: true,
                    defaultValue: settings.vccApiKey,
                    submitOnChange: true)
                if (settings.username && settings.password && settings.vccApiKey) {
                    app.removeSetting('otpCode')
                    authenticate()
                }
            }
        }

        if (atomicState.authError) {
            section('Authentication Error') {
                paragraph "${atomicState.authError}"
            }
        }
    }
}

def otpPage() {
    dynamicPage(name: 'otpPage', title: 'Volvo Credentials & API Key', uninstall: false, 'nextPage': 'mainPage') {
        if (atomicState.accessToken) {
            section('OTP Authentication') {
                paragraph 'Completed authentication. No OTP currently required.'
            }
        } else {
            section('OTP Authentication') {
                paragraph(
                    'After authenticating above, an OTP will be sent to your email address. Please enter it below.')
                input 'otpCode', 'text', title: 'OTP Code', required: true, submitOnChange: true
            }

            if (settings.otpCode && settings.otpCode.length() == 6 && !atomicState.accessToken) {
                submitOtp()
            }

            if (atomicState.authError) {
                if (!atomicState.otpUrl) {
                    section('New OTP Required') {
                        input 'authenticate', 'button', title: 'Request New OTP'
                    }
                }
                section('Authentication Error') {
                    paragraph "${atomicState.authError}"
                }
            }
        }
    }
}

def addVehiclePage() {
    dynamicPage(name: 'addVehiclePage', title: 'Manage Vehicles', uninstall: false, nextPage: 'mainPage') {
        section {
            if (atomicState.accessToken) {
                app.removeSetting('otpCode')
                // Fetch the list of vehicles
                def vehicles = getVehicles().data
                def vinOptions = [:]
                if (vehicles?.size() > 0) {
                    vehicles.each { vehicle ->
                        def vin = vehicle.vin
                        def friendlyName = "VIN ${vin}"
                        vinOptions[vin] = friendlyName
                    }
                }

                input 'vinToAdd', 'enum', title: 'Select Vehicle to add:', options: vinOptions, submitOnChange: true

                if (settings.vinToAdd) {
                    input 'addThisVehicle', 'button', title: 'Add Vehicle'
                }
                if (atomicState.vehicleError) {
                    section('Error') {
                        paragraph "${atomicState.vehicleError}"
                    }
                    atomicState.remove('vehicleError')
                }
            } else {
                paragraph 'Please authenticate on the main page first.'
            }
        }
        section('Existing Vehicles:') {
            List<com.hubitat.app.ChildDeviceWrapper> childDevices = getChildDevices()
            if (childDevices?.size() > 0) {
                childDevices.each { child ->
                    def vin = child.deviceNetworkId
                    def friendlyName = child.displayName
                    href(
                        name: "removeVehicle_${vin}",
                        title: "Remove ${friendlyName} (${vin})",
                        description: 'Remove this vehicle and its device',
                        params: [vinToRemove: vin],
                        page: 'removeVehiclePage'
                    )
                }
            } else {
                paragraph 'No vehicles added yet.'
            }
        }
    }
}

def removeVehiclePage(Map params) {
    dynamicPage(name: 'removeVehiclePage', title: 'Remove Vehicle', uninstall: false) {
        def vin = params?.vinToRemove
        if (!vin) {
            section {
                paragraph 'Car has been removed.'
            }
            return
        }

        app.updateSetting('vinToRemove', vin)

        section {
            paragraph "Are you sure you want to remove the vehicle with VIN: ${vin}?"
            input 'confirmRemoveVehicle', 'button', title: 'Confirm Removal'
        }

        params.clear()
    }
}

void appButtonHandler(String btnName) {
    logDebug "appButtonHandler called with button: ${btnName}"
    switch (btnName) {
        case 'authenticate':
            authenticate()
            break
        case 'addThisVehicle':
            addThisVehicle()
            break
        case 'refreshTokenNow':
            refreshTokenNow()
            break
        case 'confirmRemoveVehicle':
            removeVehicle()
            break
        case 'clearAuthentication':
            atomicState.remove('username')
            atomicState.remove('vccApiKey')
            clearTokens()
            break
        default:
            logWarn "Unhandled button pressed in appButtonHandler: ${btnName}"
            break
    }
}

void removeVehicle() {
    String vin = app.getSetting('vinToRemove')
    logInfo "Removing vehicle: ${vin}"
    com.hubitat.app.ChildDeviceWrapper child = getChildDevice(vin)
    if (child) {
        try {
            deleteChildDevice(vin)
        } catch (e) {
            logError "Error removing child device ${vin}: ${e.message}"
        }
    }
    app.removeSetting('vinToRemove')
    scheduleDeviceRefresh()
}

void addThisVehicle() {
    def vin = app.getSetting('vinToAdd')?.trim()?.toUpperCase()

    if (!vin || vin.size() != 17) {
        logWarn "Invalid VIN provided: ${vin}"
        atomicState.vehicleError = 'No VIN provided or invalid VIN length.'
        return
    }

    com.hubitat.app.ChildDeviceWrapper d = getChildDevice(vin)
    if (d) {
        logWarn "Device with DNI ${vin} already exists."
        atomicState.vehicleError = "Device with VIN ${vin} already exists."
        return
    }

    try {
        Map data = getVehicleData(vin, ['vehicle'])
        Map vehicleData = data['vehicle'].data
        logDebug "Adding vehicle: ${vehicleData} (${vin})"
        String label = "Volvo ${vehicleData.descriptions.model}"
        addChildDevice(nameSpace(), "${appName()} Device", vin, [label: label, name: label])
        logInfo "Added Volvo Car Device: ${label} (${vin})"
        runIn(2, refreshVehicleData, [data: [vin: vin]])
        } catch (e) {
        logError "Error adding child device ${vin}: ${e.message}"
    }

    app.removeSetting('vinToAdd')
    scheduleDeviceRefresh()
}

void authenticate() {
    logInfo 'Attempting authentication...'
    atomicState.remove('authError')
    atomicState.remove('otpUrl')

    Map payload = [
        client_id: 'h4Yf0b',
        response_type: 'code',
        response_mode: 'pi.flow',
        acr_values: 'urn:volvoid:aal:bronze:2sv',
        scope: 'openid ' +
                'conve:brake_status ' +
                'conve:climatization_start_stop ' +
                'conve:command_accessibility ' +
                'conve:commands ' +
                'conve:diagnostics_engine_status ' +
                'conve:diagnostics_workshop ' +
                'conve:doors_status ' +
                'conve:engine_status ' +
                'conve:environment ' +
                'conve:fuel_status ' +
                'conve:honk_flash ' +
                'conve:lock ' +
                'conve:lock_status ' +
                'conve:navigation ' +
                'conve:odometer_status ' +
                'conve:trip_statistics ' +
                'conve:tyre_status ' +
                'conve:unlock ' +
                'conve:vehicle_relation ' +
                'conve:warnings ' +
                'conve:windows_status ' +
                'energy:battery_charge_level ' +
                'energy:charging_connection_status ' +
                'energy:charging_system_status ' +
                'energy:electric_range ' +
                'energy:estimated_charging_time ' +
                'energy:recharge_status '
    // Note: Volvo will return a 400 error if you include the following scopes:
    //   energy:charging_current_limit
    //   energy:target_battery_level
    //   location:read
    //   conve:engine_start_stop
    // Currently used but unknown why - API docs do not mention these:
    //   conve:environment
    //   conve:navigation
    // Other scopes that may be available but not used:
    //   email
    //   profile
    //   customer:attributes
    //   vehicle:attributes
    //   volvo_on_call:all
    ]

    try {
        httpPost([
                    uri: authApiUrl(),
                    body: payload,
                    requestContentType: 'application/x-www-form-urlencoded',
                    contentType: 'application/json'])
        { resp ->
            if (resp.status == 200 || resp.status == 400) {
                def cookies = resp?.headers?.'Set-Cookie'
                atomicState.authCookies = cookies
                handleAuthResponse(resp.data)
            } else {
                handleAuthError("Initial authentication request failed. Status: ${resp.status}", resp.data)
            }
        }
    } catch (e) {
        handleAuthError("Exception during initial authentication: ${e.message}")
    }
}

void submitOtp() {
    logInfo 'Submitting OTP...'
    atomicState.remove('authError')
    String otpUrl = atomicState.otpUrl
    if (!otpUrl) {
        handleAuthError('OTP submission URL not found. Please restart authentication.')
        return
    }

    Map payload = [otp: settings.otpCode]
    Map headers = apiHeaders(true, false) + ['x-xsrf-header': 'PingFederate', 'Cookie': atomicState.authCookies]

    atomicState.remove('otpUrl') // use only once

    logDebug "OTP URL: ${otpUrl}, Payload: ${payload}, Headers: ${headers}"
    try {
        httpPostJson([
                uri: otpUrl,
                body: payload,
                headers: headers,
                contentType: 'application/json'])
        { resp ->
            if (resp.status == 200) {
                app.removeSetting('password')
                def cookies = resp?.headers?.'Set-Cookie'
                atomicState.authCookies = cookies
                handleAuthResponse(resp.data)
             } else {
                handleAuthError("OTP submission failed. Status: ${resp.status}", resp.data)
            }
        }
    } catch (e) {
        app.removeSetting('otpCode')
        handleAuthError("Exception during OTP submission: ${e.message}")
    }
}

void refreshTokenNow() {
    logInfo 'Manual token refresh requested.'
    refreshToken()
}

void sendCommand(String vin, String command, Map body = null) {
    logInfo "App requested command '${command}' for VIN ${vin}"
    if (!atomicState.accessToken) { return }
    if (now() >= atomicState.tokenExpiresAt) { return }

    Map commandEndpoints = [
        'lock': "/connected-vehicle/v2/vehicles/${vin}/commands/lock",
        'unlock': "/connected-vehicle/v2/vehicles/${vin}/commands/unlock",
        'climatization-start': "/connected-vehicle/v2/vehicles/${vin}/commands/climatization-start",
        'climatization-stop': "/connected-vehicle/v2/vehicles/${vin}/commands/climatization-stop",
        'engine-start': "/connected-vehicle/v2/vehicles/${vin}/commands/engine-start",
        'engine-stop': "/connected-vehicle/v2/vehicles/${vin}/commands/engine-stop",
        'honk': "/connected-vehicle/v2/vehicles/${vin}/commands/honk",
        'flash': "/connected-vehicle/v2/vehicles/${vin}/commands/flash",
        'honk-flash': "/connected-vehicle/v2/vehicles/${vin}/commands/honk-flash",
        'lock-guard': "/connected-vehicle/v2/vehicles/${vin}/commands/lock-reduced-guard",
    ]

    String endpointPath = commandEndpoints[command]
    if (!endpointPath) {
        logError "Unknown command requested: ${command}"
        return
    }

    String url = apiBaseUrl() + endpointPath

    try {
        asynchttpPost('processCommandCallBack', [
                uri: url,
                headers: apiHeaders(),
                body: (body ?: [:]),
                contentType: 'application/json'],
                [command: command])
    } catch (e) {
        logError "Exception sending command '${command}' for VIN ${vin}: ${e.message}"
    }
}

def processCommandCallBack(resp, data) {
    if (resp.getStatus() == 200 || resp.getStatus() == 202) {
        logDebug "Command '${data['command']}' for VIN ${resp.json.data.vin}. Status: ${resp.json.data.invokeStatus}"
        result = resp.json
    } else {
        logError "Command '${command}' failed for VIN ${vin}. Status: ${resp.getStatus()}"
        result = [error: "API Error ${resp.status}", responseData: resp.json]
    }
}

void refreshVehicleData(Map params) {
    String vin = params.vin
    if (!vin) {
        logWarn 'refreshVehicleData called without VIN'
        return
    }
    logDebug "Refreshing data for VIN: ${vin}"
    com.hubitat.app.ChildDeviceWrapper child = getChildDevice(vin)
    if (!child) {
        logWarn "Device not found for VIN: ${vin}"
        return
    }

    Map apiData = getVehicleData(vin)
    if (apiData.error) {
        logWarn "Failed to get data for ${vin}: ${apiData.error}"
        child.parse([error: apiData.error])
    } else {
        child.parse(apiData)
    }
}

void refreshToken() {
    logDebug 'Refreshing token...'
    if (!atomicState.refreshToken) {
        logWarn 'No refresh token available. Please re-authenticate.'
        handleAuthError('Refresh token missing.')
        return
    }

    Map payload = [
        refresh_token: atomicState.refreshToken,
        grant_type: 'refresh_token'
    ]
    Map headers = apiHeaders(false, true)

    try {
        httpPost([
                uri: tokenUrl(),
                body: payload,
                headers: headers,
                requestContentType: 'application/x-www-form-urlencoded',
                contentType : 'application/json'])
        { resp ->
            if (resp.status == 200) {
                storeTokens(resp.data)
                atomicState.remove('authError')
                logDebug 'Successfully refreshed tokens.'
             } else {
                handleAuthError("Token refresh failed. Status: ${resp.status}", resp.data)
                clearTokens()
            }
        }
    } catch (e) {
        handleAuthError("Exception during token refresh: ${e.message}")
        clearTokens()
    }
}

void debugLogOff() {
    app.updateSetting('logEnable', [value:'false', type:'bool'])
}

private void storeTokens(Map tokenData) {
    logDebug "Token data: ${tokenData}"
    atomicState.accessToken = tokenData.access_token
    atomicState.refreshToken = tokenData.refresh_token ?: atomicState.refreshToken
    int expiresIn = tokenData.expires_in.toInteger()
    atomicState.tokenExpiresAt = now() + (expiresIn * 1000)
    scheduleTokenRefresh(expiresIn)
    logInfo "Tokens stored. Access token expires at ${formatExpiry(atomicState.tokenExpiresAt)}"

    if (settings.vccApiKey != app.getSetting('vccApiKey')) {
        app.updateSetting('vccApiKey', settings.vccApiKey)
    }
}

private void handleAuthResponse(Map json) {
    logDebug "Handling auth response: ${json}"
    String status = json.status
    logDebug "Authentication status: ${status}"
    if (status == 'COMPLETED') {
        def code = json?.authorizeResponse?.code
        if (code) {
            requestToken(code)
        } else {
            handleAuthError('Authentication completed, but no authorization code received.')
        }
    } else if (status == 'OTP_REQUIRED') {
        atomicState.otpUrl = json?._links?.checkOtp?.href + '?action=checkOtp'
        if (atomicState.otpUrl) {
            logDebug "OTP Required. Stored OTP URL: ${atomicState.otpUrl}"
        } else {
            handleAuthError('OTP required, but OTP submission URL not found in response.')
        }
    } else if (status == 'OTP_VERIFIED') {
        String continueUrl = json?._links?.continueAuthentication?.href + '?action=continueAuthentication'
        if (continueUrl) {
            continueAuthentication(continueUrl)
        } else {
            handleAuthError('OTP verified, but continuation URL not found.')
        }
    } else if (status == 'USERNAME_PASSWORD_REQUIRED') {
        String checkUserPassUrl = json?._links?.checkUsernamePassword?.href + '?action=checkUsernamePassword'
        if (checkUserPassUrl) {
            submitUsernamePassword(checkUserPassUrl)
         } else {
            handleAuthError('Username/Password required, but submission URL not found.')
        }
    }
    else {
        handleAuthError("Unhandled authentication status: ${status}", json.toString())
    }
}

private void submitUsernamePassword(String url) {
    logInfo 'Submitting username/password...'
    Map payload = [
        username: settings.username,
        password: settings.password
    ]
    Map headers = apiHeaders(false, true) + ['x-xsrf-header': 'PingFederate', 'Cookie': atomicState.authCookies]

    try {
        httpPostJson([uri: url, body: payload, headers: headers, contentType: 'application/json']) { resp ->
            if (resp.status == 200 || resp.status == 400) {
                def cookies = resp?.headers?.'Set-Cookie'
                atomicState.authCookies = cookies
                handleAuthResponse(resp.data)
             } else {
                handleAuthError("Username/Password submission failed. Status: ${resp.status}", resp.data)
            }
        }
    } catch (e) {
        handleAuthError("Exception during Username/Password submission: ${e.message}")
    }
}

private void continueAuthentication(String url) {
    logInfo 'Continuing authentication flow...'
    try {
        Map headers = apiHeaders(false, true) + ['x-xsrf-header': 'PingFederate', 'Cookie': atomicState.authCookies]
        httpGet([uri: url, headers: headers, contentType: 'application/json']) { resp ->
            if (resp.status == 200 || resp.status == 400) {
                def cookies = resp?.headers?.'Set-Cookie'
                atomicState.authCookies = cookies
                handleAuthResponse(resp.data)
             } else {
                handleAuthError("Continue authentication step failed. Status: ${resp.status}", resp.data)
            }
        }
    } catch (e) {
        handleAuthError("Exception during continue authentication: ${e.message}")
    }
}

private void requestToken(String authCode) {
    logInfo 'Requesting token with authorization code...'

    Map payload = [
        code: authCode,
        grant_type: 'authorization_code'
    ]
    Map headers = apiHeaders(false, true) + ['Cookie': atomicState.authCookies]

    try {
        httpPost([
                uri: tokenUrl(),
                body: payload,
                headers: headers,
                requestContentType: 'application/x-www-form-urlencoded',
                contentType: 'application/json'])
        { resp ->
            if (resp.status == 200) {
                storeTokens(resp.data)
                atomicState.remove('authError')
                atomicState.remove('authCookies')
             } else {
                handleAuthError("Token request failed. Status: ${resp.status}", resp.data)
            }
        }
    } catch (e) {
        handleAuthError("Exception during token request: ${e}")
    }
}

private void clearTokens() {
    logWarn 'Clearing stored tokens due to refresh failure.'
    atomicState.remove('accessToken')
    atomicState.remove('refreshToken')
    atomicState.remove('tokenExpiresAt')
    unschedule('refreshToken')
}

private void handleAuthError(String message, String responseData = null) {
    logError "Authentication Error: ${message}"
    if (responseData) { logDebug "Auth Error Response Data: ${responseData}" }
    atomicState.authError = message
}

private void scheduleTokenRefresh(Integer expiresInSeconds = null) {
    unschedule('refreshToken')
    if (atomicState.refreshToken && atomicState.tokenExpiresAt) {
        def refreshDelaySeconds
        if (expiresInSeconds) {
            refreshDelaySeconds = (expiresInSeconds * 0.8).toInteger()
            if (refreshDelaySeconds < 60) { refreshDelaySeconds = 60 }
        } else {
            def nowMillis = now()
            def expiryMillis = atomicState.tokenExpiresAt
            if (expiryMillis > nowMillis) {
                def remainingMillis = expiryMillis - nowMillis
                refreshDelaySeconds = (remainingMillis / 1000 * 0.8).toInteger()
                if (refreshDelaySeconds < 60) { refreshDelaySeconds = 60 }
            } else {
                logWarn 'Token already expired, attempting immediate refresh.'
                refreshToken()
                return
            }
        }

        logDebug "Scheduling token refresh in ${refreshDelaySeconds} seconds."
        runIn(refreshDelaySeconds, refreshToken, [overwrite: true])
    }
}

private void scheduleDeviceRefresh() {
    unschedule('refreshAllVehicleData')
    String interval = settings.refreshIntervalMinutes ?: '10'
    int childDeviceCount = getChildDevices()?.size() ?: 0
    if (childDeviceCount > 0) {
        logInfo "Scheduling vehicle data refresh every ${interval} minutes."
        schedule("0 0/${interval} * ? * *", refreshAllVehicleData)
    } else {
        logInfo 'Vehicle data refresh disabled (interval <= 0 or no vehicles).'
    }
}

private Map getVehicleData(String vin, List<String> endpoints = null) {
    logDebug "App requested data for VIN ${vin}, endpoints: ${endpoints ?: 'all'}"
    if (!atomicState.accessToken) {
        logWarn "API call attempted without access token for VIN ${vin}"
        refreshToken()
        return [error: 'Not authenticated']
    }
    if (now() >= atomicState.tokenExpiresAt) {
        logWarn "API call attempted with expired token for VIN ${vin}"
        refreshToken()
        return [error: 'Token expired, attempting refresh']
    }

    Map results = [:]

    def allEndpoints = [
        //Connected Vehicle API
        'availability': "${apiBaseUrl()}/connected-vehicle/v2/vehicles/${vin}/command-accessibility",
        'brakes': "${apiBaseUrl()}/connected-vehicle/v2/vehicles/${vin}/brakes",
        'commands': "${apiBaseUrl()}/connected-vehicle/v2/vehicles/${vin}/commands",
        'diagnostics': "${apiBaseUrl()}/connected-vehicle/v2/vehicles/${vin}/diagnostics",
        'doors': "${apiBaseUrl()}/connected-vehicle/v2/vehicles/${vin}/doors",
        'engineStatus': "${apiBaseUrl()}/connected-vehicle/v2/vehicles/${vin}/engine-status",
        'engineWarnings': "${apiBaseUrl()}/connected-vehicle/v2/vehicles/${vin}/engine",
        'odometer': "${apiBaseUrl()}/connected-vehicle/v2/vehicles/${vin}/odometer",
        'statistics': "${apiBaseUrl()}/connected-vehicle/v2/vehicles/${vin}/statistics",
        'tyres': "${apiBaseUrl()}/connected-vehicle/v2/vehicles/${vin}/tyres",
        'warnings': "${apiBaseUrl()}/connected-vehicle/v2/vehicles/${vin}/warnings",
        'windows': "${apiBaseUrl()}/connected-vehicle/v2/vehicles/${vin}/windows",
        'vehicle': "${apiBaseUrl()}/connected-vehicle/v2/vehicles/${vin}",
        // Location API
        'location': "${apiBaseUrl()}/location/v1/vehicles/${vin}/location",
    ]

    def child = getChildDevice(vin)
    def fuelType = child?.currentValue('fuelType') ?: 'UNKNOWN'
    if (fuelType == 'PETROL/ELECTRIC' || fuelType == 'ELECTRIC' || fuelType == 'NONE') {
        allEndpoints['rechargeStatus'] = "${apiBaseUrl()}/energy/v1/vehicles/${vin}/recharge-status"
    } else if (fuelType == 'PETROL/ELECTRIC' || fuelType == 'DIESEL' || fuelType == 'PETROL') {
        allEndpoints['fuel'] = "${apiBaseUrl()}/connected-vehicle/v2/vehicles/${vin}/fuel"
    }

    def endpointsToFetch = endpoints ?: allEndpoints.keySet()

    endpointsToFetch.each { endpointKey ->
        if (allEndpoints[endpointKey]) {
            String url = allEndpoints[endpointKey]
            try {
                httpGet([
                        uri: url,
                        headers: apiHeaders(),
                        textParser: true]) // need to use text parser to work around the energy endpoint
                { resp ->
                    if (resp.status == 200) {
                        String responseBodyString = resp.data.text
                        logDebug "Data for ${endpointKey} (${vin}): ${responseBodyString}"
                        def jsonSlurper = new JsonSlurper()
                        def parsedJson = jsonSlurper.parseText(responseBodyString)
                        results[endpointKey] = parsedJson
                    } else {
                        logWarn "Failed to get data for ${endpointKey} (${vin}). Status: ${resp.status}"
                        results[endpointKey] = [error: "API Error ${resp.status}"]
                    }
                }
                pauseExecution(200)
             } catch (e) {
                logError "Exception getting data for ${endpointKey} (${vin}): ${e.message}"
                results[endpointKey] = [error: "Exception: ${e.message}"]
            }
        } else {
            logWarn "Unknown data endpoint requested: ${endpointKey}"
        }
    }

    return results
}

private Map getVehicles() {
    def url = "${apiBaseUrl()}/connected-vehicle/v2/vehicles"

    try {
        httpGet([uri: url, headers: apiHeaders(), contentType: 'application/json']) { resp ->
            if (resp.status == 200) {
                return resp.data
            }

            logWarn "Failed to get data for ${endpointKey} (${vin}). Status: ${resp.status}"
            results[endpointKey] = [error: "API Error ${resp.status}"]
        }
    } catch (e) {
        logError "Exception getting data for ${url}: ${e.message}"
    }
}

void refreshAllVehicleData() {
    logDebug 'Executing scheduled refresh for all vehicles'
    getChildDevices()?.each { child ->
        refreshVehicleData([vin: child.deviceNetworkId])
        pauseExecution(1000)
    }
}

private Map apiHeaders(boolean includeAuth = true, boolean includeStandardAuth = false) {
    def headers = [
        'User-Agent': 'vca-android/123',
        'vcc-api-key': settings.vccApiKey
    ]
    if (includeAuth && atomicState.accessToken) {
        headers['Authorization'] = "Bearer ${atomicState.accessToken}"
    } else if (includeStandardAuth) {
        headers['Authorization'] = ('Basic '
         + 'aDRZZjBiOlU4WWtTYlZsNnh3c2c1WVFxWmZyZ1ZtSWFEcGhP'
         + 'c3kxUENhVXNpY1F0bzNUUjVrd2FKc2U0QVpkZ2ZJZmNMeXc=')
    }
    return headers
}

private String formatExpiry(Long expiryTimestamp) {
    if (!expiryTimestamp) { return 'N/A' }
    try {
        def df = new java.text.SimpleDateFormat('yyyy-MM-dd HH:mm:ss z')
        df.setTimeZone(location.timeZone ?: TimeZone.getDefault())
        return df.format(new Date(expiryTimestamp))
    } catch (e) {
        return new Date(expiryTimestamp).toString()
    }
}

private void logInfo(String msg) { log.info "${appName()}: ${msg}" }
private void logDebug(String msg) { if (settings.logEnable) { log.debug "${appName() }: ${msg }" } }
private void logWarn(String msg) { log.warn "${appName()}: ${msg}" }
private void logError(String msg) { log.error "${appName()}: ${msg}" }
