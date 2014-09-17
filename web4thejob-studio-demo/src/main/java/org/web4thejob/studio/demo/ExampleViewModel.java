package org.web4thejob.studio.demo;

import com.web4thejob.jpatest.Customer;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;

import java.util.ArrayList;
import java.util.List;

public class ExampleViewModel {
    private List<Customer> customerList = new ArrayList<>();
    private int pointer = 0;

    @Init
    public void init() {
        Customer customer;

        customer = new Customer();
        customer.setId(1);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customerList.add(customer);

        customer.setId(2);
        customer = new Customer();
        customer.setFirstName("John***");
        customer.setLastName("Doe***");
        customerList.add(customer);
    }

    public Customer getCurrentCustomer() {
        return customerList.get(pointer);
    }

    public String getCurrentIndex() {
        return pointer + 1 + " out of " + customerList.size();
    }

    @Command
    @NotifyChange({"currentCustomer", "currentIndex"})
    public void next() {
        if (pointer < customerList.size())
            pointer++;
    }

    @Command
    @NotifyChange({"currentCustomer", "currentIndex"})
    public void prev() {
        if (pointer > 0)
            pointer--;
    }
}
