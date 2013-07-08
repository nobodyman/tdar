package org.tdar.core.service;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;

/*
 * This service attempts to centralize and support the creation of URL strings from within the java app. It's centralized here
 * to help manage changes, and because we can get the basename from the configuration class
 */
@Service
public class UrlService {

    public static final String TDAR_NAMESPACE_URL = "http://www.tdar.org/namespace";
    public static final String TDAR_NAMESPACE_PREFIX = "tdar";

    private String baseUrl;

    public String getBaseUrl() {
        if (baseUrl == null) {
            baseUrl = StringUtils.stripEnd(TdarConfiguration.getInstance().getBaseUrl().trim(), "/");
        }
        return baseUrl;
    }

    public String absoluteUrl(Addressable resource) {
        return String.format("%s%s", StringUtils.stripEnd(getBaseUrl(), "/"), relativeUrl(resource));
    }

    public static String reformatViewUrl(String url) {
        if (url.matches("(.+)/view\\?id=([0-9]+)&?$")) {
            url = url.replaceFirst("(.+)/view\\?id=(\\d+)", "$1/$2");
        } else if (url.matches("(.+)/view\\?id=([0-9]+)&(.+)$")) {
            url = url.replaceFirst("(.+)/view\\?id=(\\d+)&(.+)", "$1/$2?$3");
        } else if (url.matches("(.+)/creators\\?id=([0-9]+)&?$")) {
            url = url.replaceFirst("(.+)/creators\\?id=(\\d+)", "$1/creators/$2");
        } else if (url.matches("(.+)/creators\\?id=([0-9]+)&(.+)$")) {
            url = url.replaceFirst("(.+/creators)\\?id=(\\d+)&(.+)", "$1/$2?$3");
        }

        return url;
    }
    
    public String relativeUrl(Addressable resource) {
        return String.format("/%s/%s", resource.getUrlNamespace(), resource.getId());
    }

    public String absoluteUrl(String namespace, Long id) {
        return String.format("%s/%s/%s", StringUtils.stripEnd(getBaseUrl(), "/"), namespace, id);
    }

    public String downloadUrl(InformationResourceFileVersion version) {
        return String.format("%s/filestore/%d/get", StringUtils.stripEnd(getBaseUrl(), "/"), version.getId());
    }

    public String getPairedSchemaUrl() {
        return String.format("%s/schema/current schema.xsd", getBaseUrl());
    }

    public static String getCurrentAbsoluteUrlPath(HttpServletRequest servletRequest) {
        String activePage = "";
        // using getAttribute allows us to get the orginal url out of the page when a forward has taken place.
        String queryString = "?"+servletRequest.getAttribute("javax.servlet.forward.query_string");
        String requestURI = ""+servletRequest.getAttribute("javax.servlet.forward.request_uri");
        if(requestURI == "null") {
            // using getAttribute allows us to get the orginal url out of the page when a include has taken place.
            queryString = "?"+servletRequest.getAttribute("javax.servlet.include.query_string");
            requestURI = ""+servletRequest.getAttribute("javax.servlet.include.request_uri");
        }
        if(requestURI == "null") {
            queryString = "?"+servletRequest.getQueryString();
            requestURI = servletRequest.getRequestURI();
        }
        if(queryString.equals("?null")) queryString = "";
        activePage = requestURI+queryString;
        return activePage;
    }
}
