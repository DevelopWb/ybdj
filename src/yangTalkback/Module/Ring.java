package yangTalkback.Module;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;

import java.io.IOException;

import yangTalkback.App.App;
import yangTalkback.Act.R;

public class Ring {
	private static MediaPlayer _mPlayer = null;
	private static AudioManager _audioService = null;
	private static Vibrator _vibrator = null;

	public final static int GRing = 0;

	public static void Play() {
		Play(R.raw.duanxin);
	}

	public static void Play(int resId) {
		if (_mPlayer == null) {
			_mPlayer = new MediaPlayer();
			_mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			_mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer player) {
					player.seekTo(0);
				}
			});
			AssetFileDescriptor file = App.Application.getResources().openRawResourceFd(R.raw.duanxin);
			try {
				_mPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
				file.close();
				_mPlayer.setVolume(100, 100);
				_mPlayer.prepare();
			} catch (IOException ioe) {
				_mPlayer = null;
			}
			_audioService = (AudioManager) App.Application.getSystemService(Context.AUDIO_SERVICE);
			_vibrator = (Vibrator) App.Application.getSystemService(Context.VIBRATOR_SERVICE);
		}
		App.LastAct.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		boolean shouldPlayBeep = true;
		if (_audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL)
			shouldPlayBeep = false;

		if (shouldPlayBeep && _mPlayer != null)
			_mPlayer.start();

		// ��һ��
		_vibrator.vibrate(500);
		// ��һ��������ָ��һ���𶯵�Ƶ�����顣ÿ����Ϊһ�飬ÿ��ĵ�һ��Ϊ�ȴ�ʱ�䣬�ڶ���Ϊ��ʱ�䡣
		// ���� [2000,500,100,400],���ȵȴ�2000���룬��500���ٵȴ�100����400
		// �ڶ���������repestָ���� �ڼ�����������һ����������� ��λ�ÿ�ʼѭ���𶯡�
		// ��һֱ����ѭ����������Ҫ�� vibrator.cancel()������ֹ
		// vibrator.vibrate(new long[]{300,500},0);

	}

	// public static void Play(int sound) {
	// Play();
	// }
}
