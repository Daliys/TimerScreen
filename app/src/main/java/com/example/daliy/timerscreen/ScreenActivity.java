package com.example.daliy.timerscreen;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Daliys on 07.12.2017.
 */

public class ScreenActivity extends AppCompatActivity {

    Button buttonBefore;
    Button buttonNext;
    public static Button button1,button2,button3,button4;
    public static TextView textViewMinute, textViewSecond;
    TextView textViewInformation;
    TextView textViewStatus;

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

        textViewMinute = findViewById(R.id.textViewMin);
        textViewSecond = findViewById(R.id.textViewSec);

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


    }

}
