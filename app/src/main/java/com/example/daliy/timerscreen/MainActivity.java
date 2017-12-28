package com.example.daliy.timerscreen;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by Daliys on 05.12.2017.
 *
 *
 */
public class MainActivity extends AppCompatActivity {
    ListView listDevice;
    TextView textView;
    Button buttonSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FinishManager.addActivity(this);    // передаём текущую активность (для закрытия приложения при критических ошибках)
        Bluetooth.context = this;           // передаем текущий контент (для вывода диалоговых окон на экран)

        listDevice = findViewById(R.id.listDevice);
        textView = findViewById(R.id.textView);
        buttonSelect = findViewById(R.id.buttonSelect);

        Bluetooth bt = new Bluetooth();


        String[] stringDevices =  bt.StartBluetooth();
        if(stringDevices != null) {
            // Создаем лист с спаренными bluetooth устройствами
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_multiple_choice, stringDevices);
            listDevice.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);    // указываем множественный выбор Bluetooth
            listDevice.setAdapter(arrayAdapter);

            buttonSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    textView.setText("Mac : ");

                    // при условии что не 1 устройство не выбрано, попросить выбрать минимум 1 устройство
                    if(listDevice.getCheckedItemCount() <= 0){
                        MessageDialog.messageDialog("Внимание!" , "\nНужно выбрать минимум 1 устройство!" ,MainActivity.this);
                        return;
                    }

                    SparseBooleanArray sparseBooleanArray = listDevice.getCheckedItemPositions(); // получаем массив со всеми эллементами листа и с булевскими значениями выброно ли устройство или нет
                    for(int a = 0; a < listDevice.getCount(); a++){
                        if(sparseBooleanArray.get(a)){
                           Bluetooth.AddBluetoothConnectionDevices(a);  // добовляем все выбранные устройства в лист для дальнейшего подключения
                        }
                    }
                    // переходим на активность с управлением bluetooth
                    Intent intent = new Intent(MainActivity.this, ScreenActivity.class);
                    startActivity(intent);
                    FinishManager.finishActivity(MainActivity.class);
                }
            });

        }else{
            MessageDialog.messageDialogAndClose("Ошибка","Нет подключенных Bluetooth устройств",this);
        }

    }




}
