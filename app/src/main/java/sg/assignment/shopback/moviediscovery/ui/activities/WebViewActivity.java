package sg.assignment.shopback.moviediscovery.ui.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import cn.pedant.SweetAlert.SweetAlertDialog;
import sg.assignment.shopback.moviediscovery.R;
import sg.assignment.shopback.moviediscovery.network.themoviedb.MovieApi;

public class WebViewActivity extends AppCompatActivity {

    private WebView mWebview ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWebview  = new WebView(this);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.setWebViewClient(new WebViewClient() {

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);

                SweetAlertDialog errorDialog = new SweetAlertDialog(WebViewActivity.this,
                        SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getString(R.string.error_title))
                        .setContentText(description);
                errorDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        onBackPressed();
                    }
                });
                errorDialog.setCancelable(true);
                errorDialog.setCanceledOnTouchOutside(true);
                errorDialog.show();

            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {

                SweetAlertDialog errorDialog = new SweetAlertDialog(WebViewActivity.this,
                        SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getString(R.string.error_title))
                        .setContentText(rerr.getDescription().toString());
                errorDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        onBackPressed();
                    }
                });
                errorDialog.setCancelable(true);
                errorDialog.setCanceledOnTouchOutside(true);
                errorDialog.show();
            }
        });
        mWebview.loadUrl(getString(R.string.cathay_cinema_url));
        setContentView(mWebview);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.book_movie));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.web_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

       if(item.getItemId() == R.id.open_in_browser)
       {
           try {
               Intent myIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.cathay_cinema_url)));
               startActivity(myIntent);
           } catch (ActivityNotFoundException e) {
               e.printStackTrace();
               SweetAlertDialog warningDialog = new SweetAlertDialog(WebViewActivity.this,
                       SweetAlertDialog.WARNING_TYPE)
                       .setTitleText(getString(R.string.no_webbrowser_title))
                       .setContentText(getString(R.string.no_webbrowser_message));
               warningDialog.setCancelable(true);
               warningDialog.setCanceledOnTouchOutside(true);
               warningDialog.show();
           }
           return true;
       }
       else
           finish();

        return super.onOptionsItemSelected(item);
    }

}
