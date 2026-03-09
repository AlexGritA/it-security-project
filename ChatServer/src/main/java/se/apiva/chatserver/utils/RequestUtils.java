package se.apiva.chatserver.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import se.apiva.chatserver.controllers.dao.ApiResponse;
import tools.jackson.databind.ObjectMapper;

public class RequestUtils {

    public static Boolean isContentTypeJson(HttpServletRequest req, HttpServletResponse resp) {

        try {

            String contentType = req.getContentType();
            if (contentType != null && contentType.startsWith("application/json")) {
                return true;
            }

            resp.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            resp.getWriter().write("Expected application/json");

        } catch (Exception e) { }

        return false;
    }

    public static void sendApiResponse(
            HttpServletRequest req,
            HttpServletResponse resp,
            int httpStatusCode,
            ApiResponse.Status status,
            String message
    ) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            resp.setStatus(httpStatusCode);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            objectMapper.writeValue(resp.getWriter(),
                    new ApiResponse(status, message));

        } catch (Exception e) { }
    }

    public static void sendUnauthorizedResponse(HttpServletRequest req, HttpServletResponse resp) {
        RequestUtils.sendApiResponse(
                req,
                resp,
                HttpServletResponse.SC_UNAUTHORIZED,
                ApiResponse.Status.UNAUTHORIZED,
                "Unauthorized"
        );
    }

    public static String getJwtToken(HttpServletRequest req, HttpServletResponse resp) {

        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return null;
        }

        String token = auth.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            return null;
        }

        return token;
    }

}
