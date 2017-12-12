package yangTalkback.Codec;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.Semaphore;

import AXLib.Utility.IDisposable;
import AXLib.Utility.Queue;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.ThreadEx;
import Tools.*;
import yangTalkback.Codec.Cfg.VideoEncodeCfg;
import yangTalkback.Media.MediaFrame;

import android.annotation.SuppressLint;
import android.media.MediaRecorder;
import android.os.Environment;

//����ͷ�ɼ�������
@SuppressLint("NewApi")
public class CameraEncoder extends CameraEncoderBase implements MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener, IDisposable {
	// ����Ĳ����ļ�
	private final String TESTFILE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/axvchat_test.mp4";
	private static int _openCount = 0;// �򿪴���
	private Date _lastStartTime;// ���һ������ʱ��
	private int _restartSpan = 10;// ���ü������
	private boolean _needRestart = false;// �Ƿ���Ҫ����
	private boolean _restarting = false;// �Ƿ�����������
	private boolean _isworking = false;// ��ǰ�Ƿ��ڲɼ���
	private MediaRecorder mediaRec = null;// �ɼ���
	private InputStream fisVideoRead = null;// �ɼ����ݶ�ȡ��
	private DataInputStream disVideoRead = null;// �ɼ����ݶ�ȡ��
	private LSS lss = null;// �ɼ����ݶ�ȡ��
	private Tools.MP4Config mp4Config;// �������
	private Semaphore lock = new Semaphore(0);// ͬ���ź���
	private boolean mMediaRecRecording;// ��ǰ�Ƿ�����¼����
	private Thread receThread = null;// �ɼ����ݽ����߳�
	private Thread _pushThread = null;// �ɼ����������߳�
	private int _frameIndex = 0;// ֡��
	private boolean _pushMode = false;// ����ģʽ
	protected CameraEncoderDataReceiver receiver = null;// �ɼ����ݽ�����
	private byte[] h264head = new byte[] { 0, 0, 0, 1 };// H264 ֡�ָ�
	private byte[] spspps = null;// H264 SPS PPS
	private byte[] h263head = new byte[] { 0, 0, 80 };// H263֡ͷû���õ�
	private final static int h263FrameMaxSize = 1024 * 64;// H263֡��С,û���õ�
	private Queue<MediaFrame> _pushQueue = new Queue<MediaFrame>();// ֡���Ͷ���
	public boolean Stoped = false;// �Ƿ��Ѿ�ֹͣ

	public CameraEncoder(VideoEncodeCfg cfg, int restartSpan, CameraEncoderDataReceiver receiver) {
		encCfg = cfg;
		this.receiver = receiver;
		_restartSpan = restartSpan;
		_needRestart = restartSpan > 0;

		_pushMode = android.os.Build.VERSION.SDK_INT > 15;
	}

	public void start() {
		 

	}

	public void stop() {
		if (!_isworking)
			return;
		_isworking = false;

		try {
			releaseMediaRecorder();
		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			RuntimeExceptionEx.PrintException(e);
		}
		try {
			mMediaRecRecording = false;
			ThreadEx.stop(receThread);
			ThreadEx.stop(_pushThread);
			receThread = null;
		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			RuntimeExceptionEx.PrintException(e);
		}

		if (lss != null)
			lss.Dispose();
		lss = null;
		Stoped = true;
	}

	// ��ʼ��,��ISTESE=TRUEʱ,Ҫ�����Բ�����Ҫ��Ϊ�˻�ȡSPS PPS
	private boolean initMediaRec(boolean isTest) throws Exception {
		 
		return true;
	}

	private void releaseMediaRecorder() {
		releaseMediaRecorder(0);
	}

	// ���¸����豸����ʵ�ֵĽӿڷ�ʽ�кܶ಻ͬ�����������˺ܶ������
	private void releaseMediaRecorder(int sleep) {
		 
	}

	// ��ȡ�ɼ������߳�
	public void ReceiveThread() throws Exception {
		 
	}

	// �����߳�
	public void PushThread() {

		 
	}

	// ��Ҫ�Ż�
	private MediaFrame DecodeH264(byte[] data) throws Exception {
		return null;
	}

	private void Skipmdat(DataInputStream dis) throws IOException {
		 
	}

	protected void onEncoded(MediaFrame mf) {
		 
	}

	// ���òɼ���
	public void Restart() {
		 

	}

	// ����
	public void AutoFocus() {
		if (camera != null) {
			camera.autoFocus(null);
		}
	}

	@Override
	public void Dispose() {
		if (lss != null)
			lss.Dispose();
	}

	// �ɼ����ݽ�����
	public static interface CameraEncoderDataReceiver {
		void Received(MediaFrame frame);
	}

}
