package se.apiva.chatserver.controllers;

import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import se.apiva.chatserver.controllers.dao.UserResponse;
import se.apiva.chatserver.daos.UserDAO;
import se.apiva.chatserver.models.User;
import se.apiva.chatserver.utils.JwtUtils;
import se.apiva.chatserver.utils.RequestUtils;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/user")
public class UserController extends HttpServlet {

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String action = req.getParameter("action") != null ? req.getParameter("action") : "";
        String search = req.getParameter("search") != null ? req.getParameter("search") : "";

        // User has provided valid Username and Password - generate JWT-token
        JwtUtils jwt = new JwtUtils(
                "MY_LONG_AND_SECRET_TOKEN_AT_LEAST_192BIT_LONG_TO_BE_SAFE!",
                "http://apiva.se",
                "message-api"
        );

        // Fetch the token from the Http Header
        String jwtToken = RequestUtils.getJwtToken(req, resp);
        if(jwtToken == null){
            RequestUtils.sendUnauthorizedResponse(req, resp);
            return;
        }

        // Validate the token and fetch the username from the Subject
        Claims claims = jwt.verify(jwtToken);
        String username = claims.getSubject();

        try {
            UserDAO userDAO = new UserDAO();
            UserResponse userResponse = new UserResponse();
            if ("SEARCH".equals(action)) {
                // Find users similar to search string
                List<User> users = userDAO.searchUserByUsername(search);
                // Convert data type from Model (where there are secrets like password) to
                // UserResponse that is the public interface without secrets
                for(User user : users){
                    userResponse.addUser(
                            user.getId(),
                            user.getUsername()
                    );
                }
            }

            if ("ME".equals(action) || action.isEmpty()) {
                // Load information for the current user
                User user = userDAO.getUserByUsername(username);
                userResponse.addUser(user.getId(), user.getUsername());
            }

            // Send respons to the caller
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(resp.getWriter(), userResponse);

        } catch (Exception e) {}

    }
}
