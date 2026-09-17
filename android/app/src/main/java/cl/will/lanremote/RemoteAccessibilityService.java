package cl.will.lanremote;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.WindowManager;
import android.view.Gravity;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Locale;

public class RemoteAccessibilityService extends AccessibilityService {
    private static volatile RemoteAccessibilityService instance;
    private Handler mainHandler;
    private WindowManager cursorWm;
    private View cursorView;
    private WindowManager.LayoutParams cursorLp;
    private float cursorX=100,cursorY=100;

    @Override
    protected void onServiceConnected() {
        instance = this;
        mainHandler = new Handler(Looper.getMainLooper());
        cursorWm = (WindowManager)getSystemService(WINDOW_SERVICE);
        createCursor();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) { }

    @Override
    public void onInterrupt() { }

    @Override
    public void onDestroy() {
        if (cursorView != null) { try { cursorWm.removeView(cursorView); } catch (Throwable ignored) {} cursorView=null; }
        if (instance == this) instance = null;
        super.onDestroy();
    }

    private Point currentScreenSize() {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point p = new Point();
        display.getRealSize(p);
        return p;
    }

    private float xFromNorm(int n, int width) {
        return (Math.max(0, Math.min(65535, n)) / 65535.0f) * Math.max(1, width - 1);
    }

    private float yFromNorm(int n, int height) {
        return (Math.max(0, Math.min(65535, n)) / 65535.0f) * Math.max(1, height - 1);
    }

