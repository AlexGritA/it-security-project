package se.apiva.chatserver.controllers.dao;

import java.util.ArrayList;
import java.util.List;

public class UserResponse {

    private List<User> users = new ArrayList<>();

    public UserResponse() { }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public void addUser(Integer id, String username){
        users.add(
                new User(
                        id,
                        username
                )
        );
    }

    public class User {
        private Integer id;
        private String username;

        public User() { }

        public User(Integer id, String username) {
            this.id = id;
            this.username = username;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", username='" + username + '\'' +
                    '}';
        }
    }

}
