package com.example.threadtask3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import com.example.threadtask3.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    Handler handler;
    private static Object lock = new Object();
    private int number = 0;
    private boolean isLast = false;
    private String answer = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        handler = new Handler(Looper.getMainLooper()) {
            // Looper – запускает цикл обработки сообщений
            // getMainLooper – цикл в главном потоке обработки (UI)
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                char[] chars = (char[]) msg.obj;
                String str =  String.valueOf(chars);
                // проверка на новое слово
                if(isLast){
                    answer = answer + str + " ";
                    binding.TV.setText(answer);
                }else{
                    binding.TV.setText(answer + str);
                }
            }
        };

        class MyThread extends Thread {
            private String[] textsplit;
//            private String[] textToView;
//            private String text;
            private int numberthread;

            public MyThread(String text, int numberthread) {
//                this.text = text;
                this.textsplit= text.split(" ");
//                this.textToView = new String[textsplit.length];
                this.numberthread = numberthread;
            }

            @Override
            public void run() {
                super.run();
//                char[] textchars = text.toCharArray();
                for (int i = 0; i < textsplit.length; i++) {
                    synchronized (lock) { // синхронизируем данный блок кода
                        while (number != numberthread) {
                            try {
                                lock.wait(); // останавливает выполнение текущего потока
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        char [] word = textsplit[i].toCharArray();
                        char [] textToView = new char[word.length];
                        for (int j = 0; j < word.length; j++) {
                            char ch = word[j];
                            textToView[j] = ch;
                            isLast = false;
                            if (j == (word.length - 1)) isLast = true;

                            Message msg = new Message();
                            msg.obj = textToView;
                            handler.sendMessage(msg);
                            try {
                                if (ch == '.' || ch == '!' || ch == '?') {
                                    Thread.sleep(1000);
                                } else {
                                    Thread.sleep(400);
                                }
                                if (j == (word.length - 1)) {
                                    number = (number + 1) % 3;
                                    //textToView = new char[textchars.length];
                                    lock.notifyAll(); //  возвращает блокировку потоку из которого вызван
                                }
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        while (number != numberthread) {
                            try {
                                lock.wait(); // останавливает выполнение текущего потока
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        if (i == (textsplit.length - 1)) {
                            number = (number + 1) % 3;
                            isLast = true;
                            lock.notifyAll(); //  возвращает блокировку потоку из которого вызван
                        }
                    }
                }
            }
        }

        binding.But.setOnClickListener(new View.OnClickListener()

            {
                @Override
                public void onClick (View view){
                    answer = "";
                    number = 0;
                    binding.TV.setText("");
                    String text1 = binding.ET1.getText().toString();
                    String text2 = binding.ET2.getText().toString();
                    String text3 = binding.ET3.getText().toString();
                    MyThread thread1 = new MyThread(text1, 0);
                    MyThread thread2 = new MyThread(text2,  1);
                    MyThread thread3 = new MyThread(text3, 2);
                    thread1.start();
                    thread2.start();
                    thread3.start();
            }
            });
        }
    }