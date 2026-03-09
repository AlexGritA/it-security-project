package se.apiva.chatserver.daos;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import se.apiva.chatserver.utils.HibernateUtil;
import java.io.Serializable;
import java.util.List;

public abstract class GenericDAO<T, ID extends Serializable> {

    private final Class<T> persistentClass;

    public GenericDAO(Class<T> persistentClass) {
        this.persistentClass = persistentClass;
    }

    public T findById(ID id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        return session.find(persistentClass, id);
    }

    public List<T> findAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Query<T> query = session.createQuery("from " + persistentClass.getSimpleName());
        return query.getResultList();
    }

    public void save(T entity) {
        Transaction tx = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.persist(entity);
            tx.commit();
        } catch (Throwable ex) {
            if(tx != null) tx.rollback();
            throw ex;
        }
    }

    public void update(T entity) {
        Transaction tx = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.merge(entity);
            tx.commit();
        } catch (Throwable ex) {
            if(tx != null) tx.rollback();
            throw ex;
        }
    }

    public void delete(T entity) {
        Transaction tx = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.remove(entity);
            tx.commit();
        } catch (Throwable ex) {
            if(tx != null) tx.rollback();
            throw ex;
        }
    }

}
