package vaulsys.webservices.ghasedak;

import javax.persistence.*;

@Entity
@Table(name = "Credentials")
public class Credentials {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "credentials_seq")
    @SequenceGenerator(name = "credentials_seq", sequenceName ="cred_seq", allocationSize = 1)
    private long id;

    private String username;

    @Column(length = 2000)
    private String password;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
