package se.apiva.chatserver.controllers;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

@WebServlet("/api/login")
public class LoginController extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(LoginController.class);

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

        if (!RequestUtils.isContentTypeJson(req, resp)) return;

        try {
            // Convert Body in JSON to LoginRequest-object
            LoginRequest loginRequest = objectMapper.readValue(req.getReader(), LoginRequest.class);
            // Load User from database
            User user = new UserDAO().getUserByUsername(loginRequest.getUsername());

            // Handle login
            if (user != null && loginRequest.getPassword() != null) {
                //Verify the provided password against the stored BCrypt hash
                if (BCrypt.checkpw(loginRequest.getPassword(), user.getPassword())) {

                    //Logger info for successful login attempt
                    logger.info("Successful login for user: " + loginRequest.getUsername());

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
                    return;
                }
            }
            //Logger warning for failed login attempt
            logger.warn("Failed login attempt for username: " + loginRequest.getUsername());
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
