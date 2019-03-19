/*
    Description: Test(no used).
*/

package com.smart.domain;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity
public class User {
    private int id;
    private String name;
    private String password;
    private String dockerId;
    private String number;
    private Timestamp date;

    @Id
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "name", nullable = true, length = 32)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "password", nullable = true, length = 32)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Basic
    @Column(name = "docker_id", nullable = true, length = 12)
    public String getDockerId() {
        return dockerId;
    }

    public void setDockerId(String dockerId) {
        this.dockerId = dockerId;
    }

    @Basic
    @Column(name = "number", nullable = true, length = 32)
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Basic
    @Column(name = "date", nullable = false)
    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != user.id) return false;
        if (name != null ? !name.equals(user.name) : user.name != null) return false;
        if (password != null ? !password.equals(user.password) : user.password != null) return false;
        if (dockerId != null ? !dockerId.equals(user.dockerId) : user.dockerId != null) return false;
        if (number != null ? !number.equals(user.number) : user.number != null) return false;
        if (date != null ? !date.equals(user.date) : user.date != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (dockerId != null ? dockerId.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }
}
/**
 * @program: reporter
 * @description: for SCNU's work
 * @author: tover elite run
 * @create: 2019-01-21
 **/