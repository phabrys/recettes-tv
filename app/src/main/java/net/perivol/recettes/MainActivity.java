package net.perivol.recettes;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Affiche le recueil de recettes en plein écran.
 * La navigation (flèches, OK) est gérée par le site ; le bouton Retour remonte
 * l'historique du site, puis quitte l'application depuis l'accueil.
 */
public class MainActivity extends Activity {

    private static final String HOME_URL = "https://recette.d.perivol.net/";
    private static final String RETRY_URL = "recettes://reessayer";
    private static final long RETRY_DELAY_MS = 10_000;
    // Au-delà de cette absence, le site est rechargé au retour dans l'application
    private static final long RELOAD_AFTER_MS = 30 * 60_000;

    private static final String ERROR_HTML = "<!doctype html><html lang=\"fr\"><head><meta charset=\"utf-8\">"
            + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><style>"
            + "html,body{margin:0;height:100%;background:#17120f;color:#f5ece3;"
            + "font-family:system-ui,sans-serif;font-size:clamp(14px,1.6vw,48px)}"
            + "body{display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center}"
            + "h1{font-size:2.4rem;margin:0 0 .6rem}p{color:#bba999;font-size:1.2rem;margin:0 0 2rem}"
            + "a{background:#f4a259;color:#1d130b;text-decoration:none;font-weight:700;font-size:1.3rem;"
            + "padding:.7rem 1.6rem;border-radius:.8rem;outline:none}"
            + "a:focus{box-shadow:0 0 0 .25rem #f5ece3}</style></head><body>"
            + "<h1>🍳 Recettes indisponibles</h1>"
            + "<p>Le serveur ne répond pas. Nouvel essai automatique dans quelques secondes…</p>"
            + "<a href=\"" + RETRY_URL + "\" id=\"r\">Réessayer</a>"
            + "<script>document.getElementById('r').focus()</script></body></html>";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable retry = this::loadHome;
    private WebView web;
    private boolean showingError;
    private boolean clearHistoryWhenLoaded;
    private long pausedAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // L'écran reste allumé tant que l'application est ouverte (pratique en cuisinant)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        web = new WebView(this);
        web.setBackgroundColor(0xFF17120F);
        setContentView(web);

        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        // Ignore le réglage de taille de police du système : le site gère lui-même ses tailles
        settings.setTextZoom(100);

        web.setWebViewClient(new Client());
        if (savedInstanceState == null || web.restoreState(savedInstanceState) == null) {
            web.loadUrl(HOME_URL);
        }
        web.requestFocus();
    }

    private void loadHome() {
        handler.removeCallbacks(retry);
        if (showingError) {
            showingError = false;
            clearHistoryWhenLoaded = true;
        }
        web.loadUrl(HOME_URL);
    }

    private void showError() {
        showingError = true;
        web.loadDataWithBaseURL(null, ERROR_HTML, "text/html", "utf-8", null);
        handler.removeCallbacks(retry);
        handler.postDelayed(retry, RETRY_DELAY_MS);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (!showingError && web.canGoBack()) web.goBack();
                else finish();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
        // Android TV garde l'application en mémoire : sans rechargement, on reverrait
        // indéfiniment l'ancienne version du site et ses recettes
        if (pausedAt != 0 && SystemClock.elapsedRealtime() - pausedAt > RELOAD_AFTER_MS) {
            clearHistoryWhenLoaded = true;
            loadHome();
        }
        pausedAt = 0;
    }

    @Override
    protected void onPause() {
        pausedAt = SystemClock.elapsedRealtime();
        web.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(retry);
        web.destroy();
        super.onDestroy();
    }

    private class Client extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri url = request.getUrl();
            if (RETRY_URL.equals(url.toString())) {
                loadHome();
                return true;
            }
            // Reste sur le site des recettes, ignore les liens externes
            return !Uri.parse(HOME_URL).getHost().equals(url.getHost());
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (request.isForMainFrame()) showError();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (clearHistoryWhenLoaded && url.startsWith(HOME_URL)) {
                clearHistoryWhenLoaded = false;
                view.clearHistory();
            }
        }
    }
}
