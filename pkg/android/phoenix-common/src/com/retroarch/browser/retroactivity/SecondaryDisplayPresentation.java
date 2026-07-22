package com.retroarch.browser.retroactivity;

import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.util.Log;

/**
 * Presentation that displays game data (like RetroAchievements) on a secondary screen.
 */
public class SecondaryDisplayPresentation extends Presentation {

    private static final String TAG = "SecondaryDisplayPresentation";
    private WebView mWebView;
    private String mPendingJson = null;
    private boolean mIsPageLoaded = false;

    public SecondaryDisplayPresentation(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(getContext());
        root.setBackgroundColor(Color.BLACK);

        mWebView = new WebView(getContext());
        mWebView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mWebView.setBackgroundColor(Color.BLACK);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "WebView page finished loading: " + url);
                mIsPageLoaded = true;
                if (mPendingJson != null) {
                    Log.i(TAG, "Flushing pending JSON after page load.");
                    updateData(mPendingJson);
                    mPendingJson = null;
                }
            }
        });

        root.addView(mWebView);
        setContentView(root);

        loadBaseLayout();
    }

    private void loadBaseLayout() {
        String html = "<html><head>" +
                "<style>" +
                "body { background-color: #0F111A; color: #E0E0E0; font-family: sans-serif; padding: 0; margin: 0; overflow: hidden; }" +
                ".dashboard { position: fixed; top: 0; left: 0; right: 0; height: 120px; background: #1A1C2E; display: flex; flex-direction: column; padding: 8px 10px 0 10px; border-bottom: 4px solid #00BFA5; z-index: 100; box-sizing: border-box; }" +
                ".game-title { font-size: 19px; font-weight: 800; color: #00BFA5; margin: 0 0 2px 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; width: 100%; flex-shrink: 0; text-align: center; }" +
                ".telemetry-grid { display: flex; flex-direction: column; flex: 1; justify-content: center; align-items: center; width: 100%; }" +
                ".telemetry-row { display: flex; justify-content: center; width: 100%; gap: 15px; }" +
                ".column { display: flex; flex-direction: column; align-items: center; min-width: 70px; }" +
                ".val-text { font-size: 18px; font-weight: bold; color: #FFF; line-height: 1.0; margin-bottom: 2px; }" +
                ".anchored-row { display: flex; align-items: baseline; justify-content: space-between; width: 100%; padding-bottom: 0px; margin-bottom: -1px; flex-shrink: 0; }" +
                ".game-progress { font-size: 12px; color: #888; font-weight: 900; white-space: nowrap; flex: 0 0 auto; }" +
                ".labels-container { display: flex; justify-content: center; gap: 15px; flex: 1; align-items: baseline; }" +
                ".label { font-size: 10px; color: #888; font-weight: 900; text-transform: uppercase; letter-spacing: 0.5px; width: 70px; text-align: center; }" +
                ".clock-container { text-align: right; flex: 0 0 auto; }" +
                ".clock { font-size: 12px; font-weight: 900; color: #888; }" +
                ".progress-bar-bg { width: calc(100% + 20px); margin-left: -10px; height: 8px; background: #2A2E45; overflow: hidden; flex-shrink: 0; }" +
                ".progress-bar-fill { height: 100%; background: #4CAF50; width: 0%; transition: width 0.5s ease-out; }" +
                ".content { margin-top: 120px; height: calc(100vh - 120px); overflow-y: auto; padding: 18px; box-sizing: border-box; scroll-behavior: smooth; }" +
                ".achievement { display: flex; align-items: center; margin-bottom: 12px; padding: 12px; background: #1E2132; border-radius: 10px; border: 1px solid #2A2E45; position: relative; overflow: hidden; }" +
                ".achievement.unlocked { border-left: 4px solid #4CAF50; background: #242938; }" +
                ".achievement.challenge { border: 2px solid #FFD600; background: #2A2410; box-shadow: 0 0 15px rgba(255, 214, 0, 0.2); transform: scale(1.01); }" +
                ".achievement-fill { position: absolute; top: 0; left: 0; bottom: 0; background: rgba(0, 191, 165, 0.1); z-index: 0; transition: width 0.5s ease-out; }" +
                ".icon { width: 56px; height: 56px; margin-right: 15px; background: #2A2E45; border-radius: 6px; flex-shrink: 0; z-index: 1; }" +
                ".info { flex-grow: 1; min-width: 0; z-index: 1; }" +
                ".title { font-size: 16px; font-weight: bold; margin: 0; color: #FFF; }" +
                ".desc { font-size: 12px; color: #B0B0B0; margin: 4px 0 0 0; line-height: 1.3; }" +
                ".achievement-footer { display: flex; align-items: center; justify-content: space-between; margin-top: 6px; }" +
                ".points { font-size: 11px; color: #FFD600; font-weight: 800; }" +
                ".step-progress { font-size: 11px; color: #00BFA5; font-weight: bold; font-family: monospace; }" +
                ".badge-pill { position: absolute; top: 0; right: 0; font-size: 9px; font-weight: 900; padding: 1px 8px; border-bottom-left-radius: 6px; text-transform: uppercase; z-index: 2; color: #000; }" +
                ".badge-missable { background: #FF5252; color: #FFF; }" +
                ".badge-progression { background: #00BFA5; }" +
                ".badge-win { background: #FFD600; }" +
                ".badge-challenge { border: 1px solid #FFD600; background: rgba(255, 214, 0, 0.2); color: #FFD600; }" +
                "</style>" +
                "<script>" +
                "function formatTemp(t) { return t > 0 ? (t/1000).toFixed(1) + '°' : '--'; }" +
                "let lastAchievementIds = '';" +
                "function update(data) {" +
                "  if(!data) return;" +
                "  const titleEl = document.getElementById('game-title');" +
                "  if(titleEl) titleEl.innerText = data.game_title || 'RetroArch Lnk';" +
                "  " +
                "  if(document.getElementById('fps')) document.getElementById('fps').innerText = data.fps ? Math.round(data.fps) : '--';" +
                "  if(document.getElementById('frametime')) document.getElementById('frametime').innerText = data.frametime ? data.frametime.toFixed(1) + 'ms' : '--';" +
                "  if(document.getElementById('cpu_util')) document.getElementById('cpu_util').innerText = Math.round(data.cpu_util || 0) + '%';" +
                "  if(document.getElementById('gpu_util')) document.getElementById('gpu_util').innerText = Math.round(data.gpu_util || 0) + '%';" +
                "  if(document.getElementById('power_w')) document.getElementById('power_w').innerText = data.power_w ? data.power_w.toFixed(1) + 'W' : '--';" +
                "  if(document.getElementById('temp_cpu')) document.getElementById('temp_cpu').innerText = formatTemp(data.temp_cpu);" +
                "  if(document.getElementById('temp_gpu')) document.getElementById('temp_gpu').innerText = formatTemp(data.temp_gpu);" +
                "  if(document.getElementById('battery')) document.getElementById('battery').innerText = (data.battery !== undefined) ? data.battery + '%' : '--';" +
                "  " +
                "  if (data.achievements) {" +
                "    const total = data.achievements.length;" +
                "    const unlocked = data.achievements.filter(a => a.unlocked).length;" +
                "    const percent = total > 0 ? Math.round((unlocked / total) * 100) : 0;" +
                "    document.getElementById('progress-text').innerText = unlocked + ' / ' + total + ' (' + percent + '%)';" +
                "    document.getElementById('progress-fill').style.width = percent + '%';" +
                "    " +
                "    const currentIds = data.achievements.map(a => a.title + a.unlocked + a.progress_text + a.is_challenge + a.type).join('|');" +
                "    if (currentIds === lastAchievementIds) return;" +
                "    lastAchievementIds = currentIds;" +
                "    const list = document.getElementById('achievement-list');" +
                "    if(!list) return;" +
                "    let html = '';" +
                "    let challengeId = null;" +
                "    data.achievements.forEach(a => {" +
                "      const statusClass = a.unlocked ? 'unlocked' : (a.is_challenge ? 'challenge' : 'locked');" +
                "      if(a.is_challenge) challengeId = 'cheevo-' + a.title.replace(/\\s+/g, '-');" +
                "      " +
                "      /* Robust Step Progress check: ensure it's a non-empty string and not just '0%' or similar boilerplate */" +
                "      const hasProgress = a.progress_text && a.progress_text.length > 0;" +
                "      " +
                "      const fillWidth = a.unlocked ? 100 : (a.progress_percent || 0);" +
                "      " +
                "      let typeBadge = '';" +
                "      if (a.is_challenge) typeBadge = '<div class=\"badge-pill badge-challenge\">Active Challenge</div>';" +
                "      else if (a.type === 1) typeBadge = '<div class=\"badge-pill badge-missable\">Missable</div>';" +
                "      else if (a.type === 2) typeBadge = '<div class=\"badge-pill badge-progression\">Progression</div>';" +
                "      else if (a.type === 3) typeBadge = '<div class=\"badge-pill badge-win\">Win Condition</div>';" +
                "      " +
                "      html += '<div id=\"' + (a.is_challenge ? challengeId : '') + '\" class=\"achievement ' + statusClass + '\">' +" +
                "              typeBadge +" +
                "              '<div class=\"achievement-fill\" style=\"width:' + fillWidth + '%\"></div>' +" +
                "              '<img class=\"icon\" src=\"' + (a.unlocked ? a.badge_url : a.badge_locked_url) + '\">' +" +
                "              '<div class=\"info\">' +" +
                "              '<p class=\"title\">' + a.title + '</p>' +" +
                "              '<p class=\"desc\">' + a.description + '</p>' +" +
                "              '<div class=\"achievement-footer\">' +" +
                "              '<span class=\"points\">🪙 ' + a.points + ' Points</span>' +" +
                "              (hasProgress ? '<span class=\"step-progress\">' + a.progress_text + '</span>' : '') +" +
                "              '</div></div></div>';" +
                "    });" +
                "    list.innerHTML = html;" +
                "    if(challengeId) {" +
                "       setTimeout(() => {" +
                "         const el = document.getElementById(challengeId);" +
                "         if(el) el.scrollIntoView({ behavior: 'smooth', block: 'center' });" +
                "       }, 100);" +
                "    }" +
                "  }" +
                "}" +
                "setInterval(() => {" +
                "  const clockEl = document.getElementById('clock');" +
                "  if(clockEl) {" +
                "    const now = new Date();" +
                "    const h = String(now.getHours()).padStart(2, '0');" +
                "    const m = String(now.getMinutes()).padStart(2, '0');" +
                "    clockEl.innerText = h + ':' + m;" +
                "  }" +
                "}, 1000);" +
                "</script>" +
                "</head><body>" +
                "<div class='dashboard'>" +
                "  <p class='game-title' id='game-title'>Waiting...</p>" +
                "  <div class='telemetry-grid'>" +
                "    <div class='telemetry-row'>" +
                "      <div class='column'><span class='val-text' id='frametime'>--ms</span><span class='val-text' id='fps'>--</span></div>" +
                "      <div class='column'><span class='val-text' id='cpu_util'>--%</span><span class='val-text' id='temp_cpu'>--°</span></div>" +
                "      <div class='column'><span class='val-text' id='gpu_util'>--%</span><span class='val-text' id='temp_gpu'>--°</span></div>" +
                "      <div class='column'><span class='val-text' id='battery'>--%</span><span class='val-text' id='power_w'>--W</span></div>" +
                "    </div>" +
                "  </div>" +
                "  <div class='anchored-row'>" +
                "    <div class='game-progress' id='progress-text'>-- / --</div>" +
                "    <div class='labels-container'>" +
                "      <span class='label'>FRAME</span>" +
                "      <span class='label'>CPU</span>" +
                "      <span class='label'>GPU</span>" +
                "      <span class='label'>BATT</span>" +
                "    </div>" +
                "    <div class='clock-container'><span class='clock' id='clock'>--:--</span></div>" +
                "  </div>" +
                "  <div class='progress-bar-bg'><div class='progress-bar-fill' id='progress-fill'></div></div>" +
                "</div>" +
                "<div class='content' id='achievement-list'></div>" +
                "</body></html>";

        mWebView.loadDataWithBaseURL("https://retroarch.dual", html, "text/html", "UTF-8", null);
    }

    public void updateData(final String json) {
        if (!mIsPageLoaded) {
            mPendingJson = json;
            return;
        }

        if (mWebView == null) return;

        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.evaluateJavascript("update(" + json + ");", null);
            }
        });
    }
}
