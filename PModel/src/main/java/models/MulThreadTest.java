package models;
//package com.dr.runnable1;


import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lxx on 2017/4/27.
 */
 class MyTest {
    private String Mname;
    final CountDownLatch countDownLatch = new CountDownLatch(3);
    public List<String> labels = new ArrayList<>();
    public String data = "A1";
    public MyTest(String name){
        this.Mname = name;
        System.out.println("this is the test");
    }
    public void test()throws InterruptedException{
        //第一种方法
        Runnable r1 = new Multhread("线程A");
        Runnable r2 = new Multhread("线程B");
        Runnable r3 = new Multhread("线程C");
        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r2);
        Thread t3 = new Thread(r3);
        t1.start();
        t2.start();
        t3.start();
        countDownLatch.await();
        System.out.println(this.labels);
        System.out.println(data);
    }
    class Multhread extends Thread{
        private  String name = "inner";
        public Multhread(String name){
            this.name = name;
        }
        public void run(){
            //打印输出
            String temp = this.name ;
            synchronized (this) {
                for (int i = 0; i < 2; i++) {
                    System.out.print(Mname);
                    System.out.println(i + ":");
                    System.out.println(this.name + "----->运行");
//                labels.add(temp);
                    System.out.println("before:" + data);
                }
                data = temp;
                System.out.println("after:" + data);
                countDownLatch.countDown();
            }
        }
    }
}
 public class MulThreadTest {
    public static void main(String args[]) throws InterruptedException {
        MyTest myTest=new MyTest("test");
        myTest.test();
    }
}
