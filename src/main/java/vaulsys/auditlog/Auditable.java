package vaulsys.auditlog;

import java.io.Serializable;
import java.util.List;

public interface Auditable<T extends Number> extends Serializable{
	
	public List<AuditableProperty> getAuditableFields();
	
	public T getId();
	
}