    private void tapInternal(int nx, int ny) {
        Point size = currentScreenSize();
        Path path = new Path();
        path.moveTo(xFromNorm(nx, size.x), yFromNorm(ny, size.y));
        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 0, 55))
                .build();
        dispatchGesture(gesture, null, null);
    }

    private void swipeInternal(int x1, int y1, int x2, int y2, int durationMs) {
        Point size = currentScreenSize();
        Path path = new Path();
        path.moveTo(xFromNorm(x1, size.x), yFromNorm(y1, size.y));
        path.lineTo(xFromNorm(x2, size.x), yFromNorm(y2, size.y));
        int duration = Math.max(80, Math.min(2000, durationMs));
        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 0, duration))
                .build();
        dispatchGesture(gesture, null, null);
    }


    private void createCursor() {
        if (cursorView != null) return;
        View v = new View(this);
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL); d.setColor(Color.argb(210,255,255,255)); d.setStroke(3,Color.BLACK);
        v.setBackground(d);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(28,28,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,android.graphics.PixelFormat.TRANSLUCENT);
        lp.gravity=Gravity.TOP|Gravity.LEFT; lp.x=(int)cursorX; lp.y=(int)cursorY;
        cursorView=v; cursorLp=lp; v.setVisibility(View.GONE); cursorWm.addView(v,lp);
    }

    private void setCursorVisibleInternal(boolean show) { if(cursorView==null)createCursor(); if(cursorView!=null)cursorView.setVisibility(show?View.VISIBLE:View.GONE); }
    private void moveCursorInternal(int dx,int dy) {
        Point sz=currentScreenSize(); cursorX=Math.max(0,Math.min(sz.x-1,cursorX+dx*1.6f)); cursorY=Math.max(0,Math.min(sz.y-1,cursorY+dy*1.6f));
        if(cursorView==null)createCursor(); if(cursorView!=null){cursorLp.x=(int)cursorX-14;cursorLp.y=(int)cursorY-14;try{cursorWm.updateViewLayout(cursorView,cursorLp);}catch(Throwable ignored){}}
    }
    private void clickCursorInternal(){ Point sz=currentScreenSize(); tapInternal((int)(cursorX*65535f/Math.max(1,sz.x-1)),(int)(cursorY*65535f/Math.max(1,sz.y-1))); }
    private void scrollCursorInternal(int dir){ Point sz=currentScreenSize(); float amount=Math.max(180,sz.y*0.25f); float y2=Math.max(0,Math.min(sz.y-1,cursorY+(dir>0?-amount:amount))); Path path=new Path();path.moveTo(cursorX,cursorY);path.lineTo(cursorX,y2);GestureDescription g=new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path,0,220)).build();dispatchGesture(g,null,null); }

    public static void cursorVisible(final boolean show){final RemoteAccessibilityService s=instance;if(s!=null)s.mainHandler.post(()->s.setCursorVisibleInternal(show));}
    public static void moveCursor(final int dx,final int dy){final RemoteAccessibilityService s=instance;if(s!=null)s.mainHandler.post(()->s.moveCursorInternal(dx,dy));}
    public static void clickCursor(){final RemoteAccessibilityService s=instance;if(s!=null)s.mainHandler.post(()->s.clickCursorInternal());}
    public static void scrollCursor(final int dir){final RemoteAccessibilityService s=instance;if(s!=null)s.mainHandler.post(()->s.scrollCursorInternal(dir));}

    private AccessibilityNodeInfo focusedInput() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return null;
        AccessibilityNodeInfo node = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (node == null) node = root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
        return node;
    }

    private boolean setNodeText(AccessibilityNodeInfo node, String value, int cursor) {
        if (node == null || !node.isEditable()) return false;
        Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value);
        boolean ok = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
        if (ok) {
            Bundle sel = new Bundle();
            int safe = Math.max(0, Math.min(cursor, value.length()));
            sel.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, safe);
            sel.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, safe);
            node.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, sel);
        }
        return ok;
    }

    private boolean commitTextInternal(String incoming) {
        AccessibilityNodeInfo node = focusedInput();
        if (node == null || !node.isEditable()) return false;
        CharSequence cs = node.getText();
        String old = cs == null ? "" : cs.toString();
        int start = node.getTextSelectionStart();
        int end = node.getTextSelectionEnd();
        if (start < 0 || end < 0 || start > old.length() || end > old.length()) {
            start = old.length();
            end = old.length();
        }
        if (start > end) { int tmp = start; start = end; end = tmp; }
        String value = old.substring(0, start) + incoming + old.substring(end);
        return setNodeText(node, value, start + incoming.length());
    }

    private boolean keyInternal(String keyName) {
        AccessibilityNodeInfo node = focusedInput();
        if (node == null || !node.isEditable()) return false;
        String key = keyName.toUpperCase(Locale.US);
        CharSequence cs = node.getText();
        String old = cs == null ? "" : cs.toString();
        int start = node.getTextSelectionStart();
        int end = node.getTextSelectionEnd();
        if (start < 0 || end < 0 || start > old.length() || end > old.length()) {
            start = old.length();
            end = old.length();
        }
        if (start > end) { int tmp = start; start = end; end = tmp; }

        if ("SPACE".equals(key)) return commitTextInternal(" ");

        if ("BACKSPACE".equals(key)) {
            if (start != end) return setNodeText(node, old.substring(0, start) + old.substring(end), start);
            if (start == 0) return true;
            int previous = Character.offsetByCodePoints(old, start, -1);
            return setNodeText(node, old.substring(0, previous) + old.substring(start), previous);
        }

        if ("LEFT".equals(key) || "RIGHT".equals(key)) {
            int pos;
            if (start != end) pos = "LEFT".equals(key) ? start : end;
            else if ("LEFT".equals(key)) pos = start == 0 ? 0 : Character.offsetByCodePoints(old, start, -1);
            else pos = start >= old.length() ? old.length() : Character.offsetByCodePoints(old, start, 1);
            Bundle sel = new Bundle();
            sel.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, pos);
            sel.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, pos);
            return node.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, sel);
        }

        if ("ENTER".equals(key)) {
            if (Build.VERSION.SDK_INT >= 30 && node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER.getId())) return true;
            return commitTextInternal("\n");
        }

        return false;
    }

    public static void tapNormalized(int x, int y) {
        RemoteAccessibilityService s = instance;
        if (s != null) s.tapInternal(x, y);
    }

    public static void swipeNormalized(int x1, int y1, int x2, int y2, int durationMs) {
        RemoteAccessibilityService s = instance;
        if (s != null) s.swipeInternal(x1, y1, x2, y2, durationMs);
    }

    public static void global(String action) {
        RemoteAccessibilityService s = instance;
        if (s == null || action == null) return;
        String a = action.toUpperCase(Locale.US);
        if ("BACK".equals(a)) s.performGlobalAction(GLOBAL_ACTION_BACK);
        else if ("HOME".equals(a)) s.performGlobalAction(GLOBAL_ACTION_HOME);
        else if ("RECENTS".equals(a)) s.performGlobalAction(GLOBAL_ACTION_RECENTS);
        else if ("NOTIFICATIONS".equals(a)) s.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);
    }

    public static void commitRemoteText(final String text) {
        if (RemoteImeService.isAvailable()) { RemoteImeService.commitRemoteText(text); return; }
        final RemoteAccessibilityService s = instance;
        if (s == null || text == null) {
            RemoteImeService.commitRemoteText(text);
            return;
        }
        s.mainHandler.post(new Runnable() {
            @Override public void run() {
                if (!s.commitTextInternal(text)) RemoteImeService.commitRemoteText(text);
            }
        });
    }

    public static void remoteKey(final String keyName) {
        if (RemoteImeService.isAvailable()) { RemoteImeService.remoteKey(keyName); return; }
        final RemoteAccessibilityService s = instance;
        if (s == null || keyName == null) {
            RemoteImeService.remoteKey(keyName);
            return;
        }
        s.mainHandler.post(new Runnable() {
            @Override public void run() {
                if (!s.keyInternal(keyName)) RemoteImeService.remoteKey(keyName);
            }
        });
    }
}
