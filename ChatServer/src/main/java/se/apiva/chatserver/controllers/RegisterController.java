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

@WebServlet("/api/register")
public class RegisterController extends HttpServlet {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if(!RequestUtils.isContentTypeJson(req, resp)) return;

        try {
            // Convert Body in JSON to LoginRequest-object
            LoginRequest loginRequest = objectMapper.readValue(req.getReader(), LoginRequest.class);

            // Validate user input
            if(!validateRequest(req, resp, loginRequest)) return;

            // Save the new User to the database
            User user = new User();
            user.setUsername(loginRequest.getUsername());
            user.setPassword(loginRequest.getPassword());
            new UserDAO().save(user);

            RequestUtils.sendApiResponse(
                    req,
                    resp,
                    HttpServletResponse.SC_OK,
                    ApiResponse.Status.SUCCESS,
                    "User created"
            );
        } catch (Exception e) {
            RequestUtils.sendApiResponse(
                    req,
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.Status.ERROR,
                    "User could not be created"
            );
        }

    }

    private Boolean validateRequest(HttpServletRequest req, HttpServletResponse resp, LoginRequest loginRequest) {
        String error = validateRequest(loginRequest);
        if(error != null) {
            RequestUtils.sendApiResponse(
                    req,
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.Status.ERROR,
                    error
            );
            return false;
        }
        return true;
    }

    private String validateRequest(LoginRequest loginRequest){
        if (loginRequest.getUsername() == null || loginRequest.getUsername().length() < 3){
            return "Username is too short";
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().length() < 8){
            return "Password is too short";
        }

        User user = new UserDAO().getUserByUsername(loginRequest.getUsername());
        if(user != null){
            return "User already exists";
        }

        return null;
    }
}
