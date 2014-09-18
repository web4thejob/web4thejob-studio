/*
 * Copyright 2014 Veniamin Isaias
 *
 * This file is part of Web4thejob Studio.
 *
 * Web4thejob Studio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Web4thejob Studio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Web4thejob Studio.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.web4thejob.studio.demo;

import com.web4thejob.jpatest.Customer;
import org.zkoss.zk.ui.WebApp;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

import static org.zkoss.lang.Generics.cast;

public class DemoInitializer implements org.zkoss.zk.ui.util.WebAppInit {
    @Override
    public void init(WebApp webApp) throws Exception {
        String name = "MyJoblet";
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(name, null);

        //I don't have a Desktop yet, so I cant't use org.web4thejob.studio.support.JpaUtil.setEntityManagerFactory()
        Map<String, EntityManagerFactory> emfs = cast(webApp.getAttribute("w4tjstudio-emfs"));
        if (emfs == null) {
            emfs = new HashMap<>();
            webApp.setAttribute("w4tjstudio-emfs", emfs);
        }
        emfs.put(name, emf);

        initDb(emf);
    }

    private void initDb(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        Customer customer;

        //courtesy of http://www.generatedata.com/
        customer = new Customer();
        customer.setFirstName("Kieran");
        customer.setLastName("Reid");
        em.persist(customer);

        customer = new Customer();
        customer.setFirstName("Angela");
        customer.setLastName("Bennett");
        em.persist(customer);

        customer = new Customer();
        customer.setFirstName("Rashad");
        customer.setLastName("Pratt");
        em.persist(customer);

        customer = new Customer();
        customer.setFirstName("Sonya");
        customer.setLastName("Gonzales");
        em.persist(customer);

        customer = new Customer();
        customer.setFirstName("Buckminster");
        customer.setLastName("Ewing");
        em.persist(customer);

        em.getTransaction().begin();
        em.flush();
        em.getTransaction().commit();
    }
}
