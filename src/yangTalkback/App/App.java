package yangTalkback.App;

import java.util.Stack;

import com.google.code.microlog4android.config.PropertyConfigurator;

import yangTalkback.Act.AppStatusService;
import yangTalkback.Base.ActBase;
import yangTalkback.Base.ActCLBase;
import yangTalkback.Base.AndroidConfig;
import yangTalkback.Base.AndroidConsole;
import yangTalkback.Base.Prompt;
import yangTalkback.Comm.*;
import yangTalkback.Module.NotifyManage;
import yangTalkback.Net.ClientConnection;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import AXLib.Utility.Console;
import AXLib.Utility.EventArg;
import AXLib.Utility.ICallback;
import AXLib.Utility.ListEx;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.ThreadEx;
import AXLib.Utility.Ex.Config.GeneralConfig;

public class App extends Application {

	private static ConnectionEventHandle _connectionEventHandle = new ConnectionEventHandle();
	private static boolean _inited = false;// �Ƿ��ѳ�ʼ��
	private static Stack<ActCLBase> _actStack = new Stack<ActCLBase>();// ������ͼջ
	private static Intent _intentAppStatusService = null;
	public static ActCLBase LastAct = null;// ջ�����һ����ͼ
	public static ActCLBase FirstAct = null;// ջ�е�һ����ͼ
	public static Thread UIThread = null;// ���߳�
	public static boolean IsAppExit = false;// APP�Ƿ��Ѿ��˳�
	public static boolean IsBack = false;// ��ǰ�Ƿ��л�����̨����
	public static String LastTalkChannelKey = null;// ���Խ�KEY;
	private static ClientConnection _connection = null;// �������������
	private static Object _lock = new Object();

	public static ClientConnection GetConnection() {
		return _connection;
	}

	// ���õ�ǰ����
	public static void SetConnection(ClientConnection connection) {
		synchronized (_lock) {
			if (_connection != null) {
				_connection.Disconnected.remove(_connectionEventHandle, "OnDisconnected");
				_connection.Error.remove(_connectionEventHandle, "OnError");
				_connection.Timeout.remove(_connectionEventHandle, "OnTimeout");
			}
			_connection = connection;
			_connection.Disconnected.add(_connectionEventHandle, "OnDisconnected");
			_connection.Error.add(_connectionEventHandle, "OnError");
			_connection.Timeout.add(_connectionEventHandle, "OnTimeout");
		}
	}

	public static ListEx<IDModel> OnLineIDList = new ListEx<IDModel>();// ϵͳID����

	public static TalkbackStatus TalkbackStatus = yangTalkback.Comm.TalkbackStatus.Idle;// ʵʱ�Խ�

	public static void SetTalkbackStatus(TalkbackStatus status) {
		TalkbackStatus = status;
		if (_connection != null && _connection.getIsConnected())
			_connection.Heart();
	}

	public static Application Application;

	public static boolean IsTest = AppConfig._D;// ��ʶ��ǰ�Ƿ������ģʽ

	// ��ʼ��
	private static void Init(ActBase act) {

		PropertyConfigurator.getConfigurator(act).configure();
		if (_inited)
			return;

		Application = act.getApplication();//
		Console.CPrint = new AndroidConsole();// ��ʼ��������Ϣ
		GeneralConfig.Instance = new AndroidConfig(Application);// ��ʼ��������
		UIThread = Thread.currentThread();
		Prompt.Init();// ʵ��ʼ���Ի���

		Intent intent = _intentAppStatusService = new Intent(act, AppStatusService.class);
		act.startService(intent);

		FirstAct = (ActCLBase) act;
		_inited = true;
 

	}

	// ������ͼ��ջ
	public static void PushAct(ActBase act) {
		if (LastAct != act) {
			LastAct = (ActCLBase) act;
			_actStack.push((ActCLBase) act);
		}
		if (!_inited)
			Init(act);
	}

	// ������ͼ��ջ
	public static void popAct(ActBase act) {
		if (_actStack.size() > 1 && _actStack.peek() == act) {
			_actStack.pop();
			LastAct = _actStack.peek();
		}
	}

	// ǰ��̨�л�����
	public static void ForegroundChange(boolean status) {
		try {
			if (AppConfig.Instance.LeaveExitApp)
				exit();
			IsBack = !status;
		} catch (Throwable e) {
			exit();
		}

	}

	// �˳�APP
	public static void exit() {

		try {

			NotifyManage.cancel();
			ActCLBase act = null;
			while (_actStack.size() > 1) {
				act = _actStack.pop();
				act.finish();
			}
			act = _actStack.pop();
			if (_intentAppStatusService != null) {
				act.stopService(_intentAppStatusService);

			}

			act.finish();

		} catch (Throwable e) {
			String s = RuntimeExceptionEx.GetStackTraceString(e);
			RuntimeExceptionEx.PrintException(e);
		}
		IsAppExit = true;
		System.exit(0);
		android.os.Process.killProcess(android.os.Process.myPid());

	}

	// ˯��һ������
	public static void SleepOrWait(Object obj) {
		SleepOrWait(obj, 10);
	}

	// ˯��һ������
	public static void SleepOrWait(Object obj, int time) {
		ThreadEx.sleep(time);
	}

	private ListEx<Activity> mainActivity = new ListEx<Activity>();

	public ListEx<Activity> MainActivity() {
		return mainActivity;
	}

	public void addActivity(Activity act) {
		mainActivity.add(act);
	}

	public void removeActivity(Activity act) {
		mainActivity.remove(act);
	}

	public void finishAll() {
		for (Activity act : mainActivity.toArray(new Activity[0])) {
			if (!act.isFinishing()) {
				act.finish();
				mainActivity.remove(act);
			}
		}
	}

	private static class ConnectionEventHandle {
		public void OnDisconnected(EventArg<Object> arg) {
			try {
				boolean b = AppConfig.Instance.TimeoutReconnect;
			} catch (Exception e) {
				exit();
				return;
			}
			LastAct.OnClientConnectionDisconnected();
		}

		public void OnError(EventArg<Exception> arg) {
			CLLog.Error(arg.e);
			try {
				boolean b = AppConfig.Instance.TimeoutReconnect;
			} catch (Exception e) {
				exit();
				return;
			}
			Exception e = arg.e;
			LastAct.OnClientConnectionDisconnected(e);
		}

		public void OnTimeout(EventArg<Object> arg) {
			try {
				boolean b = AppConfig.Instance.TimeoutReconnect;
			} catch (Exception e) {
				exit();
				return;
			}
			LastAct.post(new ICallback() {

				@Override
				public void invoke() {
					LastAct.OnClientConnectionTimeout();
				}
			});

		}
	}

}
