package yangTalkback.Act;

import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

/** 
 * �����߿ع������� ���� 
 * @author JPH 
 * @date 2015-6-9 ����4:03:45 
 */  
public class HeadSetUtil {  
  
    private static HeadSetUtil headSetUtil;  
    private OnHeadSetListener headSetListener = null;  
  
    public static HeadSetUtil getInstance() {  
        if (headSetUtil == null) {  
            headSetUtil = new HeadSetUtil();  
        }  
        return headSetUtil;  
    }  
  
    /** 
     * ���ö�������˫�������ӿ� ������openǰ���ô˽ӿڣ�����������Ч 
     * @param headSetListener 
     */  
    public void setOnHeadSetListener(OnHeadSetListener headSetListener) {  
        this.headSetListener = headSetListener;  
    }  
  
    /** 
     * ΪMEDIA_BUTTON ��ͼע���������ע�Ὺ�������߿ؼ���, ����������ýӿڼ���֮���ٵ��ô˷���������ӿ���Ч�� 
     * @param context 
     */  
    public void open(Context context) {  
        if(headSetListener==null){  
            throw new IllegalStateException("please set headSetListener");  
        }  
        AudioManager audioManager = (AudioManager) context  
                .getSystemService(Context.AUDIO_SERVICE);  
        ComponentName name = new ComponentName(context.getPackageName(),  
                MediaButtonReceiver.class.getName());  
        audioManager.registerMediaButtonEventReceiver(name);  
        Log.i("ksdinf", "open");  
    }  
    /** 
     * �رն����߿ؼ���  
     * @param context 
     */  
    public void close(Context context) {  
        AudioManager audioManager = (AudioManager) context  
                .getSystemService(Context.AUDIO_SERVICE);  
        ComponentName name = new ComponentName(context.getPackageName(),  
                MediaButtonReceiver.class.getName());  
        audioManager.unregisterMediaButtonEventReceiver(name);  
    }  
    /** 
     * ɾ����������˫�������ӿ� 
     */  
    public void delHeadSetListener() {  
        this.headSetListener = null;  
    }  
  
    /** 
     * ��ȡ��������˫���ӿ� 
     *  
     * @return 
     */  
    protected OnHeadSetListener getOnHeadSetListener() {  
        return headSetListener;  
    }  
  
    /** 
     * ������ť��˫������ 
     */  
    public interface OnHeadSetListener {  
        /** 
         * ��������,���̡߳� �˽ӿ������������ڵ�������1��� ��Ϊ��Ҫ�ж�1�����Ƿ��Լ�����������еĻ��Ǿ���˫���� 
         */  
        public void onClick();  
        /** 
         * ˫���������˽ӿ������̣߳����Է���ʹ�� 
         */  
        public void onDoubleClick();  
        /** 
         * ������ 
         */  
        public void onThreeClick();  
    }  
}  
