package com.mawaidi.alarmtonepicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.ActivityResultListener;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

@DesignerComponent(
    version = 2,
    description = "Alarm Tone Picker and Player for Mawaidi App",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = ""
)
@SimpleObject(external = true)
public class AlarmTonePicker extends AndroidNonvisibleComponent implements ActivityResultListener {

    private final Context context;
    private final Activity activity;
    private int requestCode = 0;
    private Ringtone currentRingtone;

    public AlarmTonePicker(ComponentContainer container) {
        super(container.$form());
        this.activity = container.$context();
        this.context = container.$context();
    }

    @SimpleFunction(description = "Open system alarm tone picker")
    public void PickAlarmTone() {
        if (requestCode == 0) {
            requestCode = form.registerForActivityResult(this);
        }
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "اختر نغمة المنبه");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_ALARM_ALERT_URI);
        activity.startActivityForResult(intent, requestCode);
    }

    @SimpleFunction(description = "Play alarm tone from URI string")
    public void PlayTone(String uriString) {
        try {
            StopTone();
            Uri uri;
            if (uriString == null || uriString.trim().isEmpty()) {
                uri = Settings.System.DEFAULT_ALARM_ALERT_URI;
            } else {
                uri = Uri.parse(uriString);
            }
            currentRingtone = RingtoneManager.getRingtone(context, uri);
            if (currentRingtone != null) {
                currentRingtone.play();
            }
        } catch (Exception e) {
            ErrorOccurred("Play Error: " + e.getMessage());
        }
    }

    @SimpleFunction(description = "Stop currently playing alarm tone")
    public void StopTone() {
        try {
            if (currentRingtone != null && currentRingtone.isPlaying()) {
                currentRingtone.stop();
            }
        } catch (Exception e) {
            ErrorOccurred("Stop Error: " + e.getMessage());
        }
    }

    @Override
    public void resultReturned(int requestCode, int resultCode, Intent data) {
        if (this.requestCode == requestCode && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                String title = (ringtone != null) ? ringtone.getTitle(context) : "نغمة مخصصة";
                TonePicked(title, uri.toString());
            }
        }
    }

    @SimpleEvent(description = "Triggered when user selects an alarm tone")
    public void TonePicked(String title, String uri) {
        EventDispatcher.dispatchEvent(this, "TonePicked", title, uri);
    }

    @SimpleEvent(description = "Triggered when an error occurs")
    public void ErrorOccurred(String message) {
        EventDispatcher.dispatchEvent(this, "ErrorOccurred", message);
    }
}
