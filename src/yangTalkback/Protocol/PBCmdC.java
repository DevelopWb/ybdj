package yangTalkback.Protocol;

public class PBCmdC extends PBodyJSON {
	public String Cmd;
	public String JSON;

	public PBCmdC() {

	}

	public PBCmdC(String cmd, String json) {
		Cmd = cmd;
		JSON = json;
	}

	public PBCmdC(Short from, String cmd, String json) {
		From = from;
		Cmd = cmd;
		JSON = json;
	}

	// TALK_Req ����Խ�
	// TALK_Leave �뿪�Խ�
	// TALK_Enter ����Խ�
	// TALK_Invite ����Խ�

	/** TALK_Req ����Խ� */
	public final static String CMD_Type_TALK_Req = "TALK_Req";

	/** TALK_Leave �뿪�Խ� */
	public final static String CMD_Type_TALK_Leave = "TALK_Leave";

	/** TALK_Enter ����Խ� */
	public final static String CMD_Type_TALK_Enter = "TALK_Enter";

	/** TALK_Invite ����Խ� */
	public final static String CMD_Type_TALK_Invite = "TALK_Invite";

	/** TALK_Info �Խ���Ϣ */
	public final static String CMD_Type_TALK_Info = "TALK_Info";
	
	/** TALK_MyChannel �ҵĶԽ���Ϣ */
	public final static String CMD_Type_TALK_MyChannel = "TALK_MyChannel";
	
	
	/** SetMode ���öԽ�ģʽ trueΪ0Ϊ������1Ϊ˫�� */
	public final static String CMD_Type_TALK_SetMode = "TALK_SetMode";
	 
	public final static String CMD_Type_RECORD_List = "RECORD_List";
	public final static String CMD_Type_RECORD_Get = "RECORD_Get";
	
}