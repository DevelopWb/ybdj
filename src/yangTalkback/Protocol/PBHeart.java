package yangTalkback.Protocol;

/**
 ������
 
*/
public class PBHeart extends PBodyJSON
{
	//��Ƶͨ��״̬
	public int TalkStatus;
	public int MonitorPublishStatus;
	/**ʵʱ�Խ�״̬*/
	public int TalkbackStatus;
	@Override
	public String toString()
	{
		return super.toString() + "  TalkbackStatus:" + (new Integer(TalkbackStatus)).toString();

	}
}