package com.appzone.qrscanner.generator.barcode.qrcode.scanner.creator.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class BackgroundMusic {
    MediaPlayer m = null;
    MediaPlayer soundPlayer = null;

    private static BackgroundMusic myInstance = null;
    private Context mContext;
    Vibrator v;

    public static BackgroundMusic getInstance(Context mcontext) {
        if (myInstance == null) {
            myInstance = new BackgroundMusic(mcontext);
        }
        return myInstance;
    }

    public BackgroundMusic(Context mcontext) {
        this.mContext = mcontext;
    }






    public void Playsound(Activity activity, String audio) {


        if (Paper.book().read(Utilities.Sound_setting, true)) {
            try {
                if (soundPlayer != null) {

                    if (soundPlayer.isPlaying()) {
                        soundPlayer.stop();
                        soundPlayer.release();
                        soundPlayer = new MediaPlayer();
                    } else {
                        soundPlayer = new MediaPlayer();
                    }
                } else {
                    soundPlayer = new MediaPlayer();
                }

                AssetFileDescriptor descriptor = activity.getAssets().openFd(audio);
                soundPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();

                soundPlayer.prepare();
                soundPlayer.setVolume(1f, 1f);

                soundPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }




    public void PlayVibrate500(Activity activity,int time) {


        if (Paper.book().read(Utilities.Vib_setting, true)) {

            v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
            int medium_gap = 500;   // Length of Gap Between Letters
            long[] pattern = {
                    0,
                    medium_gap,    // S
            };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect vibe = VibrationEffect.createWaveform(pattern, 1);
                v.vibrate(vibe);
            } else {
                //deprecated in API 26
                v.vibrate(pattern, 1);
            }

            new CountDownTimer(time, 1000) {
                public void onTick(long millisUntilFinished) {

                }

                public void onFinish() {
                    if (v != null)
                        v.cancel();
                }
            }.start();



        }
    }


}
