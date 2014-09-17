package com.web4thejob.jpatest;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String lastName;
    private String firstName;
    private Date birthdate;
    @OneToMany(mappedBy = "customer")
    private List<Product> products;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }


    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
