package org.mangorage.mangobot.website.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

public final class WebUtil {
    private static final String TOKEN_NAME = "user_token";

    public static String getOrCreateUserToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (TOKEN_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return setVisitorIdCookie(response, STR."\{UUID.randomUUID()}"); // No visitor ID cookie found
    }

    public static String setVisitorIdCookie(HttpServletResponse response, String visitorId) {
        Cookie cookie = new Cookie(TOKEN_NAME, visitorId);
        cookie.setMaxAge(60 * 60 * 24 * 365); // 1 year expiration
        cookie.setPath("/"); // Cookie is available to the entire site
        response.addCookie(cookie);
        return visitorId;
    }
}
