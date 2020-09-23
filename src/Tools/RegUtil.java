package Tools;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lgd.buglib.BugSdkInit;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import yangTalkback.Act.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Administrator on 2017/4/13.
 */

public class RegUtil {
    private Context context;
    private Dialog dialog_reg;
    private ProgressDialog p;
    private String input;
    boolean isoverTime = true;
    private String code = "";
    private final SharedPreferences sp;
    public static String strreg;
    private DialogCancelInterface dcif;

    public RegUtil(Context context) {
        this.context = context;
        sp = context.getSharedPreferences("RegCode", MODE_PRIVATE);
        regist();
    }

    public void regist() {

        try {
            strreg = sp.getString("REGCODE", "");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (strreg == null || TextUtils.isEmpty(strreg)) {
            showRegDialog();
        }else{
            againRegist();
        }

    }
    /**
     * @Params:
     * @Author: liuguodong
     * @Date: 2018/2/11 11:35
     * @return��
     */
    // ������֤��
    private void againRegist() {
        input = strreg;
        p = ProgressDialog.show(context, "���Ժ�",
                "ע������֤���벻Ҫ������������", true);
        new CountDownTimer(8000, 1000) {
            @Override
            public void onTick(long arg0) {
            }

            @Override
            public void onFinish() {
                if (isoverTime) {
                    mMessageHandler2.sendEmptyMessage(1);
                }
            }
        };
        new Thread() {
            public void run() {
                TelephonyManager telephonyManager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                String IMEI = telephonyManager.getDeviceId();
                String sURL = "http://218.246.35.74:5050/PC/Default.aspx?Number="
                        + input + "&Onlycode=" + IMEI;
                java.net.URL l_url = null;
                try {
                    l_url = new java.net.URL(sURL);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    p.dismiss();
                    mMessageHandler2.sendEmptyMessage(3);
                }
                java.net.HttpURLConnection l_connection = null;
                try {
                    l_connection = (java.net.HttpURLConnection) l_url.openConnection();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    p.dismiss();
                    mMessageHandler2.sendEmptyMessage(4);
                }
                try {
                    l_connection.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                    p.dismiss();
                    mMessageHandler2.sendEmptyMessage(5);
                }
                InputStream l_urlStream = null;
                try {
                    l_urlStream = l_connection.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                    p.dismiss();
                    mMessageHandler2.sendEmptyMessage(6);
                    return;
                }
                java.io.BufferedReader l_reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(l_urlStream));
                String sCurrentLine = "";
                code = "";
                try {
                    while ((sCurrentLine = l_reader.readLine()) != null) {
                        code += sCurrentLine;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    p.dismiss();
                    mMessageHandler2.sendEmptyMessage(7);
                }
                p.dismiss();
                mMessageHandler2.sendEmptyMessage(0);
            }
        }.start();
    }
    Handler mMessageHandler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:// time out
                    Toast.makeText(context, "�������ӳ�ʱ��ע������֤ʧ��", Toast.LENGTH_SHORT).show();
                    if (p != null) {
                        p.dismiss();
                    }
                    break;
            /* ��ȡ��ʶ��Ϊ �뿪�����߳�ʱ��ȡ�õĶ��� */
                case 0:
                    if (code == null || TextUtils.isEmpty(code)) {
                        Toast.makeText(context, "ע������֤ʧ�ܣ�������", Toast.LENGTH_SHORT).show();
                        showRegDialog();
                        return;
                    }
                    if (code.equalsIgnoreCase("1")) {
                        Toast.makeText(context, "ע������֤�ɹ�", Toast.LENGTH_SHORT).show();
                        isoverTime = false;
                        SharedPreferences.Editor et = sp.edit();
                        et.putString("REGCODE", input);
                        et.commit();
                        SharedPreferencesUtils.setParam(context, "regcode", input + "");
//                        showPasswordInputDialog();
                    } else if (code.equalsIgnoreCase("11")) {
                        showRegDialog();
                        Toast.makeText(context, "ע���볬����Чʹ�ô���", Toast.LENGTH_SHORT).show();
                    } else if (code.equalsIgnoreCase("12")) {
                        showRegDialog();
                        Toast.makeText(context, "ע�����ѹ���", Toast.LENGTH_SHORT).show();
                    } else if (code.equalsIgnoreCase("13")) {
                        showRegDialog();
                        Toast.makeText(context, "ע���볬����Чʹ�ô������ѹ���", Toast.LENGTH_SHORT).show();
                    } else if (code.equalsIgnoreCase("14")) {
                        showRegDialog();
                        Toast.makeText(context, "��ע����δ��Ȩ�ڴ˻���ʹ��", Toast.LENGTH_SHORT).show();
                    } else if (code.equalsIgnoreCase("15")) {
                        showRegDialog();
                        Toast.makeText(context, "ע�����ѱ�����", Toast.LENGTH_SHORT).show();
                    } else if (code.equalsIgnoreCase("16")) {
                        showRegDialog();
                        Toast.makeText(context, "ע���벻����", Toast.LENGTH_SHORT).show();
                    } else if (code.equalsIgnoreCase("17")) {
                        showRegDialog();
                        Toast.makeText(context, "ע���з���δ֪�쳣,ע��ʧ��", Toast.LENGTH_SHORT).show();
                    } else {
                        showRegDialog();
                        Toast.makeText(context, "ע�����������������", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 3:
                    Toast.makeText(context, "������֤�쳣", Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    Toast.makeText(context, "������֤�������������磩�쳣", Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    Toast.makeText(context, "������֤���������磩�쳣", Toast.LENGTH_SHORT).show();
                    break;
                case 6:
                    Toast.makeText(context, "������֤����ȡ���ݣ��쳣", Toast.LENGTH_SHORT).show();
                    break;
                case 7:
                    Toast.makeText(context, "������֤���������ݣ��쳣", Toast.LENGTH_SHORT).show();
                    break;
                case 10:
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private void showRegDialog() {

        View v = LayoutInflater.from(context).inflate(R.layout.reg_dialog, null);
        dialog_reg = new Dialog(context, R.style.DialogStyle);
        dialog_reg.setCanceledOnTouchOutside(false);
        dialog_reg.show();
        dialog_reg.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dcif.ToFinishActivity();
                dialog_reg.dismiss();
            }
        });
        Window window = dialog_reg.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        lp.width = dip2px(context, 290); // ���
        lp.height = dip2px(context, 200); // �߶�
        lp.alpha = 0.7f; // ͸����
        window.setAttributes(lp);
        window.setContentView(v);
        final TextView reg = (TextView) v.findViewById(R.id.editTextReg);
        ImageButton ib = (ImageButton) v.findViewById(R.id.imageButtonReg);
        ImageButton.OnClickListener listener = new ImageButton.OnClickListener() {

            public void onClick(View v) {

                input = reg.getText().toString().trim();
                if (input == null || TextUtils.isEmpty(input)) {
                    Toast.makeText(context, "ע�����������������",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // ������֤��
                p = ProgressDialog.show(context, "���Ժ�",
                        "ע������֤���벻Ҫ������������", true);
                new CountDownTimer(8000, 1000) {

                    @Override
                    public void onTick(long arg0) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onFinish() {


                        if (isoverTime) {
                            mMessageHandler.sendEmptyMessage(1);
                        }
                    }
                };

                new Thread() {
                    public void run() {

                        // Message m = new Message();
                        // m.what = 0;

                        TelephonyManager telephonyManager = (TelephonyManager) context
                                .getSystemService(Context.TELEPHONY_SERVICE);
                        String IMEI = telephonyManager.getDeviceId();

                        String sURL = "http://218.246.35.74:5050/PC/Default.aspx?Number="
                                + input + "&Onlycode=" + IMEI;

                        java.net.URL l_url = null;
                        try {
                            l_url = new java.net.URL(sURL);
                        } catch (MalformedURLException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            // Toast.makeText(context,
                            // "������֤�쳣",Toast.LENGTH_SHORT).show();

                            p.dismiss();

                            mMessageHandler
                                    .sendEmptyMessage(3);
                        }
                        java.net.HttpURLConnection l_connection = null;
                        try {
                            l_connection = (java.net.HttpURLConnection) l_url
                                    .openConnection();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            // Toast.makeText(context, "������֤���������磩�쳣",
                            // Toast.LENGTH_SHORT).show();

                            p.dismiss();

                            mMessageHandler
                                    .sendEmptyMessage(4);
                        }
                        try {
                            l_connection.connect();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            // Toast.makeText(context, "������֤���������磩�쳣",
                            // Toast.LENGTH_SHORT).show();

                            p.dismiss();

                            mMessageHandler
                                    .sendEmptyMessage(5);

                        }
                        InputStream l_urlStream = null;
                        try {
                            l_urlStream = l_connection.getInputStream();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            p.dismiss();

                            mMessageHandler
                                    .sendEmptyMessage(6);
                            return;

                        }

                        java.io.BufferedReader l_reader = new java.io.BufferedReader(
                                new java.io.InputStreamReader(l_urlStream));
                        String sCurrentLine = "";
                        code = "";
                        try {
                            while ((sCurrentLine = l_reader.readLine()) != null) {
                                code += sCurrentLine;
                            }
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            // Toast.makeText(context, "������֤���������ݣ��쳣",
                            // Toast.LENGTH_SHORT).show();

                            p.dismiss();

                            mMessageHandler
                                    .sendEmptyMessage(7);
                        }

                        p.dismiss();

                        mMessageHandler.sendEmptyMessage(0);

                    }
                }.start();

            }
        };

        ib.setOnClickListener(listener);

    }

    Handler mMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:// time out
                    Toast.makeText(context, "�������ӳ�ʱ��ע������֤ʧ��",
                            Toast.LENGTH_SHORT).show();


                    if (p != null) {
                        p.dismiss();
                    }


                    break;
            /* ��ȡ��ʶ��Ϊ �뿪�����߳�ʱ��ȡ�õĶ��� */
                case 0:


                    if (code == null || TextUtils.isEmpty(code)) {
                        Toast.makeText(context, "ע������֤ʧ�ܣ�������",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (code.equalsIgnoreCase("1")) {

                        Toast.makeText(context, "ע������֤�ɹ�",
                                Toast.LENGTH_SHORT).show();
                        isoverTime = false;
                        SharedPreferences.Editor et = sp.edit();
                        et.putString("REGCODE", input);
                        et.commit();
                        SharedPreferencesUtils.setParam(context,"regcode",input+"");


                        dialog_reg.dismiss();
//                        showPasswordInputDialog();
                        // onConfirm.OK();

                    } else if (code.equalsIgnoreCase("11")) {

                        Toast.makeText(context, "ע���볬����Чʹ�ô���",
                                Toast.LENGTH_SHORT).show();

                    } else if (code.equalsIgnoreCase("12")) {

                        Toast.makeText(context, "ע�����ѹ���",
                                Toast.LENGTH_SHORT).show();

                    } else if (code.equalsIgnoreCase("13")) {

                        Toast.makeText(context, "ע���볬����Чʹ�ô������ѹ���",
                                Toast.LENGTH_SHORT).show();

                    } else if (code.equalsIgnoreCase("14")) {

                        Toast.makeText(context, "��ע����δ��Ȩ�ڴ˻���ʹ��",
                                Toast.LENGTH_SHORT).show();

                    } else if (code.equalsIgnoreCase("15")) {

                        Toast.makeText(context, "ע�����ѱ�����",
                                Toast.LENGTH_SHORT).show();

                    } else if (code.equalsIgnoreCase("16")) {

                        Toast.makeText(context, "ע���벻����",
                                Toast.LENGTH_SHORT).show();

                    } else if (code.equalsIgnoreCase("17")) {

                        Toast.makeText(context, "ע���з���δ֪�쳣,ע��ʧ��",
                                Toast.LENGTH_SHORT).show();

                    } else {

                        Toast.makeText(context, "ע�����������������",
                                Toast.LENGTH_SHORT).show();

                    }

                    break;
                case 3:
                    Toast.makeText(context, "������֤�쳣", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case 4:
                    Toast.makeText(context, "������֤�������������磩�쳣",
                            Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    Toast.makeText(context, "������֤���������磩�쳣",
                            Toast.LENGTH_SHORT).show();
                    break;
                case 6:
                    Toast.makeText(context, "������֤����ȡ���ݣ��쳣",
                            Toast.LENGTH_SHORT).show();
                    break;
                case 7:
                    Toast.makeText(context, "������֤���������ݣ��쳣",
                            Toast.LENGTH_SHORT).show();
                    break;
                case 10:

                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }

        ;
    };

    public void SetDialogCancelCallBack(DialogCancelInterface dcif) {
        this.dcif = dcif;
    }

    public interface DialogCancelInterface {
        void ToFinishActivity();
        void ToFinishActivity_pwd();
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    /**
     * ��������ĵ���
     */
//    public void showPasswordInputDialog() {
//        SharedPreferences sp = context.getSharedPreferences("RegCode", MODE_PRIVATE);
//        String spString = sp.getString("REGCODE", "12345678");
//        Log.i("qweqwe","bug�ϴ���ȡ��ע����"+spString);
//        new BugSdkInit(context).init(spString,"KCZD_CRASH","KCZD","");
//
//
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//       final AlertDialog dialogx = builder.create();
//        View view = View.inflate(context, R.layout.dialog_input_password, null);
//        dialogx.setView(view, 0, 0, 0, 0);
//        dialogx.setCanceledOnTouchOutside(false);
//        dialogx.setOnKeyListener(new DialogInterface.OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    dcif.ToFinishActivity();
//                }
//                return false;
//            }
//        });
//        final EditText etPassword = (EditText) view
//                .findViewById(R.id.et_password);
//        ImageButton btnOk = (ImageButton) view.findViewById(R.id.btn_ok);
//
//        btnOk.setOnClickListener(new View.OnClickListener() {
//
//
//            @Override
//            public void onClick(View arg0) {
//                // TODO Auto-generated method stub
//
//                String password = etPassword.getText().toString().trim();  //���������
//                SharedPreferences savedPasswordPref = context.getSharedPreferences(
//                        "savedPassword", 0);
//                String savedPassword = savedPasswordPref.getString(   //�õ���������������Ĭ������
//                        "savedPassword", "8888888");  //Ĭ������
//
//
//
//                if (savedPassword.equals(password)) {
//
//                    Toast.makeText(context, "��֤ͨ��",
//                            Toast.LENGTH_SHORT).show();
//
//                    //�����Ƿ�ע���״̬
//                    SharedPreferences isReged = context.getSharedPreferences(
//                            "isReged", 0);
//                    SharedPreferences.Editor editor = isReged.edit();
//                    editor.putBoolean("isReged", true);
//                    editor.commit();
//
//
//                    dialogx.dismiss();
//
//                } else if(savedPassword.equals("")) {
//                    Toast.makeText(context, "���벻��Ϊ��",
//                            Toast.LENGTH_SHORT).show();
//
//                }else {
//                    Toast.makeText(context, "�������",
//                            Toast.LENGTH_SHORT).show();
//                    etPassword.setText("");
//                }
//            }
//
//
//
//        });
//
//        dialogx.show();
//
//
//
//    }


}
