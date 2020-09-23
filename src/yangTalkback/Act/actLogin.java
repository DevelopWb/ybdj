package yangTalkback.Act;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.example.volumekey.AExecuteAsRoot;
import com.example.volumekey.VolumeService;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import AXLib.Model.RefObject;
import AXLib.Utility.EventArg;
import AXLib.Utility.Ex.StringEx;
import AXLib.Utility.ICallback;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.StreamSocket;
import AXLib.Utility.ThreadEx;
import Tools.RegOperateTool;
import Tools.RegUtil;
import yangTalkback.App.App;
import yangTalkback.App.AppConfig;
import yangTalkback.Base.ActCLBase;
import yangTalkback.Base.AutoRefView;
import yangTalkback.Cpt.ImageButtonEx;
import yangTalkback.Net.ClientConnection;

@AutoRefView(id = R.layout.act_login, layout = 0x03)
public class actLogin extends ActCLBase {
    private static boolean _D = AppConfig._D;
    private static boolean _D1 = _D && true;// ���ز���
    private boolean _tryConnecting = false;// �Ƿ����ڳ�������
    private int _tryConnectionCount = 0;// �������Ӵ���
    private Thread _connectThread = null;// ���ӷ������߳�
    private Class<?> _loginGoPage = null;
    private boolean _isAutorunMode = false;
    @AutoRefView(id = R.act_login.tbID)
    public EditText tbID;// �������
    @AutoRefView(id = R.act_login.tbPwd)
    public EditText tbPwd;
    @AutoRefView(id = R.act_login.cbRPwd)
    public CheckBox cbRPwd;
    @AutoRefView(id = R.act_login.ibRPwd, click = "ibRPwd_Click")
    public ImageButtonEx ibRPwd;
    @AutoRefView(id = R.act_login.btSetting, click = "btSetting_Click")
    public Button btSetting;// ���ð�ť
    @AutoRefView(id = R.act_login.btLogin, click = "btLogin_Click")
    public Button btLogin;// ��¼��ť
    private boolean _isAutoBootRuned = false;
    private int _maxTryLoginCount = 50;
    private Object _lock = new Object();
    private Object _loginLock = new Object();

