package yangTalkback;

/**
 * Created by Administrator on 2017/12/19.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import java.util.Timer;
import java.util.TimerTask;



public class MediaButtonReceiver extends BroadcastReceiver {

    private Timer timer = null;
    private HeadSetUtil.OnHeadSetListener headSetListener = null;
    private static MTask myTimer = null;

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
            KeyEvent keyEvent = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT); //获得KeyEvent对象
            if(headSetListener != null){
                try {
                    if(keyEvent.getAction() == KeyEvent.ACTION_UP){
                        if (clickCount==0) {
                            clickCount++;
                            myTimer = new MTask();
                            timer.schedule(myTimer,1000);
                        }else if (clickCount==1) {
                            clickCount++;
                        }else if (clickCount==2) {
                            clickCount=0;
                            myTimer.cancel();
                            headSetListener.onThreeClick();
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        abortBroadcast();//终止广播(不让别的程序收到此广播，免受干扰)
    }

    class MTask extends TimerTask {
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

    Handler mhHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==1){
                headSetListener.onClick();
            }else if (msg.what==2) {
                headSetListener.onDoubleClick();
            }else if (msg.what==3) {
                headSetListener.onThreeClick();
            }
        }
    };

}