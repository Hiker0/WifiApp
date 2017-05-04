package com.alllen.mylibrary;

import android.content.Context;
import android.nfc.FormatException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Johnson on 2017-04-09.
 */

public class IpEditView extends LinearLayout {

    private static  String TAG = "IpEditView";
    /**
     * IP[3]:IP[2]:IP[1]:IP[0]
     */
    private TextView mSplit1,mSplit2,mSplit3;
    private String mSplit;
    private EditText[] mIPs = new EditText[4];

    public IpEditView(Context context) {
        this(context, null);
    }
    public IpEditView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public IpEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSplit = getResources().getString(R.string.default_split);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.ip_edit_view,this);
        mIPs[0] = (EditText) findViewById(R.id.ip_0);
        mIPs[1] = (EditText) findViewById(R.id.ip_1);
        mIPs[2] = (EditText) findViewById(R.id.ip_2);
        mIPs[3] = (EditText) findViewById(R.id.ip_3);

        new TextListener(null, mIPs[0],mIPs[1]).register();
        new TextListener(mIPs[0],mIPs[1],mIPs[2]).register();
        new TextListener(mIPs[1],mIPs[2],mIPs[3]).register();
        new TextListener(mIPs[2],mIPs[3],null).register();

        mSplit1 = (TextView) findViewById(R.id.split_1);
        mSplit2 = (TextView) findViewById(R.id.split_2);
        mSplit3 = (TextView) findViewById(R.id.split_2);
    }

    private int checkIpString(String ipstr){
        if(ipstr==null || ipstr.isEmpty() ){
            Log.d(TAG,"ipstr is empty");
            return -1;
        }
        int ip = -1;
        try {
            ip = Integer.parseInt(ipstr);
        }catch (NumberFormatException e){
            Log.d(TAG,"ipstr is not a integer");
            return -1;
        }
        if (ip > 255 || ip < 0) {
            Log.d(TAG,"ip is out of bounce");
            return -1;
        }
        return ip;
    }

    public void setIpAddress(String ipaddress) throws FormatException {
        String[] ips = ipaddress.split("\\"+mSplit);
        if(ips.length != 4){
            throw new FormatException();
        }

        for(int i=0;i<4;i++){
            if(checkIpString(ips[i]) < 0){
                throw new FormatException();
            }
        }

        for(int i=0;i<4;i++){
            mIPs[i].setText(ips[i]);
        }

    }
    void setSplit(String split){
        String substring = split.substring(0,1);
        if(substring != null && substring.length() == 1){
            mSplit = substring;
            mSplit1.setText(mSplit);
            mSplit2.setText(mSplit);
            mSplit3.setText(mSplit);
        }
    }
   public String getIpAddress( ){

        StringBuilder sb = new StringBuilder();
       String defaultIp = getResources().getString(R.string.default_ip);
       int ip;
       for(int i=0; i<4;i++){
           ip = checkIpString(mIPs[i].getText().toString());
           if(ip > 0){
               sb.append(ip);
           }else{
               sb.append(defaultIp);
           }
           if(i != 3) {
               sb.append(mSplit);
           }
       }
        return sb.toString();
    }

    class TextListener implements TextWatcher, View.OnKeyListener, View.OnTouchListener{
        EditText mForward;
        EditText mNext;
        EditText mView;

        TextListener(EditText forward, EditText view ,EditText next){
            mForward = forward;
            mNext = next;
            mView = view;
        }

        public void register(){
            if(mView!=null){
                mView.addTextChangedListener(this);
                mView.setOnKeyListener(this);
                mView.setOnTouchListener(this);
            }
        }
        public void unregister(){
            if(mView!=null){
                mView.removeTextChangedListener(this);
                mView.setOnKeyListener(null);
            }
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP){
                if(mNext != null){
                    mNext.requestFocus();
                    return true;
                }
            }
            if(keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_UP){
                if(mView.getText().length()==0 && mForward != null){
                    mForward.requestFocus();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            Log.d(TAG, "beforeTextChanged:"+s + " " +start +" " +count +" " +  after);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.d(TAG, "onTextChanged:"+s + " " +start +" " +count +" " +  before);
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d(TAG, "afterTextChanged:"+s );
            String content = s.toString();

            if(s.length() == 3 ){
                if (content != null && !content.isEmpty()) {
                        int ip = Integer.parseInt(content);
                        if (ip > 255) {
                            s.delete(s.length() - 1, s.length());
                        }
                }

                if( mNext != null) {
                    mNext.requestFocus();
                }
            }else if(s.length() > 3){
                s.delete(3,s.length());
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
//            if(event.getAction() == MotionEvent.ACTION_UP){
//                v.requestFocus();
//                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.showSoftInput(v,InputMethodManager.SHOW_FORCED);
//            }
            return false;
        }
    }

    interface ErrorListener {
        static int ERR_OUT=0;
        void onError(int code, String msg);
    }
}
