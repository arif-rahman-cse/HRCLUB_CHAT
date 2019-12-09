package com.hrclubbd.hrclub;


import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.hrclubbd.hrclub.ui.LoginActivity;

public class UserPanel extends AppCompatActivity implements View.OnClickListener {

    //wiz
    private Button chatButton;
    private WebView webView;
    private ProgressBar mProgressBar;
    private String url = "https://hrclubbd.com/member-login";
    private ImageView backButton;

    //file Upload
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userpanel);


        webView = findViewById(R.id.web_view);
        mProgressBar = findViewById(R.id.progress_bar);
        chatButton = findViewById(R.id.chat_btn);
        backButton = findViewById(R.id.back_button);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.loadUrl(url);
        webView.setWebViewClient(new MyWebClient());


        webView.setWebChromeClient(new MyWebChromeClient());

        //Set listener
        chatButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
    }



    //--------------------------------------------- set WebViewClient --------------------------
    public class MyWebClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
            try {
                webView.stopLoading();
            } catch (Exception e) {
            }

            if (webView.canGoBack()) {
                webView.goBack();
            }

            webView.loadUrl("about:blank");

            AlertDialog.Builder builder = new AlertDialog.Builder(UserPanel.this);
            builder.setMessage("No Internet Connection Found!")
                    .setCancelable(false).setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    startActivity(getIntent());
                }
            })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UserPanel.super.onBackPressed();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            alertDialog.show();
            super.onReceivedError(webView, errorCode, description, failingUrl);
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            //Hide Specific Part of WebView
            view.loadUrl("javascript:(function() { " +

                    "document.getElementsByClassName('header header-1')[0].style.display='none'; " +
                    "document.getElementsByClassName('section banner-page')[0].style.display='none'; " +
                    "document.getElementsByClassName('cd-top cd-is-visible')[0].style.display='none'; " +
                    "document.getElementsByClassName('footer')[0].style.display='none'; " +

                    "})()");


            webView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            //chatButton.setVisibility(View.VISIBLE);

            if (url.equals("https://hrclubbd.com/member-profile")) {
                chatButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
            }
            if (url.equals("https://hrclubbd.com/member-login")) {
                chatButton.setVisibility(View.INVISIBLE);
                backButton.setVisibility(View.INVISIBLE);
            }
        }

    }


    //-------------------------------------MyWebChromeClient-------------------------------

    private class MyWebChromeClient extends WebChromeClient {

        // For 3.0+ Devices (Start)
        // onActivityResult attached before constructor
        protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
        }


        // For Lollipop 5.0+ Devices
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }

            uploadMessage = filePathCallback;

            Intent intent = fileChooserParams.createIntent();
            try {
                startActivityForResult(intent, REQUEST_SELECT_FILE);
            } catch (ActivityNotFoundException e) {
                uploadMessage = null;
                Toast.makeText(UserPanel.this, "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }

        //For Android 4.1 only
        protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
        }

        protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
        }

    }

    //------------------------------ Permission ------------------------------------

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            // Use UserPanel.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != UserPanel.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else
            Toast.makeText(UserPanel.this, "Failed to Upload Image", Toast.LENGTH_LONG).show();
    }


    // This method is used to detect back button
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to exit?")
                    .setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UserPanel.super.onBackPressed();
                }
            })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    //Set Onclick listener
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.chat_btn) {
            Intent intent = new Intent(UserPanel.this, LoginActivity.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.back_button) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                super.onBackPressed();
            }
        }

    }

}
