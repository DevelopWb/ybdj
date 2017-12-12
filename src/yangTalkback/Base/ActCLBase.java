package yangTalkback.Base;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;

import java.io.EOFException;
import java.net.SocketException;

import AXLib.Model.RefObject;
import AXLib.Utility.Ex.StringEx;
import AXLib.Utility.ICallback;
import AXLib.Utility.JSONHelper;
import AXLib.Utility.ListEx;
import AXLib.Utility.Predicate;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.ThreadEx;
import yangTalkback.Act.R;
import yangTalkback.Act.actGoTalkbackActivity;
import yangTalkback.Act.actTalkback;
import yangTalkback.App.App;
import yangTalkback.App.AppConfig;
import yangTalkback.Comm.CLLog;
import yangTalkback.Comm.IDModel;
import yangTalkback.Module.Ring;
import yangTalkback.Net.ClientConnection;
import yangTalkback.Protocol.PBCallC;
import yangTalkback.Protocol.PBCallClosureC;
import yangTalkback.Protocol.PBCallR;
import yangTalkback.Protocol.PBCmdC;
import yangTalkback.Protocol.PBCmdM;
import yangTalkback.Protocol.PBCmdR;
import yangTalkback.Protocol.PBMedia;
import yangTalkback.Protocol.PBMonitorCloseC;
import yangTalkback.Protocol.PBMonitorCloseR;
import yangTalkback.Protocol.PBMonitorOpenC;
import yangTalkback.Protocol.PBMonitorOpenR;

//import AXVChat.Net.Model.CRequestTalk;

//��Ƶ�Խ�������ͼ����
public class ActCLBase extends ActAutoRefView {

	public final static int ActivityResult_Code_Talk = 0x08;
	public final static int TimeoutReconnect = 0x09;

	protected AppConfig _ac = null;
	protected ClientConnection _connection = App.GetConnection();
	protected boolean _cancelRequestTalk = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (_connection != null && _connection.getIsConnected())
			setTitle("ʵʱ�Խ� ID" + _connection.ID);
		else
			setTitle("ʵʱ�Խ�");

