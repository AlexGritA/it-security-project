package se.apiva.chatserver.controllers;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;
import se.apiva.chatserver.controllers.dao.ApiResponse;
import se.apiva.chatserver.controllers.dao.LoginRequest;
import se.apiva.chatserver.daos.UserDAO;
import se.apiva.chatserver.models.User;
import se.apiva.chatserver.utils.JwtUtils;
import se.apiva.chatserver.utils.RequestUtils;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;

@WebServlet("/api/register")
public class RegisterController extends HttpServlet {

    //Rate limiter - allows 10 requests per minute
    private static final Bucket rateLimiter = Bucket.builder()
            .addLimit(Bandwidth.builder().capacity(10).refillIntervally(10, Duration.ofMinutes(1)).build())
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //Check rate limit before processing request
        if (!rateLimiter.tryConsume(1)) {
            RequestUtils.sendApiResponse(
                    req,
                    resp,
                    429,
                    ApiResponse.Status.ERROR,
                    "Too many requests, please try again later"
            );
            return;
        }

        if(!RequestUtils.isContentTypeJson(req, resp)) return;

        try {
            // Convert Body in JSON to LoginRequest-object
            LoginRequest loginRequest = objectMapper.readValue(req.getReader(), LoginRequest.class);

            // Validate user input
            if(!validateRequest(req, resp, loginRequest)) return;

            // Save the new User to the database
            User user = new User();
            user.setUsername(loginRequest.getUsername());
            //Hash the password using BCrypt before storing in database
            user.setPassword(BCrypt.hashpw(loginRequest.getPassword(), BCrypt.gensalt()));

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
        //Validate password
        String password = loginRequest.getPassword();
        if (password == null || password.length() < 12) {
            return "Password must be at least 12 characters long";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Password must contain at least one number";
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            return "Password must contain at least one special character";
        }

        User user = new UserDAO().getUserByUsername(loginRequest.getUsername());
        if(user != null){
            return "User already exists";
        }

        return null;
    }
}
