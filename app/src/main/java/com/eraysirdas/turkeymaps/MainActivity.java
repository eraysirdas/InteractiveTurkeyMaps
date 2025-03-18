package com.eraysirdas.turkeymaps;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
     final String svgUrl = "file:///android_asset/tr.svg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        WebView webView = findViewById(R.id.webView);

        // WebView ayarları
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // JavaScript'i etkinleştir
        webSettings.setBuiltInZoomControls(true); // Yerleşik zoom kontrollerini etkinleştir
        webSettings.setDisplayZoomControls(false); // Zoom kontrollerini gizle (isteğe bağlı)
        webSettings.setSupportZoom(true); // Zoom'u destekle
        webSettings.setUseWideViewPort(true); // Geniş görünüm portunu etkinleştir

        webView.setInitialScale(100);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // SVG yüklendikten sonra JavaScript'i enjekte et
                injectJavaScript(view);
            }
        });

        // JavaScript ile iletişim için arayüz ekle
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        // SVG dosyasını yükle (assets klasöründen)
        webView.loadUrl(svgUrl);
    }

    // JavaScript kodunu enjekte et
    private void injectJavaScript(WebView webView) {
        String jsCode =  "var lastClickedPath = null;"+
                "document.addEventListener('click', function(event) {" +
                "    var target = event.target;" +
                "    if (target.tagName.toLowerCase() === 'path') {" +
                "        var id = target.getAttribute('id');" +
                "        var name = target.getAttribute('name');" +
                "    if(lastClickedPath !== null) {"+
                "            lastClickedPath.style.fill = '#6f9c76';" +
                "        }" +
                "        target.style.fill = '#004d00';" +
                "        lastClickedPath = target;" +
                "        Android.onPathClicked(id, name);" +
                "    }" +
                "});";
        webView.evaluateJavascript(jsCode, null);
    }

    // JavaScript ile iletişim için arayüz
    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void onPathClicked(String id, String name) {
            // Tıklanan bölgenin ID ve adını Toast mesajı olarak göster
            //String message = "Tıklanan Bölge: " + name + " (ID: " + id + ")";
            //Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            goDetailsActivity(id,name);
        }
    }

    private void goDetailsActivity(String id, String name) {
        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
        intent.putExtra("name",name);
        intent.putExtra("id",id);
        startActivity(intent);
    }
}