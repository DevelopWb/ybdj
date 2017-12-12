package yangTalkback.Act;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;

public class VolumeChangeActivity extends Activity {

    /**
     * ��ǰ����
     */
    private int currentVolume;
    /**
     * ���������Ķ���
     */
    public AudioManager mAudioManager;
    /**
     * ϵͳ�������
     */
    private int maxVolume;
    /**
     * ȷ���رճ����ֹͣ�߳�
     */
    private boolean isDestroy;
 
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        isDestroy = false;
        // ���AudioManager����
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);//��������,���Ҫ�������������仯�����ΪAudioManager.STREAM_RING
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }
 
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        isDestroy = true;
    }
 
    /**
     * ���������������߳�
     */
    private Thread volumeChangeThread;
 
    /**
     * �������������仯 ˵���� ��ǰ�����ı�ʱ��������ֵ����Ϊ���ֵ��2
     */
    public void onVolumeChangeListener()
    {
 
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeChangeThread = new Thread()
        {
            public void run()
            {
                while (!isDestroy)
                    {
                        int count = 0;
                        boolean isDerease = false;
                        // ������ʱ����
                        try
                            {
                                Thread.sleep(20);
                            } catch (InterruptedException e)
                            {
                                System.out.println("error in onVolumeChangeListener Thread.sleep(20) " + e.getMessage());
                            }
 
                        if (currentVolume < mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
                            {
                                count++;
                                currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                // ������������ maxVolume-2��ԭ���ǣ�������ֵ�����ֵ����Сֵʱ���������ӻ��û�иı䣬����ÿ�ζ�����Ϊ�̶���ֵ��
                                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume - 2,
                                        AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                            }
                        if (currentVolume > mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
                            {
                                count++;
                                currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume - 2,
                                        AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                if (count == 1)
                                    {
                                        isDerease = true;
                                    }
 
                            }
 
                        if (count == 2)
                            {
                                System.out.println("����������+");
 
                            } else if (isDerease)
                            {
                                System.out.println("����������-");
                            }
 
                    }
            };
        };
        volumeChangeThread.start();
    }
 
}
