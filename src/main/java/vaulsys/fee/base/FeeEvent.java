package vaulsys.fee.base;

public class FeeEvent {
    private Long code;
    private String name;
    private String description;

    public FeeEvent() {
        this.code = (long) 0;
        this.name = "";
        this.description = "";
    }

    public FeeEvent(Long code, String name) {
        this.code = code;
        this.name = name;
        this.description = name;
    }

    public FeeEvent(Long code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
