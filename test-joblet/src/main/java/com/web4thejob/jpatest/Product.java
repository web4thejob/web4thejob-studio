package com.web4thejob.jpatest;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by Veniamin on 17/4/2014.
 */

@Entity
public class Product {
    @Id
    private long id;
    private String LastName;
    private String FirstName;
    private Date Birthdate;


}
