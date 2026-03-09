package se.apiva.chatserver.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import se.apiva.chatserver.controllers.dao.ApiResponse;
import se.apiva.chatserver.controllers.dao.LoginRequest;
import se.apiva.chatserver.daos.UserDAO;
import se.apiva.chatserver.models.User;
import se.apiva.chatserver.utils.JwtUtils;
import se.apiva.chatserver.utils.RequestUtils;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@WebServlet("/api/login")
public class LoginController extends HttpServlet {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if(!RequestUtils.isContentTypeJson(req, resp)) return;

        try {
            // Convert Body in JSON to LoginRequest-object
            LoginRequest loginRequest = objectMapper.readValue(req.getReader(), LoginRequest.class);
            // Load User from database
            User user = new UserDAO().getUserByUsername(loginRequest.getUsername());

            // Handle login
            if (user != null && loginRequest.getPassword() != null) {
                if (user.getPassword().equals(loginRequest.getPassword())) {

                    // User has provided valid Username and Password - generate JWT-token
                    JwtUtils jwt = new JwtUtils(
                            "MY_LONG_AND_SECRET_TOKEN_AT_LEAST_192BIT_LONG_TO_BE_SAFE!",
                            "http://apiva.se",
                            "message-api"
                    );
                    String jwtToken = jwt.createToken(user.getUsername(), null);

                    RequestUtils.sendApiResponse(
                            req,
                            resp,
                            HttpServletResponse.SC_OK,
                            ApiResponse.Status.SUCCESS,
                            jwtToken
                    );
                }
            }

            RequestUtils.sendApiResponse(
                    req,
                    resp,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.Status.UNAUTHORIZED,
                    "Invalid Username or Password"
            );

        } catch (Exception e) {
            RequestUtils.sendApiResponse(
                    req,
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.Status.ERROR,
                    "Invalid JSON"
            );
        }
    }
}
