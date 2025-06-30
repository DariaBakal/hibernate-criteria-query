package ma.hibernate.dao;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ma.hibernate.model.Phone;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class PhoneDaoImpl extends AbstractDao implements PhoneDao {
    public PhoneDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Phone create(Phone phone) {
        Transaction transaction = null;
        try (Session session = factory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(phone);
            transaction.commit();
            return phone;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Can't create phone: " + phone, e);
        }
    }

    @Override
    public List<Phone> findAll(Map<String, String[]> params) {
        try (Session session = factory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Phone> query = cb.createQuery(Phone.class);
            Root<Phone> phoneRoot = query.from(Phone.class);

            List<Predicate> predicates = new ArrayList<>();

            params.entrySet().stream()
                    .filter(entry -> entry.getValue() != null && entry.getValue().length > 0)
                    .forEach(entry -> {
                        String key = entry.getKey();
                        CriteriaBuilder.In<String> predicate = cb.in(phoneRoot.get(key));
                        Arrays.stream(entry.getValue()).forEach(predicate::value);
                        predicates.add(predicate);
                    });

            Optional.of(predicates)
                    .filter(p -> !p.isEmpty())
                    .ifPresent(p -> query.where(cb.and(p.toArray(new Predicate[0]))));

            return session.createQuery(query).getResultList();
        }
    }
}

