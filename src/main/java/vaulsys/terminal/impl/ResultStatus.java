package vaulsys.terminal.impl;

import vaulsys.persistence.IEnum;
import javax.persistence.Embeddable;

@Embeddable
public class ResultStatus implements IEnum {

    int  result ;
    public static ResultStatus Ok = new ResultStatus(1);
    public static ResultStatus Failed = new ResultStatus(2);
    public static ResultStatus Timeout = new ResultStatus(3);
    public static ResultStatus NotFound = new ResultStatus(4);
    public static ResultStatus Security = new ResultStatus(5);

    public ResultStatus() {

    }

    public ResultStatus(int result) {
        this.result = result;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResultStatus that = (ResultStatus) o;

        if (result != that.result) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return result;
    }

    @Override
    public String toString() {
        return "ResultStatus{" +
                "result=" + result +
                '}';
    }



}
