package yangTalkback.Act;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;

/**
 * Created by Administrator on 2017/12/12.
 */

public class AqueierUtils {
//    private Context mcontext;
//    public AqueierUtils(Context context){
//        this.mcontext=context;
//    }
    public static  void getAqueierUtils(Context context){
        PowerManager pm = (PowerManager) context .getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.SCREEN_DIM_WAKE_LOCK, "StartupReceiver");//���Ĳ�����LogCat���õ�Tag
        wl.acquire();

        //��Ļ����
        KeyguardManager km= (KeyguardManager) context .getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("StartupReceiver");//������LogCat���õ�Tag
        kl.disableKeyguard();
    }

}
