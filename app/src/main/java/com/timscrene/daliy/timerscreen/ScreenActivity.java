package com.timscrene.daliy.timerscreen;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Daliys on 07.12.2017.
 *
 */

public class ScreenActivity extends AppCompatActivity {

    Button buttonBefore;
    Button buttonNext;
    public static Button button1,button2,button3,button4,button5,button6;
    public static TextView textViewMinute, textViewSecond, textViewColon, textViewDev1, textViewDev2;
    TextView textViewInformation;
    TextView textViewStatus;
    String fontPath1 = "fonts/digital7.ttf";
    String ColorText = "#32CD32";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        FinishManager.addActivity(this); // передаём текущую активность (для закрытия приложения при критических ошибках)
        Bluetooth.context = this;       // передаем текущий контент (для вывода диалоговых окон на экран)

        Bluetooth.ConnectToCurrentBluetooth();  // подключаемся к выбранаму изначально bluetooth (изначально он всегда = 0)
        InitializationElements();
        Bluetooth.GetDataBluetooth();
    }

    private void InitializationElements(){
        buttonBefore = findViewById(R.id.buttonBefore);
        buttonNext = findViewById(R.id.buttonNext);
        textViewInformation = findViewById(R.id.textViewInformation);
        textViewStatus = findViewById(R.id.textViewStatus);



        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);
        button6 = findViewById(R.id.button6);

        textViewMinute = findViewById(R.id.textViewMin);
        textViewSecond = findViewById(R.id.textViewSec);
        textViewColon = findViewById(R.id.textViewColon);
        textViewDev1 = findViewById(R.id.textViewDev1);
        textViewDev2 = findViewById(R.id.textViewDev2);

        Typeface typeface1 = Typeface.createFromAsset(getAssets(), fontPath1);

        textViewMinute.setTypeface(typeface1);
        textViewMinute.setTextColor(Color.parseColor(ColorText));
        textViewSecond.setTypeface(typeface1);
        textViewSecond.setTextColor(Color.parseColor(ColorText));
        textViewColon.setTypeface(typeface1);
        textViewColon.setTextColor(Color.parseColor(ColorText));
        textViewDev1.setTypeface(typeface1);
        textViewDev1.setTextColor(Color.parseColor(ColorText));
        textViewDev2.setTypeface(typeface1);
        textViewDev2.setTextColor(Color.parseColor(ColorText));


        textViewInformation.setText(Bluetooth.GetCurrentBluetoothName() + "\n" + Bluetooth.GetCurrentBluetoothMac());
        textViewStatus.setText("ONLINE");


        Bluetooth.StartCheckStatusConnection(textViewStatus);
        SetOnClickListeners();

    }

    private void SetOnClickListeners(){

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bluetooth.ConnectToNextBluetooth();
                textViewInformation.setText(Bluetooth.GetCurrentBluetoothName() + "\n" + Bluetooth.GetCurrentBluetoothMac());
            }
        });
        buttonBefore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bluetooth.ConnectToBeforeBluetooth();
                textViewInformation.setText(Bluetooth.GetCurrentBluetoothName() + "\n" + Bluetooth.GetCurrentBluetoothMac());
            }
        });


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bluetooth.SendData("1");
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bluetooth.SendData("2");
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bluetooth.SendData("3");
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bluetooth.SendData("4");
            }
        });
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bluetooth.SendData("5");
            }
        });
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bluetooth.SendData("6");
            }
        });


    }

}
