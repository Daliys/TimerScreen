package com.example.daliy.timerscreen;

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
    private static boolean isConnect = false;
    // for data
    private static short StageMode = 0;
    private static String Minute = "";
    private static String Second = "";
    private static String SH = "";
    private static String Power = "";
    private static int lostPackets = 0;
    //


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

    public static void ConnectToNextBluetooth(){
        if(currentIdConnectedBluetooth +1 >= listMacSelectedBluetoothDevices.size()) currentIdConnectedBluetooth = 0;
        else currentIdConnectedBluetooth++;
        DisconnectBluetooth();
        ConnectToBluetooth();
    }
    public static void ConnectToBeforeBluetooth(){
        if(currentIdConnectedBluetooth -1 < 0) currentIdConnectedBluetooth = listMacSelectedBluetoothDevices.size()-1;
        else currentIdConnectedBluetooth--;
        DisconnectBluetooth();
        ConnectToBluetooth();
    }
    public static void ConnectToCurrentBluetooth(){
        DisconnectBluetooth();
        ConnectToBluetooth();
    }

    public static void ConnectToBluetooth(){
        MessageDialog messageDialogWait = new MessageDialog();
        messageDialogWait.messageDialogNoButton("","Please Wait", context);

        ConnectionTask pt = new ConnectionTask();
        pt.execute(messageDialogWait);

    }

    public static void AddBluetoothConnectionDevices(int index){
        listMacSelectedBluetoothDevices.add(macAllBluetoothDevice[index]);
        listNameSelectedBluetoothDevices.add(nameAllBluetoothDevice[index]);
        currentIdConnectedBluetooth = 0;
    }


    public static void  StartCheckStatusConnection(final TextView textView){
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){

                    if(btSocket == null) setText(false);
                    else {
                        if (btSocket.isConnected()) {
                            if(lostPackets >= 10){
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
                    Thread.currentThread().sleep(500);
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

        }catch (Exception f){
            Log.e("SendData", "Cant send Data");
        }
    }

    public static void SendData(byte bytes){
        try{
            Log.e("SendData",bytes + " d");
                OutputStream outStream = btSocket.getOutputStream();
                outStream.write(bytes);
            Log.e("SendData","Finish");

        }catch (Exception f){
            Log.e("SendData", "Cant send Data");
        }
    }
    // метод для ловли данных с bluetooth
    public static void GetDataBluetooth() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                long time = System.currentTimeMillis();
                boolean isDataRequest = false;
                while (true) {
                    if (isConnect) {
                        try {
                            inputStream = btSocket.getInputStream();

                        } catch (Exception e) {
                            Log.e("InputStream", "Cant get Input stream");
                        }
                    }
                    //Log.i("LOG",System.currentTimeMillis() + " time " + time);
                    if ((time + 500) < System.currentTimeMillis() && isConnect) {
                        time = System.currentTimeMillis();
                        SendData("D");
                        isDataRequest = true;
                        Log.e("D", "+");
                        lostPackets++;

                    }



                    try {
                        // Read from the InputStream

                        if (isConnect && isDataRequest) {
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
                                    }

                                }
                                RefreshScreen();
                            }
                        }
                    } catch (NullPointerException nulE) {

                    } catch (IOException e) {

                        Log.e("While", "Buffer =" + e);
                        //break;
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
                                ScreenActivity.button1.setText("Время");
                                ScreenActivity.button2.setText("Мощность");
                                ScreenActivity.button3.setText("Старт");
                                ScreenActivity.button4.setText("Пауза");

                                ScreenActivity.textViewMinute.setText(Minute);
                                ScreenActivity.textViewSecond.setText(Second);


                                ActivAll();

                                ScreenActivity.button4.setClickable(false);
                                ScreenActivity.button4.setAlpha(0.5f);
                                break;
                            case 1:
                                ScreenActivity.button1.setText("Время");
                                ScreenActivity.button2.setText("Мощность");
                                ScreenActivity.button3.setText("+");
                                ScreenActivity.button4.setText("-");

                                ScreenActivity.textViewMinute.setText(Minute);
                                ScreenActivity.textViewSecond.setText(Second);


                                ActivAll();

                                ScreenActivity.button2.setClickable(false);
                                ScreenActivity.button2.setAlpha(0.5f);

                                break;
                            case 2:
                                ScreenActivity.button1.setText("ШИМ");
                                ScreenActivity.button2.setText("Мощность");
                                ScreenActivity.button3.setText("+");
                                ScreenActivity.button4.setText("-");


                                ScreenActivity.textViewMinute.setText(Power);
                                ScreenActivity.textViewSecond.setText("%");


                                ActivAll();

                                break;
                            case 3:
                                ScreenActivity.button1.setText("Время");
                                ScreenActivity.button2.setText("Мощность");
                                ScreenActivity.button3.setText("Стоп");
                                ScreenActivity.button4.setText("Пауза");

                                ScreenActivity.textViewMinute.setText(Minute);
                                ScreenActivity.textViewSecond.setText(Second);


                                ActivAll();

                                ScreenActivity.button1.setClickable(false);
                                ScreenActivity.button1.setAlpha(0.5f);
                                ScreenActivity.button2.setClickable(false);
                                ScreenActivity.button2.setAlpha(0.5f);
                                ScreenActivity.button3.setAlpha(0.5f);


                                break;
                            case 4:
                                ScreenActivity.button1.setText("Время");
                                ScreenActivity.button2.setText("Мощность");
                                ScreenActivity.button3.setText("Стоп");
                                ScreenActivity.button4.setText("Пауза");

                                ScreenActivity.textViewMinute.setText(Minute);
                                ScreenActivity.textViewSecond.setText(Second);


                                ActivAll();

                                ScreenActivity.button1.setClickable(false);
                                ScreenActivity.button1.setAlpha(0.5f);
                                ScreenActivity.button2.setClickable(false);
                                ScreenActivity.button2.setAlpha(0.5f);
                                ScreenActivity.button4.setClickable(false);
                                ScreenActivity.button4.setAlpha(0.5f);


                                break;

                            case 5:
                                ScreenActivity.button1.setText("ШИМ");
                                ScreenActivity.button2.setText("Мощность");
                                ScreenActivity.button3.setText("+");
                                ScreenActivity.button4.setText("-");


                                ScreenActivity.textViewMinute.setText(SH);
                                ScreenActivity.textViewSecond.setText("");

                                ActivAll();

                                break;


                        }
                    }

                    private void ActivAll(){
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
