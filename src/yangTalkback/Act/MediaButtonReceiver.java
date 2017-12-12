package yangTalkback.Act;

import java.util.Timer;
import java.util.TimerTask;

import yangTalkback.Act.HeadSetUtil.OnHeadSetListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

/** 
 * MEDIA_BUTTON����ý�尴���㲥������ 
 * @author JPH 
 * @Date2015-6-9 ����8:35:40 
 */  
public class MediaButtonReceiver extends BroadcastReceiver{  
  
    private Timer timer = null;  
    private OnHeadSetListener headSetListener = null;  
    private static MTask myTimer = null;  
    /**��������**/  
    private static int clickCount;  
    public MediaButtonReceiver(){  
        timer = new Timer(true);  
        this.headSetListener = HeadSetUtil.getInstance().getOnHeadSetListener();  
    }  
    @Override  
    public void onReceive(Context context, Intent intent) {  
        Log.i("ksdinf", "onReceive");  
         String intentAction = intent.getAction() ;  
            if(Intent.ACTION_MEDIA_BUTTON.equals(intentAction)){  
                KeyEvent keyEvent = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT); //���KeyEvent����    
                if(headSetListener != null){  
                    try {  
                        if(keyEvent.getAction() == KeyEvent.ACTION_UP){  
                            if (clickCount==0) {//����  
                                clickCount++;  
                                myTimer = new MTask();  
                                timer.schedule(myTimer,1000);  
                            }else if (clickCount==1) {//˫��  
                                clickCount++;  
                            }else if (clickCount==2) {//������  
                                clickCount=0;  
                                myTimer.cancel();  
                                headSetListener.onThreeClick();  
                            }  
                        }  
                    } catch (Exception e) {  
                    }  
                }     
            }  
            abortBroadcast();//��ֹ�㲥(���ñ�ĳ����յ��˹㲥�����ܸ���)    
    }  
    /** 
     * ��ʱ���������ӳ�1�룬�ж��Ƿ�ᷢ��˫���������� 
     */  
    class MTask extends TimerTask{  
            @Override  
            public void run() {  
                try {  
                    if (clickCount==1) {  
                        mhHandler.sendEmptyMessage(1);  
                    }else if (clickCount==2) {  
                        mhHandler.sendEmptyMessage(2);  
                    }  
                    clickCount=0;  
                } catch (Exception e) {  
                    // TODO: handle exception  
                }  
            }  
    };  
    /** 
     * ��handle��Ŀ����Ҫ��Ϊ�˽��ӿ������߳��д��� 
     * ��Ϊ�˰�ȫ����ѽӿڷŵ����̴߳��� 
     */  
    Handler mhHandler = new Handler(){  
        @Override  
        public void handleMessage(Message msg) {  
            super.handleMessage(msg);  
            if(msg.what==1){//����  
                headSetListener.onClick();  
            }else if (msg.what==2) {//˫��  
                headSetListener.onDoubleClick();  
            }else if (msg.what==3) {//������  
                headSetListener.onThreeClick();  
            }  
        }  
    };  
          
}  