package com.donation.common.core.utils;

import javax.servlet.http.HttpServletRequest;

public class ServerUtility {
    public static String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();

        return siteURL.replace(request.getServletPath(), "");
    }
}
