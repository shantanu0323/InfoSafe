package com.sp.infosafe;

/**
 * Created by Shantanu on 3/24/2018.
 */

public class User {
    public String name;
    public String age;
    public String bloodgroup;
    public String gender;
    public String phone;
    public String email;
    public String profile_pic;

    public User() {
    }

    public User(String name, String age, String bloodgroup, String gender, String phone, String email, String profile_pic) {
        this.name = name;
        this.age = age;
        this.bloodgroup = bloodgroup;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.profile_pic = profile_pic;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBloodgroup() {
        return bloodgroup;
    }

    public void setBloodgroup(String bloodgroup) {
        this.bloodgroup = bloodgroup;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }
}
