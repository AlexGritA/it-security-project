package se.apiva.chatserver.controllers;

import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import se.apiva.chatserver.controllers.dao.ApiResponse;
import se.apiva.chatserver.controllers.dao.MessageRequest;
import se.apiva.chatserver.controllers.dao.MessageResponse;
import se.apiva.chatserver.daos.MessageDAO;
import se.apiva.chatserver.daos.UserDAO;
import se.apiva.chatserver.models.Message;
import se.apiva.chatserver.models.User;
import se.apiva.chatserver.utils.JwtUtils;
import se.apiva.chatserver.utils.RequestUtils;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/message")
public class MessageController extends HttpServlet {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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

        // Fetch data from database and send response to user
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(resp.getWriter(), createMessageResponseForUser(username));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if(!RequestUtils.isContentTypeJson(req, resp)) return;

        try {
            // Convert Request Body from JSON to Object
            MessageRequest messageRequest = objectMapper.readValue(req.getReader(), MessageRequest.class);
            // Validate Request
            if(!validateRequest(req, resp, messageRequest)) return;

            // Store message to database
            UserDAO userDAO = new UserDAO();
            Message message = new Message(
                    userDAO.findById(messageRequest.getFrom()),
                    userDAO.findById(messageRequest.getTo()),
                    messageRequest.getMessage()
            );
            new MessageDAO().save(message);

            // Send response message to caller
            RequestUtils.sendApiResponse(
                    req,
                    resp,
                    HttpServletResponse.SC_OK,
                    ApiResponse.Status.SUCCESS,
                    "Message created"
            );

        } catch (Exception e) {
            RequestUtils.sendApiResponse(
                    req,
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.Status.ERROR,
                    "Message could not be created"
            );
        }

    }

    private Boolean validateRequest(HttpServletRequest req, HttpServletResponse resp, MessageRequest messageRequest) {
        String error = validateRequest(messageRequest);
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

    private String validateRequest(MessageRequest messageRequest){

        if(messageRequest.getTo() == null) {
            return "Invalid receiving user id";
        }

        UserDAO userDAO = new UserDAO();
        User user = userDAO.findById(messageRequest.getTo());
        if(user == null) {
            return "Unknown receiving user";
        }

        if(messageRequest.getFrom() == null) {
            return "Invalid sending user id";
        }

        user = userDAO.findById(messageRequest.getFrom());
        if(user == null) {
            return "Unknown sending user";
        }

        if(messageRequest.getMessage() == null || messageRequest.getMessage().isEmpty()) {
            return "Not allowed to send empty message";
        }

        return null;
    }

    private MessageResponse createMessageResponseForUser(String username){

        User thisUser = new UserDAO().getUserByUsername(username);
        List<Message> messages = new MessageDAO().getMessagesToUser(thisUser);

        // Convert from internal model to public message - this is to hide secret data
        MessageResponse messageResponse = new MessageResponse();
        for(Message message : messages){
            messageResponse.addMessage(
                      message.getTo().getId(),
                      message.getFrom().getId(),
                      message.getTo().getUsername(),
                      message.getFrom().getUsername(),
                      message.getMessage()
            );
        }

        return messageResponse;
    }

}
