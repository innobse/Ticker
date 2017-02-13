package main;

import java.util.HashSet;
import java.util.Random;

/**
 *
 * Вариант 1:
 * Реализовать программу из 2-х потоков. Один из потоков каждую секунду генерирует случайное число в интервале [0;99].
 * Второй поток раз в пять секунд выводит количество уникальных чисел, сгенерированных первым потоком. После того, как
 * будет сгенерировано все 100 чисел, оба потока должны остановить свое выполнение.
 *
 */

public class Main {
    static final int COUNT_NUM = 100;                        //  сколько чисел генерировать
    static HashSet<Integer> nums = new HashSet<Integer>(COUNT_NUM); //  хранилище уникальных чисел

    public static void main(String[] args) {
        System.out.println("Старт программы");
        Messager messager = new Messager();
        Generator generator = new Generator();
        try {
            generator.join();
            messager.cancel();
            messager.join();
            System.out.println("Все потоки завершены");
        } catch (InterruptedException e) {
            System.out.println("Не могу дождаться завершения потоков!");
            e.printStackTrace();
        }

    }
}

class Generator extends Thread {
    private volatile boolean stop;              //  флаг остановки
    private static final int TICK = 1000;       //  один тик в ms
    private Random rand;

    Generator(long seed){
        rand = new Random(seed);
        start();
    }

    Generator(){
        rand = new Random();
        start();
    }

    @Override
    public void run(){
        while(!stop && (Main.nums.size() < Main.COUNT_NUM)){
            synchronized(Main.nums){
                Main.nums.add(rand.nextInt(Main.COUNT_NUM));
                Main.nums.notify();
            }
            try {
                Thread.sleep(TICK);
            } catch (InterruptedException e) {
                System.out.println("Уведомление не отправилось, пробуем еще");
                e.printStackTrace();
            }
        }
    }

    public void cancel(){
        stop = true;
    }
}

class Messager extends Thread {
    private volatile boolean stop;              //  флаг остановки
    private int count = 0;                      //  счетчик уведомлений генератора
    private int iter = 0;                       //  Число N, где справедливо: печатать раз в N секунд
    private final int MAX_WAIT = 2000;          //  Сколько максимум ждать генератора

    Messager(){
        this(5);
    }

    Messager(int iter){
        this.iter = iter;
        start();
    }

    @Override
    public void run(){
        while(!stop){
            synchronized(Main.nums){
                try {
                    Main.nums.wait(MAX_WAIT);
                    if (++count % iter == 0){
                        print();
                    }
                } catch (InterruptedException e) {
                    System.out.println("Уведомление не получено, пробуем еще");
                    e.printStackTrace();
                }
            }
        }
        System.out.println("\nИТОГО:");
        print();
    }

    void cancel(){
        stop = true;
    }

    private void print(){
        System.out.println(Main.nums.size() + " уникальных чисел");
        for (Integer num : Main.nums) {
            System.out.print(num + " ");
        }
        System.out.println();
    }
}
