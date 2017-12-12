package yangTalkback.Base;

import yangTalkback.App.App;
import yangTalkback.Comm.*;
import AXLib.Utility.TH;
//�߳��쳣�������
public class ThreadExceptionHandler implements Thread.UncaughtExceptionHandler {
	private Thread.UncaughtExceptionHandler handler;

	public ThreadExceptionHandler() {
		this.handler = Thread.getDefaultUncaughtExceptionHandler();
	}
	//����δ�����߳�
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		CLLog.Error(ex);
		if (App.IsTest && App.LastAct != null)
			App.LastAct.Alert(ex);

		// �Ƿ��׳��쳣//
		if (handler != null) {
			TH.Throw(ex);
			handler.uncaughtException(thread, ex);
		}

	}
}
