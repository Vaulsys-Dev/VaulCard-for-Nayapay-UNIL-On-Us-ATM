package vaulsys.terminal.atm.filetransfer;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.user.User;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "term_atm_ftrnsfr")
public class ATMFileTransferLog implements IEntity<Long> {
    @Id
    @GeneratedValue(generator = "term-atm-ftrnsfr-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "term-atm-ftrnsfr-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "term_atm_ftrnsfr_seq")})
    private Long id;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "term_atm_ftrnsfr_file",
            joinColumns = {@JoinColumn(name = "trnsfr")},
            inverseJoinColumns = {@JoinColumn(name = "file_data")})
    @ForeignKey(name = "atm_ftrnsfr_file_fk", inverseName = "term_atm_ftrnsfr_file_fdata_fk")
    private List<FileData> fileDatas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user")
    @ForeignKey(name = "atm_ftrnsfr_creator_user_fk")
    private User creatorUser;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))})
    private DateTime createdDateTime;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "term_atm_ftrnsfr_atm",
            joinColumns = {@JoinColumn(name = "trnsfr")},
            inverseJoinColumns = {@JoinColumn(name = "atm")})
    @ForeignKey(name = "term_atm_ftrnsfr_atm_trnsfr_fk", inverseName = "term_atm_ftrnsfr_atm_atm_fk")
    private List<ATMTerminal> terminals;

    @Column(name = "sucsful")
    private Boolean successful;

    /*@Column(name = "dscrp", length = 4000)
     private String description;*/

    @Column(name = "new_dscrp")
    @Lob
    private byte[] description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<FileData> getFileDatas() {
        return fileDatas;
    }

    public void setFileDatas(List<FileData> fileDatas) {
        this.fileDatas = fileDatas;
    }

    public User getCreatorUser() {
        return creatorUser;
    }

    public void setCreatorUser(User creatorUser) {
        this.creatorUser = creatorUser;
    }

    public DateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(DateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public List<ATMTerminal> getTerminals() {
        return terminals;
    }

    public void setTerminals(List<ATMTerminal> terminals) {
        this.terminals = terminals;
    }

    public Boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    public byte[] getDescription() {
        return description;
    }

    public void setDescription(byte[] description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ATMFileTransferLog other = (ATMFileTransferLog) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public String toString() {
        return String.valueOf(id);
    }
}
