package se.apiva.chatserver.daos;

import org.hibernate.query.Query;
import se.apiva.chatserver.models.User;
import se.apiva.chatserver.utils.HibernateUtil;

import java.util.List;

public class UserDAO extends GenericDAO<User, Integer>{
    public UserDAO() {
        super(User.class);
    }

    public User getUserByUsername(String username){

        if(username == null || username.isEmpty()){
            return null;
        }

        var session = HibernateUtil.getSessionFactory().openSession();
        String hql = "from " + User.class.getSimpleName();
        hql += " where username = \"" + username + "\"";

        //Use parameterized query to prevent SQL injection
        Query<User> query = session.createQuery("from " + User.class.getSimpleName() + " where username = :username");
        query.setParameter("username", username);
        List<User> users = query.getResultList();

        if(users.isEmpty()){
            return null;
        }

        return users.getFirst();
    }

    public List<User> searchUserByUsername(String search){

        if(search == null || search.isEmpty()){
            return null;
        }

        var session = HibernateUtil.getSessionFactory().openSession();
        Query<User> query = session.createQuery("from " + User.class.getSimpleName() + " u where u.username like :search");
        query.setParameter("search", "%"+search+"%");
        return query.getResultList();
    }

}
