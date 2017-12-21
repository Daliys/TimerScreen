package com.example.daliy.timerscreen;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Daliys on 06.12.2017.
 */

public class MessageDialog extends AppCompatActivity
{
    // вызывает диалоговое окно с кнопкой и дальшейшим закритием приложения
    public static void messageDialogAndClose(String title , String content, Context a){
        AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(a);
        builder.setTitle(title)
                .setCancelable(false)
                .setMessage(content)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        FinishManager.finishActivity(MainActivity.class);
                    }
                });
        alert = builder.create();
        alert.show();
    }
    // вызывает диалоговое окно с кнопкой
    public static void messageDialog(String title , String content, Context a){
        AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(a);
        builder.setTitle(title)
                .setCancelable(false)
                .setMessage(content)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                });
        alert = builder.create();
        alert.show();
    }


    AlertDialog.Builder builder;
    AlertDialog alert;
    // вызывает диалоговое окно с ожиданием закрытия
    public void messageDialogNoButton(String title , String content, Context a){
        builder = new AlertDialog.Builder(a);
        builder.setTitle(title)
                .setCancelable(false)
                .setMessage(content);

        alert = builder.create();
        alert.show();
    }
    // закрытие ожидающего диологового окна
    public void CloseMessageDialogNoButton(){
       alert.dismiss();
    }



}
