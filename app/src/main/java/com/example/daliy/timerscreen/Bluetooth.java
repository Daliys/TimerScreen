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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by Daliys     on 05.12.2017.
 */

public  class  Bluetooth {

    static String[] macAllBluetoothDevice;      // мак адресса всех спаренных Bluetooth устройств
    static String[] nameAllBluetoothDevice;     // название всех спаренных Bluetooth устройств

    static List<String> listMacSelectedBluetoothDevices  = new ArrayList<>();
    static List<String> listNameSelectedBluetoothDevices = new ArrayList<>();

    static int currentIdconnectedBluetooth;
    public final static boolean NEXT_DEVICE = true;
    public final static boolean BEFORE_DEVICE = false;

    static BluetoothAdapter bluetoothAdapter;
    static BluetoothSocket btSocket = null;
    static Context context;

    public String[] StartBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {      //Обработка событий при отсуствии bluetoothAdapter или его необнаружении

            MessageDialog.messageDialogAndClose("Критическая ошибка", "Отсуствие bluetoothAdapter или ошибки при его обнаружении/подключении. \n(Приложении будет закрыто)", context);
            return null;
        } else {         //если Bluetooth есть то продолжать

            long StartTimer = System.currentTimeMillis();
            long PastTime = 0;
            while ((!bluetoothAdapter.isEnabled()) && (PastTime < 5000)) {    //попытка включить блютуз в течении 5 сек
                PastTime = System.currentTimeMillis() - StartTimer;
                bluetoothAdapter.enable();

                try {
                    Thread.currentThread().sleep(950);
                } catch (Exception e) {
                }
            }
            if (!bluetoothAdapter.isEnabled()) {    //если блютуз после 5 сек не включился то закрыть приложение
                MessageDialog.messageDialogAndClose("Критическая ошибка", "Ошибка при включении bluetoothAdapter.\n(Приложении будет закрыто)", context);
                return null;
            }
        }




        Set<BluetoothDevice> bluetoothDevices = bluetoothAdapter.getBondedDevices();
        String[] stringDevices = new String[bluetoothDevices.size()];
        macAllBluetoothDevice = new String[bluetoothDevices.size()];
        nameAllBluetoothDevice = new String[bluetoothDevices.size()];
        int index = 0;

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
        if(currentIdconnectedBluetooth+1 >= listMacSelectedBluetoothDevices.size()) currentIdconnectedBluetooth = 0;
        else currentIdconnectedBluetooth++;
        DisconnectBluetooth();

        ConnectToBluetooth();
    }
    public static void ConnectToBeforeBluetooth(){
        if(currentIdconnectedBluetooth-1 < 0) currentIdconnectedBluetooth = listMacSelectedBluetoothDevices.size()-1;
        else currentIdconnectedBluetooth--;
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
        currentIdconnectedBluetooth = 0;
        //return listMacSelectedBluetoothDevices.get(listMacSelectedBluetoothDevices.size()-1);
    }

    public static void  StartCheckStatusConnection(final TextView textView){
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    if(btSocket == null) {
                        setText(false);
                    }else {
                        if (btSocket.isConnected()) {
                            setText(true);
                        } else {
                            setText(false);
                        }
                    }

                    try{
                        Thread.currentThread().sleep(1000);
                        //thread.sleep(3000);
                    }catch (Exception s){}
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

                        }else {
                            textView.setTextColor(Color.parseColor("#B22222"));
                            textView.setText("OFFLINE");

                        }
                     }
                });
            }

        });
        thread.start();
    }

    public static String GetCurrentBluetoothMac(){
        return listMacSelectedBluetoothDevices.get(currentIdconnectedBluetooth);
    }
    public static String GetCurrentBluetoothName(){
        return listNameSelectedBluetoothDevices.get(currentIdconnectedBluetooth);
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

          //  if(btSocket.isConnected()){
                byte[] msgBuffer = str.getBytes();
                OutputStream outStream = btSocket.getOutputStream();
                outStream.write(msgBuffer);
                //outStream.close();
          //  }

        }catch (Exception f){
            Log.e("SendData", "Cant send Data");
        }
    }
// прописать есть устройство подключено к БТ то при переключение его отконетить а потом подключить


    static class ConnectionTask extends AsyncTask<MessageDialog, Void, Void> {
        @Override
        protected Void doInBackground(MessageDialog... messageDialogsWait) {

            try {


                UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(listMacSelectedBluetoothDevices.get(currentIdconnectedBluetooth));

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
