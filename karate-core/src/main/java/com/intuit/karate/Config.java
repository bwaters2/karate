/*
 * The MIT License
 *
 * Copyright 2017 Intuit Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.intuit.karate;

import com.intuit.karate.http.HttpClient;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

/**
 *
 * @author pthomas3
 */
public class Config {

    public static final int DEFAULT_RETRY_INTERVAL = 3000;
    public static final int DEFAULT_RETRY_COUNT = 3;    

    private boolean sslEnabled = false;
    private String sslAlgorithm = "TLS";
    private String sslKeyStore;
    private String sslKeyStorePassword;
    private String sslKeyStoreType;
    private String sslTrustStore;
    private String sslTrustStorePassword;
    private String sslTrustStoreType;
    private boolean sslTrustAll = true;
    private boolean followRedirects = true;
    private int readTimeout = 30000;
    private int connectTimeout = 30000;
    private Charset charset = FileUtils.UTF8;
    private String proxyUri;
    private String proxyUsername;
    private String proxyPassword;
    private List<String> nonProxyHosts;
    private List<String>  maskRequestHeaders = new ArrayList<String>();
    private ScriptValue headers = ScriptValue.NULL;
    private ScriptValue cookies = ScriptValue.NULL;
    private ScriptValue responseHeaders = ScriptValue.NULL;
    private boolean lowerCaseResponseHeaders = false;
    private boolean corsEnabled = false;
    private boolean logPrettyRequest;
    private boolean logPrettyResponse;
    private boolean printEnabled = true;
    private boolean outlineVariablesAuto = true;
    private String clientClass;
    private HttpClient clientInstance;
    private Map<String, Object> userDefined;
    private Map<String, Object> driverOptions;
    private ScriptValue afterScenario = ScriptValue.NULL;
    private ScriptValue afterFeature = ScriptValue.NULL;

    // retry config
    private int retryInterval = DEFAULT_RETRY_INTERVAL;
    private int retryCount = DEFAULT_RETRY_COUNT;

    // report config
    private boolean showLog = true;
    private boolean showAllSteps = true;

    public Config() {
        // zero arg constructor
    }
    
    private static <T> T get(Map<String, Object> map, String key, T defaultValue) {
        Object o = map.get(key);
        return o == null ? defaultValue : (T) o;
    }

    public boolean configure(String key, ScriptValue value) { // TODO use enum
        key = StringUtils.trimToEmpty(key);
        switch (key) {
            case "headers":
                headers = value;
                return false;
            case "maskRequestHeaders":
                maskRequestHeaders = value.getAsList();
                return false;
            case "cookies":
                cookies = value;
                return false;
            case "responseHeaders":
                responseHeaders = value;
                return false;
            case "lowerCaseResponseHeaders":
                lowerCaseResponseHeaders = value.isBooleanTrue();
                return false;
            case "cors":
                corsEnabled = value.isBooleanTrue();
                return false;
            case "logPrettyResponse":
                logPrettyResponse = value.isBooleanTrue();
                return false;
            case "logPrettyRequest":
                logPrettyRequest = value.isBooleanTrue();
                return false;
            case "printEnabled":
                printEnabled = value.isBooleanTrue();
                return false;
            case "afterScenario":
                afterScenario = value;
                return false;
            case "afterFeature":
                afterFeature = value;
                return false;
            case "report":
                if (value.isMapLike()) {
                    Map<String, Object> map = value.getAsMap();
                    showLog = get(map, "showLog", showLog);
                    showAllSteps = get(map, "showAllSteps", showAllSteps);
                } else if (value.isBooleanTrue()) {
                    showLog = true;
                    showAllSteps = true;
                } else {
                    showLog = false;
                    showAllSteps = false;
                }
                return false;
            case "driver":
                driverOptions = value.getAsMap();
                return false;
            case "retry":
                if (value.isMapLike()) {
                    Map<String, Object> map = value.getAsMap();
                    retryInterval = get(map, "interval", retryInterval);
                    retryCount = get(map, "count", retryCount);                    
                }
                return false;
            case "outlineVariablesAuto":
                outlineVariablesAuto = value.isBooleanTrue();
                return false;
            // here on the http client has to be re-constructed ================
            case "httpClientClass":
                clientClass = value.getAsString();
                return true;
            case "httpClientInstance":
                clientInstance = value.getValue(HttpClient.class);
                return true;
            case "charset":
                charset = value.isNull() ? null : Charset.forName(value.getAsString());
                return true;
            case "ssl":
                if (value.isString()) {
                    sslEnabled = true;
                    sslAlgorithm = value.getAsString();
                } else if (value.isMapLike()) {
                    sslEnabled = true;
                    Map<String, Object> map = value.getAsMap();
                    sslKeyStore = (String) map.get("keyStore");
                    sslKeyStorePassword = (String) map.get("keyStorePassword");
                    sslKeyStoreType = (String) map.get("keyStoreType");
                    sslTrustStore = (String) map.get("trustStore");
                    sslTrustStorePassword = (String) map.get("trustStorePassword");
                    sslTrustStoreType = (String) map.get("trustStoreType");
                    Boolean trustAll = (Boolean) map.get("trustAll");
                    if (trustAll != null) {
                        sslTrustAll = trustAll;
                    }
                    sslAlgorithm = (String) map.get("algorithm");
                } else {
                    sslEnabled = value.isBooleanTrue();
                }
                return true;
            case "followRedirects":
                followRedirects = value.isBooleanTrue();
                return true;
            case "connectTimeout":
                connectTimeout = value.getAsInt();
                return true;
            case "readTimeout":
                readTimeout = value.getAsInt();
                return true;
            case "proxy":
                if (value == null) {
                    proxyUri = null;
                } else if (value.isString()) {
                    proxyUri = value.getAsString();
                } else {
                    Map<String, Object> map = value.getAsMap();
                    proxyUri = (String) map.get("uri");
                    proxyUsername = (String) map.get("username");
                    proxyPassword = (String) map.get("password");
                    nonProxyHosts = (List) map.get("nonProxyHosts");
                }
                return true;
            case "userDefined":
                userDefined = value.getAsMap();
                return true;
            default:
                throw new RuntimeException("unexpected 'configure' key: '" + key + "'");
        }
    }