		_ac = AppConfig.Instance;
		if (ScreenOrientationIsFit()) {
			onScreenReady();
		}

	}

	// android activity��������
	// http://www.cnblogs.com/over140/archive/2012/04/25/2331185.html
	// onCreate -> onStart -> onResume
	// onPause��->��onStop��->��onDestroy
	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onScreenReady() {
	}

	@Override
	public boolean OnKeyDown_Back() {
		return super.OnKeyDown_Back();
	}

	public void finish(final int wait) {
		if (wait == 0) {
			super.finish();
		} else {
			if (!_isLoading)
				OpenLoading("���ڹر�");
			ThreadEx.GetThreadHandle(new ICallback() {
				@Override
				public void invoke() {
					ThreadEx.sleep(wait);
					post(new ICallback() {
						@Override
						public void invoke() {
							finish();
						}
					});
				}
			}).start();
		}

	}

	@Override
	public void finish() {
		finish(0);
	}

	public boolean CallC(PBCallC pb, RefObject<String> msg) {
		msg.Value = "�κŵĺ�����æ";
		return false;
	}

	public void CallR(PBCallR pb) {

	}

	// �رջỰ
	public void CallClosureC(PBCallClosureC pb) {
	}

	public boolean MonitorOpenC(PBMonitorOpenC pb, RefObject<String> msg) {
		msg.Value = "�κŵĺ�����æ";
		return false;
	}

	public void MonitorOpenR(PBMonitorOpenR pb) {

	}

	public void MonitorCloseC(PBMonitorCloseC pb) {
		throw RuntimeExceptionEx.Create("not imp");
	}

	public void MonitorCloseR(PBMonitorCloseR pb) {

	}

	public void MediaPushIn(PBMedia pb) {
		// if (_connection != null && _connection.getIsConnected() && pb.To ==
		// _connection.ID)
		// actTalk.TempQueuePlay.offer(pb.Frame);
	}

	public void OnClientConnectionDisconnected() {
	 
		if (AppConfig.Instance.TimeoutReconnect) {
			OnClientConnectionTimeout();
			return;
		}
		OnClientConnectionDisconnected("�������ͨ�ŶϿ��������˳�!");
	}

	public void OnClientConnectionDisconnected(String msg) {
		AlertAndExit(msg);
	}

	public void OnClientConnectionDisconnected(Exception e) {
	 
		if (AppConfig.Instance.TimeoutReconnect) {
			OnClientConnectionTimeout();
			return;
		}
		if (AppConfig._D) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			// Alert(stack);
			// return;
		}
		if (e.getCause() != null && e.getCause() instanceof EOFException)
			OnClientConnectionDisconnected("�������ͨ�ų����쳣�������˳�!");
		if (e instanceof EOFException || e instanceof SocketException)
			OnClientConnectionDisconnected("�������ͨ�ų����쳣�������˳�!");
		else if (e instanceof RuntimeExceptionEx) {
			CLLog.Debug(e);
			OnClientConnectionDisconnected("�������ͨ�ų����쳣�������˳�!");
		} else {
			CLLog.Error(e);
			OnClientConnectionDisconnected("�������ͨ�ų����쳣�������˳�!");
		}
	}

	public void OnClientConnectionTimeout() {
 
		if (AppConfig.Instance.TimeoutReconnect) {
			OpenLoading("����������ӳ�ʱ,���ڳ�������...");
			Intent intent = new Intent();
			setResult(TimeoutReconnect, intent);
			finish();

		} else {
			AlertAndExit("�������ͨ�ų�ʱ�������˳���");
		}
	}

	public PBCmdR OnReceiveCmdC(PBCmdC pb) {
		return null;
	}

	public void OnReceiveCmdR(PBCmdR pb) {

	}

	public void OnReceiveCmdM(PBCmdM pb) {
		if (StringEx.equals(pb.Cmd, PBCmdC.CMD_Type_TALK_Invite)) {// �������Խ�
			String key = JSONHelper.forJSON(pb.JSON, String.class);
			OnTalk_Invite(pb.From, key);
		}
	}

	public void OnTalk_Invite(short from, final String key) {
		if (App.IsBack) {

			//������һ���ͣ��������ҳ���Ҷ�ʱ����û�н��н���������Ļ���������״̬����ʱ����Ҫ�Ȼ�����Ļ�ͽ�����Ļ
			//��Ļ����
			PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
					| PowerManager.SCREEN_DIM_WAKE_LOCK, "StartupReceiver");//���Ĳ�����LogCat���õ�Tag
			wl.acquire();

			//��Ļ����
			KeyguardManager km= (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
			KeyguardManager.KeyguardLock kl = km.newKeyguardLock("StartupReceiver");//������LogCat���õ�Tag
			kl.disableKeyguard();
			Intent intent=new Intent(this,actGoTalkbackActivity.class);
			intent.putExtra("_notification_param",key);
			startActivity(intent);

			Ring.Play(R.raw.duanxin);
//			String msg = String.format("%s����������Խ����Ƿ����?", GetNameByID(from));
//			NotifyManage.notify("�������Խ���ʱ��:" + TimeUtil.ToString(TimeUtil.getCurrentUtilDate(), TimeUtil.YYYY_SECOND), msg, key, this, actGoTalkbackActivity.class);
		} else {

//			this.Prompt(String.format("%s����������Խ����Ƿ����?", GetNameByID(from)), PromptButton.NO, new IAction<Prompt.PromptButton>() {
//				public void invoke(PromptButton obj) {
//					if (obj == PromptButton.YES) {
//						DoEnterTalkback(key);
//					}
//				}
//			});
			post(new ICallback() {

				@Override
				public void invoke() {
					// TODO Auto-generated method stub
					DoEnterTalkback(key);
				}
			});
		}
	}

	// ����Խ�
	public void DoEnterTalkback(String key) {
		startActivity(actTalkback.class, key);
	}

	public String GetNameByID(ListEx<Short> ids) {
		String name = "";
		for (short id : ids) {
			name += (name == "" ? "" : ",") + ((ActCLBase) this).GetNameByID(id);

		}
		return name;
	}

	public String GetNameByID(final short id) {

		if (_connection != null && _connection.getIsConnected()) {
			ListEx<IDModel> ids = _connection.GetAllIDByCache();
			String name = "";
			IDModel model = ids.FirstOrDefault(new Predicate<IDModel>() {
				@Override
				public boolean Test(IDModel obj) {
					return obj.ID == id;

				}
			});
			if (model != null) {
				if (StringEx.isEmpty(model.Name))
					return String.valueOf(model.ID);
				else
					return model.Name;
			} else {
				return String.valueOf(model.ID);
			}
		}
		return String.valueOf(id);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == TimeoutReconnect) {
			Intent intent1 = new Intent();
			setResult(TimeoutReconnect, intent1);
			finish();
		}
		super.onActivityResult(requestCode, resultCode, intent);

	}

}
