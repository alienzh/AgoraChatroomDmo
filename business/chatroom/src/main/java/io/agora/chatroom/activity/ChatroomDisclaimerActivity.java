package io.agora.chatroom.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;

import java.util.Locale;

import io.agora.baseui.BaseActivity;
import io.agora.buddy.tool.ResourcesTools;
import io.agora.chatroom.R;
import io.agora.secnceui.widget.titlebar.ChatroomTitleBar;

public class ChatroomDisclaimerActivity extends BaseActivity implements ChatroomTitleBar.OnBackPressListener {
    private ChatroomTitleBar titleBar;
    private MaterialTextView content;
    private MaterialTextView end;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS ;
        setNavAndStatusBarTransparent(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_disclaimer_layout;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar = findViewById(R.id.title_bar);
        end = findViewById(R.id.end);
        content = findViewById(R.id.content);
        setTextStyle(titleBar.getTitle(), Typeface.BOLD);
    }

    @Override
    protected void initListener() {
        super.initListener();
        titleBar.setOnBackPressListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        String c = getString(R.string.room_disclaimer_content_1);
        SpannableStringBuilder cBuilder = new SpannableStringBuilder(c);
        StyleSpan styleSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        if (ResourcesTools.isZh(this)){
            cBuilder.setSpan(styleSpan,0,10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }else {
            cBuilder.setSpan(styleSpan,0,22, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        content.setText(cBuilder);

        end.setMovementMethod(LinkMovementMethod.getInstance());
        String e = getString(R.string.room_disclaimer_email);
        SpannableStringBuilder eBuilder = new SpannableStringBuilder(e);
        ForegroundColorSpan graySpan = new ForegroundColorSpan(getResources().getColor(R.color.color_156EF3));
        eBuilder.setSpan(graySpan,e.length()-16,e.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        eBuilder.setSpan(new UnderlineSpan(), e.length()-16, e.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        eBuilder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Log.e("SpannableStringBuilder","onClick");
                sendEmail();
            }
        }, e.length() - 16, e.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        end.setText(eBuilder);
    }

    @Override
    protected void initSystemFit() {
        setFitSystemForTheme(false, "#00000000");
        setStatusBarTextColor(false);
    }

    @Override
    public void onBackPress(View view) {
        onBackPressed();
    }

    private void sendEmail() {
        Intent email = new Intent(android.content.Intent.ACTION_SEND);
        //??????????????????????????????????????????
        email.setType("plain/text");
        //??????????????????????????????????????????????????????
        String[] emailReciver = new String[]{"support@agora.io.com"};

        //??????????????????
        email.putExtra(android.content.Intent.EXTRA_EMAIL, emailReciver);
        //??????????????????
        email.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        //?????????????????????
        email.putExtra(android.content.Intent.EXTRA_TEXT, "");
        //???????????????????????????
        startActivity(Intent.createChooser(email, ResourcesTools.isZh(this)?"???????????????????????????":"Please select the mail sending software"));
    }
}