    public Config(Config parent) {
        sslEnabled = parent.sslEnabled;
        sslAlgorithm = parent.sslAlgorithm;
        sslTrustStore = parent.sslTrustStore;
        sslTrustStorePassword = parent.sslTrustStorePassword;
        sslTrustStoreType = parent.sslTrustStoreType;
        sslKeyStore = parent.sslKeyStore;
        sslKeyStorePassword = parent.sslKeyStorePassword;
        sslKeyStoreType = parent.sslKeyStoreType;
        sslTrustAll = parent.sslTrustAll;
        followRedirects = parent.followRedirects;
        readTimeout = parent.readTimeout;
        connectTimeout = parent.connectTimeout;
        charset = parent.charset;
        proxyUri = parent.proxyUri;
        proxyUsername = parent.proxyUsername;
        proxyPassword = parent.proxyPassword;
        nonProxyHosts = parent.nonProxyHosts;
        headers = parent.headers;
        cookies = parent.cookies;
        responseHeaders = parent.responseHeaders;
        lowerCaseResponseHeaders = parent.lowerCaseResponseHeaders;
        corsEnabled = parent.corsEnabled;
        logPrettyRequest = parent.logPrettyRequest;
        logPrettyResponse = parent.logPrettyResponse;
        printEnabled = parent.printEnabled;
        clientClass = parent.clientClass;
        clientInstance = parent.clientInstance;
        userDefined = parent.userDefined;
        driverOptions = parent.driverOptions;
        afterScenario = parent.afterScenario;
        afterFeature = parent.afterFeature;
        showLog = parent.showLog;
        showAllSteps = parent.showAllSteps;
        retryInterval = parent.retryInterval;
        retryCount = parent.retryCount;
        outlineVariablesAuto = parent.outlineVariablesAuto;
    }
        
    public void setCookies(ScriptValue cookies) {
        this.cookies = cookies;
    }   
    
    public void setClientClass(String clientClass) {
        this.clientClass = clientClass;
    }    

    //==========================================================================
    //
    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public String getSslAlgorithm() {
        return sslAlgorithm;
    }

    public String getSslKeyStore() {
        return sslKeyStore;
    }

    public String getSslKeyStorePassword() {
        return sslKeyStorePassword;
    }

    public String getSslKeyStoreType() {
        return sslKeyStoreType;
    }

    public String getSslTrustStore() {
        return sslTrustStore;
    }

    public String getSslTrustStorePassword() {
        return sslTrustStorePassword;
    }

    public String getSslTrustStoreType() {
        return sslTrustStoreType;
    }

    public boolean isSslTrustAll() {
        return sslTrustAll;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public Charset getCharset() {
        return charset;
    }

    public String getProxyUri() {
        return proxyUri;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public List<String> getNonProxyHosts() {
        return nonProxyHosts;
    }
    
    public ScriptValue getHeaders() {
        return headers;
    }

    public List<String> getRequestHeadersToMask(){return maskRequestHeaders;}

    public ScriptValue getCookies() {
        return cookies;
    }

    public ScriptValue getResponseHeaders() {
        return responseHeaders;
    }

    public boolean isLowerCaseResponseHeaders() {
        return lowerCaseResponseHeaders;
    }

    public boolean isCorsEnabled() {
        return corsEnabled;
    }

    public boolean isLogPrettyRequest() {
        return logPrettyRequest;
    }

    public boolean isLogPrettyResponse() {
        return logPrettyResponse;
    }

    public boolean isPrintEnabled() {
        return printEnabled;
    }

    public String getClientClass() {
        return clientClass;
    }

    public Map<String, Object> getUserDefined() {
        return userDefined;
    }

    public Map<String, Object> getDriverOptions() {
        return driverOptions;
    }

    public HttpClient getClientInstance() {
        return clientInstance;
    }

    public void setClientInstance(HttpClient clientInstance) {
        this.clientInstance = clientInstance;
    }

    public ScriptValue getAfterScenario() {
        return afterScenario;
    }

    public void setAfterScenario(ScriptValue afterScenario) {
        this.afterScenario = afterScenario;
    }

    public ScriptValue getAfterFeature() {
        return afterFeature;
    }

    public void setAfterFeature(ScriptValue afterFeature) {
        this.afterFeature = afterFeature;
    }

    public boolean isShowLog() {
        return showLog;
    }

    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
    }

    public boolean isShowAllSteps() {
        return showAllSteps;
    }

    public void setShowAllSteps(boolean showAllSteps) {
        this.showAllSteps = showAllSteps;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isOutlineVariablesAuto() {
        return outlineVariablesAuto;
    }        

}