    private RegOperateTool regOperateTool;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.act_login);
        acquireWakeLock();
    }

    /**
     * ��ȡrootȨ��
     */
    private void initRoot() {
        final List<String> cmds = new ArrayList<String>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < VolumeService.THREAD_NUM; i++) {
                    cmds.add("chmod 664 /dev/input/event" + i);
                }
            }
        }).start();

        boolean ret = AExecuteAsRoot.execute(cmds);
    }

    public void onScreenReady() {
        initRoot();
        _ac.LeaveExitApp = false;
        tbID.setText(_ac.LoginID);
        if (_ac.IsRememberPWD) {
            tbPwd.setText(_ac.LoginPWD);
            cbRPwd.setChecked(true);
            ibRPwd.setImageResource(R.drawable.ico_login_sel_active);
        }
        //        regOperateTool = new RegOperateTool(this, "");
        //        regOperateTool.SetCancelCallBack(new RegOperateTool.CancelCallBack() {
        //            @Override
        //            public void toFinishActivity() {
        //                finish();
        //            }
        //        });

        initRegUtil();

        _loginGoPage = actMain.class;
        // _loginGoPage = actMonitor.class;
        String autorun = GetActivityDefaultExtraValue(false);

        if (_ac.IsAutorun && _ac.IsRememberPWD && App.GetConnection() != null && App.GetConnection().getIsConnected()) {
            startActivity(actMain.class);
        } else if (_ac.IsAutorun && _ac.IsRememberPWD && StringEx.equalsIgnoreCase("Autorun", autorun)) {
            _isAutorunMode = true;
            CallByNewThread("AutoBootRun");
        } else if (_D1) {
            tbPwd.setText("000000-00");
            btLogin_Click(null);
        }
    }

    /**
     * @Params: ��ע����
     * @Author: liuguodong
     * @Date: 2018/4/19 17:04
     * @return��
     */
    private void initRegUtil() {

        RegUtil regUtil = new RegUtil(this);
        regUtil.SetDialogCancelCallBack(new RegUtil.DialogCancelInterface() {
            @Override
            public void ToFinishActivity() {
                finish();
            }

            @Override
            public void ToFinishActivity_pwd() {
                finish();
            }
        });

    }

    @Override
    public boolean OnKeyDown_Back() {
        App.exit();
        return super.OnKeyDown_Back();
    }

    public void btLogin_Click(EventArg<View> arg) {
        final String ip = _ac.ServerIP;
        String idStr = this.tbID.getText().toString();
        String pwdStr = this.tbPwd.getText().toString();
        if (StringEx.isEmpty(ip)) {
            Alert("δ���õ�½������,��������");
            return;
        }
        if (StringEx.isEmpty(idStr) || StringEx.isEmpty(pwdStr)) {
            Alert("��Ż��������벻��ȷ");
            return;
        }
        _tryConnecting = true;
        ConnectServer();
    }

    public void btSetting_Click(EventArg<View> arg) {
        startActivity(actSetting.class);
    }

    public void ibRPwd_Click(EventArg<View> arg) {
        cbRPwd.setChecked(!cbRPwd.isChecked());
        ibRPwd.setImageResource(cbRPwd.isChecked() ? R.drawable.ico_login_sel_active : R.drawable.ico_login_sel_bg);
    }

    public void AlertAndExit() {
        ThreadEx.stop(_connectThread);
        ThreadEx.ThreadCall(new ICallback() {

            @Override
            public void invoke() {
                ThreadEx.sleep(200);
                CloseLoading();
                // actLogin.this.AlertAndExit("ȡ�����ӷ������������˳�");
            }
        });

    }

    public void AutoBootRun() {
        try {
            OpenLoading("����ִ���Զ�������¼�����Ժ�", false, null);
            Thread.sleep(30 * 1000);
            post(new ICallback() {
                @Override
                public void invoke() {

                    btLogin_Click(null);
                }
            });
        } catch (Exception e) {

        } finally {

        }
    }

    private void TryConnectServer(String ip, int port) {

        while (true) {
            StreamSocket ss = null;
            try {
                ss = new StreamSocket();
                ss.connect(ip, port);
                ss.close();
                return;
            } catch (Exception e) {
                try {
                    ss.close();
                } catch (Exception e1) {

                }
                ThreadEx.sleep(1000);
            }
        }
    }

    public void ConnectServer() {
        if (!_tryConnecting)
            return;
        // loadingʱȡ����ص�����
        final ICallback cancelCallBack = new ICallback() {
            @Override
            public void invoke() {
                _tryConnecting = false;
                AlertAndExit();
            }
        };
        OpenLoading("�������ӷ�����...", true, cancelCallBack);

        final String ip = _ac.ServerIP;
        final String idStr = this.tbID.getText().toString();
        final short id = (short) Integer.parseInt(idStr);
        final String pwd = tbPwd.getText().toString();

        final StreamSocket ss = new StreamSocket();
        try {
            ss.setReceiveBufferSize(8 * 1024);
            ss.setSendBufferSize(8 * 1024);
        } catch (SocketException e) {
        }
        synchronized (_lock) {
            ThreadEx.stop(_connectThread);
            _connectThread = ThreadEx.GetThreadHandle(new ICallback() {
                @Override
                public void invoke() {

                    if (_isAutorunMode) {
                        TryConnectServer(ip, AppConfig.Instance.ServerPort);
                        ThreadEx.sleep(5000);
                    }

                    ClientConnection cc = null;
                    try {
                        ss.connect(ip, AppConfig.Instance.ServerPort);// ���ӷ�����
                    } catch (Exception ex) {
                        String stack = RuntimeExceptionEx.GetStackTraceString(ex);
                        if (_tryConnectionCount <= _maxTryLoginCount) {// ����ʧ�ܣ���������
                            for (int i = 5; i >= 0; i--) {
                                if (!_tryConnecting)
                                    return;
                                OpenLoading(String.format("���ӷ�����ʧ�ܣ�%d����Զ�������", i), true, cancelCallBack);
                                ThreadEx.sleep(1000);
                            }
                            _tryConnectionCount++;
                            OpenLoading("�����������ӷ�����...", true, cancelCallBack);
                            ThreadEx.sleep(1000);
                            if (_tryConnecting) {
                                post(new ICallback() {// ͬ����UI�������ӷ�����
                                    public void invoke() {
                                        ConnectServer();
                                    }
                                });
                            }
                            return;
                        } else {
                            AlertAndExit("δ�����ӷ����������Ժ����ԡ�");
                        }
                    }
                    _tryConnectionCount = 0;

                    cc = new ClientConnection(ss);
                    ThreadEx.sleep(1000);
                    try {
                        OpenLoading("���ڵ�¼...", true, cancelCallBack);
                        RefObject<String> refObj = new RefObject<String>(null);
                        boolean loginResult = false;
                        try {

                            synchronized (_loginLock) {
                                ClientConnection lastCC = App.GetConnection();
                                if (lastCC != null && lastCC.getIsLogined() && lastCC.getIsConnected()) {
                                    if (App.LastAct instanceof actLogin) {
                                        startActivity(actMain.class);
                                        return;
                                    }
                                } else {
                                    loginResult = cc.Login(id, pwd, "", true, refObj);
                                    if (loginResult) {
                                        App.SetConnection(cc);// ���õ�ǰ����
                                    }
                                }
                            }
                        } catch (Exception e) {

                        } finally {
                            CloseLoading();
                        }
                        if (loginResult) {
                            SaveLoginInfo();
                            OpenLoading("��¼�ɹ������ڽ���...", true, cancelCallBack);
                            ThreadEx.sleep(1000);
                            CloseLoading();
                            startActivity(_loginGoPage);// ��תҳ��
                            return;
                        } else {
                            cc.Disconnect();
                            cc = null;
                            Alert(String.format("��¼ʧ�ܣ�%s", refObj.Value), true);
                            return;
                        }
                    } catch (Exception ex) {
                        String stack = RuntimeExceptionEx.GetStackTraceString(ex);
                        if (_tryConnectionCount <= _maxTryLoginCount) {
                            for (int i = 5; i >= 0; i--) {
                                if (!_tryConnecting)
                                    return;
                                OpenLoading(String.format("�������ӳ��ִ���,%d����Զ�������", i), true, cancelCallBack);
                                ThreadEx.sleep(1000);
                            }
                            _tryConnectionCount++;
                            OpenLoading("�����������ӷ�����...", true, cancelCallBack);
                            ThreadEx.sleep(1000);
                            if (_tryConnecting) {
                                post(new ICallback() {
                                    public void invoke() {
                                        ConnectServer();
                                    }
                                });
                            }
                            return;
                        } else {
                            AlertAndExit("����������Ժ����ԡ�");
                        }
                    }
                }

            });
            _connectThread.start();
        }
    }

    // ͨ�ų�ʱ����
    public void TimeoutReconnect() {
        _tryConnecting = true;
        final ICallback cancelCallBack = new ICallback() {
            @Override
            public void invoke() {
                _tryConnecting = false;
                AlertAndExit();
            }
        };
        OpenLoading("����������ӳ�ʱ����������...", true, cancelCallBack);
        ThreadEx.ThreadCall(new ICallback() {
            @Override
            public void invoke() {
                ThreadEx.sleep(1000);
                post(new ICallback() {
                    @Override
                    public void invoke() {
                        if (_tryConnecting)
                            ConnectServer();
                    }
                });

            }
        });
    }

    public void SaveLoginInfo() {
        _ac.LoginID = this.tbID.getText().toString();
        _ac.LoginPWD = this.tbPwd.getText().toString();
        _ac.IsRememberPWD = this.cbRPwd.isChecked();
        _ac.Save();
    }

    // ����ͨ�ų�ʱ
    @Override
    public void OnClientConnectionTimeout() {
        if (_ac.TimeoutReconnect) {
            TimeoutReconnect();
        } else {
            AlertAndExit("�������ͨ�ų�ʱ�������˳���");
        }
    }

    // ���淵�أ�����ǳ�ʱ����г�ʱ����
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (resultCode == TimeoutReconnect) {
            OnClientConnectionTimeout();
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }

    }

    // ���ӶϿ��������ʹ���Զ����������κβ���
    @Override
    public void OnClientConnectionDisconnected(Exception e) {
        if (!_ac.TimeoutReconnect) {
            super.OnClientConnectionDisconnected(e);
        }
    }

    // ���ӶϿ��������ʹ���Զ����������κβ���
    @Override
    public void OnClientConnectionDisconnected() {
        if (!_ac.TimeoutReconnect) {
            super.OnClientConnectionDisconnected();
        }
    }

    public static boolean hasInternet(Activity activity, Context c) {
        ConnectivityManager manager = (ConnectivityManager) activity.getSystemService(c.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return false;
        }
        if (info.isRoaming()) {
            return true;
        }
        return true;

    }

    public static String getIMEI(Context context) {
        String IMEI;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = telephonyManager.getDeviceId();

        return IMEI;
    }

    private void CheckCDKey() {

        String cdkey = getResources().getString(R.string.cd_key);
        if (!StringEx.equalsIgnoreCase(_ac.CDKey, cdkey)) {
            if (!hasInternet(this, this.getApplicationContext())) {
                AlertAndExit("�����豸��ǰ�޿����������ӣ��������������Ӻ����ԣ�");
                return;
            }
        }
    }

    WakeLock wakeLock = null;

    // ��ȡ��Դ�������ָ÷�������ĻϨ��ʱ��Ȼ��ȡCPUʱ����������
    private void acquireWakeLock() {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) this.getSystemService(POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
                    "PostLocationService");
            if (null != wakeLock) {
                wakeLock.acquire();
            }
        }
    }

    // �ͷ��豸��Դ��
    private void releaseWakeLock() {
        if (null != wakeLock) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    @Override
    public void finish() {
        acquireWakeLock();

        super.finish();
    }


}
