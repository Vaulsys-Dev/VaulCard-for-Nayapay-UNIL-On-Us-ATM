package vaulsys.terminal.atm.filetransfer;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.user.User;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "term_atm_file")
public class FileData implements IEntity<Long> {
	@Id
    @GeneratedValue(generator="term-atm-file-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "term-atm-file-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "term_atm_file_seq") })	
	private Long id;
	
	private String filename;
	
	@Lob
	@Column(name="file_data")
	private byte[] file;

	private String description;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_user")
	@ForeignKey(name = "term_atm_cuser_fk")
	private User creatorUser;
	
	@AttributeOverrides({
		@AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
		@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))})
	private DateTime createdDateTime;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public byte[] getFile() {
		return file;
	}
	public void setFile(byte[] file) {
		this.file = file;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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
		FileData other = (FileData) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public String toString(){
		return String.format("%s (%s)", id, filename);
	}
}
