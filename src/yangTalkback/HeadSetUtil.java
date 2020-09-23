package yangTalkback;

/**
 * Created by Administrator on 2017/12/19.
 */

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;


public class HeadSetUtil {

    private static HeadSetUtil headSetUtil;
    private OnHeadSetListener headSetListener = null;

    public static HeadSetUtil getInstance() {
        if (headSetUtil == null) {
            headSetUtil = new HeadSetUtil();
        }
        return headSetUtil;
    }

    public void setOnHeadSetListener(OnHeadSetListener headSetListener) {
        this.headSetListener = headSetListener;
    }



    public void open(Context context) {
        if(headSetListener==null){
            throw new IllegalStateException("please set headSetListener");
        }
        AudioManager audioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        ComponentName name = new ComponentName(context.getPackageName(),
                MediaButtonReceiver.class.getName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            audioManager.registerMediaButtonEventReceiver(name);
        }
        Log.i("ksdinf", "open");
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public void close(Context context) {
        AudioManager audioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        ComponentName name = new ComponentName(context.getPackageName(),
                MediaButtonReceiver.class.getName());
        audioManager.unregisterMediaButtonEventReceiver(name);
    }

    public void delHeadSetListener() {
        this.headSetListener = null;
    }


    protected OnHeadSetListener getOnHeadSetListener() {
        return headSetListener;
    }


    public interface OnHeadSetListener {

        public void onClick();

        public void onDoubleClick();

        public void onThreeClick();
    }
}