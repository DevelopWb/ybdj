package Tools;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import yangTalkback.Act.CommonProgressDialog;
import yangTalkback.Act.DialogAdapter;
import yangTalkback.Act.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * ע�����������
 * Created by Administrator on 2017/3/30.
 */

public class RegOperateTool {
    private RequestQueue myRequestQueue;
    private SharedPreferences sp;
    public static boolean istoolTip = false;//ע����״̬�ı��Ƿ����ѣ����磺ע���뵽�ڣ����õ�
    public static boolean isNumberLimit = false;//�Ƿ����ƴ���
    public static boolean isForbidden = false;//�Ƿ����
    public static boolean isAllowedToMinus = false;//���ζԽ��еı������Ƿ��������
    public static int REGSIZE = 0;
    public static String URL_Reg_Center = "http://zc.xun365.net";//ע��������ϵͳ
    public static String APP_MARK = "YBDJ";//�����ʶ
    private CommonProgressDialog mProgressDialog;
    private String nearestVersion;
    private Context context;
    public static String strreg;
    private AMapLocationClient locationClient = null;
    private String Lat;
    private String Lng;
    private String Addr ;
    private Dialog dialog_Reg;
    private ProgressDialog progressDialog;
    private CancelCallBack cancelCallBack;

    public RegOperateTool(Context context) {
        this.context = context;
        sp = context.getSharedPreferences("REG", MODE_PRIVATE);
        strreg = sp.getString("OBJREG", "");
    }
    public RegOperateTool(Context context, String str) {
        this.context = context;
        sp = context.getSharedPreferences("REG", MODE_PRIVATE);
        strreg = sp.getString("OBJREG", "");
        initLocation();
        if (strreg == null || TextUtils.isEmpty(strreg)) {
            showRegDialog();
        }else {
            checkRegStatus();
        }
    }
    /**
     * ���ü��νӿ�
     *
     * @param size ��Ҫ���Ĵ���
     */
    public void SetRegisCodeNumber(final int size) {
        getRequestQueue();
        String url = URL_Reg_Center + "/WebService/RegisCode.asmx/SetRegisCodeNumber";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String str) {
                if (str == null) {
                    Toast.makeText(context, "�����������쳣������ϵ����Ա", Toast.LENGTH_SHORT).show();
                    return;
                }
                sp.edit().putInt("MINUSTIMES", 0).commit();
                String json = getStr(str);
                try {
                    JSONObject obj = new JSONObject(json);
                    String model = obj.getString("Model");
                    //  "ע�����Ѿ�����"
                    if (model.equals("ע�����Ѿ�����")) {
                        isForbidden = true;
                        SaveRegStatus("ע�����ѽ���");
                        if (istoolTip) {
                            Toast.makeText(context, "ע������Ч������ϵ����Ա", Toast.LENGTH_SHORT).show();
                        }

                        return;
                    } else if (model.equals("ע�������������")) {
                        SaveRegStatus("ע�������������");
                        REGSIZE++;
                        if (istoolTip) {
                            Toast.makeText(context, "ע������������꣬����ϵ����Ա", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    } else if (model.equals("ע����ʹ��ʱ�����")) {
                        SaveRegStatus("ע�����ѹ���");
                        if (istoolTip) {
                            Toast.makeText(context, "ע����ʹ��ʱ����ڣ�����ϵ����Ա", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    } else if (model.equals("ע���벻��ȷ")) {
                        SaveRegStatus("ע���벻��ȷ");
                        if (istoolTip) {
                            Toast.makeText(context, "ע���벻���ڣ�����ϵ����Ա", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    } else {
                        SaveRegStatus("ע��������");
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<String, String>();
                map.put("softType", "mb");
                map.put("regisCode", strreg);
                map.put("number", size + "");

                return map;
            }
        };
        myRequestQueue.add(stringRequest);

    }

    private RequestQueue getRequestQueue() {
        if (myRequestQueue == null) {
            myRequestQueue = Volley
                    .newRequestQueue(context.getApplicationContext());
        }
        return myRequestQueue;
    }

    private String getStr(String str) {
        int ii = 0;
        int j = 0;
        ii = str.indexOf("{");
        j = str.lastIndexOf("}");
        return str.substring(ii, j + 1);
    }

    /**
     * ����ע����״̬
     *
     * @param str ״̬����
     */
    private void SaveRegStatus(String str) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("REGSTATUS", str);
        editor.commit();
    }

    /**
     * �����û��δ������ע�������
     */
    public void CheckUnMinusedRegSizeToMinus() {
        if (isTheRegStatusOkNoToast(context)) {
            if (sp.getInt("UNMINUSEDSIZE", 0) > 0) {
                if (isNumberLimit) {
                    SetRegisCodeNumber(sp.getInt("UNMINUSEDSIZE", 0));
                    sp.edit().putInt("UNMINUSEDSIZE", 0).commit();
                }

            }
        }

    }

    //�鿴ע����δ���Ĵ���
    public void UnMinusedRegSizeToCommit() {
        if (REGSIZE > 1) {
            SharedPreferences.Editor et = sp.edit();
            et.putInt("UNMINUSEDSIZE", REGSIZE - 1);
            et.commit();
        }
    }

    /**
     * �ж�ע����״̬�Ƿ�����
     *
     * @return
     */
    public  boolean isTheRegStatusOk(Context context) {
        SharedPreferences sp = context.getSharedPreferences("REG", MODE_PRIVATE);
        String reg_status = sp.getString("REGSTATUS", "ע��������");
        //  "ע�����Ѿ�����"
        if (reg_status.equals("ע�����ѽ���")) {
                Toast.makeText(context, "ע������Ч������ϵ����Ա", Toast.LENGTH_SHORT).show();
            return false;
        } else if (reg_status.equals("ע�������������")) {
                Toast.makeText(context, "ע������������꣬����ϵ����Ա", Toast.LENGTH_SHORT).show();

            return false;
        } else if (reg_status.equals("ע�����ѹ���")) {
                Toast.makeText(context, "ע����ʹ��ʱ����ڣ�����ϵ����Ա", Toast.LENGTH_SHORT).show();
            return false;
        } else if (reg_status.equals("ע���벻��ȷ")) {
                Toast.makeText(context, "ע���벻���ڣ�����ϵ����Ա", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }


    /**
     * �ж�ע����״̬�Ƿ�����
     *
     * @return
     */
    public  boolean isTheRegStatusOkNoToast(Context context) {
        SharedPreferences sp = context.getSharedPreferences("REG", MODE_PRIVATE);
        String reg_status = sp.getString("REGSTATUS", "ע��������");
        //  "ע�����Ѿ�����"
        if (reg_status.equals("ע�����ѽ���")) {
            return false;
        } else if (reg_status.equals("ע�������������")) {
            return false;
        } else if (reg_status.equals("ע�����ѹ���")) {
            return false;
        } else if (reg_status.equals("ע���벻��ȷ")) {
            return false;
        } else {
            return true;
        }
    }
// �ж������Ƿ�����

    public static boolean isConnected(Context context) {
        boolean isOk = true;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobNetInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiNetInfo != null && !wifiNetInfo.isConnectedOrConnecting()) {
                if (mobNetInfo != null && !mobNetInfo.isConnectedOrConnecting()) {
                    NetworkInfo info = connectivityManager
                            .getActiveNetworkInfo();
                    if (info == null) {
                        isOk = false;
                    }
                }
            }
            mobNetInfo = null;
            wifiNetInfo = null;
            connectivityManager = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isOk;
    }



    /**
     * ���ע�����״̬
     */
    public void checkRegStatus() {
        if (!isConnected(context.getApplicationContext())) {
            Toast.makeText(context, "�����쳣�������ֻ�����", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        myRequestQueue = getRequestQueue();

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,URL_Reg_Center + "/WebService/SoftWare.asmx/GetRegisCodeInfo_NoPhoneMessage", new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {
                //���ע�����Ѿ�ע��
                if (!TextUtils.isEmpty(response)) {
                    String json = Getstr(response);
                    try {
                        JSONObject obj = new JSONObject(json);
                        String result = obj.getString("Result");
                        JSONArray mArray = obj.getJSONArray("Model");
                        if (!TextUtils.isEmpty(result) && result.equals("ok")) {
                            if (mArray.length() == 0) {
                                SaveRegStatus("ע���벻��ȷ");
                                Toast.makeText(context, "ע���벻����",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                JSONObject obj_ = (JSONObject) mArray.get(0);
                                String Imei = obj_.getString("Imei");
                                String isImei = obj_.getString("isImei");
                                String isValid = obj_.getString("isValid");
                                String isNumber = obj_.getString("isNumber");
                                String isDisabled = obj_.getString("isDisabled");
                                String isAutoUpdate = obj_.getString("isAutoUpdate");
                                String isToolTip = obj_.getString("isToolTip").trim();
//����ע����״̬��Ϣ
                                String RegisCodeState = obj_.getString("RegisCodeState");
                                if (RegisCodeState != null) {
                                    if (RegisCodeState.equals("�ѹ���")) {
                                        SaveRegStatus("ע�����ѹ���");
                                    } else if (RegisCodeState.equals("�ѽ���")) {
                                        SaveRegStatus("ע�����ѽ���");
                                    } else if (RegisCodeState.equals("�����þ�")) {
                                        SaveRegStatus("ע�������������");
                                    } else {
                                        SaveRegStatus("ע��������");
                                    }
                                }

                                SharedPreferences.Editor editor = sp.edit();
                                if (isToolTip.equals("0")) {
                                    istoolTip = false;
                                    editor.putBoolean("ISTOOLTIP", false);
                                } else {
                                    istoolTip = true;
                                    editor.putBoolean("ISTOOLTIP", true);
                                }
                                if (isNumber.equals("0")) {//0�����д�������
                                    isNumberLimit = true;
                                    editor.putBoolean("ISNUMBER", true);
                                } else {
                                    isNumberLimit = false;
                                    editor.putBoolean("ISNUMBER", false);
                                }
                                editor.commit();
                                if (istoolTip) {
                                    if (isValid != null && !TextUtils.isEmpty(isValid)) {
                                        if (isValid.equals("0")) {//ע��������ʱ��
                                            String ValidEnd = obj_.getString("ValidEnd");
                                            String time = ValidEnd.split(" ")[0];
                                            if (TheDayToNextDay(time) > 0 && TheDayToNextDay(time) < 8) {

                                                if (IsTheRegStatusTime("isValid")) {
                                                    WarnRegStatus("ע������Ч�ڻ�ʣ" + TheDayToNextDay(time) + "�죬����ϵ����Ա", "isValid");
                                                }

                                            }
                                        }
                                    }

                                    if (isNumber != null && !TextUtils.isEmpty(isNumber)) {
                                        if (isNumber.equals("0")) {//ע�����д�������
                                            String NumberTotal = obj_.getString("Number");
                                            String NumberUsed = obj_.getString("NumberNow");
                                            int NumberNow = Integer.parseInt(NumberTotal) - Integer.parseInt(NumberUsed);
                                            if (NumberNow < 100) {
                                                if (IsTheRegStatusTime("isNumber")) {
                                                    WarnRegStatus("ע���������ʣ" + NumberNow + "�Σ�����ϵ����Ա", "isNumber");
                                                }

                                            }
                                        }
                                    }

                                }
                                if (isDisabled != null && !TextUtils.isEmpty(isDisabled)) {
                                    if (isDisabled.equals("0")) {//ע�����ѽ���
                                        isForbidden = true;
                                        WarnRegStatus("ע������Ч������ϵ����Ա", "disable");
                                        return;
                                    }else{
                                        isForbidden = false;
                                    }
                                }

                                if (isAutoUpdate != null && !TextUtils.isEmpty(isAutoUpdate)) {
                                    if (isAutoUpdate.equals("1")) {//�����Զ�����
                                        GetNearestVersionFromService();
                                    }
                                }
                            }
                        } else {
                            if (IsTheRegStatusTime("isWrong")) {
                                WarnRegStatus("�����������쳣", "isWrong");
                            }

                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }


                }


            }


        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(
                    VolleyError error) {
                Toast.makeText(context, "�������쳣������ϵ����Ա", Toast.LENGTH_SHORT).show();


            }
        }) {
            @Override
            protected Map<String, String> getParams()
                    throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("regisCode", strreg);
                map.put("softwareId", APP_MARK);
                map.put("softwareType", "mb");
                return map;
            }


        };

        myRequestQueue.add(stringRequest);


    }

    public static String Getstr(String response) {

        int i = 0;
        int y = 0;
        i = response.indexOf("{");
        y = response.lastIndexOf("}");
        String str = response.substring(i, y + 1);
        return str;
    }

    /**
     * �ж��Ƿ�����ע����״̬
     */
    private boolean IsTheRegStatusTime(String status) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("NEXTWARNTIME", MODE_PRIVATE);
        final String time = sharedPreferences.getString("nextRegStatusTime" + status, "");
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String time2 = new SimpleDateFormat("yyyy-MM-dd").format(date);
        if (TextUtils.isEmpty(time)) {
            return true;
        } else {
            if (time.equals(time2)) {
                return true;
            } else {
                return false;
            }
        }

    }


    private void WarnRegStatus(final String text, final String status) {

        View v = LayoutInflater.from(context).inflate(R.layout.warn_reg_layout
                , null);
        final Dialog dialog_toWarn = new Dialog(context, R.style.DialogStyle);
        dialog_toWarn.setCanceledOnTouchOutside(false);
        dialog_toWarn.setCancelable(false);
        dialog_toWarn.show();
        Window window = dialog_toWarn.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
//        lp.width = dip2px(this, 300); // ���
//        lp.height = dip2px(this, 160); // �߶�
        // lp.alpha = 0.7f; // ͸����
        window.setAttributes(lp);
        window.setContentView(v);
//        dialog_toSet.setOnKeyListener(new DialogInterface.OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//
//                if (keyCode == event.KEYCODE_BACK) {
//                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                        finish();
//                    }
//                }
//                return false;
//            }
//        });
        final TextView nfs_set_no_tv = (TextView) v.findViewById(R.id.warn_reg_tv);
        final TextView warn_reg_textViewreg = (TextView) v.findViewById(R.id.warn_reg_textViewreg);
        nfs_set_no_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (text!=null&&!TextUtils.isEmpty(text)) {
                    if (text.equals("ע������Ч������ϵ����Ա")) {
                         if (cancelCallBack!=null) {
                            cancelCallBack.toFinishActivity();
                        }
                    }else{
                        String nextTime = GetNextWarnTime(1);
                        SharedPreferences sharedPreferences = context.getSharedPreferences("NEXTWARNTIME", MODE_PRIVATE);
                        SharedPreferences.Editor et = sharedPreferences.edit();
                        et.putString("nextRegStatusTime" + status, nextTime);
                        et.commit();
                        dialog_toWarn.dismiss();
                    }

                }

            }
        });
        warn_reg_textViewreg.setText(text);
    }


    /**
     * ��ȡ�´����ѵ�ʱ��,day���
     */
    private String GetNextWarnTime(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, day);
        Date date = calendar.getTime();
        String time = new SimpleDateFormat("yyyy-MM-dd").format(date);
        return time;
    }

    /**
     * �������ĳ�컹ʣ������
     *
     * @return
     */
    private int TheDayToNextDay(String time) {
        int day = 0;
        try {
            Calendar mCalendar = Calendar.getInstance();
            Date nowDate = mCalendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            String time2 = sdf.format(nowDate);
            Date nowDate_ = sdf.parse(time2);
            Date nextDate = sdf.parse(time);
            day = (int) ((nextDate.getTime() - nowDate_.getTime()) / (24 * 60 * 60 * 1000));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return day;
    }

    /**
     * �ӷ�������ȡ���µİ汾
     */
    private void GetNearestVersionFromService() {

        if (isConnected(context)) {
            getRequestQueue();
            StringRequest mStringRequest = new StringRequest(Request.Method.POST, URL_Reg_Center + "/WebService/SoftWare.asmx/GetAllSoftWareInfo", new Response.Listener<String>() {
                @Override
                public void onResponse(String str) {
                    if (str != null && !TextUtils.isEmpty(str)) {

                        try {
                            JSONObject obj = new JSONObject(str);
                            JSONArray infos = obj.getJSONArray("Model");
                            if (infos.length() > 0) {
                                JSONObject obj_ = (JSONObject) infos.get(0);
                                nearestVersion = obj_.getString("SoftwareVersion").trim();
                                String down_url = obj_.getString("softDownloadUrl");
                                String appDescription = obj_.getString("softDescription");
                                if (updateableSoftVersion(getAPPVersion(),nearestVersion)) {
                                    if (IsTheTime()) {
                                        WarnUpgradeDialog(down_url, appDescription);
                                    }

                                } else {//��
                                    SharedPreferences sharedPreferences = context.getSharedPreferences("NEXTWARNTIME", MODE_PRIVATE);
                                    SharedPreferences.Editor et = sharedPreferences.edit();
                                    et.putString("nextTime", "");
                                    et.commit();
                                }
                            } else {
                                Toast.makeText(context, "�������ϲ鲻�������", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {

                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("softwareId",APP_MARK);
                    map.put("softwareType", "mb");
                    return map;
                }
            };
            myRequestQueue.add(mStringRequest);
        }

    }

    /**
     * ͨ������İ汾�����ж��Ƿ�����
     * @param localVersionName ��������İ汾����
     * @param serverVersionName ���������İ汾����
     * @return
     */
    private boolean updateableSoftVersion(String localVersionName, String serverVersionName) {
        if (TextUtils.isEmpty(localVersionName) || TextUtils.isEmpty(serverVersionName)) {
            return false;
        }
        String  local3 = "0";
        String  server3 = "0";
        String[] localVersion = localVersionName.split("\\.");
        String[] serverVersion = serverVersionName.split("\\.");
        String local1 = localVersion[0];
        String local2 = localVersion[1];
        if (localVersion.length==3) {
            local3 =localVersion[2];
        }
        String server1 = serverVersion[0];
        String server2 = serverVersion[1];
        if (serverVersion.length==3) {
            server3 =serverVersion[2];
        }
        if (Integer.parseInt(server1) > Integer.parseInt(local1)) {
            return true;
        }
        if (Integer.parseInt(server2) > Integer.parseInt(local2)) {
            return true;
        }
        if (Integer.parseInt(server3) > Integer.parseInt(local3)) {
            return true;
        }
        return false;
    }
    /**
     * �ж��Ƿ���������
     */
    private boolean IsTheTime() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("NEXTWARNTIME", MODE_PRIVATE);
        final String time = sharedPreferences.getString("nextTime", "");
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String time2 = new SimpleDateFormat("yyyy-MM-dd").format(date);
        if (TextUtils.isEmpty(time)) {
            return true;
        } else {
            if (time.equals(time2)) {
                return true;
            } else {
                return false;
            }
        }

    }
    private void WarnUpgradeDialog(final String url, String description) {

        View v = LayoutInflater.from(context).inflate(R.layout.warn_layout, null);
        final Dialog dialog_toSet = new Dialog(context, R.style.DialogStyle);
        dialog_toSet.setCanceledOnTouchOutside(false);
        dialog_toSet.setCancelable(false);
        dialog_toSet.show();
        Window window = dialog_toSet.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        lp.width = dip2px(context, 300); // ���
        lp.height = dip2px(context, 260); // �߶�
        // lp.alpha = 0.7f; // ͸����
        window.setAttributes(lp);
        window.setContentView(v);
        ListView feature_lv = (ListView) v.findViewById(R.id.feature_lv);
        feature_lv.setDivider(null);
        feature_lv.setAdapter(new DialogAdapter(context, description));
        dialog_toSet.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                if (keyCode == event.KEYCODE_BACK) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        dialog_toSet.dismiss();
                    }
                }
                return false;
            }
        });
        final TextView nfs_set_sure_tv = (TextView) v.findViewById(R.id.nfs_set_sure_tv);
        final TextView nfs_set_no_tv = (TextView) v.findViewById(R.id.nfs_set_no_tv);
        nfs_set_sure_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_toSet.dismiss();
                DownAPKfromService(url);
            }
        });
        nfs_set_no_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_toSet.dismiss();
                String nextTime = GetNextWarnTime(7);
                SharedPreferences sharedPreferences = context.getSharedPreferences("NEXTWARNTIME", MODE_PRIVATE);
                SharedPreferences.Editor et = sharedPreferences.edit();
                et.putString("nextTime", nextTime);
                et.commit();
            }
        });
    }


    private void DownAPKfromService(String url) {

        if (isConnected(context)) {
            mProgressDialog = new CommonProgressDialog(context);
            mProgressDialog.setMessage("��������");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(true);
            final DownloadTask downloadTask = new DownloadTask(context);
            downloadTask.execute(url);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    downloadTask.cancel(true);
                }
            });
            //downFile(url);
        }

    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;

        DownloadTask(Context context) {
            this.context = context;
        }

        //ִ���첽����doInBackground��֮ǰִ�У�������ui�߳���ִ��
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //��ʼ���� �Ի����������ʾ
            mProgressDialog.show();
            mProgressDialog.setProgress(0);
        }

        @Override
        protected String doInBackground(String... params) {
            int i = 0;
            String uri = URL_Reg_Center + params[0];
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(uri);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file


                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }
                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();
                mProgressDialog.setMax(fileLength);
                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(GetAPKPath());
                byte data[] = new byte[4096];
                int total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
