package com.mawaidi.alarmtonepicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;

@DesignerComponent(
    version = 1,
    description = "Pick and Play System Alarm Tones",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = ""
)
@SimpleObject(external = true)
public class AlarmTonePicker extends AndroidNonvisibleComponent implements ActivityResultListener {

    private final Activity activity;
    private final Context context;
    private int requestCode = 0;
    private MediaPlayer mediaPlayer;

    public AlarmTonePicker(ComponentContainer container) {
        super(container.$form());
        this.activity = container.$context();
        this.context = container.$context();
    }

    @SimpleFunction(description = "Opens the system alarm tone picker.")
    public void PickAlarmTone() {
        if (requestCode == 0) {
            requestCode = form.registerForActivityResult(this);
        }
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "اختر نغمة المنبه");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        Uri currentUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    public void resultReturned(int requestCode, int resultCode, Intent data) {
        if (this.requestCode == requestCode && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                String title = (ringtone != null) ? ringtone.getTitle(context) : "نغمة مخصصة";
                TonePicked(uri.toString(), title);
            }
        }
    }

    @SimpleEvent(description = "Triggered when an alarm tone is selected.")
    public void TonePicked(String uri, String title) {
        EventDispatcher.dispatchEvent(this, "TonePicked", uri, title);
    }

    @SimpleFunction(description = "Plays the saved alarm tone URI.")
    public void PlayTone(String uriString) {
        try {
            StopTone();
            Uri uri = (uriString == null || uriString.isEmpty())
                    ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    : Uri.parse(uriString);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            ErrorOccurred(e.getMessage());
        }
    }

    @SimpleFunction(description = "Stops the currently playing alarm tone.")
    public void StopTone() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            ErrorOccurred(e.getMessage());
        }
    }

    @SimpleEvent(description = "Triggered when an error occurs.")
    public void ErrorOccurred(String error) {
        EventDispatcher.dispatchEvent(this, "ErrorOccurred", error);
    }
}
