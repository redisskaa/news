package ru.bloknot.news.helpclass;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutors {

    private static volatile AppExecutors instance;
    private final ExecutorService diskIO;
    private final ExecutorService networkIO;

    // Приватный конструктор для паттерна Singleton
    private AppExecutors(int numThreadsDiskIO, int numThreadsNetworkIO) {
        this.diskIO = Executors.newFixedThreadPool(numThreadsDiskIO);
        this.networkIO = Executors.newFixedThreadPool(numThreadsNetworkIO);
    }

    // Метод получения экземпляра с параметрами по умолчанию
    public static synchronized AppExecutors getInstance() {
        if (instance == null) {
            instance = new AppExecutors(3, 3); // По три потока для каждого типа задач
        }
        return instance;
    }

    // Методы для доступа к соответствующим пулам потоков
    public ExecutorService diskIO() {
        return diskIO;
    }

    public ExecutorService networkIO() {
        return networkIO;
    }
}
