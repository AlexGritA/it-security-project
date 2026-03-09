package se.apiva.chatserver.daos;

import org.hibernate.query.Query;
import se.apiva.chatserver.models.Message;
import se.apiva.chatserver.models.User;
import se.apiva.chatserver.utils.HibernateUtil;

import java.util.List;
import java.util.Objects;

public class MessageDAO extends GenericDAO<Message, Integer> {
    public MessageDAO() {
        super(Message.class);
    }

    public List<Message> getMessagesFromUser(User user){
        Objects.requireNonNull(user, "userID");

        var session = HibernateUtil.getSessionFactory().openSession();
        Query<Message> query = session.createQuery("from " + Message.class.getSimpleName() + " m where m.from = :user");
        query.setParameter("user", user);
        List<Message> messages = query.getResultList();
        return messages;
    }

    public List<Message> getMessagesToUser(User user){
        Objects.requireNonNull(user, "userID");

        var session = HibernateUtil.getSessionFactory().openSession();
        Query<Message> query = session.createQuery("from " + Message.class.getSimpleName() + " m where m.to = :user");
        query.setParameter("user", user);
        List<Message> messages = query.getResultList();
        return messages;
    }

}
