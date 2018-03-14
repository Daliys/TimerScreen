package com.timscrene.daliy.timerscreen;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.UiThread;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Daliys     on 05.12.2017.
 *
 * 
 */

public  class  Bluetooth {

    private static String[] macAllBluetoothDevice;      // мак адресса всех спаренных Bluetooth устройств
    private static String[] nameAllBluetoothDevice;     // название всех спаренных Bluetooth устройств

    private static List<String> listMacSelectedBluetoothDevices  = new ArrayList<>();
    private static List<String> listNameSelectedBluetoothDevices = new ArrayList<>();
    private static int currentIdConnectedBluetooth;

    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothSocket btSocket = null;
    static Context context;
    private static boolean isConnect = false;   // Состояние подключенно (подключено\ не подключено)
    // for data
    private static short StageMode = 0; // значения сотояния экрана
    private static String Minute = "";  // значение минут
    private static String Second = "";  // значение секунд
    private static String SH = "";      // значение шима
    private static String Power = "";   // значение мощности
    private static short Colon = 0;   // значение мощности
    private static short numLamp = 2;
    private static int lostPackets = 0; // для подсчета потерянных пакетов данных
    private static short isEnableDev1 = 0;
    private static short isEnableDev2 = 0;
    private static boolean isNewConnection = true;
    private static boolean isSendDataL = false;
    private static String ColorWhite = "#FFFFFF";
    private static String ColorGreen = "#00CC00";
    private static boolean isButtonConnection = true;
    //

        ///  включение Bluetooth и получение спаренных устройств
    public String[] StartBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {      //Обработка событий при отсуствии bluetoothAdapter или его необнаружении

            MessageDialog.messageDialogAndClose("Критическая ошибка", "Отсуствие bluetoothAdapter или ошибки при его обнаружении/подключении. \n(Приложении будет закрыто)", context);
            return null;
        } else {         //если Bluetooth есть то продолжать

            long StartTimer = System.currentTimeMillis();
            long PastTime = 0;
            while ((!bluetoothAdapter.isEnabled()) && (PastTime < 5000)) {    //попытка включить Bluetooth в течении 5 сек
                PastTime = System.currentTimeMillis() - StartTimer;
                bluetoothAdapter.enable();

                try {
                    Thread.currentThread().sleep(950);
                } catch (Exception e) {
                }
            }
            if (!bluetoothAdapter.isEnabled()) {    //если Bluetooth после 5 сек не включился? то закрыть приложение
                MessageDialog.messageDialogAndClose("Критическая ошибка", "Ошибка при включении bluetoothAdapter.\n(Приложении будет закрыто)", context);
                return null;
            }
        }


