package com.activation_cloud.audiodemo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.view.View;
import java.io.IOException;
import java.util.ArrayList;

import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements Runnable, OnPreparedListener, OnCompletionListener, View.OnClickListener {


    MediaPlayer mp ;
    AudioManager am;
    CheckBox repeat_box;
    SeekBar volumeControl;
    SeekBar scrubber;
    Thread th = new Thread(this);
    ListView list_sg;
    ArrayList<String> name_song;
    ArrayList<String> path_song;

    String DATA_SD ="";// "/storage/78F9-AF26/Documents/Media/Soundtarck Prince Of Persia_-_Time only knows.mp3";// Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+"/test.mp3";
    boolean isStop = false;
    public MainActivity() {
        path_song = new ArrayList<String>();
        name_song = new ArrayList<String>();
    }

    private boolean canMakeSmores(){

        return(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Log.i("INFO",DATA_SD);
        am = (AudioManager)getSystemService(AUDIO_SERVICE);
        repeat_box = (CheckBox)findViewById(R.id.repeat_box);
        repeat_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.i("INFO","Check repeat");
                if(mp != null)
                {
                    mp.setLooping(b);
                }
            }
        });

        if(canMakeSmores()) {
            //ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            //ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int RESULT = 105;
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RESULT);

        }

        Button start_bm = (Button)findViewById(R.id.start_bm);
        start_bm.setOnClickListener(this);
        Button pause_bm = (Button)findViewById(R.id.pause_bm);
        pause_bm.setOnClickListener(this);
        Button stop_bm = (Button)findViewById(R.id.stop_bm);
        stop_bm.setOnClickListener(this);
        Button resume_bm = (Button)findViewById(R.id.resume_bm);
        resume_bm.setOnClickListener(this);
        Button find_bm = (Button)findViewById(R.id.find_bm);
        find_bm.setOnClickListener(this);

        list_sg = (ListView)findViewById(R.id.song_list);
        list_sg.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list_sg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("INRO","Select:" + name_song.get(i));
                DATA_SD = path_song.get(i);
                InitAudio();
            }
        });

        scrubber =(SeekBar)findViewById(R.id.scraber_bar);

        //InitAudio();

        volumeControl = (SeekBar)findViewById(R.id.volume_bar);
        volumeControl.setMax(am.getStreamMaxVolume(am.STREAM_MUSIC));
        volumeControl.setProgress(am.getStreamVolume(am.STREAM_MUSIC));
        volumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                am.setStreamVolume(am.STREAM_MUSIC,i,0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        scrubber.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Log.i("INFO","Chancher scrubber");
                if(mp != null && b && !isStop)
                {
                    mp.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void run()
    {
        int curPos = 0;
        while(mp!=null)
        {
            try{
                Thread.sleep(1000);
                if(mp == null)
                    return;
                if(!isStop)
                    curPos = mp.getCurrentPosition();
                else
                    curPos = 0;
            }catch (Exception e)
            {
                e.printStackTrace();
                return;
            }
            if(scrubber == null)
                return;
            scrubber.setProgress(curPos);
        }
    }

    private void InitAudio()
    {
        releaseMP();
        mp = new MediaPlayer();
        try {
            mp.setDataSource(DATA_SD);
            //mp.setDataSource();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mp.setLooping(repeat_box.isChecked());
        mp.setOnCompletionListener(this);
        mp.setOnPreparedListener(this);
        mp.prepareAsync();

        if(th.getState() == Thread.State.NEW)
            th.start();

    }

    public void releaseMP()
    {
        if(mp != null) {
            try {
                mp.release();
                mp = null;
                isStop = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void MediaManager()
    {
        if(mp != null && mp.isPlaying())
        {
            mp.stop();
            isStop = true;
        }
        scrubber.setProgress(0);
        DATA_SD = "";
        path_song.clear();
        name_song.clear();
        ContentResolver cnRl = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] proj ={MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.TITLE};
        Cursor cursor = cnRl.query(uri,proj,null,null,null);
        if(cursor != null && cursor.moveToFirst())
        {
            //int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            //int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int idxPath = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            int idxName = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            do {
                //long thisId = cursor.getLong(idColumn);
                //cursor.getNotificationUri();
                //String thisTitle = cursor.getString(titleColumn);
                String path = cursor.getString(idxPath);
                String name = cursor.getString(idxName);
                if(isFormatMP3(path)) {
                    path_song.add(path);
                    name_song.add(name);
                    Log.i("INFO", "path:" + path + ", name:" + name);
                }
            }while(cursor.moveToNext());

            ArrayAdapter<String>adapt_song_list = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,name_song);
            list_sg.setAdapter(adapt_song_list);
        }
    }

    private boolean isPathEmpty()
    {
        if(DATA_SD.length() == 0)
            return true;

        return false;
    }
    private boolean isFormatMP3(String name)
    {
        if(name.length() <= 4) // ?.mp3
            return false;
        int len = name.length();
        if(name.charAt(len-1) == '3'&& name.charAt(len-2) == 'p'&& name.charAt(len-3) == 'm'&& name.charAt(len-4) == '.')
            return true;
        return false;
    }

    @Override
    public void  onClick(View view)
    {
        Log.i("INFO","onClick");

            switch(view.getId()) {

                case R.id.start_bm:
                    if(isPathEmpty()) {
                        Toast.makeText(this,"Select the song!",Toast.LENGTH_LONG).show();
                        return;
                    }
                    Log.i("INFO","start");
                    InitAudio();
                    break;
                case R.id.pause_bm:
                    if(isPathEmpty()) {
                        Toast.makeText(this,"Select the song!",Toast.LENGTH_LONG).show();
                        return;
                    }
                    Log.i("INFO","pause");
                    if(mp!= null) {
                        if (!isStop && mp.isPlaying())
                            mp.pause();
                    }
                    break;
                case R.id.stop_bm:
                    if(isPathEmpty()) {
                        Toast.makeText(this,"Select the song!",Toast.LENGTH_LONG).show();
                        return;
                    }
                    Log.i("INFO","stop");
                    if(mp !=null && mp.isPlaying()) {
                        mp.stop();
                        isStop = true;
                    }
                    break;
                case R.id.resume_bm:
                    if(isPathEmpty()) {
                        Toast.makeText(this,"Select the song!",Toast.LENGTH_LONG).show();
                        return;
                    }
                    Log.i("INFO","resume");
                    if(mp != null) {
                        if (!isStop)
                            mp.start();
                    }
                    break;
                case R.id.find_bm:
                    MediaManager();
                    break;
            }
    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        //Log.i("INFO","Prepare");

        mp.seekTo(0);
        scrubber.setMax(mp.getDuration());
        isStop = false;
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        releaseMP();
        if(th != null && th.getState() == Thread.State.RUNNABLE)
            th.interrupt();
    }


}
