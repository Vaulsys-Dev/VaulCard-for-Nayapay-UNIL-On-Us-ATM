package vaulsys.exception.base;

//TASK Task103 : Resalat Sorush Disagreement (new feild in sorush disagreemnt)
// khatahaei ke mikhahim ui handle konad az in exception estefade mikonim
public class SwitchBusinessException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SwitchBusinessException(String message){
		super(message);
	}
}
