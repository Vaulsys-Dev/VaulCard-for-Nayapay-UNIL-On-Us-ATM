package vaulsys.contact;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class EMail implements Serializable {

    private String emailAddress;

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

}
