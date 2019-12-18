package es.ehu.test;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
//from   w  w  w  . jav a 2  s  .  c  om
public class Main {

  public static void main(final String[] args) {
    ConcurrentHashMap<Integer, UUID> chm = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, UUID> chm2 = new ConcurrentHashMap<>();
    int max=10000000;

    
    long t0, elapsed, count=0;
    String test = "create";
    System.out.println("\n"+test);
    System.gc();
    try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
    
    
    t0 = System.nanoTime();
    for (int i = 0; i < max; i++) 
      chm.put(i, UUID.randomUUID());
    
    elapsed = System.nanoTime() - t0; 
    System.out.printf("strem: Elapsed time:\t %d ns \t(%f seconds)%n", elapsed, elapsed / Math.pow(10, 9));
    System.out.println("count "+max);
    
//    test = "Copy for";
//    System.out.println("\n"+test);
//    System.gc();
//    try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
//    chm2.clear();
//    t0 = System.nanoTime();
//    for (Map.Entry<Integer, UUID> m: chm.entrySet())
//      chm2.put(m.getKey(), m.getValue());
//    //chm2 = (ConcurrentHashMap<Integer, UUID>) chm.entrySet().stream().collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));
//    elapsed = System.nanoTime() - t0; 
//    System.out.printf(test+": Elapsed time:\t %d ns \t(%f seconds)%n", elapsed, elapsed / Math.pow(10, 9));
//    System.out.println("count "+max);
//    
//    
//    test = "Copy stream";
//    System.out.println("\n"+test);
//    System.gc();
//    try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
//    chm2.clear();
//    System.gc();
//    t0 = System.nanoTime();
//    chm2 = (ConcurrentHashMap<Integer, UUID>) chm.entrySet().stream().collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));
//    elapsed = System.nanoTime() - t0; 
//    System.out.printf(test+": Elapsed time:\t %d ns \t(%f seconds)%n", elapsed, elapsed / Math.pow(10, 9));
//    
//    test = "Copy stream parallel";
//    System.out.println("\n"+test);
//    System.gc();
//    try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
//    chm2.clear();
//    t0 = System.nanoTime();
//    chm2 = (ConcurrentHashMap<Integer, UUID>) chm.entrySet().stream().parallel().collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));
//    elapsed = System.nanoTime() - t0; 
//    System.out.printf(test+": Elapsed time:\t %d ns \t(%f seconds)%n", elapsed, elapsed / Math.pow(10, 9));
//    System.out.println("count "+max);
//    try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
    
           
//    test = "Search for";
//    System.out.println("\n"+test);
//    System.gc();
//    try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
//    count=0;
//    t0 = System.nanoTime();
//    for (int i=0; i<max; i++)
//      if (chm.get(i).toString().startsWith("00")) count++;
//    elapsed = System.nanoTime() - t0;     
//    System.out.printf(test+": Elapsed time:\t %d ns \t(%f seconds)%n", elapsed, elapsed / Math.pow(10, 9));
//    System.out.println("count "+count);
//
//    
//    test = "Search stream";
//    System.out.println("\n"+test);
//    System.gc();
//    try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
//    t0 = System.nanoTime();
//    count=0;
//    count = chm.values().stream().filter(v -> v.toString().startsWith("00")).count();
//    elapsed = System.nanoTime() - t0;    
//    System.out.printf(test+": Elapsed time:\t %d ns \t(%f seconds)%n", elapsed, elapsed / Math.pow(10, 9));
//    System.out.println("count "+count);
//
//    
//    test = "Search stream parallel";
//    System.out.println("\n"+test);
//    System.gc();
//    try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
//    t0 = System.nanoTime();
//    count=0;
//    count = chm.values().stream().parallel().filter(v -> v.toString().startsWith("00")).count();
//    elapsed = System.nanoTime() - t0;     
//    System.out.printf(test+": Elapsed time:\t %d ns \t(%f seconds)%n", elapsed, elapsed / Math.pow(10, 9));
//    System.out.println("count "+count);
//    try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
//    
//    
    long result =0;
    test = "sum for";
    System.out.print("\n"+test+": ");    
    System.gc();
    try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
    t0 = System.nanoTime();
    for (Map.Entry<Integer, UUID> m: chm.entrySet())
      result+=m.getKey();
    elapsed = System.nanoTime() - t0;     
    System.out.println(result);
    System.out.printf("Elapsed time:\t %d ns \t(%f seconds)%n", elapsed, elapsed / Math.pow(10, 9));
    
//    
//    
//    test = "sum stream";
//    System.out.println("\n"+test);
//    System.gc();
//    try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
//    t0 = System.nanoTime();
//    count = chm.entrySet().stream().mapToInt(i -> i.getKey()).sum();
//    elapsed = System.nanoTime() - t0;    
//    System.out.printf(test+": Elapsed time:\t %d ns \t(%f seconds)%n", elapsed, elapsed / Math.pow(10, 9));
//    System.out.println("sum "+count);
//    
//    
    result =0;
    test = "sum stream";
    System.out.print("\n"+test+": ");
    System.gc();
    try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
    t0 = System.nanoTime();
    result = chm.entrySet().stream().mapToLong(i->i.getKey()).reduce(0,Long::sum);
    
//        mapToInt(i -> i.getKey()).
//        .sum();
    elapsed = System.nanoTime() - t0;    
    System.out.println(result);
    System.out.printf("Elapsed time:\t %d ns \t(%f seconds)%n", elapsed, elapsed / Math.pow(10, 9));
    
    result =0;
    test = "sum stream parallel";
    System.out.print("\n"+test+": ");
    System.gc();
    try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
    t0 = System.nanoTime();
    result = chm.entrySet().stream().parallel().mapToLong(i->i.getKey()).reduce(0,Long::sum);
//        mapToInt(i -> i.getKey()).
//        .sum();
    elapsed = System.nanoTime() - t0;    
    System.out.println(result);
    System.out.printf("Elapsed time:\t %d ns \t(%f seconds)%n", elapsed, elapsed / Math.pow(10, 9));
    
    //concurrentHashMap.forEachValue(threshold, System.out::println);

//    concurrentHashMap.forEach((id, uuid) -> {
//      if (id % 10 == 0) {
//        System.out.println(String.format("%s: %s", id, uuid));
//      }
//    });

    
//    String searchResult = concurrentHashMap.search(threshold, (id, uuid) -> {
//      if (String.valueOf(uuid).contains(String.valueOf(id))) {
//        return new String(id + ":" + uuid);
//      }
//      return null;
//    });

    System.out.println("done");
  }

}