package com.example.daliy.timerscreen;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Daliys on 07.12.2017.
 */

// отключени и подключени bluetoth отправка и принятие данных
    // не работает статус + отключение

public class ScreenActivity extends AppCompatActivity {

    Button buttonBefore;
    Button buttonNext;
    Button but2,but1;
    TextView textViewInformation;
    TextView textViewStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);

        FinishManager.addActivity(this);
        Bluetooth.context = this;


        InitializationElements();


        MessageDialog messageDialog = new MessageDialog();
        messageDialog.messageDialogNoButton("","Please Wait", this);
        messageDialog.CloseMessageDialogNoButton();
    }

    private void InitializationElements(){
        buttonBefore = findViewById(R.id.buttonBefore);
        buttonNext = findViewById(R.id.buttonNext);
        textViewInformation = findViewById(R.id.textViewInformation);
        textViewStatus = findViewById(R.id.textViewStatus);

        but1 = findViewById(R.id.button);
        but2 = findViewById(R.id.button2);
        but1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bluetooth.DisconnectBluetooth();
            }
        });
        but2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bluetooth.SendData("Holla");
            }
        });

        textViewInformation.setText(Bluetooth.GetCurrentBluetoothName() + "\n" + Bluetooth.GetCurrentBluetoothMac());
        textViewStatus.setText("ONLINE");
        Bluetooth.StartCheckStatusConnection(textViewStatus);

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bluetooth.ConnectToNextBluetooth();

            }
        });
        buttonBefore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bluetooth.ConnectToBeforeBluetooth();

            }
        });

    }

}
