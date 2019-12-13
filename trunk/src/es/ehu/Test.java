package es.ehu;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test {
  

  public static void main(String[] args) {
    Test t = new Test();
    
    
    ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<String, String>();
    
//    for (long i=0; i<1000000000; i++)
//      chm.put(""+i, ""+Math.random());
    
    System.out.println("start");
    
    
    Future<String> completableFuture = null;
    try {
      completableFuture = t.calculateAsync();
    } catch (InterruptedException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    System.out.println("hago cosas...");

    String result ="";
    try {
      result = completableFuture.get();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("sigo haciendo cosas...");
    System.out.println(result);
    System.out.println("asdfasdf");
    

}
  
  

  public Future<String> calculateAsync() throws InterruptedException {
    CompletableFuture<String> completableFuture = new CompletableFuture<>();
  System.out.println("inicio calculateAsync");
    Executors.newCachedThreadPool().submit(() -> {
      System.out.println("espero");
        Thread.sleep(3000);
        System.out.println("sigo");
        completableFuture.complete("Hello");
        return null;
    });
  
    return completableFuture;
  }

}