        Set<BluetoothDevice> bluetoothDevices = bluetoothAdapter.getBondedDevices();
        String[] stringDevices = new String[bluetoothDevices.size()];
        macAllBluetoothDevice = new String[bluetoothDevices.size()];
        nameAllBluetoothDevice = new String[bluetoothDevices.size()];
        int index = 0;
        // если есть минимум 1 спаренное устройство добавить в лист
        if (bluetoothDevices.size() >= 1) {
            for (BluetoothDevice device : bluetoothDevices) {
                macAllBluetoothDevice[index] = device.getAddress();
                nameAllBluetoothDevice[index] = device.getName();
                stringDevices[index] = device.getName() + "\n" + device.getAddress();
                index++;
            }
            return stringDevices;
        }
        return null;
    }

    // подключение к следующему устройству
    public static void ConnectToNextBluetooth(){
        if(currentIdConnectedBluetooth +1 >= listMacSelectedBluetoothDevices.size()) currentIdConnectedBluetooth = 0;
        else currentIdConnectedBluetooth++;
        DisconnectBluetooth();
        ConnectToBluetooth();
        isButtonConnection= false;
    }
    // подключение к предыдущему устровйтву
    public static void ConnectToBeforeBluetooth(){
        if(currentIdConnectedBluetooth -1 < 0) currentIdConnectedBluetooth = listMacSelectedBluetoothDevices.size()-1;
        else currentIdConnectedBluetooth--;
        DisconnectBluetooth();
        ConnectToBluetooth();
        isButtonConnection= false;
    }
    //подключение к текущему устройтву (при старте подключение к 0)
    public static void ConnectToCurrentBluetooth(){
        DisconnectBluetooth();
        ConnectToBluetooth();
        isButtonConnection= false;
    }

    private static void ConnectToBluetooth(){
        MessageDialog messageDialogWait = new MessageDialog();
        messageDialogWait.messageDialogNoButton("","Please Wait", context);
        isNewConnection = true;

        ConnectionTask pt = new ConnectionTask();
        pt.execute(messageDialogWait);

    }

    public static void AddBluetoothConnectionDevices(int index){
        listMacSelectedBluetoothDevices.add(macAllBluetoothDevice[index]);
        listNameSelectedBluetoothDevices.add(nameAllBluetoothDevice[index]);
        currentIdConnectedBluetooth = 0;
    }

    // проверка на подключение к устройству (1 в сек)
    public static void  StartCheckStatusConnection(final TextView textView){
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){

                    if(btSocket == null) setText(false);
                    else {
                        if (btSocket.isConnected()) {
                            if(lostPackets >= 10){      // если потеряно больше чем 10 пакетов (не пришел ответ) попробовать переподключиться к устройству
                                lostPackets = 0;
                                setText(false);
                                Reconnect();
                            }else
                            setText(true);

                        }
                        else setText(false);
                    }

                    try{
                        Thread.currentThread().sleep(1000);
                    }catch (Exception s){ Log.e("Bluetooth","Check isConnect: "  +s.toString());}
                }
            }

            @UiThread
            public void setText(final boolean isConnect) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if(isConnect) {
                            textView.setTextColor(Color.parseColor("#32CD32"));
                            textView.setText("ONLINE");
                            Bluetooth.isConnect = true;

                        }else {
                            textView.setTextColor(Color.parseColor("#B22222"));
                            textView.setText("OFFLINE");
                            Bluetooth.isConnect = false;
                        }
                     }

                });
            }

            @UiThread
            public void Reconnect() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                       Bluetooth.ConnectToCurrentBluetooth();
                    }

                });
            }
        });
        thread.start();
    }

    public static String GetCurrentBluetoothMac(){
        return listMacSelectedBluetoothDevices.get(currentIdConnectedBluetooth);
    }
    public static String GetCurrentBluetoothName(){
        return listNameSelectedBluetoothDevices.get(currentIdConnectedBluetooth);
    }

    public static void DisconnectBluetooth(){
        try{
            Log.i("BtSocket  ", "con  " + btSocket.isConnected());
            if(btSocket != null){
                if(btSocket.isConnected()) {
                    btSocket.close();
                    Thread.currentThread().sleep(250);
                }
                Log.i("BtSocket  ", "dis  " + btSocket.isConnected());
            }
        }catch (Exception v){    Log.e("BtSocket  ", " cant DisconnectBluetooth  " + v);}
    }


    public static void SendData(String str){

        try{
                byte[] msgBuffer = str.getBytes();
                OutputStream outStream = btSocket.getOutputStream();
                outStream.write(msgBuffer);
                Log.d("SendData",  str + "");
        }catch (Exception f){
            Log.e("SendData", "Cant send Data");
        }
    }
    // метод для получения данных с bluetooth
    public static void GetDataBluetooth() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                long time = System.currentTimeMillis();
                boolean isDataRequest = false;
                while (true) {
                    if (isConnect && isButtonConnection) {
                        try {
                            inputStream = btSocket.getInputStream();

                        } catch (Exception e) {
                            Log.e("InputStream", "Cant get Input stream");
                        }
                    }
                    //Log.i("LOG",System.currentTimeMillis() + " time " + time);
                    if((time + 500) < System.currentTimeMillis() && (isNewConnection || !isSendDataL) && isConnect) {    // при новом пожключение к девайсу запросить сколько ламп у него установленно
                        SendData("B");
                        isNewConnection = false;
                        isDataRequest = true;
                        time = System.currentTimeMillis();
                        Log.e("B", "+ Send");
                        lostPackets++;
                    }
                    else if ((time + 500) < System.currentTimeMillis() && isConnect) {
                        time = System.currentTimeMillis();
                        SendData("D");
                        isDataRequest = true;
                        Log.e("D", "+ Send");
                        lostPackets++;
                    }



                    try {
                        // Read from the InputStream

                        if (isConnect && isDataRequest && isButtonConnection) {
                            isDataRequest = false;
                            int count = inputStream.available();
                            if (count >= 1) {
                                lostPackets = 0;
                                byte[] bytes = new byte[count];
                                inputStream.read(bytes);
                                String str = "";
                                for(int z = 0; z < count ; z++) str += bytes[z]  + "";
                                String sstr = new String(bytes);
                                Log.d("DATA", str+" - " + sstr + " byte" + count);


                                // StageMode == C  , Min == M, Sec == S, SH == H, power == P
                                // C1M123SH225P90

                                for (int i = 0; i < count; i++) {
                                    switch (bytes[i]){
                                        case 'C':
                                            if((i+1) < count)
                                            StageMode = (short)(bytes[i+1]-48);
                                            break;
                                        case 'M':
                                            if((i+2) < count && bytes[i+2] >= 48 && bytes[i+2] < 58 ){ ///////// --------------------
                                                Minute = (bytes[i+1] - 48)  + ""+ (bytes[i+2]-48) + "";
                                            }else{
                                                if((i+1) < count)
                                                Minute = "0"+(bytes[i+1]-48);
                                            }
                                            break;
                                        case 'S':
                                            if((i+2) < count && bytes[i+2] >= 48 && bytes[i+2] < 58 ){ ///////// --------------------
                                                Second = (bytes[i+1] - 48) +""+ (bytes[i+2]-48) + "";
                                            }else{
                                                if((i+1) < count)
                                                Second = "0"+ (bytes[i+1] - 48);
                                            }
                                            break;
                                        case 'H':
                                            if((i+2) < count && bytes[i+2] >= 48 && bytes[i+2] < 58 ) { ///////// --------------------
                                                SH = (bytes[i + 1] - 48) + "" + (bytes[i + 2] - 48) + "";
                                                if ((i+3) < count && bytes[i + 3] >= 48 && bytes[i + 3] < 58) {
                                                    SH +=  (bytes[i + 3] - 48) + "";
                                                }
                                            }else {
                                                if((i+1) < count)
                                                SH = "0" + (bytes[i + 1] - 48);
                                            }
                                            break;
                                        case 'P':
                                            if((i+2) < count && bytes[i+2] >= 48 && bytes[i+2] < 58 ) { ///////// --------------------
                                                Power = (bytes[i + 1] - 48) + "" + (bytes[i + 2] - 48) + "";
                                                if ((i+3) < count && bytes[i + 3] >= 48 && bytes[i + 3] < 58) {
                                                    Power +=  (bytes[i + 3] - 48) + "";
                                                }
                                            }else {
                                                if((i+1) < count)
                                                Power = "0" + (bytes[i + 1] - 48);
                                            }
                                            break;
                                        case 'I':
                                            if((i+1) < count)
                                                Colon = (short)(bytes[i+1]-48);
                                            break;
                                        case 'R':
                                            if((i+1) < count)
                                                isEnableDev1 = (short)(bytes[i+1]-48);
                                            break;
                                        case 'E':
                                            if((i+1) < count)
                                                isEnableDev2 = (short)(bytes[i+1]-48);
                                            break;
                                        case 'L':
                                            if((i+1) < count)
                                                numLamp = (short)(bytes[i+1]-48);
                                                isSendDataL = true;
                                            break;


                                    }
                                }
                                RefreshScreen();
                            }
                        }
                    } catch (NullPointerException nulE) {
                    } catch (IOException e) {
                        Log.e("While", "Buffer =" + e);
                    }
                }
            }


            @UiThread
            public void RefreshScreen() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {         //              (UI)PressBut(mode(Bt)) --> data(Bt)
                                                //          (Bt)refresh(2/c) --^
                        Log.e("REFRESH","True" + " c"+StageMode + "M" + Minute + "S" + Second + "H"+SH+"P"+Power);
                        switch (StageMode){
                            case 0:

                                SetButtonsText("Время","Мощность","Старт","Пауза");

                                ScreenActivity.textViewMinute.setText(Minute);
                                ScreenActivity.textViewSecond.setText(Second);


                                ActivateAll();

                                SetButtonsTextColor(ColorGreen,ColorGreen,ColorWhite,ColorWhite);



                                ScreenActivity.textViewColon.setVisibility(View.VISIBLE);

                                ScreenActivity.button4.setClickable(false);
                                ScreenActivity.button4.setAlpha(0.5f);
                                break;
                            case 1:

                                SetButtonsText("Время","Мощность","+","-");

                                ScreenActivity.textViewMinute.setText(Minute);
                                ScreenActivity.textViewSecond.setText(Second);


                                ActivateAll();

                                SetButtonsTextColor(ColorWhite,ColorWhite,ColorGreen,ColorGreen);

                                ScreenActivity.textViewColon.setVisibility(View.VISIBLE);

                                ScreenActivity.button2.setClickable(false);
                                ScreenActivity.button2.setAlpha(0.5f);

                                break;
                            case 2:

                                SetButtonsText("ШИМ","Мощность","+","-");

                                ScreenActivity.textViewMinute.setText(Power);
                                ScreenActivity.textViewSecond.setText("%");


                                ActivateAll();

                                SetButtonsTextColor(ColorGreen,ColorWhite,ColorGreen,ColorGreen);



                                break;
                            case 3:

                                SetButtonsText("Время","Мощность","Стоп","Пауза");

                                ScreenActivity.textViewMinute.setText(Minute);
                                ScreenActivity.textViewSecond.setText(Second);


                                ActivateAll();

                                SetButtonsTextColor(ColorWhite,ColorWhite,ColorWhite,ColorGreen);


                                ScreenActivity.textViewColon.setVisibility(View.VISIBLE);
                                if(Colon == 1 ){
                                    ScreenActivity.textViewColon.setTextColor(Color.parseColor("#FFC600"));
                                }



                                ScreenActivity.button1.setClickable(false);
                                ScreenActivity.button1.setAlpha(0.5f);
                                ScreenActivity.button2.setClickable(false);
                                ScreenActivity.button2.setAlpha(0.5f);
                                //ScreenActivity.button3.setAlpha(0.5f);


                                break;
                            case 4:

                                SetButtonsText("Время","Мощность","Стоп","Пауза");

                                ScreenActivity.textViewMinute.setText(Minute);
                                ScreenActivity.textViewSecond.setText(Second);


                                ActivateAll();

                                SetButtonsTextColor(ColorWhite,ColorWhite,ColorGreen,ColorWhite);


                                ScreenActivity.textViewColon.setVisibility(View.VISIBLE);
                                ScreenActivity.button1.setClickable(false);
                                ScreenActivity.button1.setAlpha(0.5f);
                                ScreenActivity.button2.setClickable(false);
                                ScreenActivity.button2.setAlpha(0.5f);

                                break;

                            case 5:

                                SetButtonsText("ШИМ","Мощность","+","-");

                                ScreenActivity.textViewMinute.setText(SH);
                                ScreenActivity.textViewSecond.setText("");

                                ActivateAll();

                                SetButtonsTextColor(ColorWhite,ColorGreen,ColorGreen,ColorGreen);

                                break;
                        }
                        if(numLamp == 2) {
                            ScreenActivity.textViewDev1.setVisibility(View.VISIBLE);
                            ScreenActivity.textViewDev2.setVisibility(View.VISIBLE);
                            ScreenActivity.button5.setVisibility(View.VISIBLE);
                            ScreenActivity.button6.setVisibility(View.VISIBLE);

                            if (isEnableDev1 == 0) {      // проверка на то включено ли устройство 1 или выключено
                                ScreenActivity.textViewDev1.setText("Изл.\n\n620 нм\n\nВЫКЛ");
                                ScreenActivity.textViewDev1.setAlpha(0.3f);

                            } else {
                                ScreenActivity.textViewDev1.setText("Изл.\n\n620 нм\n\nВКЛ");
                                ScreenActivity.textViewDev1.setAlpha(1);

                            }

                            if (isEnableDev2 == 0) {      // проверка на то включено ли устройство 2 или выключено
                                ScreenActivity.textViewDev2.setText("Изл.\n\n740 нм\n\nВЫКЛ");
                                ScreenActivity.textViewDev2.setAlpha(0.3f);
                            } else {
                                ScreenActivity.textViewDev2.setText("Изл.\n\n740 нм\n\nВКЛ");
                                ScreenActivity.textViewDev2.setAlpha(1);
                            }
                        }else{
                            ScreenActivity.textViewDev1.setVisibility(View.INVISIBLE);
                            ScreenActivity.textViewDev2.setVisibility(View.INVISIBLE);
                            ScreenActivity.button5.setVisibility(View.INVISIBLE);
                            ScreenActivity.button6.setVisibility(View.INVISIBLE);
                        }

                    }

                    private void SetButtonsText(String but1, String but2, String but3, String but4 ){
                        ScreenActivity.button1.setText(but1);
                        ScreenActivity.button2.setText(but2);
                        ScreenActivity.button3.setText(but3);
                        ScreenActivity.button4.setText(but4);
                    }

                    private void SetButtonsTextColor(String but1, String but2, String but3, String but4){
                        ScreenActivity.button1.setTextColor(Color.parseColor(but1));
                        ScreenActivity.button2.setTextColor(Color.parseColor(but2));
                        ScreenActivity.button3.setTextColor(Color.parseColor(but3));
                        ScreenActivity.button4.setTextColor(Color.parseColor(but4));
                    }

                    private void ActivateAll(){
                        if(Colon == 0)
                            ScreenActivity.textViewColon.setTextColor(Color.parseColor("#32CD32"));
                        if(StageMode != 3){
                            ScreenActivity.textViewColon.setTextColor(Color.parseColor("#32CD32"));
                        }

                        if(StageMode == 0){
                            ScreenActivity.button5.setAlpha(1);
                            ScreenActivity.button5.setClickable(true);
                            ScreenActivity.button6.setAlpha(1);
                            ScreenActivity.button6.setClickable(true);
                        }else{
                            ScreenActivity.button5.setAlpha(0.4f);
                            ScreenActivity.button5.setClickable(false);
                            ScreenActivity.button6.setAlpha(0.4f);
                            ScreenActivity.button6.setClickable(false);
                        }

                        ScreenActivity.textViewColon.setVisibility(View.INVISIBLE);
                        ScreenActivity.button1.setAlpha(1);
                        ScreenActivity.button2.setAlpha(1);
                        ScreenActivity.button3.setAlpha(1);
                        ScreenActivity.button4.setAlpha(1);

                        ScreenActivity.button1.setClickable(true);
                        ScreenActivity.button2.setClickable(true);
                        ScreenActivity.button3.setClickable(true);
                        ScreenActivity.button4.setClickable(true);
                    }
                });
            }
        });
        thread.start();
    }


    static class ConnectionTask extends AsyncTask<MessageDialog, Void, Void> {
        @Override
        protected Void doInBackground(MessageDialog... messageDialogsWait) {

            try {
                UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(listMacSelectedBluetoothDevices.get(currentIdConnectedBluetooth));

                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                if(bluetoothAdapter.isDiscovering())
                    bluetoothAdapter.cancelDiscovery();

                btSocket.connect();
                messageDialogsWait[0].CloseMessageDialogNoButton();
                isButtonConnection= true;
            } catch (Exception e) {
                try {
                    btSocket.close();
                }catch (Exception s){Log.e("Close Socket","Cant Close Socket");}
                Log.e("ConnectTo",e+" ");
                messageDialogsWait[0].CloseMessageDialogNoButton();
                onProgressUpdate();
            }
           return null;
        }
        @Override
        protected void onProgressUpdate(Void... items) {
           // MessageDialog.messageDialog("Критическая ошибка","Ошибка при подключении к Bluetooth. Проверте доступность Bluetooth.",context);
            logMsg();
        }
        @Override
        protected void onPostExecute(Void unused) {
        }
        @UiThread
        public void logMsg() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    MessageDialog.messageDialog("Критическая ошибка","Ошибка при подключении к Bluetooth. Проверте доступность Bluetooth.",context);
                }
            });
        }
    }
}
