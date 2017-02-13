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
    static final int COUNT_NUM = 7;                               //  сколько чисел генерировать
    static HashSet<Integer> nums = new HashSet<Integer>(COUNT_NUM); //  хранилище уникальных чисел
    public static volatile boolean stop = false;

    public static void main(String[] args) {
        System.out.println("Старт программы");
        Messager messager = new Messager();
        Generator generator = new Generator();
        try {
            generator.join();
            messager.join();
            System.out.println("Все потоки завершены");
        } catch (InterruptedException e) {
            System.out.println("Не могу дождаться завершения потоков!");
            e.printStackTrace();
        }

    }
}

class Generator extends Thread {
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
        while(!Main.stop && (Main.nums.size() < Main.COUNT_NUM)){
            synchronized(Main.nums){
                Main.nums.add(rand.nextInt(Main.COUNT_NUM));
                Main.nums.notifyAll();
            }
            try {
                Thread.sleep(TICK);
            } catch (InterruptedException e) {
                System.out.println("Уведомление не отправилось, пробуем еще");
                e.printStackTrace();
            }
        }
        Main.stop = true;
        synchronized (Main.nums){
            Main.nums.notifyAll();
        }
    }
}

class Messager extends Thread {
    private int count = 0;                      //  счетчик уведомлений генератора
    private final int iter;                     //  Число N, где справедливо: печатать раз в N секунд

    Messager(){
        this(5);
    }

    Messager(int iter){
        this.iter = iter;
        start();
    }

    @Override
    public void run(){
        while(!Main.stop){
            synchronized(Main.nums){
                try {
                    Main.nums.wait();
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

    private void print(){
        System.out.println(Main.nums.size() + " уникальных чисел");
        for (Integer num : Main.nums) {
            System.out.print(num + " ");
        }
        System.out.println();
    }
}
