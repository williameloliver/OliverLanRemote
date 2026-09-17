package cl.will.lanremote;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

import java.util.Locale;

public class RemoteImeService extends InputMethodService {
    private static volatile RemoteImeService instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public void onDestroy() {
        if (instance == this) instance = null;
        super.onDestroy();
    }

    public static boolean isAvailable() { return instance != null; }

    public static void commitRemoteText(final String text) {
        final RemoteImeService s = instance;
        if (s == null || text == null) return;
        s.getMainExecutor().execute(new Runnable() {
            @Override public void run() {
                InputConnection ic = s.getCurrentInputConnection();
                if (ic != null) ic.commitText(text, 1);
            }
        });
    }

    public static void remoteKey(final String keyName) {
        final RemoteImeService s = instance;
        if (s == null || keyName == null) return;
        s.getMainExecutor().execute(new Runnable() {
            @Override public void run() {
                InputConnection ic = s.getCurrentInputConnection();
                if (ic == null) return;
                String k = keyName.toUpperCase(Locale.US);
                int code = 0;
                if ("SPACE".equals(k)) { ic.commitText(" ", 1); return; }
                if ("BACKSPACE".equals(k)) code = KeyEvent.KEYCODE_DEL;
                else if ("ENTER".equals(k)) code = KeyEvent.KEYCODE_ENTER;
                else if ("LEFT".equals(k)) code = KeyEvent.KEYCODE_DPAD_LEFT;
                else if ("RIGHT".equals(k)) code = KeyEvent.KEYCODE_DPAD_RIGHT;
                else if ("UP".equals(k)) code = KeyEvent.KEYCODE_DPAD_UP;
                else if ("DOWN".equals(k)) code = KeyEvent.KEYCODE_DPAD_DOWN;
                if (code != 0) {
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, code));
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, code));
                }
            }
        });
    }
}
