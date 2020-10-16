package com.sunyikeji.playmp3;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;



        import java.io.File;
        import java.io.IOException;
        import java.util.Timer;
        import java.util.TimerTask;

        import android.app.Activity;
        import android.media.MediaPlayer;
        import android.os.Bundle;
        import android.os.Environment;
import android.util.Log;
import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.Button;
        import android.widget.SeekBar;
        import android.widget.SeekBar.OnSeekBarChangeListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    private Button play_pause, reset;
    private SeekBar seekbar;
    private boolean ifplay = false;
    private MediaPlayer player = null;
    private String musicName = "test.mp3";
    private boolean iffirst = false;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private boolean isChanging=false;//互斥变量，防止定时器与SeekBar拖动时进度冲突
    //读写权限
    private String[] permissionStorage = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissionStorage, REQUEST_PERMISSION_CODE);
            }
        }
        player = new MediaPlayer();
        findViews();// 各组件
    }

    private void findViews() {
        play_pause = (Button) findViewById(R.id.play_pause);
        reset = (Button) findViewById(R.id.reset);
        play_pause.setOnClickListener(new MyClick());
        reset.setOnClickListener(new MyClick());

        seekbar = (SeekBar) findViewById(R.id.seekbar);
        seekbar.setOnSeekBarChangeListener(new MySeekbar());
    }

    class MyClick implements OnClickListener {
        public void onClick(View v) {
            File file = new File(Environment.getExternalStorageDirectory(), musicName);
            // 判断有没有要播放的文件
            if (file.exists()) {
                switch (v.getId()) {
                    case R.id.play_pause:
                        if (player != null && !ifplay) {
                            play_pause.setText("暂停");
                            if (!iffirst) {
                                player.reset();
                                try {
                                    player.setDataSource(file.getPath());
                                    player.prepare();// 准备

                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (IllegalStateException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                seekbar.setMax(player.getDuration());//设置进度条
                                //----------定时器记录播放进度---------//
                                mTimer = new Timer();
                                mTimerTask = new TimerTask() {
                                    @Override
                                    public void run() {
                                        if(isChanging==true) {
                                            return;
                                        }
                                        seekbar.setProgress(player.getCurrentPosition());
                                    }
                                };
                                mTimer.schedule(mTimerTask, 0, 10);
                                iffirst=true;
                            }
                            player.start();// 开始
                            ifplay = true;
                        } else if (ifplay) {
                            play_pause.setText("继续");
                            player.pause();
                            ifplay = false;
                        }
                        break;
                    case R.id.reset:
                        if (ifplay) {
                            player.seekTo(0);
                        } else {
                            player.reset();
                            try {
                                player.setDataSource(file.getAbsolutePath());
                                player.prepare();// 准备
                                player.start();// 开始
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            }
        }
    }
    //进度条处理
    class MySeekbar implements OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            isChanging=true;
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            player.seekTo(seekbar.getProgress());
            isChanging=false;
        }

    }
    //来电处理
    protected void onDestroy() {
        if(player != null){
            if(player.isPlaying()){
                player.stop();
            }
            player.release();
        }
        super.onDestroy();
    }

    protected void onPause() {
        if(player != null){
            if(player.isPlaying()){
                player.pause();
            }
        }
        super.onPause();
    }

    protected void onResume() {
        if(player != null){
            if(!player.isPlaying()){
                player.start();
            }
        }
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.d("0930****", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
    }



}