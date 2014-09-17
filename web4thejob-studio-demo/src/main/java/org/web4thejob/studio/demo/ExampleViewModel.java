package org.web4thejob.studio.demo;

import com.web4thejob.jpatest.Customer;
import org.web4thejob.studio.support.JpaUtil;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

import static org.zkoss.lang.Generics.cast;

public class ExampleViewModel {
    private List<Customer> customerList;
    private int pointer = 0;

    @Init
    public void init() {
        EntityManager em = JpaUtil.getEntityManagerFactory("MyJoblet").createEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Customer> cq = cb.createQuery(Customer.class);
        cq.from(Customer.class);
        Query query = em.createQuery(cq);
        customerList = cast(query.getResultList());
    }

    public Customer getCurrentCustomer() {
        return customerList.get(pointer);
    }

    public String getCurrentIndex() {
        return pointer + 1 + " out of " + customerList.size();
    }

    @Command
    @NotifyChange(".")
    public void next() {
        if (pointer < customerList.size())
            pointer++;
    }

    @Command
    @NotifyChange(".")
    public void prev() {
        if (pointer > 0)
            pointer--;
    }

    public boolean isFirst() {
        return pointer == 0;
    }

    public boolean isLast() {
        return pointer + 1 == customerList.size();
    }

}