//					if (fileLength > 0) // only if total length is known
//					{
//						i = (int) (total * 100 / fileLength);
//					}
                    mProgressDialog.setProgress(total);
//					publishProgress(i);
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }
                if (connection != null)
                    connection.disconnect();
            }
            return "right";
        }

        //��ui�߳���ִ�� ���Բ���ui
        @Override
        protected void onPostExecute(String string) {
            // TODO Auto-generated method stub
            super.onPostExecute(string);
            //������� �Ի������������
            if (string.equals("right")) {
                mProgressDialog.cancel();
                Toast.makeText(context, "�������", Toast.LENGTH_SHORT).show();
                installApk();
            } else {
                mProgressDialog.cancel();
                Toast.makeText(context, "����ʧ��", Toast.LENGTH_LONG).show();
            }


        }

        /*
         * ��doInBackground�������Ѿ�����publishProgress���� ���������ִ�н��Ⱥ�
         * ����������� ʵ�ֽ������ĸ���
         * */
        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);

        }
    }

    /**
     * ��װAPK
     */
    private void installApk() {
        File file = new File(GetAPKPath());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * �������ذ��洢·��
     */
    private String GetAPKPath() {
        File file = new File("/mnt/sdcard/.toInstallPG");
        if (!file.exists()) {
            file.mkdir();
        }
        String path = "/mnt/sdcard/.toInstallPG" + "/" + getAPPName() + nearestVersion + ".apk";
        return path;
    }

    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    /**
     * ��ȡ�������
     */
    public String getAPPName() {
        String appName = "";
        PackageManager pm = context.getPackageManager();//�õ�PackageManager����
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(context.getPackageName(), 0);
            appName = (String) pm.getApplicationLabel(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }

    // ��ʱ���ת���ַ���
    public static String getDateToString(long time) {
        Date d = new Date(time);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return sf.format(d);
    }

    /**
     *����ע�ᴰ��
     */
    public void showRegDialog() {

        View v = LayoutInflater.from(context).inflate(R.layout.reg_dialog, null);
        dialog_Reg = new Dialog(context, R.style.DialogStyle);
        dialog_Reg.setCanceledOnTouchOutside(false);
        dialog_Reg.show();
        dialog_Reg.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog_Reg.dismiss();
                //�˳�����
                 if (cancelCallBack!=null) {
                            cancelCallBack.toFinishActivity();
                        }
            }
        });
        Window window = dialog_Reg.getWindow();
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
                if (!RegOperateTool.isConnected(context.getApplicationContext())) {
                    Toast.makeText(context, "�����쳣�������ֻ�����", Toast.LENGTH_LONG)
                            .show();
                    return;
                }

                final String input = reg.getText().toString();
                if (input == null || TextUtils.isEmpty(input)) {
                    Toast.makeText(context, "������ע����",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // ������֤��
                progressDialog = ProgressDialog.show(context, "���Ժ�",
                        "ע������֤���벻Ҫ������������", true);
                progressDialog.setCancelable(true);

                getRequestQueue();

                StringRequest stringRequest = new StringRequest(
                        Request.Method.POST, RegOperateTool.URL_Reg_Center + "/WebService/SoftWare.asmx/GetRegisCodeInfo", new Response.Listener<String>() {


                    @Override
                    public void onResponse(String response) {
                        //���ע�����Ѿ�ע��
                        if (!TextUtils.isEmpty(response)) {
                            String json = Getstr(response);
                            try {
                                JSONObject obj = new JSONObject(json);
                                String result = obj.getString("Result");
                                JSONArray mArray = obj.getJSONArray("Model");
                                if (!TextUtils.isEmpty(result) && result.equals("ok")) {
                                    if (mArray.length() == 0) {
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "ע���벻����",
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        JSONObject obj_ = (JSONObject) mArray.get(0);
                                        String regStatus = obj_.getString("RegisCodeState").trim();
                                        String guestName = obj_.getString("Customer").trim();
                                        String Imei = obj_.getString("Imei").trim();
                                        String MAC = obj_.getString("MAC").trim();
                                        String isToolTip = obj_.getString("isToolTip").trim();
                                        String isNumber = obj_.getString("isNumber").trim();
                                        String Version = obj_.getString("Version").trim();
                                        if (!getAPPVersion().equals(Version)) {
                                            progressDialog.dismiss();
                                            Toast.makeText(context, "��ע�����Ѱ�����汾������ϵ����Ա",
                                                    Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                        if (regStatus.equals("����")) {
                                            if (TextUtils.isEmpty(Imei)) {//˵����ע����û�а�IMEI
                                                if (TextUtils.isEmpty(MAC)) {//Ҳû�а�MAC
                                                    RegSuccess(input, guestName, isToolTip,isNumber);
                                                } else {
                                                    //TODO ���Mac�Ƿ�һ��
                                                    if (macAddress().equals(MAC)) {
                                                        RegSuccess(input, guestName, isToolTip,isNumber);
                                                    } else {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(context, "��ȷ��ע����󶨵��ֻ�(MAC)�Ƿ���ȷ",
                                                                Toast.LENGTH_LONG).show();
                                                    }

                                                }

                                            } else {//��֤ע�����Ƿ�ƥ��
                                                if (GetImei().equals(Imei)) {
                                                    if (TextUtils.isEmpty(MAC)) {
                                                        RegSuccess(input, guestName, isToolTip,isNumber);
                                                    } else {
                                                        //TODO ���Mac�Ƿ�һ��
                                                        if (macAddress().equals(MAC)) {
                                                            RegSuccess(input, guestName, isToolTip,isNumber);
                                                        } else {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(context, "��ȷ��ע����󶨵��ֻ�(MAC)�Ƿ���ȷ",
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    }

                                                } else {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(context, "��ȷ��ע����󶨵��ֻ�(IMEI)�Ƿ���ȷ",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        } else if (regStatus.equals("�ѹ���")) {
                                            progressDialog.dismiss();
                                            Toast.makeText(context, "ע�����ѹ��ڣ�����ϵ����Ա", Toast.LENGTH_LONG).show();
                                        } else if (regStatus.equals("�����þ�")) {
                                            progressDialog.dismiss();
                                            Toast.makeText(context, "ע������ô������þ�������ϵ����Ա", Toast.LENGTH_LONG).show();
                                        } else if (regStatus.equals("�ѽ���")) {
                                            progressDialog.dismiss();
                                            Toast.makeText(context, "ע������Ч������ϵ����Ա", Toast.LENGTH_LONG).show();
                                        }
                                    }


                                } else {
                                    Toast.makeText(context, "�����������쳣", Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }


                        }


                    }


                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(
                            VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "�������쳣������ϵ����Ա", Toast.LENGTH_SHORT).show();


                    }
                }) {
                    @Override
                    protected Map<String, String> getParams()
                            throws AuthFailureError {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("regisCode", input);
                        map.put("softwareId", RegOperateTool.APP_MARK);
                        map.put("softwareType", "mb");
                        map.put("phoneMessage", getPhoneMessage());
                        return map;
                    }


                };

                myRequestQueue.add(stringRequest);


            }
        };

        ib.setOnClickListener(listener);

    }








    /**
     * ��ʼ����λ
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void initLocation() {
        //��ʼ��client
        locationClient = new AMapLocationClient(context.getApplicationContext());
        // ���ö�λ����
        locationClient.setLocationListener(locationListener);
        startLocation();
    }

    /**
     * Ĭ�ϵĶ�λ����
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//��ѡ�����ö�λģʽ����ѡ��ģʽ�и߾��ȡ����豸�������硣Ĭ��Ϊ�߾���ģʽ
        mOption.setGpsFirst(false);//��ѡ�������Ƿ�gps���ȣ�ֻ�ڸ߾���ģʽ����Ч��Ĭ�Ϲر�
        mOption.setHttpTimeOut(30000);//��ѡ��������������ʱʱ�䡣Ĭ��Ϊ30�롣�ڽ��豸ģʽ����Ч
        mOption.setInterval(2000);//��ѡ�����ö�λ�����Ĭ��Ϊ2��
        mOption.setNeedAddress(true);//��ѡ�������Ƿ񷵻�������ַ��Ϣ��Ĭ����true
        mOption.setOnceLocation(true);//��ѡ�������Ƿ񵥴ζ�λ��Ĭ����false
//		mOption.setOnceLocationLatest(false);//��ѡ�������Ƿ�ȴ�wifiˢ�£�Ĭ��Ϊfalse.�������Ϊtrue,���Զ���Ϊ���ζ�λ��������λʱ��Ҫʹ��
//		AMapLocationClientOption.setLocationProtocol(AMapLocationProtocol.HTTP);//��ѡ�� �������������Э�顣��ѡHTTP����HTTPS��Ĭ��ΪHTTP
//		mOption.setSensorEnable(false);//��ѡ�������Ƿ�ʹ�ô�������Ĭ����false
        return mOption;
    }
    /**
     * ��ʼ��λ
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void startLocation() {
//		//���ݿؼ���ѡ���������ö�λ����
//		resetOption();
        // ���ö�λ����
        locationClient.setLocationOption(getDefaultOption());
        // ������λ
        locationClient.startLocation();
    }

    /**
     * ֹͣ��λ
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void stopLocation() {
        // ֹͣ��λ
        locationClient.stopLocation();
    }

    /**
     * ���ٶ�λ
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void destroyLocation() {
        if (null != locationClient) {
            /**
             * ���AMapLocationClient���ڵ�ǰActivityʵ�����ģ�
             * ��Activity��onDestroy��һ��Ҫִ��AMapLocationClient��onDestroy
             */
            stopLocation();
            locationClient.onDestroy();
            locationClient = null;
        }
    }
    /**
     * ��λ����
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation loc) {
            if (null != loc) {
                //������λ���
                Lat = loc.getLatitude() + "";
                Lng = loc.getLongitude() + "";
                Addr = loc.getAddress();
                CheckSavedVersion();
            }
        }
    };

    private String getPhoneMessage() {
        String lac = "";
        String cid = "";
        String nid = "";
        String imei = "";
        String phoneNo = "";
        String imsi = "";
        String mac = "";
        StringBuffer phoneInfo_sb = new StringBuffer();
        TelephonyManager mTManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (mTManager != null) {
            imei = mTManager.getDeviceId();
            phoneNo = mTManager.getLine1Number();
            imsi = mTManager.getSubscriberId();
        }
        mac = macAddress();
        phoneInfo_sb.append("PhoneNo:" + phoneNo + "," + "Imei:" + imei + "," + "Imsi:" + imsi + "," + "Mac:" + mac + ",");

        if (mTManager != null) {
            int phonetype = mTManager.getPhoneType();
            if (phonetype == TelephonyManager.PHONE_TYPE_GSM) {
                GsmCellLocation gcl = (GsmCellLocation) mTManager.getCellLocation();
                cid = gcl.getCid() + "";
                lac = gcl.getLac() + "";
            } else if (phonetype == TelephonyManager.PHONE_TYPE_CDMA) {
                CdmaCellLocation gcl = (CdmaCellLocation) mTManager
                        .getCellLocation();
                if (gcl != null) {
                    nid = gcl.getNetworkId() + "";// nid
                    cid = gcl.getBaseStationId() + "";// cellid
                    lac = gcl.getSystemId() + ""; // sid
                }

            }
            phoneInfo_sb.append("Lat:" + Lat + "," + "Log:" + Lng + "," + "Lac:" + lac + "," + "Cid:" + cid + "," + "Nid:" + nid + "," + "Addr:" + Addr);
        }
        return phoneInfo_sb.toString();
    }
    /**
     * ��ȡ����汾��
     */
    private String getAPPVersion() {
        PackageManager pm = context.getPackageManager();//�õ�PackageManager����
        String version_app = "";
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);//�õ�PackageInfo���󣬷�װ��һЩ���������Ϣ������
            version_app = pi.versionName;//��ȡ�嵥�ļ���versionCode�ڵ��ֵ
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version_app;
    }


    public String macAddress() {
        String address = null;
        try {
            // �ѵ�ǰ�����ϵķ�������ӿڵĴ��� Enumeration������
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface netWork = interfaces.nextElement();
                // �������Ӳ����ַ������ʹ�ø����ĵ�ǰȨ�޷��ʣ��򷵻ظ�Ӳ����ַ��ͨ���� MAC����
                byte[] by = netWork.getHardwareAddress();
                if (by == null || by.length == 0) {
                    continue;
                }
                StringBuilder builder = new StringBuilder();
                for (byte b : by) {
                    builder.append(String.format("%02X:", b));
                }
                if (builder.length() > 0) {
                    builder.deleteCharAt(builder.length() - 1);
                }
                String mac = builder.toString();
                // ��·�����������豸��MAC��ַ�б�����ӡ֤�豸Wifi�� name �� wlan0
                if (netWork.getName().equals("wlan0")) {
                    address = mac;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return address;
    }


    /**
     * ע������֤�ɹ���
     */
    private void RegSuccess(String input, String guestName, String isToolTip,String isNumber) {
        Toast.makeText(context, "ע������֤�ɹ�",
                Toast.LENGTH_LONG).show();
        strreg = input;
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("OBJREG", input);
        editor.putString("GUESTNAME", guestName);
        if (isToolTip.equals("0")) {//0������ʾ
            istoolTip = false;
            editor.putBoolean("ISTOOLTIP", false);
        } else {
            istoolTip = true;
            editor.putBoolean("ISTOOLTIP", true);
        }
        if (isNumber.equals("0")) {//0�����д�������
            isNumberLimit =true;
            editor.putBoolean("ISNUMBER", true);
        } else {
            isNumberLimit =false;
            editor.putBoolean("ISNUMBER", false);
        }
        editor.commit();
        dialog_Reg.dismiss();
        progressDialog.dismiss();

    }
    private String GetImei() {

        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        return imei;
    }

    private void CheckSavedVersion() {
        String savedVersion = sp.getString("SavedVersion", "");
//        String savedVersion = "1.0";
        String nowVersion = getAPPVersion();
        if (savedVersion.equals("")) {
            SharedPreferences.Editor et = sp.edit();
            et.putString("SavedVersion", nowVersion);
            et.commit();
        } else {
            if (!savedVersion.equals(nowVersion) && Double.parseDouble(nowVersion) > Double.parseDouble(savedVersion)) {
                //�ϴ��汾��Ϣ
                String info = GetInfoWhenVersionChanged(savedVersion, nowVersion);
                UploadVersionInfo(info);
            }

        }

    }

    private String GetInfoWhenVersionChanged(String originalVersion, String newestVersion) {
        String PhoneNo = "";
        String Imei = "";
        String time = "";
        time = RegOperateTool.getDateToString(System.currentTimeMillis());
        StringBuffer Info_sb = new StringBuffer();
        TelephonyManager mTManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (mTManager != null) {
            Imei = mTManager.getDeviceId();
            PhoneNo = mTManager.getLine1Number();
        }
        Info_sb.append("SoftName:" + getAPPName() + "," + "GuestName:" + sp.getString("GUESTNAME", "") + "," + "RegCode:" + strreg + "," + "PhoneNo:" + PhoneNo + "," + "Imei:" + Imei + "," + "Mac:" + macAddress() + "," + "Lat:" + Lat + "," + "Lng:" + Lng + "," + "Addr:" + Addr + "," + "OriginalVersion:" + originalVersion + "," + "NewestVersion:" + newestVersion + "," + "Time:" + time);

        return Info_sb.toString();
    }


    private void UploadVersionInfo(final String info) {
        getRequestQueue();
        String url = RegOperateTool.URL_Reg_Center + "/WebService/SoftWare.asmx/SetVersionInfo";
        StringRequest mStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (s != null && !TextUtils.isEmpty(s)) {
                    SharedPreferences.Editor et = sp.edit();
                    et.putString("SavedVersion", getAPPVersion());
                    et.commit();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("regisCode", strreg);
                map.put("versionMsg", info);
                return map;
            }
        };
        myRequestQueue.add(mStringRequest);
    }
    public void SetCancelCallBack(CancelCallBack callBack){
        this.cancelCallBack = callBack;
    }
    public interface CancelCallBack{
        void toFinishActivity();
    }
}
