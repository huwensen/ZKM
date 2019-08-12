package com.test.zoo;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
         ZooKeeper zookeeper=new ZooKeeper("localhost:2181", 2000, new Watcher() {
             @Override
             public void process(WatchedEvent watchedEvent) {
                 System.out.println(watchedEvent.getState());
                 System.out.println(watchedEvent.getPath());
                 System.out.println(watchedEvent.getType());
                 System.out.println(watchedEvent.getWrapper());

             }
         });
        Stat stat = new Stat();

        List<String> children = zookeeper.getChildren("/", true);
        for (String child : children) {
            System.out.println(child);
        }
        zookeeper.close();
    }
}
